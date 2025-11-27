import subprocess
import re
import json
import argparse
import time
import concurrent.futures
from typing import List, Any

import z3
from z3 import *
import ast
import base64

start_time = 0
# Create the Z3 solver
solver = Solver()

RESOURCE_DIR = "resources"
RUNNABLE_DOR= "resources/runnable"
os.makedirs(RESOURCE_DIR, exist_ok=True)
os.makedirs(RUNNABLE_DOR, exist_ok=True)
UNHANDLED_ERROR = "Unhandled error"
TESTCASE_GENERATION_RESULT = "Testcase generation result"


def print_verification_timeout_result():
    result = Result(-3, "OVERTIME", "")
    print(f"result:" + result.to_json())
def print_verification_unexpected_result():
    result = Result(-1,"","")
    print(f"result:" + result.to_json())

def get_class_name(java_code: str):
    match = re.search(r'class\s+(\w+)', java_code)
    if match:
        return match.group(1)  # 返回匹配的类名
    else:
        return None  # 如果没有匹配到，返回 None

def run_java_code(java_code: str, timeout_seconds=20):
    classname = get_class_name(java_code)
    print(classname)
    file_path = RUNNABLE_DOR + "/"  + classname + ".java"
    print(file_path)
    with open(file_path, "w") as file:
        file.write(java_code)
    try:
        subprocess.run(["javac", file_path], check=True)
    except subprocess.CalledProcessError:
        print("Error during Java compilation.")
        return ""
    try:
        result = subprocess.run(
            ["java", file_path],
            capture_output=True,
            text=True,
            timeout=timeout_seconds
        )
        # print(" result.stdout:" + result.stdout)
        return result
    except subprocess.TimeoutExpired:
        print("Java execution timeout!")
        raise
    except subprocess.CalledProcessError:
        print("Error during Java execution.")
        raise

def parse_execution_path(execution_output: str) -> List[str]:
    lines = execution_output.splitlines()
    execution_path = []

    for line in lines:
        if "current value" in line or "Entering loop" in line or "Exiting loop" in line or "Evaluating if condition" in line \
                or "Return statement" in line or "Function input" in line or "Entering forloop" in line \
                or "Exiting forloop" in line or ("Under condition" in line and "true" in line):
            execution_path.append(line)


    return execution_path

def combind_expr_and_list(expr: str, exprList: List[str]):
    #默认T和preCts中的Ct都是用()包围起来的
    com_expr = expr
    for ct in exprList:
        com_expr = f"{com_expr} && !({ct})"
    return com_expr.strip().strip("&&")

def simplify_expression(expression):
    """
    Simplify logical expressions and remove redundant negations and redundant conditions.
    """
    # Remove double negatives!! (expr) -> (expr)
    expression = re.sub(r'!\(!\((.*?)\)\)', r'\1', expression)
    expression = re.sub(r'\s+', ' ', expression)  # Remove excess space
    return expression

def replace_variables(current_condition: str, variable: str, new_value: str) -> str:
    """
    Replace the variable in the logical condition with the new value.
    """
    pattern = rf'\b{re.escape(variable)}\b'  # Match variable names exactly
    new_value = f"{new_value}"
    return re.sub(pattern, new_value, current_condition)

class Result:
    def __init__(self, status: int, counter_example: str, path_constrain: str):
        self.status = status
        self.counter_example = counter_example  # string 类型字段
        self.path_constrain = path_constrain
    def to_json(self) -> str:
        """
        将 Result 对象序列化为 JSON 字符串
        """
        return json.dumps(self.__dict__)
    @classmethod
    def from_json(cls, json_string: str) -> 'Result':
        data_dict = json.loads(json_string)
        return cls(data_dict["status"], data_dict["counter_example"], data_dict["path_constrain"])
    def __str__(self):
        return f"Result(status={self.status}, counter_example={self.counter_example}, path_constrain={self.path_constrain})"

class SpecUnit:
    def __init__(self, program: str, T: str, D: str, pre_constrains: List[str]):
        self.program = program  # string 类型字段
        self.T = T
        self.D = D
        self.pre_constrains = pre_constrains

    def to_json(self) -> str:
        """
        将 SpecUnit 对象序列化为 JSON 字符串
        """
        return json.dumps(self.__dict__)

    @classmethod
    def from_json(cls, json_string: str) -> 'SpecUnit':
        """
        从 JSON 字符串反序列化为 SpecUnit 对象
        """
        data_dict = json.loads(json_string)
        return cls(data_dict["program"], data_dict["T"], data_dict["D"],data_dict["pre_constrains"])

    def __str__(self):
        return f"SpecUnit(name={self.program}, T={self.T}, D={self.D},pre_constrains={self.pre_constrains})"

class FSFValidationUnit:
    def __init__(self, allTs: List[str], vars: dict):
        self.allTs = allTs  # string 类型字段
        self.vars = vars
    def to_json(self) -> str:
        """
        将 FSFValidationUnit 对象序列化为 JSON 字符串
        """
        return json.dumps(self.__dict__)

    @classmethod
    def from_json(cls, json_string: str) -> 'FSFValidationUnit':
        """
        从 JSON 字符串反序列化为 FSFValidationUnit 对象
        """
        data_dict = json.loads(json_string)
        return cls(data_dict["allTs"], data_dict["vars"])

    def __str__(self):
        return f"FSFValidationUnit(name={self.allTs}, T={self.vars})"

############# java_expr_z3_expr ##############
def solver_check_z3(z3_expr:str, vars_types:dict = "")->str:
    try:
        solver = Solver()
        solver.add(z3_expr)

        if solver.check() == sat:
            print("The expression is satisfiable ❌")
            model = solver.model()
            model_str = "["
            # 更完整的流程：遍历所有变量类型字典，主动用 model.eval 获取值
            for var_name, var_type in vars_types.items():
                if var_name == "return_value":
                    continue
                try:
                    # model_completion=True 保证即使 model 没有赋值也能返回默认值
                    z3_val = model.eval(z3.Bool(var_name) if var_type in ["bool", "boolean"] else z3.BitVec(var_name, 32) if var_type in ["int", "char"] else z3.Real(var_name), model_completion=True)
                    if var_type == "int":
                        var_value = str(z3_val.as_signed_long())
                    elif var_type == "char":
                        var_value = chr(z3_val.as_long() & 0x10FFFF)
                    elif var_type in ["bool", "boolean"]:
                        var_value = str(z3_val)
                    elif var_type == "double":
                        var_value = str(z3_val)
                    else:
                        var_value = str(z3_val)
                    model_str = model_str + f"{var_name}={var_value}, "
                except Exception as e:
                    model_str = model_str + f"{var_name}=ERROR, "
            model_str = model_str.rstrip(", ") + "]"
            print(model_str)
            return model_str
        else:
            print("The expression is unsatisfiable ✅")
            #创建 Result 对象
            return "OK"

    except Exception as e:
        print("solver check fail!")
        print("Error Message:", e)
        raise
        # return "ERROR"

def replace_char_literals(expr):
    # 替换 Java 表达式中的字符字面量，如 'a' -> 97
    return re.sub(r"'(.)'", lambda m: str(ord(m.group(1))), expr)

def to_z3_val(val):
    if isinstance(val, int):
        return z3.IntVal(val)
    if isinstance(val, float):
        return z3.RealVal(val)
    return val

def convert_ternary(expr: str) -> str:
    # 支持嵌套和括号，递归处理三目运算符
    # 只处理最外层的 cond ? a : b
    import re
    def repl(m):
        cond = m.group(1).strip()
        a = m.group(2).strip()
        b = m.group(3).strip()
        return f"({a} if {cond} else {b})"
    # 处理括号包裹的三目
    pattern = re.compile(r'\(([^()]+)\?([^:]+):([^()]+)\)')
    while True:
        new_expr = pattern.sub(repl, expr)
        if new_expr == expr:
            break
        expr = new_expr
    # 处理无括号的三目
    pattern2 = re.compile(r'([^?\s]+)\?([^:]+):([^\s)]+)')
    expr = pattern2.sub(repl, expr)
    return expr

def java_expr_to_z3(expr_str, var_types: dict):
    """
    :param expr_str: Java格式逻辑表达式，如 "(b1 == true && x > 5)"
    :param var_types: dict，变量名到类型的映射，如 {'b1': 'bool', 'x': 'int'}
    :return: Z3 表达式
    """
    expr_str = expr_str.strip()
    expr_str = expr_str.lstrip()  # 进一步去除前导空白
    expr_str = " ".join(expr_str.splitlines())  # 合并为单行，去除多余缩进
    print(f"Java Expression: {repr(expr_str)}")  # 用repr��便调试不可见字符
    expr_str = remove_type_transfer_stmt_in_expr(expr_str)
    # expr_str = convert_ternary(expr_str)  # 新增三目运算符转换
    # 构建 Z3 变量
    z3_vars = {}
    for name, vtype in var_types.items():
        if vtype == 'boolean' or vtype == 'bool':
            z3_vars[name] = z3.Bool(name)
        elif vtype == 'int':
            z3_vars[name] = z3.BitVec(name,32)
            # z3_vars[name] = z3.Int(name)
        elif vtype == 'char':
            z3_vars[name] = z3.BitVec(name,32)
            # z3_vars[name] = z3.Int(name)
        elif vtype == 'double':
            z3_vars[name] = z3.Real(name)
        else:
            raise ValueError(f"不支持的变量类型: {vtype}")

    # 替换 Java 风格语法
    expr_str = replace_char_literals(expr_str)
    expr_str = expr_str.replace("true", "True").replace("false", "False")
    expr_str = expr_str.replace("&&", " and ").replace("||", " or ").replace("!", " not ")
    expr_str = expr_str.replace("not =","!=") # 纠错，由于将! 替换为 not,会导致 != 变为 not =，需要纠正为 !=
    expr_str = expr_str.strip()  # 再次去除前后空白，防止前导空格导致IndentationError

    # AST 转换器
    class Z3Transformer(ast.NodeTransformer):
        def visit_Name(self, node):
            if node.id in z3_vars:
                return z3_vars[node.id]
            elif node.id in {"char","int", "boolean","float", "double"}: #避免 Java 中的类型名被误认为变量
                return ""
            else:
                raise ValueError(f"未知变量: {node.id}")

        def visit_Constant(self, node):
            if isinstance(node.value, bool):
                return node.value
            elif isinstance(node.value, int):
                return z3.BitVecVal(node.value,32)
                # return z3.IntVal(node.value)
            elif isinstance(node.value, float):
                return z3.RealVal(node.value)
            elif isinstance(node.value, str):
                if len(node.value) == 1:#字符常量
                    return z3.BitVecVal(ord(node.value),32)
                    # return z3.IntVal(node.value)
                return node.value
            else:
                raise ValueError(f"不支持的常量类型: {node.value}")

        def visit_BoolOp(self, node):
            values = [self.visit(v) for v in node.values]
            if isinstance(node.op, ast.And):
                return z3.And(*values)
            elif isinstance(node.op, ast.Or):
                return z3.Or(*values)
            else:
                raise ValueError(f"不支持的布尔操作: {type(node.op)}")

        def visit_UnaryOp(self, node):
            if isinstance(node.op, ast.Not):
                return z3.Not(self.visit(node.operand))
            if isinstance(node.op, ast.USub):
                return -self.visit(node.operand)
            else:
                raise ValueError(f"不支持的一元操作: {type(node.op)}")

        def visit_Compare(self, node):
            left = self.visit(node.left)
            right = self.visit(node.comparators[0])
            op = node.ops[0]


            left = to_z3_val(left)
            right = to_z3_val(right)
            #
            # Int/Real 混用时提升为 Real
            if (z3.is_int_value(left) and z3.is_real(right)) or (z3.is_real(left) and z3.is_int_value(right)):
                left = z3.ToReal(left)
                right = z3.ToReal(right)

            # BitVec 和 Int 混用时全部转 Int
            if isinstance(left, z3.BitVecRef) and isinstance(right, z3.IntNumRef):
                left = z3.BV2Int(left, is_signed=False)
            if isinstance(right, z3.BitVecRef) and isinstance(left, z3.IntNumRef):
                right = z3.BV2Int(right, is_signed=False)
            # # 确保 BitVec 的大小一致
            if is_bv(left) and is_bv(right):
                if left.size() == 16 and right.size() == 32:
                    left = SignExt(16, left)  # 扩展为 32 位
                if right.size() == 16 and left.size() == 32:
                    right = SignExt(16, right)
            if isinstance(op, ast.Eq):
                return left == right
            elif isinstance(op, ast.NotEq):
                return left != right
            elif isinstance(op, ast.Gt):
                return left > right
            elif isinstance(op, ast.GtE):
                return left >= right
            elif isinstance(op, ast.Lt):
                return left < right
            elif isinstance(op, ast.LtE):
                return left <= right
            else:
                raise ValueError(f"不支持的比较运算符: {type(op)}")

        def visit_BinOp(self, node):
            left = self.visit(node.left)
            right = self.visit(node.right)
            op = node.op

            if isinstance(op, ast.Add):
                return left + right
            elif isinstance(op, ast.Sub):
                return left - right
            elif isinstance(op, ast.Mult):
                return left * right
            elif isinstance(op, ast.Div):
                return left / right
            elif isinstance(op, ast.Mod):
                # return left % right
                return z3.SRem(left, right)
            elif isinstance(op, ast.Pow):
                # 支持幂运算 x ** n
                if isinstance(left, z3.BitVecRef) and isinstance(right, z3.BitVecRef):
                    left1 = BV2Int(left, is_signed=True)
                    right1 = BV2Int(right, is_signed=True)
                return Int2BV(left1 ** right1,32)
            elif isinstance(op, ast.BitAnd):
                # 确保操作数都是位向量
                if not (isinstance(left, z3.BitVecRef) and isinstance(right, z3.BitVecRef)):
                    left = z3.Int2BV(left,32) if is_int(left) else left
                    right = z3.Int2BV(right,32) if is_int(right) else right
                return left & right
            elif isinstance(op, ast.BitOr):
                if not (isinstance(left, z3.BitVecRef) and isinstance(right, z3.BitVecRef)):
                    left = z3.Int2BV(left,32) if is_int(left) else left
                    right = z3.Int2BV(right,32) if is_int(right) else right
                    # raise TypeError("按位或运算的操作数必须是位向量")
                return left | right
            elif isinstance(op, ast.BitXor):
                if not (isinstance(left, z3.BitVecRef) and isinstance(right, z3.BitVecRef)):
                    left = z3.Int2BV(left,32) if is_int(left) else left
                    right = z3.Int2BV(right,32) if is_int(right) else right
                    # raise TypeError("按位异或运算的操作数必须是位向量")
                return left ^ right
            else:
                raise ValueError(f"Unsupported Operator: {type(op)}")
    try:
        parsed = ast.parse(expr_str, mode="eval")
    except Exception as e:
        print(f"ast.parse error: {e}, expr_str={repr(expr_str)}")
        z3_expr = f"ERROR Info: {e}"  # 或者根据需要设置默认值
        raise
    try:
        z3_expr = Z3Transformer().visit(parsed.body)
    except Exception as e:
        print(f"Z3Transformer Exception: {e}")
        z3_expr = f"ERROR Info: {e}"  # 或者根据需要设置默认值
        raise
    return z3_expr

def parse_md_def(java_code: str) -> dict:
    lines = java_code.splitlines()
    var_types = {}
    for line in lines:
        line = line.strip()
        if line.startswith("public static") and "main" not in line:
            return_type = line.split()[2]
            params_def = line.split("(")[1].split(")")[0]
            var_types["return_value"] = return_type
            if params_def.strip():  # 非空才处理
                params = params_def.split(",")
                for param in params:
                    param = param.strip()
                    param_type = param.split()[0]
                    param_name = param.split()[1]
                    var_types[param_name] = param_type
            print(var_types)
    return var_types
############# java_expr_z3_expr ##############

def add_value_constraints(logic_expr: str, var_types: dict) -> str:
    """
    添加变量值约束到逻辑表达式中
    :param logic_expr: 原始逻辑表达式
    :param var_types: 变量类型字典
    :return: 添加了变量值约束的逻辑表达式
    """
    value_constraints_expr = ""
    for var, vtype in var_types.items():
        if var == "return_value":
            continue
        if vtype == 'int':
            value_constraints_expr += f" && ({var} >= -32768 && {var} <= 32767)"
        elif vtype == 'char':
            value_constraints_expr += f" && ({var} >= 32 && {var} <= 126)"
        # elif vtype == 'double':
        #     value_constraints_expr += f" && ({var} >= -1.0 && {var} <= 1.0)"
    value_constraints_expr = value_constraints_expr.strip().strip("&&").strip()
    if len(value_constraints_expr) > 0:
        logic_expr = f"({logic_expr})" + f" && ({value_constraints_expr})"
    return logic_expr

def deal_with_spec_unit_json(spec_unit_json: str):
    #读取SpecUnit对象
    spec_unit = None
    # print(f"Processing SpecUnit JSON: {spec_unit_json}")
    try:
        spec_unit = SpecUnit.from_json(spec_unit_json)
    except json.JSONDecodeError as e:
        print(f"Error decoding JSON: {e}")
    program = spec_unit.program
    T = spec_unit.T
    D = spec_unit.D
    previous_cts = spec_unit.pre_constrains

    #运行程序,获得输出
    try:
        output = run_java_code(program, timeout_seconds=20)
    except subprocess.TimeoutExpired:
        print_verification_timeout_result()
        return

    execution_output = ""
    if output is None:
        print("Java code execution failed.")
        return

    #特殊处理为Exception的TD组
    if "Exception" in D:
        if output.stderr is not None and "Exception" in output.stderr:
            result = Result(0,"","Exception founded!")
            print("result:" + result.to_json())
        else :
            result = Result(1,"","Exception founded!")
            print("result:" + result.to_json())
        return

    if output.stderr is not None and "Exception" in output.stderr:
        result = Result(-2,"Exception founded:" + str(output.stderr),"")
        print("result:" + result.to_json())
        return
    if output.stdout is not None:
        execution_output = output.stdout
    if not execution_output:
        print("No output from Java code execution.")
    #分析路径输出，得到本次执行路径相关的Ct
    var_types = parse_md_def(program)
    input_vars = list(var_types.keys())
    # print(f"input_vars: {input_vars}")
    execution_path = parse_execution_path(execution_output)
    print("\nExecution Path:")
    for step in execution_path:
        print(step)
    print("end Execution Path")
    current_ct = get_ct_from_execution_path(execution_path);
    if current_ct == "":
        current_ct = "true"
    print(f"current_Ct_: {current_ct}")
    new_d = update_D_with_execution_path(D,execution_path,input_vars)
    print("new_d:" + new_d)
    # 构建新的逻辑表达式并检查可满足性
    negated_d = f"!({new_d})"
    new_logic_expression = f"({T}) && ({current_ct}) && ({negated_d})"
    new_logic_expression = simplify_expression(new_logic_expression)
    new_logic_expression = add_value_constraints(new_logic_expression, var_types)
    print(f"\nT && Ct && !D: {new_logic_expression}")

    z3_expr = java_expr_to_z3(new_logic_expression, var_types)
    # if z3_expr.startswith("ERROR"):
    #     result = Result(1,z3_expr,"")
    #     print("result:" + result.to_json())
    #     return
    print("Z3 expression of T && Ct && !D: " + str(z3_expr))
    solver_result = solver_check_z3(z3_expr,var_types)
    if solver_result == "OK":
        #组装 combined_expr
        previous_cts.append(current_ct)
        combined_expr = combind_expr_and_list(f"({T})", previous_cts)
        print("Post path verification: T && !previous_cts && !current_ct:" + combined_expr)
        combined_expr = add_value_constraints(combined_expr, var_types)
        z3_expr = java_expr_to_z3(combined_expr, var_types)
        print("Z3 expression of (T) && !(previous_cts) && !(current_ct): " + str(z3_expr))
        scr = solver_check_z3(z3_expr,var_types)
        if scr == "OK":
            result = Result(3,"",current_ct)
        # elif(scr == "ERROR"):
        #     result = Result(1,scr,"")
        else:
            result = Result(0,"",current_ct)
    # elif solver_result == "ERROR":
    #     result = Result(1,"",current_ct)
    else:
        result = Result(2,solver_result,"")
    print("result:" + result.to_json())

def remove_type_transfer_stmt_in_expr(expr: str) -> str:
    ans = expr.replace("(long)","").replace("(int)","").replace("(short)","").replace("(byte)","").replace("(char)","")
    return ans

def get_ct_from_execution_path(execution_path:List[str]):
    ct = ""
    for step in reversed (execution_path):
        if "Evaluating if condition" in step:
            condition_match = re.search(r"Evaluating if condition: (.*?) is evaluated as: (.*?)", step)
            if condition_match:
                if_condition = condition_match.group(1).strip()
                if_condition = remove_type_transfer_stmt_in_expr(if_condition)
                ct = f"{ct} && {if_condition}"
            # Check whether it is a condition to enter the loop
        elif "Entering loop" in step:
            condition_match = re.search(r"Entering loop with condition: (.*?) is evaluated as: true", step)
            if condition_match:
                loop_condition = condition_match.group(1).strip()
                ct = f"{ct} && {loop_condition}"
        elif "Entering forloop" in step:
            condition_match = re.search(r"Entering forloop with condition: (.*?) is evaluated as: true", step)
            if condition_match:
                loop_condition = condition_match.group(1).strip()
                ct = f"{ct} && {loop_condition}"

            # Check whether it is a condition for exiting the loop
        elif "Exiting loop" in step:
            condition_match = re.search(r"Exiting loop, condition no longer holds: (.*?) is evaluated as: false", step)
            if condition_match:
                loop_condition = condition_match.group(1).strip()
                ct = f"{ct} && !{loop_condition}"
        elif "Exiting forloop" in step:
            condition_match = re.search(r"Exiting forloop, condition no longer holds: (.*?) is evaluated as: false", step)
            if condition_match:
                loop_condition = condition_match.group(1).strip()
                ct = f"{ct} && !{loop_condition}"

            # Check for variable assignment
        elif "current value" in step:
            assignment_match = re.search(r"(.*?) = (.*?), current value of (.*?): (.*?)$", step)
            if assignment_match:
                variable = assignment_match.group(1).strip()
                value = assignment_match.group(2).strip()
                ct = replace_variables(ct,variable,value)
        elif "Under condition" in step:
            condition_assignment_match = re.search(r"Under condition (.*) = (.*), condition is : (.*)", step)
            if condition_assignment_match:
                variable = condition_assignment_match.group(1).strip()
                value = condition_assignment_match.group(2).strip()
                ct = replace_variables(ct,variable,value)

    #先去掉空格，再去掉多余的 &&
    return ct.strip().strip("&&")

def update_D_with_execution_path(D: str, execution_path: List[str], input_vars: List[str]) -> str:
    print(f"original D : {D}")
    if("return_value" in D):
        D = replace_variables(D,"return_value","(return_value)")
    D = D.replace("(char)", "").replace("(long)","").replace("(int)","").replace("(double)","")

    for input_var in input_vars:
        if input_var in D and "return_value" != input_var:
            D = replace_variables(D, input_var, f"__{input_var}__")  # 确保输入变量被括号包围
    print(f"now D is {D}")
    newd = D
    for step in reversed(execution_path):
        if "current value" in step or "Function input" in step or "Under condition" in step:
            assignment_match = re.search(r"(.*?) = (.*?), current value of (.*?): (.*?)$", step)
            # input_param_match = re.search(r"Function input (.*)? parameter (.*?) = (.*?)$", step)
            condition_assignment_match = re.search(r"Under condition (.*) = (.*), condition is : (.*)", step)
            type = ""
            if assignment_match:
                variable = assignment_match.group(1).strip()
                value = assignment_match.group(2).strip()
            # elif input_param_match:
            #     type = input_param_match.group(1).strip()
            #     variable = input_param_match.group(2).strip()
            #     value = input_param_match.group(3).strip()
            elif condition_assignment_match:
                variable = condition_assignment_match.group(1).strip()
                value = condition_assignment_match.group(2).strip()
            else :
                continue
            # for sd in split_d:
            #     if variable in sd:
            #         # 替换 D 中的变��
            #         sd = replace_variables(sd, variable, value)
            #     update_d.append(sd.strip())
            # split_d = update_d
            # update_d = []
            if type and type == "char":
                value = f"'{value}'" # 给char类型变量带上''
            # value = f"({value})" # 确保value不会影响newd结构
            newd = replace_variables(newd,variable,value)

    for input_var in input_vars:
        if f"__{input_var}__" in newd:
            newd = replace_variables(newd, f"__{input_var}__", input_var)
    return newd.strip().strip("&&")

def read_java_code_from_file(file_path):
    """
    Read Java code from the specified file.
    """
    with open(file_path, "r") as file:
        java_code = file.read()
    return java_code

def fsf_validate(fu_json: str):
    fu = FSFValidationUnit.from_json(fu_json)
    print(fu)
    ts = fu.allTs
    ts_size = len(ts)
    and_ts = []
    or_connect_ts = ""
    #验证每个T的可满足性，即T不可以无解
    for t in ts:
        z3_expr = java_expr_to_z3(t, fu.vars)
        if isinstance(z3_expr, str) and z3_expr.startswith("ERROR"):
            result = Result(-1, z3_expr, "")
            print("FSF validation result:" + result.to_json())
            return
        r = solver_check_z3(z3_expr,fu.vars)
        if r == "OK": #z3_expr无解
            result = Result(-2, t, "")
            print("FSF validation result:" + result.to_json())
            return


    #验证完备性，即!（T1 || T2 || T3 || ...）无解
    for t in ts:
        or_connect_ts = f"{or_connect_ts}||({t})"
    or_connect_ts = or_connect_ts.strip().strip("||").strip()
    or_connect_ts = f"!({or_connect_ts})"
    # print("验证完备性: " + or_connect_ts)
    z3_expr = java_expr_to_z3(or_connect_ts, fu.vars)
    if isinstance(z3_expr, str) and z3_expr.startswith("ERROR"):
        result = Result(-1, z3_expr, "")
        print("FSF validation result:" + result.to_json())
        return
    r = solver_check_z3(z3_expr,fu.vars)
    if r == "OK": #unsat，具有完备性
        print("T possesses completeness")
    else: #不具有完备性
        result = Result(3, or_connect_ts, " lacks completeness")
        print("FSF validation result:" + result.to_json())
        return

    #验证互斥性，即T1 && T2无解
    for i in range(ts_size):
        for j in range(i + 1, ts_size):
            t1 = ts[i]
            t2 = ts[j]
            and_ts.append(f"({t1}) && ({t2})")
    result = Result(0, "", "")
    for and_t in and_ts:
        # print("正在验证: " + and_t)
        z3_expr = java_expr_to_z3(and_t, fu.vars)
        r = solver_check_z3(z3_expr,fu.vars)
        if r == "OK":
            continue
        else:
            result = Result(2, and_t, r)
            break
    print("FSF validation result:" + result.to_json())

def z3_generate_testcase(spec_unit_json:str):
    spec_unit = None
    r = Result(0,"","")
    # print(f"Processing SpecUnit JSON: {spec_unit_json}")
    try:
        spec_unit = SpecUnit.from_json(spec_unit_json)
    except json.JSONDecodeError as e:
        print(f"{UNHANDLED_ERROR}: z3_solver_runner 解析 spec_unit 失败 {e}")
    constrains_expr = spec_unit.T
    program = spec_unit.program
    var_types = parse_md_def(program)
    z3_expr = java_expr_to_z3(constrains_expr, var_types)
    print(f"z3 generating testcase under constrains: [{z3_expr}]")
    var_values = solver_check_z3(z3_expr,var_types)
    if(var_values == "OK"):
        #没有可用解
        r = Result(1,"","")
    else:
        r = Result(0,var_values,"")
    print(f"{TESTCASE_GENERATION_RESULT}: {r.to_json()}")

def test_z3_generate_testcase():
    gu_json = " {\"program\":\"public class PowerOfTwo_Mutant1 {\\n\\n    public static boolean isPowerOfTwo(int n) {\\n        return n >= 0 && (n & (n - 1)) == 0;\\n    }\\n}\\n\",\"preconditions\":[],\"T\":\"n > 0 && (n & (n - 1)) == 0 && ( n < 2147483647 ) && ( n > -2147483648 )\",\"D\":\"true\",\"pre_constrains\":[]}"
    z3_generate_testcase(gu_json)

def run_with_timeout(func, arg, timeout_seconds, task_name):
    with concurrent.futures.ThreadPoolExecutor() as executor:
        future = executor.submit(func, arg)
        try:
            future.result(timeout=timeout_seconds)
        except concurrent.futures.TimeoutError:
           print_verification_timeout_result()
        except Exception as e:
            print_verification_unexpected_result()
def main():
    #创建解析器
    parser = argparse.ArgumentParser()
    # 添加参数定义
    parser.add_argument('-s', '--su', '--specUnit', help='Enter the JSON string of the SpecUnit object to be validated.', required=False)
    parser.add_argument('-f', '--fu', '--fsfValidationUnit', help='Enter the JSON string of the fsfValidationUnit object to be validated.', required=False)
    parser.add_argument('-g', '--gu', '--generationUnit', help='Enter the generationUnit containing the constraints and the program.', required=False)
    # 解析命令行参数
    args = parser.parse_args()
    spec_unit_json = args.su
    fsf_validation_unit_json = args.fu
    generation_unit = args.gu
    if spec_unit_json is None and fsf_validation_unit_json is None and generation_unit is None:
        print("Please provide the JSON string to be validated.")
        return
    if spec_unit_json is not None:
        print("start")
        import re
        spec_unit_json = re.sub(r'\s+', '', spec_unit_json)  # 去掉所有空白字符
        spec_unit_json = base64.b64decode(spec_unit_json).decode("utf-8")
        #print(spec_unit_json)
        #deal_with_spec_unit_json(spec_unit_json)
        run_with_timeout(deal_with_spec_unit_json, spec_unit_json, 20, "SpecUnit 验证")

    if fsf_validation_unit_json is not None:
        run_with_timeout(fsf_validate, fsf_validation_unit_json, 20, "FSF 验证")

    if generation_unit is not None:
        run_with_timeout(z3_generate_testcase, generation_unit, 20, "测试用例生成")

def init_files():
    import os
    if not os.path.exists(RESOURCE_DIR):
        os.makedirs(RESOURCE_DIR)
    if not os.path.exists(RUNNABLE_DOR):
        os.makedirs(RUNNABLE_DOR)
    if not os.path.exists(RESOURCE_DIR + "/TestCase.java"):
        program = """
            public class TestCase{
                public static int Abs(int num){
                    if(num < 0){
                        System.out.println("Evaluating if condition: num < 0 is evaluated as: " + (num < 0));
                        return -num;
                    }
                    else{
                        return num;
                    }
                }
            
                public static void main(String[] args){
                    int num = -3;
                    int result = TestCase.Abs(num);
                    System.out.println("result = Abs.Abs(num), current value of result: " + result);
                }
            }
            """
        with open(RESOURCE_DIR + "/TestCase.java", "w") as file:
            file.write(program)

def test_main_4():
    program = read_java_code_from_file("resources/TestCase.java")
    print(program)
    expr = "targetHeight - currentHeight >= 30 && targetHeight <= currentHeight"
    su = SpecUnit(program,expr,"",[]).to_json()
    z3_generate_testcase(su)


if __name__ == "__main__":
    # test_main_2()
    # test_main_3("{\"allTs\":[\"T1\",\"T2\"],\"vars\":{\"a\":\"int\",\"b\":\"String\"}}")
    # test_main()
    main()
    # test_z3_generate_testcase()
    # test_main_4()


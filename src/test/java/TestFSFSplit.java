import org.TBFV4R.TBFV.TBFVResult;
import org.TBFV4R.utils.FSFSplit;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

public class TestFSFSplit {
    @Test
    void testFSFSplit(){
        List<String[]> fsfTwoPart = FSFSplit.parseFSFString("x is greater than 0 &&\n" +
                "  n becomes the least non-negative integer such that\n" +
                "  ((n)-1)*(n)/2<x && (1+(n))*(n)/2>=x ||\n" +
                "\n" +
                "  x is less than or equal to 0 &&\n" +
                "  n becomes 0");
        System.out.println("Select a test condition:");
        for (int i = 0; i < fsfTwoPart.size(); i++) {
            System.out.println("\t"+(i+1)+")"+fsfTwoPart.get(i)[0]+"\t(T"+(i+1)+")");
        }
        System.out.print("Enter index:");
    }
    @Test
    void getBase64(){
        String suJson = "{\n" +
                "  \"program\": \"public class Abs_Original {\\n\\n    public static int Abs(int num) {\\n        System.out.println(\\\"Function input int parameter num = \\\" + (num));\\n        if (num < 0) {\\n            System.out.println(\\\"Evaluating if condition: (num < 0) is evaluated as: \\\" + (num < 0));\\n            System.out.println(\\\"return_value = -num , current value of return_value : \\\" + (-num));\\n            return -num;\\n        } else {\\n            System.out.println(\\\"Evaluating if condition: !(num < 0) is evaluated as: \\\" + !(num < 0));\\n            System.out.println(\\\"return_value = num , current value of return_value : \\\" + (num));\\n            return num;\\n        }\\n    }\\n\\n    public static void main(String[] args) {\\n        int num = 1;\\n        int result = Abs(num);\\n        System.out.println(result);\\n    }\\n}\",\n" +
                "  \"preconditions\": [],\n" +
                "  \"T\": \"num >= 0\",\n" +
                "  \"D\": \"return_value == num\",\n" +
                "  \"pre_constrains\": []\n" +
                "}\n";
        String encoded = Base64.getEncoder().encodeToString(suJson.getBytes(StandardCharsets.UTF_8));
        System.out.println(encoded);
        System.out.println(suJson);
    }
    @Test
    void getBase64_2() throws IOException, InterruptedException {
        TBFVResult res =null;
        String suJson = "{\"program\":\"public class Abs_Original {\\n\\n    public static int Abs(int num) {\\n        System.out.println(\\\"Function input int parameter num = \\\" + (num));\\n        if (num < 0) {\\n            System.out.println(\\\"Evaluating if condition: (num < 0) is evaluated as: \\\" + (num < 0));\\n            System.out.println(\\\"return_value = -num , current value of return_value : \\\" + (-num));\\n            return -num;\\n        } else {\\n            System.out.println(\\\"Evaluating if condition: !(num < 0) is evaluated as: \\\" + !(num < 0));\\n            System.out.println(\\\"return_value = num , current value of return_value : \\\" + (num));\\n            return num;\\n        }\\n    }\\n\\n    public static void main(String[] args) {\\n        int num = -1;\\n        int result = Abs(num);\\n        System.out.println(result);\\n    }\\n}\\n\",\"preconditions\":[],\"T\":\"num < 0\",\"D\":\"return_value == -num\",\"pre_constrains\":[]}\n";
        String encoded = Base64.getEncoder().encodeToString(suJson.getBytes(StandardCharsets.UTF_8));
        System.out.println(encoded);
        System.out.println(suJson);
        ProcessBuilder pb = new ProcessBuilder(
                "python3",
                "resources/z3_validation_runner.py",
                "--su",
                encoded
        );

        Process process = pb.start();
        // 1. 捕获 Python 脚本的标准输出 (stdout)
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));

// 2. ***捕获 Python 脚本的标准错误 (stderr) - 这是关键***
        BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

// 读取并打印输出
        System.out.println("--- Python STDOUT ---");
        String s;
        while ((s = stdInput.readLine()) != null) {
            System.out.println(s);
        }

// 读取并打印错误
        System.out.println("--- Python STDERR (异常信息) ---");
        while ((s = stdError.readLine()) != null) {
            System.err.println(s); // 使用 System.err 打印错误
        }

// 3. 等待进程结束并获取退出代码
        int exitCode = process.waitFor();
        System.out.println("--- Python 进程退出代码: " + exitCode + " ---");
    }
}

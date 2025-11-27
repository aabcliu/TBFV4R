import gradio as gr
import requests
import json
import threading
import websocket

BASE_URL = "http://localhost:8080/api"
WS_URL = "ws://localhost:8080/ws/logs"

log_messages = []

def ws_listener():
    def on_message(ws, message):
        log_messages.append(message)
    ws = websocket.WebSocketApp(WS_URL, on_message=on_message)
    ws.run_forever()

threading.Thread(target=ws_listener, daemon=True).start()

def fetch_logs(extra_logs=None):
    all_logs = log_messages.copy()
    if extra_logs:
        all_logs.append(extra_logs)
    combined = "\n".join(all_logs)
    return combined.encode("utf-8", errors="replace").decode("utf-8")

def generate_ifsf(code: str):
    resp = requests.post(f"{BASE_URL}/generateIFSF", data=code.encode("utf-8"))
    return resp.text

def generate_fsf(ifsf: str):
    resp = requests.post(f"{BASE_URL}/generateFSF", data=ifsf.encode("utf-8"))
    return resp.text

def get_conditions(fsf: str):
    resp = requests.post(f"{BASE_URL}/getFSFConditions", data=fsf.encode("utf-8"))
    return resp.json()

def generate_test_case(condition: str):
    resp = requests.post(f"{BASE_URL}/generateTestCase", params={"condition": condition})
    return resp.text

def simulate_test_case(ssmp: str, currentT: str, currentD: str, test_case_map: dict):
    headers = {'Content-Type': 'application/json'}
    resp = requests.post(f"{BASE_URL}/simulateTestCase",
                         params={"ssmp": ssmp, "currentT": currentT, "currentD": currentD},
                         data=json.dumps(test_case_map),
                         headers=headers)
    return resp.text

def run_pipeline(code_content):
    ifsf = generate_ifsf(code_content)
    fsf = generate_fsf(ifsf)
    conditions = get_conditions(fsf)
    ssmp = code_content
    results = []

    extra_log = "=== Informal FSF ===\n" + ifsf + "\n=== Formal FSF ===\n" + fsf

    for cond in conditions:
        condition_expr = cond["T"]
        currentD = cond["D"]
        test_case = generate_test_case(condition_expr)
        test_case_map = {kv.split("=")[0]: kv.split("=")[1] for kv in test_case.split(",")}
        sim_result = simulate_test_case(ssmp, condition_expr, currentD, test_case_map)

        results.append({
            "T": condition_expr,
            "D": currentD,
            "Proposed Test Case": test_case,
            "Simulation Result": sim_result
        })
    return results, extra_log

with gr.Blocks() as demo:
    gr.Markdown("## Input your code and run the FSF pipeline")
    code_content = gr.Textbox(label="Code Content", lines=15)

    fsf_output = gr.Dataframe(
        headers=["Condition (T)", "Description (D)", "Proposed Test Case", "Simulation Result"],
        datatype=["str", "str", "str", "str"], interactive=False
    )
    run_btn = gr.Button("Run Pipeline")
    logs_output = gr.Textbox(label="Logs (including IFSF/FSF)", lines=20, interactive=False,
                             elem_classes="emoji-log-box")

    

    def on_run(code_content):
        results, extra_log = run_pipeline(code_content)
        fsf_list = [[r["T"], r["D"], r["Proposed Test Case"], r["Simulation Result"]] for r in results]
        return fsf_list, fetch_logs(extra_log)

    run_btn.click(on_run, inputs=[code_content], outputs=[fsf_output, logs_output])
    demo.load(lambda: None, [], [])

    demo.queue()
    demo.launch()

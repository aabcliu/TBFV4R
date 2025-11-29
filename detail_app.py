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

def fetch_logs():
    return "\n".join(log_messages)

def generate_ifsf(code_content):
    resp = requests.post(f"{BASE_URL}/generateIFSF", data=code_content.encode("utf-8"))
    return resp.text

def generate_fsf(ifsf):
    resp = requests.post(f"{BASE_URL}/generateFSF", data=ifsf.encode("utf-8"))
    return resp.text

def get_conditions(fsf):
    resp = requests.post(f"{BASE_URL}/getFSFConditions", data=fsf.encode("utf-8"))
    return resp.json()

def generate_test_case(condition):
    resp = requests.post(f"{BASE_URL}/generateTestCase", params={"condition": condition})
    return resp.text

def replace_test_case_value(test_case, new_value, condition):
    resp = requests.post(f"{BASE_URL}/replaceTestCaseValue",
                         params={"testCase": test_case, "newValue": new_value, "condition": condition})
    return resp.text

def run_test_case(ssmp, currentT, currentD, test_case_map):
    headers = {'Content-Type': 'application/json'}
    resp = requests.post(f"{BASE_URL}/simulateTestCase",
                         params={"ssmp": ssmp, "currentT": currentT, "currentD": currentD},
                         data=json.dumps(test_case_map),
                         headers=headers)
    return resp.text

with gr.Blocks() as demo:
    gr.Markdown("## Step 1: Input your code")
    code_input = gr.Textbox(label="Code Content", lines=15)

    ifsf_output = gr.Textbox(label="Generated Informal FSF", lines=10)
    ifsf_btn = gr.Button("Generate IFSF")

    gr.Markdown("## Step 2: Generate Formal FSF")
    fsf_output = gr.Textbox(label="Generated Formal FSF", lines=10)
    fsf_btn = gr.Button("Generate FSF")

    gr.Markdown("## Step 3: FSF Conditions Table & Path Selection")
    table_output = gr.Dataframe(headers=["Index", "Condition (T)", "Description (D)"], datatype=["str","str","str"])
    condition_dropdown = gr.Dropdown(label="Select Condition (T) to simulate", choices=[])

    replacement_input = gr.Textbox(label="Replacement Value (optional)", placeholder="e.g., newValue=5")
    simulate_btn = gr.Button("Simulate Selected Condition")
    results_output = gr.Textbox(label="Simulation Result", lines=5)
    logs_output = gr.Textbox(label="Real-time Logs", lines=15)

    # Step 1 callback
    def on_generate_ifsf(code_content):
        ifsf = generate_ifsf(code_content)
        return ifsf, ifsf
    ifsf_btn.click(on_generate_ifsf, inputs=[code_input], outputs=[ifsf_output, ifsf_output])

    # Step 2 callback
    def on_generate_fsf(ifsf_text, code_content):
        fsf = generate_fsf(ifsf_text)
        conditions = get_conditions(fsf)
        table_data = [[str(idx), c["T"], c["D"]] for idx, c in enumerate(conditions)]
        condition_choices = [c["T"] for c in conditions]
        return fsf, fsf, table_data, gr.update(choices=condition_choices)
    fsf_btn.click(on_generate_fsf, inputs=[ifsf_output, code_input],
                  outputs=[fsf_output, fsf_output, table_output, condition_dropdown])

    # Step 3 callback
    def simulate_selected(cond_selected, code_content, fsf_text, replace_val):
        if not cond_selected:
            return "No condition selected", fetch_logs()

        conditions = get_conditions(fsf_text)
        cond_info = next(c for c in conditions if c["T"] == cond_selected)

        test_case = generate_test_case(cond_info["T"])

        # 调用后端替换接口
        if replace_val.strip():
            try:
                new_value = int(replace_val.strip())
                replaced_test_case = replace_test_case_value(test_case, new_value, cond_info["T"])
                if replaced_test_case == "Invalid replacement, condition not satisfied.":
                    return replaced_test_case, fetch_logs()
                test_case = replaced_test_case
            except Exception as e:
                return f"Replacement failed: {str(e)}", fetch_logs()

        test_case_map = {kv.split("=")[0]: kv.split("=")[1] for kv in test_case.split(",")}
        result = run_test_case(code_content, cond_info["T"], cond_info["D"], test_case_map)
        return result, fetch_logs()

    simulate_btn.click(simulate_selected,
                       inputs=[condition_dropdown, code_input, fsf_output, replacement_input],
                       outputs=[results_output, logs_output])

demo.launch()
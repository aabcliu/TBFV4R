package org.TBFV4R.api;

import org.TBFV4R.TBFV.Runner;
import org.TBFV4R.TBFV.TBFVResult;
import org.TBFV4R.llm.Model;
import org.TBFV4R.utils.EvalUtil;
import org.TBFV4R.utils.FSFSplit;
import org.TBFV4R.utils.TBFVResultDecoder;
import org.TBFV4R.utils.WebLoggingOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.*;

import java.io.PrintStream;
import java.util.*;
import java.util.Base64;

@RestController
@RequestMapping("/api")
public class WebAPIController {

    private final Model model = new Model();
    private final Logger logger = LogManager.getLogger(WebAPIController.class);
    private final WebSocketConfig wsConfig;

    public WebAPIController(WebSocketConfig wsConfig) {
        this.wsConfig = wsConfig;
        PrintStream logOut = new PrintStream(new WebLoggingOutputStream(logger, false, wsConfig), true);
        System.setOut(logOut);
        PrintStream logErr = new PrintStream(new WebLoggingOutputStream(logger, true, wsConfig), true);
        System.setErr(logErr);
    }

    @PostMapping("/generateIFSF")
    public String generateIFSF(@RequestBody String code) {
        return model.code2IFSF(code);
    }

    @PostMapping("/generateFSF")
    public String generateFSF(@RequestBody String IFSF) {
        return model.IFSF2FSF(IFSF);
    }

    @PostMapping("/getFSFConditions")
    public List<Map<String, String>> getFSFConditions(@RequestBody String FSF) {
        List<String[]> fsfTwoPart = FSFSplit.parseFSFString(FSF);
        List<Map<String, String>> conditions = new ArrayList<>();
        for (String[] arr : fsfTwoPart) {
            Map<String, String> map = new HashMap<>();
            map.put("T", arr[0]);
            map.put("D", arr[1]);
            conditions.add(map);
        }
        return conditions;
    }

    @PostMapping("/generateTestCase")
    public String generateTestCase(@RequestParam String condition) {
        return model.generateTestCase(condition);
    }

    @PostMapping("/replaceTestCaseValue")
    public String replaceTestCaseValue(@RequestParam String testCase,
                                       @RequestParam int newValue,
                                       @RequestParam String condition) {
        String newTestCase = EvalUtil.replaceNumber(testCase, newValue);
        if(EvalUtil.evalBoolean("var " + newTestCase + ";", condition)){
            return newTestCase;
        } else {
            return "Invalid replacement, condition not satisfied.";
        }
    }



    @PostMapping("/simulateTestCase")
    public String simulateTestCase(@RequestBody Map<String, Object> request) {
        try {
            System.out.println("Getting parameters...");

            String ssmpBase64 = (String) request.get("ssmp");
            String currentTBase64 = (String) request.get("currentT");
            String currentDBase64 = (String) request.get("currentD");

            String ssmp = new String(Base64.getDecoder().decode(ssmpBase64), "UTF-8");
            String currentT = new String(Base64.getDecoder().decode(currentTBase64), "UTF-8");
            String currentD = new String(Base64.getDecoder().decode(currentDBase64), "UTF-8");

            Map<String, Object> testCaseObj = (Map<String, Object>) request.get("testCaseMap");
            Map<String, String> testCaseMap = new HashMap<>();
            for (Map.Entry<String, Object> entry : testCaseObj.entrySet()) {
                testCaseMap.put(entry.getKey(), entry.getValue().toString());
            }

            System.out.println("Validating...");
            TBFVResult tbfvResult = Runner.validateWithTestCase(ssmp, currentT, currentD, testCaseMap);
            return TBFVResultDecoder.parse(tbfvResult);

        } catch (Exception e) {
            e.printStackTrace();
            StringBuilder x = new StringBuilder();
            for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                x.append(stackTraceElement.toString()+"\n");
            }
            return "Error: " + x;
        }
    }

}

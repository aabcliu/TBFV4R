package org.TBFV4R.TBFV;


import org.TBFV4R.verification.FSFValidationUnit;
import org.TBFV4R.verification.SpecUnit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

public class Z3Solver {
    public static TBFVResult callZ3Solver(SpecUnit su) throws IOException {
        TBFVResult res =null;
        String suJson = su.toJson();
        String encoded = Base64.getEncoder().encodeToString(suJson.getBytes(StandardCharsets.UTF_8));
//        ProcessBuilder pb = new ProcessBuilder("python3", "resources/dynamic_testing.py", "--specunit",suJson);
        ProcessBuilder pb = new ProcessBuilder("python3", "resources/z3_validation_runner.py", "--su",encoded);
        Map<String, String> env = pb.environment();
        env.put("PYTHONIOENCODING", "UTF-8");
        Process process = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder errorInfo = new StringBuilder();
        String line;
        while((line = reader.readLine()) != null){
            if(line.startsWith("result:")){
                String resultJson = line.substring("result:".length()).trim();
                res = new TBFVResult(resultJson);
            }
            System.out.println(line);
        }

        // 
        BufferedReader errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        while((line = errReader.readLine()) != null){
            System.err.println("Error: " + line);
            errorInfo.append(line).append("\n");
        }

        // 
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if(res == null && !errorInfo.toString().isEmpty()){
            System.out.println("TBFV result");
            res = new TBFVResult(-1,"z3：\n" + errorInfo, "");
        }
        return res;
    }

    public static TBFVResult callZ3Solver(FSFValidationUnit fu) throws IOException {
        TBFVResult res =null;
        String fuJson = fu.toJson();
        ProcessBuilder pb = new ProcessBuilder("python3", "resources/z3_validation_runner.py", "--fu",fuJson);
        Process process = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while((line = reader.readLine()) != null){
            if(line.startsWith("FSF validation result:")){
                String resultJson = line.substring("FSF validation result:".length()).trim();
                res = new TBFVResult(resultJson);
            }
            System.out.println(line);
        }

        StringBuilder errorInfo = new StringBuilder();
        while((line = reader.readLine()) != null){
            if(line.startsWith("result:")){
                String resultJson = line.substring("result:".length()).trim();
                res = new TBFVResult(resultJson);
            }
            System.out.println(line);
        }
        BufferedReader errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        while((line = errReader.readLine()) != null){
            System.err.println("Error: " + line);
            errorInfo.append(line).append("\n");
        }

        // 
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if(res == null && !errorInfo.toString().isEmpty()){
            System.out.println("TBFV result");
            res = new TBFVResult(-1,"z3：\n" + errorInfo, "");
        }
        return res;
    }

    public static TBFVResult callZ3Solver2GenerateTestcase(SpecUnit gu) throws IOException {
        TBFVResult res =null;
        String guJson = gu.toJson();
        System.out.println("guJson: " + guJson);
        ProcessBuilder pb = new ProcessBuilder("python3", "resources/z3_validation_runner.py", "--gu",guJson);
        Process process = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder errorInfo = new StringBuilder();
        String line;
        while((line = reader.readLine()) != null){
            if(line.startsWith("Testcase generation result:")){
                String resultJson = line.substring("Testcase generation result:".length()).trim();
                res = new TBFVResult(resultJson);
            }
            System.out.println(line);
        }

        // 
        BufferedReader errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        while((line = errReader.readLine()) != null){
            System.err.println("Error: " + line);
            errorInfo.append(line).append("\n");
        }

        // 
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if(res == null){
            System.out.println("TBFV result");
            res = new TBFVResult(-1,"z3：\n" + errorInfo, "");
        }
        return res;
    }

}

package org.TBFV4R;

import org.TBFV4R.TBFV.Runner;
import org.TBFV4R.TBFV.TBFVResult;
import org.TBFV4R.TBFV.Z3Solver;
import org.TBFV4R.llm.Model;
import org.TBFV4R.tcg.ExecutionEnabler;
import org.TBFV4R.utils.*;
import org.TBFV4R.verification.SpecUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

import static org.TBFV4R.path.ExecutionPathPrinter.addPrintStmt;
import static org.TBFV4R.path.TransWorker.trans2SSMP;
import static org.TBFV4R.utils.InputUtil.readLineAndLog;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);
    public static void main(String[] args) throws IOException {
        PrintStream logOut = new PrintStream(new LoggingOutputStream(logger, false), true);
        System.setOut(logOut);
        PrintStream logErr = new PrintStream(new LoggingOutputStream(logger, true), true);
        System.setErr(logErr);

        Model model = new Model();
        System.out.println("Input path to Code File:");
        String file = readLineAndLog();//"dataset/Abs_Original.java";
        String code = FileUtil.readLinesAsString(file, "\n");
        System.out.println("Original Code & Description");
        System.out.println(code);
        System.out.println();

        String IFSF = model.code2IFSF(code);
        System.out.println("Informal FSF:");
        System.out.println(IFSF);
        System.out.println();

        String FSF = model.IFSF2FSF(IFSF);
        System.out.println("Formal FSF:");
        System.out.println(FSF);
        System.out.println();

        List<String[]> ifsfTwoPart = FSFSplit.parseIFSFString(IFSF);
        List<String[]> fsfTwoPart = FSFSplit.parseFSFString(FSF);
        while (true) {
            System.out.println("Select a test condition:");
            int i;
            for (i = 0; i < ifsfTwoPart.size(); i++) {
                System.out.println("\t" + (i + 1) + ")" + ifsfTwoPart.get(i)[0] + "\t(T" + (i + 1) + ")");
            }
            System.out.println("\t" + (i + 1) + ")" +  "Exit");
            System.out.println();


            int line = Integer.parseInt(readLineAndLog());
            line -= 1;
            if(line+1> fsfTwoPart.size()) return;
            String condition = fsfTwoPart.get(line)[0];
            String testCase = model.generateTestCase(condition);
            System.out.println("Proposed test case: " + testCase);
            boolean passTest = false;
            if (EvalUtil.evalBoolean("var " + testCase + ";", condition)) {
                System.out.println("Test Case:" + testCase + " satisfied condition " + condition);
                System.out.println("Press Enter or type one of {accept/ok/yes/y/confirm} to accept.");
                System.out.println("Or input ONE integer to replace:");
                passTest = true;
            } else {
                System.out.println("Test Case:" + testCase + " can not satisfied condition " + condition + " please check it manually");
                System.out.println("input ONE integer to replace:");
            }
            do {
                String x = readLineAndLog();
                Optional<Integer> input = InputUtil.processInput(x);
                if (input.isPresent()) {
                    int newValue = input.get();
                    passTest = false;
                    String newTestCase = EvalUtil.replaceNumber(testCase, newValue);
                    if (EvalUtil.evalBoolean("var " + newTestCase + ";", condition)) {
                        passTest = true;
                        testCase = newTestCase;
                        System.out.println("The new test case is " + testCase);
                    } else {
                        System.out.println("The integer does not satisfied condition " + condition);
                        System.out.println("input ONE integer to replace:");
                    }
                }
            } while (!passTest);

            System.out.println("[Run] Simulate test case");
            //code = addPrintStmt(code);
            String ssmp = trans2SSMP(code);
            HashMap<String, String> testCaseMap = new HashMap<>();

            String currentT = fsfTwoPart.get(line)[0];
            String currentD = fsfTwoPart.get(line)[1];
            for (String string : testCase.split(",")) {
                String LHS = string.split("=")[0];
                System.out.println(LHS);
                String RHS = string.split("=")[1];
                testCaseMap.put(LHS, RHS);
            }
            //currentD=currentD.replace(LHS,RHS);
            try {
                TBFVResult tbfvResult = Runner.validateWithTestCase(ssmp, currentT, currentD, testCaseMap);
                System.out.println(TBFVResultDecoder.parse(tbfvResult));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
    }
}

package org.TBFV4R;

import org.TBFV4R.TBFV.Runner;
import org.TBFV4R.TBFV.TBFVResult;
import org.TBFV4R.TBFV.Z3Solver;
import org.TBFV4R.llm.Model;
import org.TBFV4R.tcg.ExecutionEnabler;
import org.TBFV4R.utils.*;
import org.TBFV4R.verification.SpecUnit;

import java.io.IOException;
import java.util.*;

import static org.TBFV4R.path.ExecutionPathPrinter.addPrintStmt;
import static org.TBFV4R.path.TransWorker.trans2SSMP;

public class Main {
    public static void main(String[] args) throws IOException {
        Model model = new Model();
        System.out.println("Input path to Code File:");
        Scanner s = new Scanner(System.in);
        String file = s.nextLine();//"dataset/Abs_Original.java";
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
            for (int i = 0; i < ifsfTwoPart.size(); i++) {
                System.out.println("\t" + (i + 1) + ")" + ifsfTwoPart.get(i)[0] + "\t(T" + (i + 1) + ")");
            }
            System.out.print("Enter index:");

            int line = Integer.parseInt(s.nextLine());
            line -= 1;
            String condition = ifsfTwoPart.get(line)[0];
            String testCase = model.generateTestCase(condition);
            System.out.println("Proposed test case: " + testCase);
            boolean passTest = false;
            if (EvalUtil.evalBoolean("var " + testCase + ";", condition)) {
                System.out.println("Test Case:" + testCase + " satisfied condition " + condition);
                System.out.println("Press Enter or type one of {accept/ok/yes/y/confirm/是/好/确认} to accept.");
                System.out.println("Or input ONE integer to replace:");
                passTest = true;
            } else {
                System.out.println("Test Case:" + testCase + " can not satisfied condition " + condition + " please check it manually");
                System.out.println("input ONE integer to replace:");
            }
            do {
                String x = s.nextLine();
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
            String LHS = testCase.split("=")[0];
            String RHS = testCase.split("=")[1];
            testCaseMap.put(LHS, RHS);
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

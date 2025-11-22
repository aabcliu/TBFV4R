package org.TBFV4R;

import org.TBFV4R.llm.Model;
import org.TBFV4R.utils.FSFSplit;
import org.TBFV4R.utils.FileUtil;

import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        Model model = new Model();
        String code = FileUtil.readLinesAsString("dataset/Abs_Original.java","\n");
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

        List<String[]> fsfTwoPart = FSFSplit.parseFSFString(IFSF);
        System.out.println("Select a test condition:");
        for (int i = 0; i < fsfTwoPart.size(); i++) {
            System.out.println("\t"+(i+1)+")"+fsfTwoPart.get(i)[0]+"\t(T"+(i+1)+")");
        }
        System.out.print("Enter index:");

    }
}

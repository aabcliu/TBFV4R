import org.TBFV4R.utils.FSFSplit;
import org.junit.jupiter.api.Test;

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
}

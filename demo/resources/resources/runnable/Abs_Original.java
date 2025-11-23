public class Abs_Original {

    public static int Abs(int num) {
        System.out.println("Function input int parameter num = " + (num));
        if (num < 0) {
            System.out.println("Evaluating if condition: (num < 0) is evaluated as: " + (num < 0));
            System.out.println("return_value = -num , current value of return_value : " + (-num));
            return -num;
        } else {
            System.out.println("Evaluating if condition: !(num < 0) is evaluated as: " + !(num < 0));
            System.out.println("return_value = num , current value of return_value : " + (num));
            return num;
        }
    }

    public static void main(String[] args) {
        int num = -1;
        int result = Abs(num);
        System.out.println(result);
    }
}

public class ChangeLaneSimple {

    public static int Control_Lane_Change(int current_lane) {
        System.out.println("Function input int parameter current_lane = " + (current_lane));
        int target_lane = 2;
        System.out.println("target_lane = (2), current value of target_lane: " + (2));
        double car_speed = 28.0;
        System.out.println("car_speed = (28.0), current value of car_speed: " + (28.0));
        double safe_distance = 30.0;
        System.out.println("safe_distance = (30.0), current value of safe_distance: " + (30.0));
        double distanceAhead = 50.0;
        System.out.println("distanceAhead = (50.0), current value of distanceAhead: " + (50.0));
        double frontCarSpeed = 30.0;
        System.out.println("frontCarSpeed = (30.0), current value of frontCarSpeed: " + (30.0));
        double distanceBehind = 45.0;
        System.out.println("distanceBehind = (45.0), current value of distanceBehind: " + (45.0));
        double behindCarSpeed = 25.0;
        System.out.println("behindCarSpeed = (25.0), current value of behindCarSpeed: " + (25.0));
        int decision = 0;
        System.out.println("decision = (0), current value of decision: " + (0));
        int i = 0;
        System.out.println("i = (0), current value of i: " + (0));
        while (i < 1) {
            System.out.println("Entering loop with condition: (i < 1) is evaluated as: " + (i < 1));
            if (current_lane == target_lane) {
                System.out.println("Evaluating if condition: (current_lane == target_lane) is evaluated as: " + (current_lane == target_lane));
                decision = 0;
                System.out.println("decision = (0), current value of decision: " + (decision));
            } else {
                System.out.println("Evaluating if condition: !(current_lane == target_lane) is evaluated as: " + !(current_lane == target_lane));
                boolean safeAhead = (distanceAhead >= safe_distance) && (frontCarSpeed >= car_speed);
                System.out.println("safeAhead = ((distanceAhead >= safe_distance) && (frontCarSpeed >= car_speed)), current value of safeAhead: " + ((distanceAhead >= safe_distance) && (frontCarSpeed >= car_speed)));
                boolean safeBehind = (distanceBehind >= safe_distance) && (car_speed >= behindCarSpeed);
                System.out.println("safeBehind = ((distanceBehind >= safe_distance) && (car_speed >= behindCarSpeed)), current value of safeBehind: " + ((distanceBehind >= safe_distance) && (car_speed >= behindCarSpeed)));
                if (safeAhead && safeBehind) {
                    decision = 1;
                    System.out.println("decision = (1), current value of decision: " + (decision));
                }
            }
            i++;
        }
        System.out.println("Exiting loop, condition no longer holds: (i < 1) is evaluated as: " + (i < 1));
        System.out.println("return_value = decision , current value of return_value : " + (decision));
        return decision;
    }

    public static void main(String[] args) {
        int current_lane = 1;
        int result = Control_Lane_Change(current_lane);
        System.out.println(result);
    }
}

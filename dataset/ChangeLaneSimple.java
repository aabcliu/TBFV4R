public class ChangeLaneSimple {

    public static int Control_Lane_Change(int current_lane) {
        int target_lane = 2;
        double car_speed = 28.0;
        double safe_distance = 30.0;
        double distanceAhead = 50.0;
        double frontCarSpeed = 30.0;
        double distanceBehind = 45.0;
        double behindCarSpeed = 25.0;

        int decision = 0;
        int i = 0;
        while (i < 1) {
            if (current_lane == target_lane) {
                decision = 0;
            } else {
                boolean safeAhead = (distanceAhead >= safe_distance) && (frontCarSpeed >= car_speed);
                boolean safeBehind = (distanceBehind >= safe_distance) && (car_speed >= behindCarSpeed);
                if (safeAhead && safeBehind) {
                    decision = 1;
                }
            }
            i++;
        }
        return decision;
    }

}

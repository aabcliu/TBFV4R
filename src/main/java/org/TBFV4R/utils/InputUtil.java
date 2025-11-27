package org.TBFV4R.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.Scanner;

public class InputUtil {
    private static final Logger logger = LogManager.getLogger(InputUtil.class);
    private static final Scanner scanner = new Scanner(System.in);

    public static String readLineAndLog() {
        String line = scanner.nextLine();
        logger.info("User Input: "+ line);
        return line;
    }
    public static int readIntAndLog() {
        int integer = scanner.nextInt();
        logger.info("User Input: "+ integer);
        return integer;
    }

    public static Optional<Integer> processInput(String input) {
        if (input == null) return Optional.empty();
        String trimmed = input.trim();
        if (trimmed.isEmpty() ||
                trimmed.equalsIgnoreCase("accept") ||
                trimmed.equalsIgnoreCase("ok") ||
                trimmed.equalsIgnoreCase("yes") ||
                trimmed.equalsIgnoreCase("y") ||
                trimmed.equalsIgnoreCase("confirm") ||
                trimmed.equals("是") ||
                trimmed.equals("好") ||
                trimmed.equals("确认")) {
            return Optional.empty();
        }

        try {
            int value = Integer.parseInt(trimmed);
            return Optional.of(value);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}

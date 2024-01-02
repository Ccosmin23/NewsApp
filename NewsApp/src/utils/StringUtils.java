package utils;

public class StringUtils {
    public static String applyBoldTo(String text) {
        return "'\033[1m" + text + "\033[0m'";
    }
}

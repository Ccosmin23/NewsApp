package utils;

public class StringUtils {
    public static String applyBoldTo(String text, Boolean withSingleQuotationMarks) {
        String textToReturn;

        if (withSingleQuotationMarks) {
            textToReturn = "'\033[1m" + text + "\033[0m'";
        } else {
            textToReturn = "\033[1m" + text + "\033[0m";
        }

        return textToReturn;
    }
}

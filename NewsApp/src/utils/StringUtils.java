package utils;

import java.util.Random;

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

    //   Generează un șir de caractere cu lungime cuprins în intervalul ["minSize", "maxSize"]
    // și conține caractere de la 'a' la 'z'
    public static String randomString (int minSize, int maxSize) {
        StringBuilder textToReturn = new StringBuilder("");
        Random rng = new Random();
        
        int i = 0;
        int length = rng.nextInt(minSize, maxSize);

        while (i < length) {
            textToReturn.append((char) rng.nextInt(0x61, 0x7A));
            
            i += 1;
        }

        return textToReturn.toString();
    }
}

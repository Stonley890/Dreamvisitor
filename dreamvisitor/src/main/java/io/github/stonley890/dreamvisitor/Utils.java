package io.github.stonley890.dreamvisitor;

public class Utils {

    /**
     * Adds the hyphens back into a String UUID.
     * @param uuid the UUID as a string without hyphens.
     * @return a UUID as a string with hyphens.
     */
    public static String formatUuid(String uuid) {
        return uuid.replaceFirst(
                "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                "$1-$2-$3-$4-$5");
    }

    public static String escapeMarkdownFormatting(String string) {
        return string.replaceAll("_","\\_").replaceAll("\\*","\\\\*").replaceAll("\\|","\\\\|");
    }
}

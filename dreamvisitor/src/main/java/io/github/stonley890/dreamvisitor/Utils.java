package io.github.stonley890.dreamvisitor;

public class Utils {

    /**
     * Adds the hyphens back into a String UUID.
     * @param uuid the UUID as a {@link String} without hyphens.
     * @return a UUID as a string with hyphens.
     */
    public static String formatUuid(String uuid) {
        return uuid.replaceFirst(
                "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                "$1-$2-$3-$4-$5");
    }

    /**
     * Escapes all Discord markdown elements in a {@link String}.
     * @param string the {@link String} to format.
     * @return the formatted {@link String}.
     */
    public static String escapeMarkdownFormatting(String string) {
        return string.replaceAll("_","\\_").replaceAll("\\*","\\\\*").replaceAll("\\|","\\\\|");
    }
}

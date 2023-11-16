package io.github.stonley890.dreamvisitor;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.shanerx.mojang.Mojang;

import java.util.UUID;

public class Utils {

    /**
     * Adds the hyphens back into a String UUID.
     * @param uuid the UUID as a {@link String} without hyphens.
     * @return a UUID as a string with hyphens.
     */
    @Contract(pure = true)
    public static @NotNull String formatUuid(@NotNull String uuid) {
        return uuid.replaceFirst(
                "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                "$1-$2-$3-$4-$5");
    }

    /**
     * Escapes all Discord markdown elements in a {@link String}.
     * @param string the {@link String} to format.
     * @return the formatted {@link String}.
     */
    public static @NotNull String escapeMarkdownFormatting(@NotNull String string) {
        return string.replaceAll("_","\\\\_").replaceAll("\\*","\\\\*").replaceAll("\\|","\\\\|");
    }

    public static String getUsernameOfUuid(@NotNull UUID uuid) {
        Mojang mojang = new Mojang().connect();
        return mojang.getPlayerProfile(uuid.toString()).getUsername();
    }

    public static String getUsernameOfUuid(@NotNull String uuid) {
        Mojang mojang = new Mojang().connect();
        return mojang.getPlayerProfile(uuid).getUsername();
    }

    public static @Nullable UUID getUUIDOfUsername(@NotNull String username) {
        Mojang mojang = new Mojang().connect();
        try {
            return UUID.fromString(formatUuid(mojang.getUUIDOfUsername(username)));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

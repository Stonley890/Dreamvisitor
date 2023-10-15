package io.github.stonley890.dreamvisitor;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

import static io.github.stonley890.dreamvisitor.Dreamvisitor.plugin;

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
}

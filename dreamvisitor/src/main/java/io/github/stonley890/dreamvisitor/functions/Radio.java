package io.github.stonley890.dreamvisitor.functions;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Radio {
    public static void buildMessage(String message, @NotNull String name, @NotNull Type type, @Nullable String tag) {

        // Set color of name to red if from console
        String finalMessage = getString(message, name, type);

        // Send messageBuilder
        Dreamvisitor.getPlugin().getLogger().info(ChatColor.stripColor(finalMessage));
        for (Player operator : Bukkit.getServer().getOnlinePlayers())
        {
            switch (type) {
                case STANDARD -> {
                    if (operator.isOp() || operator.hasPermission("dreamvisitor.radio"))
                        operator.sendMessage(finalMessage);
                }
                case ADMIN -> {
                    if (operator.isOp()) operator.sendMessage(finalMessage);
                }
                case TAG -> {
                    if (operator.getScoreboardTags().contains(tag) || operator.isOp()) operator.sendMessage(finalMessage);
                }
            }

        }
    }

    private static @NotNull String getString(String message, @NotNull String name, @NotNull Type type) {
        ChatColor nameColor = ChatColor.YELLOW;
        if (name.equals("Console")) {
            nameColor = ChatColor.RED;
        }

        // Build messageBuilder
        String radioType = "[Staff Radio]";
        if (type == Type.ADMIN) radioType = "[Admin Radio]";
        else if (type == Type.TAG) radioType = "[Tag Radio]";

        return ChatColor.DARK_AQUA + radioType + nameColor + name + ": " + ChatColor.WHITE + message;
    }

    public enum Type {
        STANDARD,
        ADMIN,
        TAG
    }
}

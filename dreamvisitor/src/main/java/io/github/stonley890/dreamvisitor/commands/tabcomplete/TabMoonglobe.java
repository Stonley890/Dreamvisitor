package io.github.stonley890.dreamvisitor.commands.tabcomplete;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TabMoonglobe implements TabCompleter {
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {

            // competition modify <player> <action>
            suggestions.add("remove");
            suggestions.add("create");

        } else if (args.length == 2) {

            if (args[0].equals("remove")) {
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    suggestions.add(onlinePlayer.getName());
                }
            } else if (args[0].equals("create")) {
                suggestions.add("@a");
                suggestions.add("@e");
                suggestions.add("@p");
                suggestions.add("@r");
                suggestions.add("@s");
                for (Player player : Bukkit.getOnlinePlayers()) {
                    suggestions.add(player.getName());
                }
            }
        } else if (args.length == 3) {
            if (args[0].equals("create")) {
                if (sender instanceof Player player) {
                    suggestions.add(String.valueOf(Math.round(player.getLocation().getX() * 100) / 100));
                } else {
                    suggestions.add("~");
                }
            }
        } else if (args.length == 4) {
            if (args[0].equals("create")) {
                if (sender instanceof Player player) {
                    suggestions.add(String.valueOf(Math.round(player.getLocation().getY() * 100) / 100));
                } else {
                    suggestions.add("~");
                }
            }
        } else if (args.length == 5) {
            if (args[0].equals("create")) {
                if (sender instanceof Player player) {
                    suggestions.add(String.valueOf(Math.round(player.getLocation().getZ() * 100) / 100));
                } else {
                    suggestions.add("~");
                }
            }
        }

        return suggestions;
    }
}

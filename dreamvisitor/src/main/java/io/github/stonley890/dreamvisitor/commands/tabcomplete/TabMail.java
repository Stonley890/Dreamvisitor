package io.github.stonley890.dreamvisitor.commands.tabcomplete;

import io.github.stonley890.dreamvisitor.commands.tabcomplete.utils.TabCompleteTree;
import io.github.stonley890.dreamvisitor.data.Mail;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TabMail implements TabCompleter {
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        // mail [locations [add <x> <y> <z> <name> <weight> <homeTribe> | remove <name> | list] | delivery [terminal <playerSelector> | add <playerSelector> <start> <end> | remove <playerSelector> | list]]

        List<String> suggestions = new ArrayList<>();

        TabCompleteTree tabTree = new TabCompleteTree("mail");


        if (args.length == 1) {

            suggestions.add("locations");
            suggestions.add("delivery");

        } else if (args.length == 2) {

            if (args[0].equals("locations")) {
                suggestions.add("add");
                suggestions.add("remove");
                suggestions.add("list");
            } else if (args[0].equals("delivery")) {
                suggestions.add("terminal");
                suggestions.add("add");
                suggestions.add("remove");
                suggestions.add("list");
            }
        } else if (args.length == 3) {
            if (args[0].equals("locations")) {
                if (args[1].equals("remove")) {
                    for (Mail.MailLocation location : Mail.getLocations()) {
                        suggestions.add(location.getName());
                    }
                }
            } else if (args[0].equals("delivery")) {
                if (args[1].equals("terminal")) {
                    suggestions.add("@a");
                    suggestions.add("@p");
                    suggestions.add("@r");
                    suggestions.add("@s");
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        suggestions.add(player.getName());
                    }
                } else if (args[1].equals("remove")) {
                    for (Mail.Deliverer deliverer : Mail.getDeliverers()) {
                        suggestions.add(deliverer.getPlayer().getName());
                    }
                } else if (args[1].equals("add")) {
                    suggestions.add("@a");
                    suggestions.add("@p");
                    suggestions.add("@r");
                    suggestions.add("@s");
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        suggestions.add(player.getName());
                    }
                }
            }
        } else if (args.length == 4) {
            if (args[0].equals("locations")) {
                if (args[1].equals(""))
                if (sender instanceof Player player) {
                    suggestions.add(String.valueOf(Math.round(player.getLocation().getX() * 100) / 100));
                } else {
                    suggestions.add("~");
                }
            }
        } else if (args.length == 5) {
            if (args[0].equals("create")) {
                if (sender instanceof Player player) {
                    suggestions.add(String.valueOf(Math.round(player.getLocation().getY() * 100) / 100));
                } else {
                    suggestions.add("~");
                }
            }
        } else if (args.length == 6) {
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
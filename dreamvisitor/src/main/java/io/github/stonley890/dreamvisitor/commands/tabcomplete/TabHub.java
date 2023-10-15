package io.github.stonley890.dreamvisitor.commands.tabcomplete;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.shanerx.mojang.Mojang;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TabHub implements TabCompleter {
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        ArrayList<String> suggestions = new ArrayList<>();

        if (args.length == 1) {

            suggestions.add("@a");
            suggestions.add("@e");
            suggestions.add("@p");
            suggestions.add("@r");
            suggestions.add("@s");

            for (Player player : Bukkit.getOnlinePlayers()) {
                suggestions.add(player.getName());
            }

        }

        return suggestions;

    }
}

package io.github.stonley890.dreamvisitor.commands.tabcomplete;

import java.io.File;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.shanerx.mojang.Mojang;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import kotlin.collections.builders.ListBuilder;

public class TabSoftWhitelist implements TabCompleter {

    List<String> suggestions = new ListBuilder<>();

    Dreamvisitor plugin = Dreamvisitor.getPlugin();

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        // Clear previous suggestions or create a new ListBuilder
        if (suggestions != null) {
            suggestions.clear();
        } else {
            suggestions = new ListBuilder<>();
        }

        if (args.length == 0) {

            // pausebypass <add|remove|list>
            suggestions.add("add");
            suggestions.add("remove");
            suggestions.add("list");
            suggestions.add("on");
            suggestions.add("off");

        } else if (args.length == 1) {

            if (args[0].equals("add")) {

                // pausebypass add <player>
                for (Player player : Bukkit.getOnlinePlayers()) {
                    suggestions.add(player.getName());
                }

            } else if (args[0].equals("remove")) {

                //pausebypass remove <player>

                Mojang mojang = new Mojang().connect();

                // Load pauseBypass.yml
                File file = new File(plugin.getDataFolder().getAbsolutePath() + "/softWhitelist.yml");
                FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);

                // If players are bypassing
                if (file.exists() && fileConfig.get("players") != null) {

                    // Get names and add them to tab suggestions
                    for (String player : ((List<String>) fileConfig.get("players"))) {
                        suggestions.add(mojang.getPlayerProfile(player).getUsername());
                    }

                }
                
            }
        }

        return suggestions;
    }

}

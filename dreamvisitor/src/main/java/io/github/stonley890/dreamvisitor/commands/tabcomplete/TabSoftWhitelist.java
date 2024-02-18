package io.github.stonley890.dreamvisitor.commands.tabcomplete;

import io.github.stonley890.dreamvisitor.Main;
import io.github.stonley890.dreamvisitor.data.PlayerUtility;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TabSoftWhitelist implements TabCompleter {

    Main plugin = Main.getPlugin();

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {

        ArrayList<String> suggestions = new ArrayList<>();

        if (args.length == 1) {

            // pausebypass <add|remove|list>
            suggestions.add("add");
            suggestions.add("remove");
            suggestions.add("list");
            suggestions.add("on");
            suggestions.add("off");

        } else if (args.length == 2) {

            if (args[0].equals("add")) {

                // pausebypass add <player>
                for (Player player : Bukkit.getOnlinePlayers()) {
                    suggestions.add(player.getName());
                }

            } else if (args[0].equals("remove")) {

                //pausebypass remove <player>

                // Load pauseBypass.yml
                File file = new File(plugin.getDataFolder().getAbsolutePath() + "/softWhitelist.yml");
                FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);

                // If players are bypassing
                if (file.exists() && fileConfig.get("players") != null) {

                    // Get names and add them to tab suggestions
                    for (String player : (Objects.requireNonNull(fileConfig.getStringList("players")))) {
                        suggestions.add(PlayerUtility.getUsernameOfUuid(player));
                    }

                }
                
            }
        }

        return suggestions;
    }

}

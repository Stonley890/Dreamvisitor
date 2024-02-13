package io.github.stonley890.dreamvisitor.commands;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.functions.Moonglobe;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CmdMoonglobe implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

        if (args.length == 0) {

            sender.sendMessage(Dreamvisitor.PREFIX + "Number of active moon globes: " + Moonglobe.activeMoonglobes.size());

        } else if (args.length == 1) {

            Player player = Bukkit.getPlayer(args[0]);

            if (player == null) {

                for (Moonglobe activeMoonglobe : Moonglobe.activeMoonglobes) {
                    activeMoonglobe.remove(null);
                }

                sender.sendMessage(Dreamvisitor.PREFIX + "Deleted all moon globes.");
            }
            else {

                new Moonglobe(player.getUniqueId(), player.getLocation(), 64);
                sender.sendMessage(Dreamvisitor.PREFIX + "Created a moon globe.");

            }

        }

        return true;
    }
}

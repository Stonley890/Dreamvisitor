package io.github.stonley890.commands;

import java.util.TimerTask;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.github.stonley890.Main;

public class CmdPanic implements CommandExecutor {

    Main plugin = Main.getPlugin();
    boolean panicAsked = false;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!panicAsked) {
            panicAsked = true;
            sender.sendMessage(
                    ChatColor.RED + "Are you sure you want to kick all players? Run /panic again to confirm.");
            new java.util.Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    panicAsked = false;
                }
            }, 5000);
        } else {
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                if (!player.isOp()) {
                    player.kickPlayer("Panic!");
                }
            }
            Main.playerlimit = 0;
            plugin.getConfig().set("playerlimit", 0);
            plugin.saveConfig();
            Bukkit.getServer().broadcastMessage(
                    ChatColor.RED + "Panicked by " + sender.getName() + ".\nPlayer limit override set to 0.");
        }
        return true;
    }
}

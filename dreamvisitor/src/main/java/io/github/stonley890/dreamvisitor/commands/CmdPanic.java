package io.github.stonley890.dreamvisitor.commands;

import java.util.TimerTask;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.Main;
import org.jetbrains.annotations.NotNull;

public class CmdPanic implements CommandExecutor {

    final Main plugin = Main.getPlugin();
    boolean panicAsked = false;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!panicAsked) {
            panicAsked = true;
            sender.sendMessage(Main.PREFIX +
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
            Main.playerLimit = 0;
            plugin.getConfig().set("playerlimit", 0);
            plugin.saveConfig();
            Bukkit.getServer().broadcastMessage(
                    ChatColor.RED + "Panicked by " + sender.getName() + ".\nPlayer limit override set to 0.");
            Bot.sendMessage(Bot.gameLogChannel, "**Panicked by " + sender.getName());
        }
        return true;
    }
}

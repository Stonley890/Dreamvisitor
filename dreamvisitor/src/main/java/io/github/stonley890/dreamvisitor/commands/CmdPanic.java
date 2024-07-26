package io.github.stonley890.dreamvisitor.commands;

import java.util.TimerTask;

import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import org.jetbrains.annotations.NotNull;

public class CmdPanic implements DVCommand {

    final Dreamvisitor plugin = Dreamvisitor.getPlugin();
    boolean panicAsked = false;

    @NotNull
    @Override
    public CommandAPICommand getCommand() {
        return new CommandAPICommand("panic")
                .withPermission("dreamvisitor.panic")
                .withHelp("Panic!", "Kicks all non-operators from the server and sets the player limit to 0.")
                .executesNative((sender, args) -> {
                    if (!panicAsked) {
                        panicAsked = true;
                        sender.sendMessage(Dreamvisitor.PREFIX +
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
                        Dreamvisitor.playerLimit = 0;
                        plugin.getConfig().set("playerlimit", 0);
                        plugin.saveConfig();
                        Bukkit.getServer().broadcastMessage(
                                ChatColor.RED + "Panicked by " + sender.getName() + ".\nPlayer limit override set to 0.");
                        Bot.sendLog("**Panicked by " + sender.getName());
                    }
                });
    }
}

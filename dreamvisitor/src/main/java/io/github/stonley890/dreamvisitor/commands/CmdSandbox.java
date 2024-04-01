package io.github.stonley890.dreamvisitor.commands;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.data.PlayerMemory;
import io.github.stonley890.dreamvisitor.data.PlayerUtility;
import io.github.stonley890.dreamvisitor.functions.Sandbox;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CmdSandbox implements CommandExecutor {


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

        // sandbox [<player> [on | off]]

        if (args.length == 0) {

            List<Player> sandboxedPlayers = new ArrayList<>();

            for (Player player : Bukkit.getOnlinePlayers()) {
                PlayerMemory memory = PlayerUtility.getPlayerMemory(player.getUniqueId());
                if (memory.sandbox) sandboxedPlayers.add(player);
            }

            if (sandboxedPlayers.isEmpty()) {
                sender.sendMessage(Dreamvisitor.PREFIX + "No players currently online are in sandbox mode. Use /sandbox <player> [on|off] to toggle sandbox mode.");
                return true;
            }

            ComponentBuilder messageBuilder = new ComponentBuilder(Dreamvisitor.PREFIX + "Players currently sandboxed:\n");

            HoverEvent tooltop = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to remove."));

            for (Player player : sandboxedPlayers) {
                messageBuilder.append("[").color(ChatColor.WHITE)
                        .append(player.getName()).color(ChatColor.YELLOW)
                        .event(tooltop).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sandbox " + player.getName() + " off"))
                        .append("] ").color(ChatColor.WHITE).event((ClickEvent) null);
            }

            sender.spigot().sendMessage(messageBuilder.create());
            return true;

        } else if (args.length < 3) {

            String targetString = args[0];

            List<Entity> targetedEntities = Bukkit.selectEntities(sender, targetString);
            List<Player> targetedPlayers = new ArrayList<>();

            for (Entity targetedEntity : targetedEntities) {
                if (!(targetedEntity instanceof Player)) {
                    sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "This command only applies to players!");
                    return true;
                }
                targetedPlayers.add((Player) targetedEntity);
            }

            if (args.length == 2) {
                // On or off
                if (args[1].equals("on")) {

                    targetedPlayers.forEach(Sandbox::enableSandbox);
                    sender.sendMessage(Dreamvisitor.PREFIX + "Enabled sandbox mode for " + targetedPlayers.size() + " players.");

                } else if (args[1].equals("off")) {

                    targetedPlayers.forEach(Sandbox::disableSandbox);
                    sender.sendMessage(Dreamvisitor.PREFIX + "Disabled sandbox mode for " + targetedPlayers.size() + " players.");

                } else {
                    sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "Invalid argument! /sandbox [player] [on|off]");
                }
            } else {
                // Toggle
                targetedPlayers.forEach(player -> {
                    if (PlayerUtility.getPlayerMemory(player.getUniqueId()).sandbox) Sandbox.disableSandbox(player);
                    else Sandbox.enableSandbox(player);
                });
                sender.sendMessage(Dreamvisitor.PREFIX + "Toggled sandbox mode for " + targetedPlayers.size() + " players.");
            }
        }

        return true;
    }
}

package io.github.stonley890.dreamvisitor.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
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
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CmdSandbox implements DVCommand {

    @NotNull
    @Override
    public CommandAPICommand getCommand() {
        return new CommandAPICommand("sandbox")
                .withHelp("Manage the sandbox.", "Manage players' access to Sandbox Mode.")
                .withPermission(CommandPermission.fromString("dreamvisitor.sandbox"))
                .withOptionalArguments(new EntitySelectorArgument.ManyPlayers("players"))
                .withOptionalArguments(new BooleanArgument("state"))
                .executesNative((sender, args) -> {
                    Collection<Player> players = (Collection<Player>) args.get("players");
                    if (players == null) {
                        List<Player> sandboxedPlayers = new ArrayList<>();

                        for (Player player : Bukkit.getOnlinePlayers()) {
                            PlayerMemory memory = PlayerUtility.getPlayerMemory(player.getUniqueId());
                            if (memory.sandbox) sandboxedPlayers.add(player);
                        }

                        if (sandboxedPlayers.isEmpty()) {
                            sender.sendMessage(Dreamvisitor.PREFIX + "No players currently online are in sandbox mode. Use /sandbox <player> [true|false] to toggle sandbox mode.");
                        }

                        ComponentBuilder messageBuilder = new ComponentBuilder(Dreamvisitor.PREFIX + "Players currently sandboxed:\n");

                        HoverEvent tooltip = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to remove."));

                        for (Player player : sandboxedPlayers) {
                            messageBuilder.append("[").color(ChatColor.WHITE)
                                    .append(player.getName()).color(ChatColor.YELLOW)
                                    .event(tooltip).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sandbox " + player.getName() + " off"))
                                    .append("] ").color(ChatColor.WHITE).event((ClickEvent) null);
                        }

                        sender.spigot().sendMessage(messageBuilder.create());
                    } else {
                        Object stateArg = args.get("state");
                        if (stateArg == null) {
                            players.forEach(player -> {
                                if (PlayerUtility.getPlayerMemory(player.getUniqueId()).sandbox) Sandbox.disableSandbox(player);
                                else Sandbox.enableSandbox(player);
                            });
                            sender.sendMessage(Dreamvisitor.PREFIX + "Toggled sandbox mode for " + players.size() + " players.");
                        } else {
                            boolean sandboxState = (boolean) stateArg;
                            if (sandboxState) {
                                players.forEach(Sandbox::enableSandbox);
                                sender.sendMessage(Dreamvisitor.PREFIX + "Enabled sandbox mode for " + players.size() + " players.");
                            } else {
                                players.forEach(Sandbox::disableSandbox);
                                sender.sendMessage(Dreamvisitor.PREFIX + "Disabled sandbox mode for " + players.size() + " players.");
                            }
                        }
                    }
                });
    }
}

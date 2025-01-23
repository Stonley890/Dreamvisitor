package io.github.stonley890.dreamvisitor.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.OfflinePlayerArgument;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.data.PlayerUtility;
import io.github.stonley890.dreamvisitor.functions.SoftWhitelist;
import io.github.stonley890.dreamvisitor.functions.SystemMessage;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class CmdSoftwhitelist implements DVCommand {

    final Dreamvisitor plugin = Dreamvisitor.getPlugin();

    @NotNull
    @Override
    public CommandAPICommand getCommand() {
        return new CommandAPICommand("softwhitelist")
                .withPermission(CommandPermission.fromString("dreamvisitor.softwhitelist"))
                .withHelp("Manage the soft whitelist.", "Manage the soft whitelist.")
                .withSubcommand(new CommandAPICommand("add")
                        .withArguments(new OfflinePlayerArgument("player"))
                        .executesNative((sender, args) -> {
                            OfflinePlayer player = (OfflinePlayer) args.get("player");
                            if (player == null) throw CommandAPI.failWithString("That player could not be found!");
                            List<UUID> players = SoftWhitelist.getPlayers();
                            if (players.contains(player.getUniqueId())) throw CommandAPI.failWithString("That player is already on the soft whitelist!");
                            players.add(player.getUniqueId());
                            SoftWhitelist.setPlayers(players);
                            sender.sendMessage(SystemMessage.formatPrivate("Added " + player.getName() + "."));
                        })
                )
                .withSubcommand(new CommandAPICommand("remove")
                        .withArguments(new OfflinePlayerArgument("player"))
                        .executesNative((sender, args) -> {
                            OfflinePlayer player = (OfflinePlayer) args.get("player");
                            if (player == null) throw CommandAPI.failWithString("That player could not be found!");
                            List<UUID> players = SoftWhitelist.getPlayers();
                            if (!players.contains(player.getUniqueId())) throw CommandAPI.failWithString("That player is not on soft whitelist!");
                            players.remove(player.getUniqueId());
                            SoftWhitelist.setPlayers(players);
                            sender.sendMessage(SystemMessage.formatPrivate("Removed " + player.getName() + "."));
                        })
                )
                .withSubcommand(new CommandAPICommand("list")
                        .executesNative((sender, args) -> {
                            List<UUID> players = SoftWhitelist.getPlayers();
                            StringBuilder list = new StringBuilder();

                            for (UUID player : players) {
                                if (!list.isEmpty()) {
                                    list.append(", ");
                                }
                                list.append(PlayerUtility.getUsernameOfUuid(player));
                            }
                            sender.sendMessage(SystemMessage.formatPrivate("Players soft-whitelisted: " + list));
                        })
                )
                .withSubcommand(new CommandAPICommand("on")
                        .executesNative((sender, args) -> {
                            plugin.getConfig().set("softwhitelist", true);
                            plugin.saveConfig();
                            sender.sendMessage(SystemMessage.formatPrivate("Soft whitelist enabled."));
                        })
                )
                .withSubcommand(new CommandAPICommand("off")
                        .executesNative((sender, args) -> {
                            plugin.getConfig().set("softwhitelist", false);
                            plugin.saveConfig();
                            sender.sendMessage(SystemMessage.formatPrivate("Soft whitelist disabled."));
                        })
                );
    }
}

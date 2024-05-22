package io.github.stonley890.dreamvisitor.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import org.jetbrains.annotations.NotNull;

public class CmdTogglepvp implements DVCommand {

    final Dreamvisitor plugin = Dreamvisitor.getPlugin();
    final String pvpDisabled = "disablepvp";

    @NotNull
    @Override
    public CommandAPICommand getCommand() {
        return new CommandAPICommand("togglepvp")
                .withPermission(CommandPermission.fromString("dreamvisitor.togglepvp"))
                .withHelp("Toggle global PvP.", "Toggle whether PvP is enabled or disabled.")
                .executesNative((sender, args) -> {
                    if (plugin.getConfig().getBoolean(pvpDisabled)) {
                        plugin.getConfig().set(pvpDisabled, false);
                        Bukkit.getServer().broadcastMessage(ChatColor.BLUE + "PvP globally enabled.");
                    } else {
                        plugin.getConfig().set(pvpDisabled, true);
                        Bukkit.getServer().broadcastMessage(ChatColor.BLUE + "PvP globally disabled.");
                    }
                    plugin.saveConfig();
                });
    }
}

package io.github.stonley890.dreamvisitor.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import org.jetbrains.annotations.NotNull;

public class CmdTogglepvp implements DVCommand {

    final Dreamvisitor plugin = Dreamvisitor.getPlugin();
    final String pvpDisabled = "disablepvp";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        // togglepvp

        // Change config
        if (plugin.getConfig().getBoolean(pvpDisabled)) {
            plugin.getConfig().set(pvpDisabled, false);
            Bukkit.getServer().broadcastMessage(ChatColor.BLUE + "PvP globally enabled.");
        } else {
            plugin.getConfig().set(pvpDisabled, true);
            Bukkit.getServer().broadcastMessage(ChatColor.BLUE + "PvP globally disabled.");
        }
        plugin.saveConfig();
        return true;
    }

    @NotNull
    @Override
    public String getCommandName() {
        return "togglepvp";
    }

    @Override
    public LiteralCommandNode<?> getNode() {
        return LiteralArgumentBuilder.literal(getCommandName()).build();
    }
}

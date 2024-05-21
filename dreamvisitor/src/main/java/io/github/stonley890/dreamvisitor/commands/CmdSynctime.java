package io.github.stonley890.dreamvisitor.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CmdSynctime implements DVCommand {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        // synctime [<world>]

        if (args.length == 0) {
            if (sender instanceof Player player) {
                for (World world : Bukkit.getWorlds()) world.setFullTime(player.getWorld().getFullTime());
                sender.sendMessage(Dreamvisitor.PREFIX + "Set all worlds to match " + player.getWorld().getName() + ": " + player.getWorld().getFullTime());
            } else {
                for (World world : Bukkit.getWorlds()) world.setFullTime(Bukkit.getWorlds().get(0).getFullTime());
                sender.sendMessage(Dreamvisitor.PREFIX + "Set all worlds to match " + Bukkit.getWorlds().get(0).getName() + ": " + Bukkit.getWorlds().get(0).getFullTime());
            }
        } else {
            World world = Bukkit.getWorld(args[0]);
            if (world == null) sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "Invalid world name!");
            else {
                for (World worlds : Bukkit.getWorlds()) worlds.setFullTime(world.getFullTime());
            }
        }

        return true;
    }

    @NotNull
    @Override
    public String getCommandName() {
        return "synctime";
    }

    @Override
    public LiteralCommandNode<?> getNode() {
        return LiteralArgumentBuilder.literal(getCommandName())
                .then(RequiredArgumentBuilder.argument("world", StringArgumentType.word()))
                .build();
    }
}

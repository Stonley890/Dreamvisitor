package io.github.stonley890.dreamvisitor.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.functions.Radio;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CmdTagRadio implements DVCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        // radio <message>...

        if (args.length == 0) {
            sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "You must attach a message! /" + label + " <message>");
            return true;
        }

        if (sender instanceof Player player) {
            Radio.buildMessage(args, player.getName(), command);
            return true;
        } else if (sender instanceof ConsoleCommandSender) {
            Radio.buildMessage(args, "Console", command);
            return true;
        } else {
            return false;
        }
        
    }

    @NotNull
    @Override
    public String getCommandName() {
        return "radio";
    }

    @Override
    public LiteralCommandNode<?> getNode() {
        return LiteralArgumentBuilder.literal(getCommandName())
                .then(RequiredArgumentBuilder.argument("tag", StringArgumentType.word()))
                .then(RequiredArgumentBuilder.argument("message", StringArgumentType.greedyString()))
                .build();
    }
}

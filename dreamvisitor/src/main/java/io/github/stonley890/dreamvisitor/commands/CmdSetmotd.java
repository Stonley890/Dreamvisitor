package io.github.stonley890.dreamvisitor.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class CmdSetmotd implements DVCommand {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        // setmotd [<new-motd>...]

        if (args.length == 0) {
            Dreamvisitor.MOTD = null;
            sender.sendMessage(Dreamvisitor.PREFIX + "Reset MOTD to default:\n" + sender.getServer().getMotd());
            Dreamvisitor.debug("Existing MOTD: " + sender.getServer().getMotd());
        } else {

            StringBuilder builder = new StringBuilder();
            for (String arg : args) {
                builder.append(arg).append(" ");
            }

            Dreamvisitor.debug("Before formatting: " + builder);
            String newMotd = builder.toString().replaceAll("&", "§").replaceAll("\\\\n","\n").strip();

            Dreamvisitor.MOTD = newMotd;
            sender.sendMessage(Dreamvisitor.PREFIX + "MOTD set to\n" + newMotd);
            Dreamvisitor.debug("New MOTD: " + newMotd);
        }

        return true;
    }

    @NotNull
    @Override
    public String getCommandName() {
        return "setmotd";
    }

    @Override
    public LiteralCommandNode<?> getNode() {
        return LiteralArgumentBuilder.literal(getCommandName())
                .then(RequiredArgumentBuilder.argument("newMotd", StringArgumentType.greedyString()))
                .build();
    }
}

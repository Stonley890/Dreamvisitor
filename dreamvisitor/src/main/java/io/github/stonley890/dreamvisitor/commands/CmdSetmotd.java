package io.github.stonley890.dreamvisitor.commands;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class CmdSetmotd implements CommandExecutor {
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
}

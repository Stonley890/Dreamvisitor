package io.github.stonley890.dreamvisitor.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import org.jetbrains.annotations.NotNull;

public class CmdSetmotd implements DVCommand {

    @NotNull
    @Override
    public CommandAPICommand getCommand() {
        return new CommandAPICommand("setmotd")
                .withPermission("dreamvisitor.setmotd")
                .withHelp("Set or reset the MOTD.", "Set or reset the server MOTD.")
                .withOptionalArguments(new GreedyStringArgument("newMotd"))
                .executesNative((sender, args) -> {
                    String newMotd = (String) args.get("newMotd");
                    if (newMotd == null) {
                        Dreamvisitor.MOTD = null;
                        sender.sendMessage(Dreamvisitor.PREFIX + "Reset MOTD to default:\n" + sender.getServer().getMotd());
                        Dreamvisitor.debug("Existing MOTD: " + sender.getServer().getMotd());
                    } else {
                        String finalMotd = newMotd.replaceAll("&", "§").replaceAll("\\\\n","\n").strip();

                        Dreamvisitor.MOTD = finalMotd;
                        sender.sendMessage(Dreamvisitor.PREFIX + "MOTD set to\n" + finalMotd);
                        Dreamvisitor.debug("New MOTD: " + finalMotd);
                    }
                });
    }
}

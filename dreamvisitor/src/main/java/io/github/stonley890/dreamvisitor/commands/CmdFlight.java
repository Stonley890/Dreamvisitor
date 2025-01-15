package io.github.stonley890.dreamvisitor.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.ExecutableCommand;
import dev.jorel.commandapi.arguments.BooleanArgument;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import org.jetbrains.annotations.NotNull;

public class CmdFlight implements DVCommand {
    @NotNull
    @Override
    public ExecutableCommand<?, ?> getCommand() {
        return new CommandAPICommand("flight")
                .withSubcommands(
                        new CommandAPICommand("set")
                                .withArguments(
                                        new BooleanArgument("enabled")
                                )
                                .executesPlayer((sender, args) -> {

                                    // Get enabled arg
                                    Boolean enabled = (Boolean) args.get("enabled");
                                    assert enabled!= null;

                                    sender.setAllowFlight(enabled);
                                    sender.sendMessage("Flight set to " + enabled);
                                }),
                        new CommandAPICommand("debug")
                                .executesPlayer((sender, args) -> {
                                    Dreamvisitor.debug("Flying: " + sender.getAllowFlight());
                                    Dreamvisitor.debug("Gliding: " + sender.isGliding());
                                })
                );
    }
}

package io.github.stonley890.dreamvisitor.commands;

import dev.jorel.commandapi.ExecutableCommand;
import org.jetbrains.annotations.NotNull;

public interface DVCommand {

    @NotNull
    ExecutableCommand<?, ?> getCommand();

}

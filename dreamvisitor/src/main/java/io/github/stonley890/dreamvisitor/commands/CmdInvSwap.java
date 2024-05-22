package io.github.stonley890.dreamvisitor.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.ExecutableCommand;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.functions.InvSwap;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CmdInvSwap implements DVCommand {

    @NotNull
    @Override
    public CommandAPICommand getCommand() {
        return new CommandAPICommand("invswap")
                .withHelp("Swap you inventory.", "Swap between two different inventories.")
                .withPermission(CommandPermission.fromString("dreamvisitor.invswap"))
                .executesNative(((sender, args) -> {
                    CommandSender callee = sender.getCallee();
                    if (callee instanceof Player player) {
                        InvSwap.swapInventories(player);
                        callee.sendMessage(Dreamvisitor.PREFIX + "Your inventory has been swapped!");
                    } else throw CommandAPI.failWithString("This command can only be executed as a player!");

               }));
    }
}

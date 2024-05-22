package io.github.stonley890.dreamvisitor.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.data.PlayerMemory;
import io.github.stonley890.dreamvisitor.data.PlayerUtility;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CmdDiscord implements DVCommand {

    @NotNull
    @Override
    public CommandAPICommand getCommand() {
        return new CommandAPICommand("discord")
                .withHelp("Toggles Discord message visibility.", "Toggle whether messages from the Discord chat bridge appear in chat.")
                .executesNative(((sender, args) -> {
                    CommandSender callee = sender.getCallee();
                    if (callee instanceof Player player) {
                        PlayerMemory memory = PlayerUtility.getPlayerMemory(player.getUniqueId());
                        memory.discordToggled = !memory.discordToggled;

                        callee.sendMessage(Dreamvisitor.PREFIX + "Discord visibility toggled to " + !memory.discordToggled + ".");

                        PlayerUtility.setPlayerMemory(player.getUniqueId(), memory);
                    } else throw CommandAPI.failWithString("This command must be executed as a player!");


                }));
    }
}

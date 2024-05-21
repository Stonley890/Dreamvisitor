package io.github.stonley890.dreamvisitor.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.data.PlayerMemory;
import io.github.stonley890.dreamvisitor.data.PlayerUtility;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CmdDiscord implements DVCommand {

    @NotNull
    @Override
    public CommandAPICommand getCommand() {
        return new CommandAPICommand("discord")
                .withHelp("Toggles Discord message visibility.", "Toggle whether messages from the Discord chat bridge appear in chat.")
                .executesNative(((sender, args) -> {
                    if (sender instanceof Player player) {
                        PlayerMemory memory = PlayerUtility.getPlayerMemory(player.getUniqueId());
                        memory.discordToggled = !memory.discordToggled;

                        player.sendMessage(Dreamvisitor.PREFIX + "Discord visibility toggled to " + !memory.discordToggled + ".");

                        PlayerUtility.setPlayerMemory(player.getUniqueId(), memory);
                    } else {
                        throw CommandAPI.failWithString(Dreamvisitor.PREFIX + ChatColor.RED + "This command must be run by a player.");
                    }
                }));
    }
}

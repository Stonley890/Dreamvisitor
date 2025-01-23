package io.github.stonley890.dreamvisitor.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import io.github.stonley890.dreamvisitor.data.PlayerMemory;
import io.github.stonley890.dreamvisitor.data.PlayerUtility;
import io.github.stonley890.dreamvisitor.functions.SystemMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CmdZoop implements DVCommand {

    @NotNull
    @Override
    public CommandAPICommand getCommand() {
        return new CommandAPICommand("zoop")
                .withPermission(CommandPermission.fromString("dreamvisitor.set.zoop"))
                .withHelp("Disappear from the Discord chat bridge.", "Sends a fake leave message to Discord and hides you from the list command.")
                .executesNative((sender, args) -> {
                    CommandSender callee = sender.getCallee();
                    if (callee instanceof Player player) {
                        PlayerMemory memory = PlayerUtility.getPlayerMemory(player.getUniqueId());

                        // Change data
                        if (memory.vanished) {

                            memory.vanished = false;
                            String chatMessage = "**" + callee.getName() + " joined the game**";
                            // TODO: Send chat message

                        } else {
                            memory.vanished = true;
                            String chatMessage = "**" + callee.getName() + " left the game**";
                            // TODO: Send chat message
                        }

                        PlayerUtility.setPlayerMemory(player.getUniqueId(), memory);

                        sender.sendMessage(SystemMessage.formatPrivate("Discord vanish toggled to " + memory.vanished + "."));
                    } else throw CommandAPI.failWithString("This command must be executed as a player!");

                });
    }
}

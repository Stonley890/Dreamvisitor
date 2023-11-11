package io.github.stonley890.dreamvisitor.commands;

import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.data.PlayerMemory;
import io.github.stonley890.dreamvisitor.data.PlayerUtility;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CmdZoop implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        // If cmd executor is player
        if (sender instanceof Player player) {

            PlayerMemory memory = PlayerUtility.getPlayerMemory(player.getUniqueId());

            // Change data
            if (memory.vanished) {

                memory.vanished = false;
                String chatMessage = "**" + player.getName() + " joined the game**";
                Bot.sendMessage(Bot.gameChatChannel, chatMessage);
                Bot.sendMessage(Bot.gameLogChannel, chatMessage);

            } else {
                memory.vanished = true;
                String chatMessage = "**" + player.getName() + " left the game**";
                Bot.sendMessage(Bot.gameChatChannel, chatMessage);
                Bot.sendMessage(Bot.gameLogChannel, chatMessage);

            }

            PlayerUtility.setPlayerMemory(player.getUniqueId(), memory);

            player.sendMessage(Dreamvisitor.PREFIX + ChatColor.WHITE + "Discord vanish toggled to " + memory.vanished + ".");

        } else {
            sender.sendMessage(ChatColor.RED + "This command can only be executed by a player!");
        }
        return true;
    }

}

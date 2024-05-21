package io.github.stonley890.dreamvisitor.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
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

public class CmdZoop implements DVCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        // zoop

        // If cmd executor is player
        if (sender instanceof Player player) {

            PlayerMemory memory = PlayerUtility.getPlayerMemory(player.getUniqueId());

            // Change data
            if (memory.vanished) {

                memory.vanished = false;
                String chatMessage = "**" + player.getName() + " joined the game**";
                Bot.getGameChatChannel().sendMessage(chatMessage).queue();
                Bot.sendLog(chatMessage);

            } else {
                memory.vanished = true;
                String chatMessage = "**" + player.getName() + " left the game**";
                Bot.getGameChatChannel().sendMessage(chatMessage).queue();
                Bot.sendLog(chatMessage);
            }

            PlayerUtility.setPlayerMemory(player.getUniqueId(), memory);

            player.sendMessage(Dreamvisitor.PREFIX + ChatColor.WHITE + "Discord vanish toggled to " + memory.vanished + ".");

        } else {
            sender.sendMessage(ChatColor.RED + "This command can only be executed by a player!");
        }
        return true;
    }

    @NotNull
    @Override
    public String getCommandName() {
        return "zoop";
    }

    @Override
    public LiteralCommandNode<?> getNode() {
        return LiteralArgumentBuilder.literal("zoop").build();
    }
}

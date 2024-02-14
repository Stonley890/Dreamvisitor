package io.github.stonley890.dreamvisitor.commands;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.Utils;
import io.github.stonley890.dreamvisitor.functions.Moonglobe;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class CmdMoonglobe implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

        if (args.length == 0) {

            int activeMoonGlobeCount = Moonglobe.activeMoonglobes.size();
            sender.sendMessage(Dreamvisitor.PREFIX + "Number of active moon globes: " + activeMoonGlobeCount);

            if (activeMoonGlobeCount != 0) {
                ComponentBuilder message = new ComponentBuilder("Existing moon globes:\n");
                for (Moonglobe moonglobe : Moonglobe.activeMoonglobes) {

                    String playerName = Utils.getUsernameOfUuid(moonglobe.getPlayer());

                    message.append("[ ").color(ChatColor.GRAY)
                            .append(playerName).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to remove"))).event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/dreamvisitor:moonglobe remove " + playerName)).color(ChatColor.YELLOW)
                            .append(" ] ").color(ChatColor.GRAY);

                }
                sender.spigot().sendMessage(message.create());
            }

        } else if (args[0].equals("create")) create(sender, args);
        else if (args[0].equals("remove")) remove(sender, args);

        return true;
    }

    @Contract(pure = true)
    private static void create(CommandSender sender, String @NotNull [] args) {

        if (args.length < 2) {
            sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "You must specify a player!");
        }

    }

    @Contract(pure = true)
    private static void remove(CommandSender sender, String @NotNull [] args) {



    }
}

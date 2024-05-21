package io.github.stonley890.dreamvisitor.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import org.jetbrains.annotations.NotNull;

public class CmdSethub implements DVCommand {

    final Dreamvisitor plugin = Dreamvisitor.getPlugin();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        // sethub

        if (sender instanceof Player player) {
            Dreamvisitor.hubLocation = player.getLocation().getBlock().getLocation()
                    .add(new Location(player.getLocation().getWorld(), 0.5, 0, 0.5));
                    plugin.getConfig().set("hubLocation", Dreamvisitor.hubLocation);
                    plugin.saveConfig();
            player.sendMessage(Dreamvisitor.PREFIX + ChatColor.WHITE + "Hub location set.");
        } else {
            sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "This command must be executed by a player!");
        }
        return true;
    }

    @NotNull
    @Override
    public String getCommandName() {
        return "sethub";
    }

    @Override
    public LiteralCommandNode<?> getNode() {
        return LiteralArgumentBuilder.literal(getCommandName()).build();
    }
}

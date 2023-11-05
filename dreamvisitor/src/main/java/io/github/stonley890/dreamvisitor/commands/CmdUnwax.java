package io.github.stonley890.dreamvisitor.commands;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import org.bukkit.ChatColor;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CmdUnwax implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (sender instanceof Player player) {
            Block targetBlock = player.getTargetBlockExact(10, FluidCollisionMode.NEVER);

            if (targetBlock.getState() instanceof Sign sign) {
                sign.setWaxed(false);
                sign.update(false);
                sign.getWorld().spawnParticle(Particle.WAX_OFF, sign.getLocation().add(0.5, 0.5, 0.5), 5);
                sender.sendMessage(Dreamvisitor.PREFIX + "Wax, be gone!");
            } else sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "That is not a sign.");
        } else sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "This command must be run by a player!");

        return true;
    }
}

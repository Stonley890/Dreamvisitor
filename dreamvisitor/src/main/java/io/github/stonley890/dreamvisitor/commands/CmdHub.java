package io.github.stonley890.dreamvisitor.commands;

import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import org.jetbrains.annotations.NotNull;

public class CmdHub implements CommandExecutor {

    Dreamvisitor plugin = Dreamvisitor.getPlugin();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        if (sender instanceof Player) {
            if (plugin.getConfig().getLocation("hubLocation") == null) {
                sender.sendMessage(Dreamvisitor.prefix + ChatColor.RED + "No hub is currently set!");
            } else {
                Dreamvisitor.hubLocation = plugin.getConfig().getLocation("hubLocation");
                Player player = (Player) sender;
                assert Dreamvisitor.hubLocation != null;
                player.teleport(Dreamvisitor.hubLocation, TeleportCause.COMMAND);
                player.spawnParticle(Particle.FIREWORKS_SPARK, Dreamvisitor.hubLocation, 100);
                player.playSound(Dreamvisitor.hubLocation, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 0.5f,
                        1f);
            }
            return true;
        } else {
            sender.sendMessage(Dreamvisitor.prefix + ChatColor.RED + "This command must be run by a player.");
            return false;
        }
    }

}

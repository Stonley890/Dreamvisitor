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

public class CmdHub implements CommandExecutor {

    Dreamvisitor plugin = Dreamvisitor.getPlugin();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player) {
            if (plugin.getConfig().getLocation("hubLocation") == null) {
                sender.sendMessage(ChatColor.RED + "No hub is currently set!");
            } else {
                plugin.hubLocation = plugin.getConfig().getLocation("hubLocation");
                Player player = (Player) sender;
                player.teleport(plugin.hubLocation, TeleportCause.COMMAND);
                player.spawnParticle(Particle.FIREWORKS_SPARK, plugin.hubLocation, 100);
                player.playSound(plugin.hubLocation, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 0.5f,
                        1f);
            }
        } else {
            sender.sendMessage("This command must be run by a player!");
        }
        return true;
    }

}

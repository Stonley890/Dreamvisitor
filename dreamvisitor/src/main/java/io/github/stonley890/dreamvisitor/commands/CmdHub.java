package io.github.stonley890.dreamvisitor.commands;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import io.github.stonley890.dreamvisitor.Main;
import org.bukkit.*;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CmdHub implements CommandExecutor {

    final Main plugin = Main.getPlugin();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        if (sender.hasPermission("dreamvisitor.hub")) {

        if (args.length == 1 && sender.hasPermission("dreamvisitor.hub.select")) {

            List<Entity> entities;
            try {
                entities = Bukkit.selectEntities(sender, args[0]);
            } catch (IllegalArgumentException e) {
                sender.sendMessage(Main.PREFIX + ChatColor.RED + "Invalid arguments!");
                return true;
            }

            if (entities.isEmpty()) {
                sender.sendMessage(Main.PREFIX + ChatColor.RED + "No targets selected.");
            } else {
                if (plugin.getConfig().getLocation("hubLocation") == null) {
                    sender.sendMessage(Main.PREFIX + ChatColor.RED + "No hub is currently set!");
                } else {

                    Main.hubLocation = plugin.getConfig().getLocation("hubLocation");
                    assert Main.hubLocation != null;

                    Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");

                    for (Entity entity : entities) {

                        if (entity instanceof Player player) {

                            // Set /back location
                            if (ess != null) {
                                User user = ess.getUser(player);
                                user.setLastLocation(player.getLocation());
                            }

                            player.teleport(Main.hubLocation);
                            player.spawnParticle(Particle.FIREWORKS_SPARK, Main.hubLocation, 100);
                            player.playSound(Main.hubLocation, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 0.5f,1f);
                        } else entity.teleport(Main.hubLocation, TeleportCause.COMMAND);
                    }
                }
                if (entities.size() == 1)
                    sender.sendMessage(Main.PREFIX + "Teleported " + entities.get(0).getName() + " to the hub.");
                else
                    sender.sendMessage(Main.PREFIX + "Teleported " + entities.size() + " entities to the hub.");
            }

        } else {
            if (sender instanceof Player) {
                if (plugin.getConfig().getLocation("hubLocation") == null) {
                    sender.sendMessage(Main.PREFIX + ChatColor.RED + "No hub is currently set!");
                } else {

                    Main.hubLocation = plugin.getConfig().getLocation("hubLocation");
                    assert Main.hubLocation != null;

                    Player player = (Player) sender;

                    Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
                    if (ess != null) {
                        User user = ess.getUser(player);
                        user.setLastLocation(player.getLocation());
                    }

                    player.teleport(Main.hubLocation);
                    player.spawnParticle(Particle.FIREWORKS_SPARK, Main.hubLocation, 100);
                    player.playSound(Main.hubLocation, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 0.5f,
                            1f);
                }

            } else if (sender instanceof BlockCommandSender cmdblock) {

                // Find the closest player
                double lastDistance = 10;
                Player closest = null;

                for (Entity entity : cmdblock.getBlock().getWorld().getNearbyEntities(cmdblock.getBlock().getLocation(), 10, 10, 10)) {
                    if (entity instanceof Player) {
                        double distance = entity.getLocation().distance(cmdblock.getBlock().getLocation());
                        if (distance < lastDistance) {
                            lastDistance = distance;
                            closest = (Player) entity;
                        }
                    }
                }

                if (closest != null) {
                    if (plugin.getConfig().getLocation("hubLocation") == null) {
                        sender.sendMessage(Main.PREFIX + ChatColor.RED + "No hub is currently set!");
                    } else {

                        Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
                        if (ess != null) {
                            User user = ess.getUser(closest);
                            user.setLastLocation(closest.getLocation());
                        }

                        closest.teleport(Main.hubLocation);
                        closest.spawnParticle(Particle.FIREWORKS_SPARK, Main.hubLocation, 100);
                        closest.playSound(Main.hubLocation, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 0.5f,
                                1f);
                    }
                }

            } else {
                sender.sendMessage(Main.PREFIX + ChatColor.RED + "Missing arguments! /hub [targets]");
            }
        }

        } else sender.sendMessage(Main.PREFIX + ChatColor.RED + "You do not have permission to run that command!");

        return true;

    }

}

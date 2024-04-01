package io.github.stonley890.dreamvisitor.commands;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CmdSetback implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

        // setback <playerTargets> [<x> <y> <z> [<pitch> <yaw> [<world>]]]

        if (args.length == 0) {
            sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "Missing arguments! /setback <playerTargets> [<x> <y> <z> [<pitch> <yaw> [<world>]]]");
            return true;
        }
        if ((args.length > 1 && args.length < 4) || args.length == 5) {
            sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "Incomplete arguments! /setback <playerTargets> [<x> <y> <z> [<pitch> <yaw> [<world>]]]");
            return true;
        }
        if (args.length > 7) {
            sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "Too many arguments! /setback <playerTargets> [<x> <y> <z> [<pitch> <yaw> [<world>]]]");
            return true;
        }

        Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
        if (ess == null) {
            sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "EssentialsX is not currently active!");
            return true;
        }

        List<Entity> entities = Bukkit.selectEntities(sender, args[0]);
        if (entities.isEmpty()) {
            sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "No entities selected!");
            return true;
        }
        List<Player> players = new ArrayList<>();
        for (Entity entity : entities) {
            if (entity instanceof Player player) players.add(player);
            else {
                sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "Only players are allowed!");
            }
        }
        if (players.isEmpty()) {
            sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "No players selected!");
            return true;
        }

        Location location = null;

        double x; double y; double z;
        float pitch; float yaw;
        World world;

        if (args.length == 1) {
            if (sender instanceof Entity entity) {
                location = entity.getLocation();
            } else if (sender instanceof BlockCommandSender block) {
                location = block.getBlock().getLocation();
            }

            if (location == null) {
                sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "Location is null!");
                return true;
            }
        }
        if (args.length >= 4) {
            try {
                x = Double.parseDouble(args[1]);
                y = Double.parseDouble(args[2]);
                z = Double.parseDouble(args[3]);
            } catch (NumberFormatException e) {
                sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "Could not parse double: x, y, z; /setback <playerTargets> [<x> <y> <z> [<pitch> <yaw> [<world>]]]");
                return true;
            }
            location = new Location(null, x, y, z);
            if (sender instanceof Entity entity) {
                location.setWorld(entity.getWorld());
            } else if (sender instanceof BlockCommandSender block) {
                location.setWorld(block.getBlock().getWorld());
            }
        }
        if (args.length >= 6) {
            String pitchString = args[4];
            String yawString = args[5];
            try {
                pitch = Float.parseFloat(pitchString);
                yaw = Float.parseFloat(yawString);
            } catch (NumberFormatException e) {
                sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "Could not parse float: pitch, yaw; /setback <playerTargets> [<x> <y> <z> [<pitch> <yaw> [<world>]]]");
                return true;
            }
            location.setPitch(pitch);
            location.setYaw(yaw);
        }
        if (args.length == 7) {
            String worldName = args[6];
            world = Bukkit.getWorld(worldName);
            if (world == null) {
                sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "That world is null!");
                return true;
            }
            location.setWorld(world);
        }

        if (location.getWorld() == null) {
            sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "No world specified! You need to specify rotation and world. /setback <playerTargets> <x> <y> <z> <pitch> <yaw> <world>");
            return true;
        }

        for (Player player : players) {
            User user = ess.getUser(player);
            user.setLastLocation(location);
        }

        sender.sendMessage(Dreamvisitor.PREFIX + "Set back location to " + location.getBlockX() + ", " + location.getBlockY() +
                ", " + location.getBlockZ() + " of " + Objects.requireNonNull(location.getWorld()).getName() + " for " +
                players.size() + " player(s).");

        return true;
    }
}

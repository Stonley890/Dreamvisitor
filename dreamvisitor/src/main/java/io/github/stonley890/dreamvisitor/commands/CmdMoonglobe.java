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
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
            sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "You must specify at least one player!");
            return;
        }

        String targetString = args[1];

        // Get players
        List<Entity> entities = Bukkit.selectEntities(sender, targetString);
        List<Player> players = new ArrayList<>();

        for (Entity entity : entities) {
            if (entity instanceof Player player) players.add(player);
            else {
                sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "You cannot specify non-players!");
                return;
            }
        }

        // get location

        double x = 0;
        double y = 0;
        double z = 0;

        World world;

        if (sender instanceof Player player) world = player.getWorld();
        else if (sender instanceof BlockCommandSender block) world = block.getBlock().getWorld();
        else world = Bukkit.getWorlds().get(0);

        if (args.length > 2) {
            if (args.length < 5) {
                sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "Incomplete arguments! /moonglobe create <targets> [<x> <y> <z> [max-distance]]");
                return;
            }
            // Get location
            String xString = args[2];
            String yString = args[3];
            String zString = args[4];

            try {
                x = Double.parseDouble(xString);
                y = Double.parseDouble(yString);
                z = Double.parseDouble(zString);
            } catch (NumberFormatException e) {
                sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "Error parsing double.");
            }


        } else {
            if (sender instanceof Player player) {
                x = player.getLocation().getX();
                y = player.getLocation().getY();
                z = player.getLocation().getZ();
            } else if (sender instanceof BlockCommandSender block) {
                x = block.getBlock().getX();
                y = block.getBlock().getY();
                z = block.getBlock().getZ();
            }
            else {
                sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "You must specify a location!");
                return;
            }
        }

        Location location = new Location(world, x, y, z);

        // get distance
        float maxDistance = 256.0f;

        if (args.length > 5) {
            String maxDistanceString = args[5];
            try {
                maxDistance = Float.parseFloat(maxDistanceString);
            } catch (NumberFormatException e) {
                sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "Error parsing float.");
            }
        }

        for (Player player : players) {
            boolean alreadyHasGlobe = false;
            for (Moonglobe activeMoonglobe : Moonglobe.activeMoonglobes) {
                if (Objects.equals(activeMoonglobe.getPlayer(), player.getUniqueId())) alreadyHasGlobe = true;
            }
            if (!alreadyHasGlobe) new Moonglobe(player.getUniqueId(), location, maxDistance);
        }

    }

    @Contract(pure = true)
    private static void remove(CommandSender sender, String @NotNull [] args) {

        if (args.length < 2) {
            sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "You must specify at least one player!");
            return;
        }

        String targetString = args[1];

        // Get players
        List<Entity> entities = Bukkit.selectEntities(sender, targetString);
        List<Player> players = new ArrayList<>();

        for (Entity entity : entities) {
            if (entity instanceof Player player) players.add(player);
            else {
                sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "You cannot specify non-players!");
                return;
            }
        }

        for (Player player : players) {
            for (Moonglobe activeMoonglobe : Moonglobe.activeMoonglobes) {
                if (Objects.equals(activeMoonglobe.getPlayer(), player.getUniqueId())) activeMoonglobe.remove(null);
            }
        }

    }
}

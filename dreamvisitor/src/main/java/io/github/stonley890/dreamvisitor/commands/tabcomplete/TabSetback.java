package io.github.stonley890.dreamvisitor.commands.tabcomplete;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TabSetback implements TabCompleter {
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        // setback <playerTargets> [<x> <y> <z> [<pitch> <yaw> [<world>]]]

        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            suggestions.add("@a");
            suggestions.add("@e");
            suggestions.add("@p");
            suggestions.add("@r");
            suggestions.add("@s");
            for (Player player : Bukkit.getOnlinePlayers()) suggestions.add(player.getName());
        } else if (args.length == 2) {
            if (sender instanceof Player player) {
                RayTraceResult rayTraceResult = player.rayTraceBlocks(5);
                if (rayTraceResult != null) suggestions.add(String.valueOf(Math.round(rayTraceResult.getHitPosition().getX() * 100) / 100));
                else suggestions.add(String.valueOf(Math.round(player.getLocation().getX() * 100) / 100)); // round the number to 2 decimal places
            } else if (sender instanceof BlockCommandSender block) suggestions.add(String.valueOf(block.getBlock().getX()));
        } else if (args.length == 3) {
            if (sender instanceof Player player) {
                RayTraceResult rayTraceResult = player.rayTraceBlocks(5);
                if (rayTraceResult != null) suggestions.add(String.valueOf(Math.round(rayTraceResult.getHitPosition().getY() * 100) / 100));
                else suggestions.add(String.valueOf(Math.round(player.getLocation().getY() * 100) / 100));
            } else if (sender instanceof BlockCommandSender block) suggestions.add(String.valueOf(block.getBlock().getY()));
        } else if (args.length == 4) {
            if (sender instanceof Player player) {
                RayTraceResult rayTraceResult = player.rayTraceBlocks(5);
                if (rayTraceResult != null) suggestions.add(String.valueOf(Math.round(rayTraceResult.getHitPosition().getZ() * 100) / 100));
                else suggestions.add(String.valueOf(Math.round(player.getLocation().getZ() * 100) / 100));
            } else if (sender instanceof BlockCommandSender block) suggestions.add(String.valueOf(block.getBlock().getZ()));
        } else if (args.length == 5) {
            if (sender instanceof Player player) {
                suggestions.add(String.valueOf(Math.round(player.getLocation().getPitch() * 100) / 100));
            }
        } else if (args.length == 6) {
            if (sender instanceof Player player) {
                suggestions.add(String.valueOf(Math.round(player.getLocation().getYaw() * 100) / 100));
            }
        } else if (args.length == 7) {
            for (World world : Bukkit.getWorlds()) {
                suggestions.add(world.getName());
            }
        }

        return suggestions;
    }
}

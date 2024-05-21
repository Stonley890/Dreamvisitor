package io.github.stonley890.dreamvisitor.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.LongArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.functions.Moonglobe;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CmdMoonglobe implements DVCommand {

    @NotNull
    @Contract(pure = true)
    private static String create(@NotNull List<Player> players, @NotNull Location location, float maxDistance) {

        for (Player player : players) {
            boolean alreadyHasGlobe = false;
            for (Moonglobe activeMoonglobe : Moonglobe.activeMoonglobes) {
                if (Objects.equals(activeMoonglobe.getPlayer(), player.getUniqueId())) alreadyHasGlobe = true;
            }
            if (!alreadyHasGlobe) new Moonglobe(player.getUniqueId(), location, maxDistance);
        }

        return (Dreamvisitor.PREFIX + "Created moon globes for " + players.size() + " players.");

    }

    @NotNull
    @Override
    public CommandAPICommand getCommand() {
        return new CommandAPICommand("moonglobe")
                .withPermission(CommandPermission.fromString("dreamvisitor.moonglobes"))
                .withShortDescription("Manages moon globes.")
                .withFullDescription("Create and remove moon globes to/from players.")
                .withSubcommand(new CommandAPICommand("remove")
                        .withArguments(new EntitySelectorArgument.ManyPlayers("players"))
                        .executes((sender, args) -> {

                            String targetString = (String) args.get("players");

                            // Get players
                            assert targetString != null;
                            List<Entity> entities = Bukkit.selectEntities(sender, targetString);
                            List<Player> players = new ArrayList<>();

                            for (Entity entity : entities) {
                                if (entity instanceof Player player) players.add(player);
                                else {
                                    throw CommandAPI.failWithString("You cannot specify non-players!");
                                }
                            }

                            for (Player player : players) {
                                for (Moonglobe activeMoonglobe : Moonglobe.activeMoonglobes) {
                                    if (Objects.equals(activeMoonglobe.getPlayer(), player.getUniqueId())) activeMoonglobe.remove(null);
                                }
                            }

                            sender.sendMessage(Dreamvisitor.PREFIX + "Removed moon globes of " + players.size() + " players.");
                        })
                )
                .withSubcommand(new CommandAPICommand("create")
                        .withArguments(new EntitySelectorArgument.ManyPlayers("players"))
                        .withOptionalArguments(new LocationArgument("location"))
                        .withOptionalArguments(new LongArgument("maxDistance"))
                        .executesNative((sender, args) -> {
                            String targetString = (String) args.get("players");

                            // Get players
                            assert targetString != null;
                            List<Entity> entities = Bukkit.selectEntities(sender, targetString);
                            List<Player> players = new ArrayList<>();

                            for (Entity entity : entities) {
                                if (entity instanceof Player player) players.add(player);
                                else {
                                    throw CommandAPI.failWithString("You cannot specify non-players!");
                                }
                            }

                            Location location = getLocation(sender, args);

                            // get distance
                            float maxDistance = 256.0f;

                            Object maxDistanceArg = args.get("maxDistance");
                            if (maxDistanceArg != null) maxDistance = (float) maxDistanceArg;

                            sender.sendMessage(create(players, location, maxDistance));
                        })
                        .executes((sender, args) -> {

                            String targetString = (String) args.get("players");

                            // Get players
                            assert targetString != null;
                            List<Entity> entities = Bukkit.selectEntities(sender, targetString);
                            List<Player> players = new ArrayList<>();

                            for (Entity entity : entities) {
                                if (entity instanceof Player player) players.add(player);
                                else {
                                    throw CommandAPI.failWithString("You cannot specify non-players!");
                                }
                            }

                            World world;

                            if (sender instanceof Player player) world = player.getWorld();
                            else if (sender instanceof BlockCommandSender block) world = block.getBlock().getWorld();
                            else world = Bukkit.getWorlds().get(0);

                            // Get location
                            Location location = (Location) args.get("location");
                            if (location == null) {
                                double x; double y; double z;
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
                                    throw CommandAPI.failWithString("You must specify a location; it could not be inferred!");
                                }
                                location = new Location(world, x, y, z);
                            }

                            // get distance
                            float maxDistance = 256.0f;

                            Object maxDistanceArg = args.get("maxDistance");
                            if (maxDistanceArg != null) maxDistance = (float) maxDistanceArg;

                            sender.sendMessage(create(players, location, maxDistance));
                        })
                );
    }

    private static @NotNull Location getLocation(NativeProxyCommandSender sender, CommandArguments args) {
        World world;

        if (sender instanceof Player player) world = player.getWorld();
        else if (sender instanceof BlockCommandSender block) world = block.getBlock().getWorld();
        else world = Bukkit.getWorlds().get(0);

        // Get location
        Location location = (Location) args.get("location");
        if (location == null) {
            double x; double y; double z;

            x = sender.getLocation().getX();
            y = sender.getLocation().getY();
            z = sender.getLocation().getZ();

            location = new Location(world, x, y, z);
        }
        return location;
    }
}

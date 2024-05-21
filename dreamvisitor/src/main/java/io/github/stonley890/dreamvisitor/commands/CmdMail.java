package io.github.stonley890.dreamvisitor.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.ExecutableCommand;
import dev.jorel.commandapi.arguments.*;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.data.Tribe;
import io.github.stonley890.dreamvisitor.data.TribeUtil;
import io.github.stonley890.dreamvisitor.functions.Mail;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class CmdMail implements DVCommand {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (args.length == 0) {
            sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "Missing arguments! /mail [locations [add <x> <y> <z> <name> <weight> <homeTribe> | remove <name> | list] | delivery [terminal <playerSelector> | add <playerSelector> <start> <end> | remove <playerSelector> | list]]");
            return true;
        } else if (args[0].equals("locations")) {
            if (args.length == 1) {
                sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "Missing arguments! /mail locations [add <x> <y> <z> <name> <weight> <homeTribe> | remove <name> | list]");
                return true;
            }
            if (args[1].equals("add")) {

            } else if (args[1].equals("remove")) {
                if (args.length != 3) {
                    sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "Missing arguments! /mail locations remove <name>");
                    return true;
                }
                Mail.MailLocation location = Mail.getLocationByName(args[2]);
                if (location == null) {
                    sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "Mail location not found.");
                    return true;
                }
                Mail.removeLocation(location);
                sender.sendMessage(Dreamvisitor.PREFIX + "Removed location " + location.getName());
            } else if (args[1].equals("list")) {
                List<Mail.MailLocation> locations = Mail.getLocations();

                ComponentBuilder message = new ComponentBuilder(Dreamvisitor.PREFIX + "Mail Locations");

                for (Mail.MailLocation mailLocation : locations) {
                    TextComponent tpButton = getTextComponent(mailLocation);
                    message.append("\n").append(mailLocation.getName()).color(net.md_5.bungee.api.ChatColor.YELLOW)
                            .append(" [").append(tpButton).append("]");
                }

            }
        }

        return true;
    }

    private static @NotNull TextComponent getTextComponent(Mail.MailLocation mailLocation) {
        Location loc = mailLocation.getLocation();
        TextComponent tpButton = new TextComponent("TP");
        tpButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockX() + " in " + Objects.requireNonNull(loc.getWorld()).getName())));
        tpButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/execute in " + loc.getWorld().getName() + " run tp @s " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockX()));
        return tpButton;
    }

    @NotNull
    @Override
    public ExecutableCommand<?, ?> getCommand() {
        return new CommandAPICommand("mail")
                .withPermission(CommandPermission.fromString("dreamvisitor.mail"))
                .withHelp("Manage the mail system.", "Manage the mail economy mini-game system.")
                .withSubcommand(new CommandAPICommand("location")
                        .withSubcommand(new CommandAPICommand("add")
                                .withArguments(new LocationArgument("location"))
                                .withArguments(new StringArgument("name"))
                                .withArguments(new IntegerArgument("weight", 0))
                                .withArguments(CommandUtils.customTribeArgument("homeTribe"))
                                .executes(((sender, args) -> {

                                    Location location = (Location) args.get("location");

                                    String name = (String) args.get("name");
                                    int weight = (int) args.get("weight");
                                    Tribe tribe = (Tribe) args.get("tribe");

                                    World world;

                                    if (sender instanceof Player player) {
                                        world = player.getWorld();
                                    } else {
                                        world = sender.getServer().getWorlds().get(0);
                                    }

                                    Location location = new Location(world, x, y, z);

                                    Mail.MailLocation mailLocation = new Mail.MailLocation(location, name, weight, tribe);
                                    Mail.saveLocation(mailLocation);

                                    sender.sendMessage(Dreamvisitor.PREFIX + "Added location " + name + " at " + x + " " + y + " " + z + " in world " + world.getName() + ".");
                                }))
                        )
                        .withSubcommand(new CommandAPICommand("remove")
                                .withArguments(new StringArgument("name")
                                        .includeSuggestions(ArgumentSuggestions.strings(
                                                Mail.getLocations().stream().map(Mail.MailLocation::getName).toArray(String[]::new)
                                        ))
                                )
                        )
                        .withSubcommand(new CommandAPICommand("list")
                        )
                )
                .withSubcommand(new CommandAPICommand("delivery")
                        .withSubcommand(new CommandAPICommand("terminal")
                                .withArguments(new EntitySelectorArgument.ManyPlayers("players"))
                        )
                        .withSubcommand(new CommandAPICommand("add")
                                .withArguments(new EntitySelectorArgument.ManyPlayers("players"))
                                .withArguments(new StringArgument("start")
                                        .includeSuggestions(ArgumentSuggestions.strings(
                                                Mail.getLocations().stream().map(Mail.MailLocation::getName).toArray(String[]::new)
                                        ))
                                )
                                .withArguments(new StringArgument("end")
                                        .includeSuggestions(ArgumentSuggestions.strings(
                                                Mail.getLocations().stream().map(Mail.MailLocation::getName).toArray(String[]::new)
                                        ))
                                )
                        )
                        .withSubcommand(new CommandAPICommand("remove")
                                .withArguments(new EntitySelectorArgument.ManyPlayers("location")
                                )
                        )
                        .withSubcommand(new CommandAPICommand("list")
                        )
                );
    }
}

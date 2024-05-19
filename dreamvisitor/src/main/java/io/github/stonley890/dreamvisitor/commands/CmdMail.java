package io.github.stonley890.dreamvisitor.commands;

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

public class CmdMail implements CommandExecutor {
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
                if (args.length < 8) {
                    sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "Missing arguments! /mail locations add <x> <y> <z> <name> <weight> <homeTribe>");
                    return true;
                }
                String xArg = args[2];
                String yArg = args[3];
                String zArg = args[4];
                String name = args[5];
                String weightArg = args[6];
                String tribeArg = args[7];

                double x;
                double y;
                double z;
                int weight;

                try {
                    x = Double.parseDouble(xArg);
                    y = Double.parseDouble(yArg);
                    z = Double.parseDouble(zArg);
                    weight = Integer.parseInt(weightArg);
                } catch (NumberFormatException e) {
                    sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "Numbers could not be parsed!");
                    return true;
                }

                Tribe tribe = TribeUtil.parse(tribeArg);
                if (tribe == null) {
                    sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "Not a valid tribe!");
                    return true;
                }

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
                    Location loc = mailLocation.getLocation();
                    TextComponent tpButton = new TextComponent("TP");
                    tpButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockX() + " in " + Objects.requireNonNull(loc.getWorld()).getName())));
                    tpButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/execute in " + loc.getWorld().getName() + " run tp @s " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockX()));
                    message.append("\n").append(mailLocation.getName()).color(net.md_5.bungee.api.ChatColor.YELLOW)
                            .append(" [").append(tpButton).append("]");
                }

            }
        }

        return true;
    }
}

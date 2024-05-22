package io.github.stonley890.dreamvisitor.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.ExecutableCommand;
import dev.jorel.commandapi.arguments.*;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.data.Tribe;
import io.github.stonley890.dreamvisitor.functions.Mail;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class CmdMail implements DVCommand {

    private static @NotNull TextComponent getTextComponent(@NotNull Mail.MailLocation mailLocation) {
        Location loc = mailLocation.getLocation();
        TextComponent tpButton = new TextComponent("TP");
        tpButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockX() + " in " + Objects.requireNonNull(loc.getWorld()).getName())));
        tpButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/execute in " + loc.getWorld().getName() + " run tp @s " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockX()));
        return tpButton;
    }

    @NotNull
    @Override
    public CommandAPICommand getCommand() {
        return new CommandAPICommand("mail")
                .withPermission(CommandPermission.fromString("dreamvisitor.mail"))
                .withHelp("Manage the mail system.", "Manage the mail economy mini-game system.")
                .withSubcommand(new CommandAPICommand("location")
                        .withSubcommand(new CommandAPICommand("add")
                                .withArguments(new LocationArgument("location"))
                                .withArguments(new StringArgument("name"))
                                .withArguments(new IntegerArgument("weight", 0))
                                .withArguments(CommandUtils.customTribeArgument("homeTribe"))
                                .executesNative(((sender, args) -> {

                                    Location location = (Location) args.get("location");
                                    if (location == null) throw CommandAPI.failWithString("Location was not provided!");

                                    String name = (String) args.get("name");
                                    if (name == null) throw CommandAPI.failWithString("Name was not provided!");
                                    Object weightArg = args.get("weight");
                                    if (weightArg == null) throw CommandAPI.failWithString("Weight was not provided!");
                                    int weight = (int) weightArg;
                                    Tribe tribe = (Tribe) args.get("tribe");
                                    if (tribe == null) throw CommandAPI.failWithString("Tribe was not provided!");

                                    Mail.MailLocation mailLocation = new Mail.MailLocation(location, name, weight, tribe);
                                    Mail.saveLocation(mailLocation);

                                    sender.sendMessage(Dreamvisitor.PREFIX + "Added location " + name + " at " + location.getX() + " " + location.getY() + " " + location.getZ() + " in world " + location.getWorld().getName() + ".");
                                }))
                        )
                        .withSubcommand(new CommandAPICommand("remove")
                                .withArguments(new StringArgument("name")
                                        .includeSuggestions(ArgumentSuggestions.strings(
                                                Mail.getLocations().stream().map(Mail.MailLocation::getName).toArray(String[]::new)
                                        ))
                                )
                                .executesNative((sender, args) -> {

                                    String name = (String) args.get("name");
                                    if (name == null) throw CommandAPI.failWithString("Name is null!");

                                    Mail.MailLocation location = Mail.getLocationByName(name);
                                    if (location == null) throw CommandAPI.failWithString("Mail location not found.");
                                    Mail.removeLocation(location);
                                    sender.sendMessage(Dreamvisitor.PREFIX + "Removed location " + location.getName());
                                })
                        )
                        .withSubcommand(new CommandAPICommand("list")
                                .executesNative((sender, args) -> {
                                    List<Mail.MailLocation> locations = Mail.getLocations();

                                    ComponentBuilder message = new ComponentBuilder(Dreamvisitor.PREFIX + "Mail Locations");

                                    for (Mail.MailLocation mailLocation : locations) {
                                        TextComponent tpButton = getTextComponent(mailLocation);
                                        message.append("\n").append(mailLocation.getName()).color(net.md_5.bungee.api.ChatColor.YELLOW)
                                                .append(" [").append(tpButton).append("]");
                                    }
                                    sender.spigot().sendMessage(message.create());
                                })
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

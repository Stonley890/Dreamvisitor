package io.github.stonley890.dreamvisitor.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.*;
import dev.jorel.commandapi.executors.CommandArguments;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CmdDreamvisitor implements DVCommand {

    final Dreamvisitor plugin = Dreamvisitor.getPlugin();

    @NotNull
    @Override
    public CommandAPICommand getCommand() {
        return new CommandAPICommand("dreamvisitor")
                .executes((sender, args) -> {
                    sender.sendMessage(ChatColor.BLUE + "Dreamvisitor " + Dreamvisitor.getPlugin().getDescription().getVersion() + "\nDeveloped by Stonley890\nOpen source at https://github.com/Stonley890/Dreamvisitor");
                })
                .withSubcommand(new CommandAPICommand("reload")
                        .withPermission(CommandPermission.OP)
                        .withHelp("Reload Dreamvisitor.", "Reload Dreamvisitor's config file from disk.")
                        .executes(((sender, args) -> {
                            Dreamvisitor.getPlugin().reloadConfig();
                            sender.sendMessage(Dreamvisitor.PREFIX + "Configuration reloaded.");
                        }))
                )
                .withSubcommand(new CommandAPICommand("manage")
                        .withPermission(CommandPermission.OP)
                        .withHelp("Manage Dreamvisitor config.", "Read from or write to the Dreamvisitor configuration.")
                        .withSubcommands(
                                new CommandAPICommand("debug")
                                        .withHelp("Set debug.", """
                                                Whether to enable debug messages.
                                                This will send additional messages to help debug Dreamvisitor.
                                                Default: false""")
                                        .withOptionalArguments(new BooleanArgument("debug"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "debug";
                                            configBoolean(sender, args, key);
                                        }),
                                new CommandAPICommand("chatPaused")
                                        .withHelp("Set chatPaused.", """
                                                Whether chat is paused or not.
                                                This can be toggled in Minecraft with /pausechat
                                                Default: false""")
                                        .withOptionalArguments(new BooleanArgument("chatPaused"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "chatPaused";
                                            configBoolean(sender, args, key);
                                        }),
                                new CommandAPICommand("softwhitelist")
                                        .withHelp("Set softwhitelist.", """
                                                Whether the soft whitelist is enabled or not
                                                This can be set in Minecraft with /softwhitelist [on|off]
                                                Default: false""")
                                        .withOptionalArguments(new BooleanArgument("softwhitelist"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "softwhitelist";
                                            configBoolean(sender, args, key);
                                        }),
                                new CommandAPICommand("playerlimit")
                                        .withHelp("Set playerlimit.", """
                                                Player limit override. This will override the player limit, both over and under.
                                                This can be set in Minecraft with /playerlimit <int>
                                                Default: -1""")
                                        .withOptionalArguments(new IntegerArgument("playerlimit", -1))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "playerlimit";
                                            configInt(sender, args, key);
                                        }),
                                new CommandAPICommand("disablepvp")
                                        .withHelp("Set disablepvp.", """
                                                Whether to globally disable pvp or not.
                                                This can be toggled in Minecraft with /togglepvp
                                                Default: false""")
                                        .withOptionalArguments(new BooleanArgument("disablepvp"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "disablepvp";
                                            configBoolean(sender, args, key);
                                        }),
                                new CommandAPICommand("hubLocation")
                                        .withHelp("Get hubLocation.", """
                                                The location of the recorded hub.
                                                This can be set in Minecraft with /sethub
                                                Default: none""")
                                        .executes((sender, args) -> {
                                            @Nullable String key = "hubLocation";
                                            Location location = plugin.getConfig().getLocation(key);
                                            String reply = "none";
                                            if (location != null) reply = location.toString();
                                            sender.sendMessage(Dreamvisitor.PREFIX + key + " is currently set to\n" + reply);
                                        }),
                                new CommandAPICommand("mailDeliveryLocationSelectionDistanceWeightMultiplier")
                                        .withHelp("Set mailDeliveryLocationSelectionDistanceWeightMultiplier.", """
                                                The multiplier of the distance weight when choosing mail delivery locations.
                                                Takes the ratio (between 0 and 1) of the distance to the maximum distance between locations,
                                                  multiplies it by this value, and adds it to the mail location weight.
                                                This weight is used to randomly choose a mail location to deliver to provide a realistic
                                                  relationship between delivery locations.
                                                At 0, distance has no effect on location selection.
                                                At 1, the weight will have a slight effect on the location selection.
                                                At 10, the weight will have a significant effect on the location selection.
                                                The weight is applied inversely, making closer distances worth more than further distances.
                                                Default: 1.00""")
                                        .withOptionalArguments(new LongArgument("mailDeliveryLocationSelectionDistanceWeightMultiplier"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "mailDeliveryLocationSelectionDistanceWeightMultiplier";
                                            configLong(sender, args, key);
                                        }),
                                new CommandAPICommand("mailDistanceToRewardMultiplier")
                                        .withHelp("Set mailDistanceToRewardMultiplier.", """
                                                Mail delivery reward is calculated by multiplying the distance by this number.
                                                The result is then rounded to the nearest ten.
                                                At 0, the reward given is 0.
                                                At 1, the reward given will be the distance in blocks.
                                                Default: 0.05""")
                                        .withOptionalArguments(new LongArgument("mailDistanceToRewardMultiplier"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "mailDistanceToRewardMultiplier";
                                            configLong(sender, args, key);
                                        }),
                                new CommandAPICommand("resourcePackRepo")
                                        .withHelp("Set resourcePackRepo", """
                                                The repository path of the server resource pack.
                                                Dreamvisitor will pull the first artifact from the latest release on pack update.
                                                Default: "WOFTNW/Dragonspeak""")
                                        .withOptionalArguments(new TextArgument("resourcePackRepo"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "resourcePackRepo";
                                            configString(sender, args, key);
                                        })
                        )
                );
    }

    private void configBoolean(CommandSender sender, @NotNull CommandArguments args, String key) {
        Boolean value = (Boolean) args.get(key);
        if (value == null) sender.sendMessage(Dreamvisitor.PREFIX + key + " is currently set to\n" + plugin.getConfig().getBoolean(key));
        else {
            plugin.getConfig().set(key, value);
            plugin.saveConfig();
            sender.sendMessage(Dreamvisitor.PREFIX + "Set " + key + " to\n" + plugin.getConfig().getBoolean(key));
        }
    }

    private void configInt(CommandSender sender, @NotNull CommandArguments args, String key) {
        Integer value = (Integer) args.get(key);
        if (value == null) sender.sendMessage(Dreamvisitor.PREFIX + key + " is currently set to\n" + plugin.getConfig().getInt(key));
        else {
            plugin.getConfig().set(key, value);
            plugin.saveConfig();
            sender.sendMessage(Dreamvisitor.PREFIX + "Set " + key + " to\n" + plugin.getConfig().getInt(key));
        }
    }

    private void configLong(CommandSender sender, @NotNull CommandArguments args, String key) {
        Long value = (Long) args.get(key);
        if (value == null) sender.sendMessage(Dreamvisitor.PREFIX + key + " is currently set to\n" + plugin.getConfig().getLong(key));
        else {
            plugin.getConfig().set(key, value);
            plugin.saveConfig();
            sender.sendMessage(Dreamvisitor.PREFIX + "Set " + key + " to\n" + plugin.getConfig().getLong(key));
        }
    }

    private void configString(CommandSender sender, @NotNull CommandArguments args, String key) {
        String value = (String) args.get(key);
        if (value == null) sender.sendMessage(Dreamvisitor.PREFIX + key + " is currently set to\n" + plugin.getConfig().getString(key));
        else {
            plugin.getConfig().set(key, value);
            plugin.saveConfig();
            sender.sendMessage(Dreamvisitor.PREFIX + "Set " + key + " to\n" + plugin.getConfig().getString(key));
        }
    }

    @SuppressWarnings("unchecked")
    private void configLongList(CommandSender sender, @NotNull CommandArguments args, String key) {
        List<Long> value = (List<Long>) args.get(key);
        if (value == null) sender.sendMessage(Dreamvisitor.PREFIX + key + " is currently set to\n" + plugin.getConfig().getLongList(key));
        else {
            plugin.getConfig().set(key, value);
            plugin.saveConfig();
            sender.sendMessage(Dreamvisitor.PREFIX + "Set " + key + " to\n" + plugin.getConfig().getLongList(key));
        }
    }
}

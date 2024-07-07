package io.github.stonley890.dreamvisitor.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LongArgument;
import dev.jorel.commandapi.arguments.TextArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.data.Tribe;
import io.github.stonley890.dreamvisitor.data.TribeUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

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
                                        .withHelp("Set debug.", "Whether to enable debug messages.\n" +
                                                "This will send additional messages to help debug Dreamvisitor.\n" +
                                                "Default: false")
                                        .withOptionalArguments(new BooleanArgument("debug"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "debug";
                                            configBoolean(sender, args, key);
                                        }),
                                new CommandAPICommand("website-url")
                                        .withHelp("Set website-url.", "Website URL\n" +
                                                "The URL for the whitelisting website.\n" +
                                                "Default: \"https://woftnw.duckdns.org\"")
                                        .withOptionalArguments(new TextArgument("website-url"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "website-url";
                                            configString(sender, args, key);
                                        }),
                                new CommandAPICommand("web-whitelist")
                                        .withHelp("Set web-whitelist.", "Whether web whitelisting is enabled or not\n" +
                                                "This can be set with the /toggleweb Discord command.\n" +
                                                "Default: true")
                                        .withOptionalArguments(new BooleanArgument("web-whitelist"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "web-whitelist";
                                            configBoolean(sender, args, key);
                                        }),
                                new CommandAPICommand("chatChannelID")
                                        .withHelp("Set chatChannelID.", "The channel ID of the game chat.\n" +
                                                "This can be set on Discord with /setgamechat\n" +
                                                "Default: 880269118975119410")
                                        .withOptionalArguments(new LongArgument("chatChannelID"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "chatChannelID";
                                            configLong(sender, args, key);
                                        }),
                                new CommandAPICommand("logChannelID")
                                        .withHelp("Set logChannelID.", "The channel ID of the log chat.\n" +
                                                "This can be set on Discord /setlogchat\n" +
                                                "Default: 1114730320068104262")
                                        .withOptionalArguments(new LongArgument("logChannelID"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "logChannelID";
                                            configLong(sender, args, key);
                                        }),
                                new CommandAPICommand("whitelistChannelID")
                                        .withHelp("Set whitelistChannelID.", "The channel ID of the whitelist chat.\n" +
                                                "This can be set on Discord /setwhitelist\n" +
                                                "Default: 858461513991323688")
                                        .withOptionalArguments(new LongArgument("whitelistChannelID"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "whitelistChannelID";
                                            configLong(sender, args, key);
                                        }),
                                new CommandAPICommand("tribe-roles")
                                        .withHelp("Set tribe-roles.", "The role IDs of tribes on the main server.\n" +
                                                "This can be set on Discord with /setrole")
                                        .withArguments(CommandUtils.customTribeArgument("tribe"))
                                        .withOptionalArguments(new LongArgument("role-id"))
                                        .executes((sender, args) -> {
                                            @Nullable Long roleId = (Long) args.get("role-id");
                                            @NotNull Tribe tribe = (Tribe) Objects.requireNonNull(args.get("tribe"));
                                            int tribeIndex = TribeUtil.indexOf(tribe);
                                            List<Long> emblems = plugin.getConfig().getLongList("triberoles");
                                            if (roleId == null) sender.sendMessage(Dreamvisitor.PREFIX + "role of " + tribe.getName() + " is currently set to\n" + emblems.get(tribeIndex));
                                            else {
                                                emblems.set(tribeIndex, roleId);
                                                plugin.getConfig().set("triberoles", emblems);
                                                plugin.saveConfig();
                                                sender.sendMessage(Dreamvisitor.PREFIX + "Set role of " + tribe.getName() + " to\n" + roleId);
                                            }
                                        }),
                                new CommandAPICommand("chatPaused")
                                        .withHelp("Set chatPaused.", "Whether chat is paused or not.\n" +
                                                "This can be toggled in Minecraft with /pausechat\n" +
                                                "Default: false")
                                        .withOptionalArguments(new BooleanArgument("chatPaused"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "chatPaused";
                                            configBoolean(sender, args, key);
                                        }),
                                new CommandAPICommand("softwhitelist")
                                        .withHelp("Set softwhitelist.", "Whether the soft whitelist is enabled or not\n" +
                                                "This can be set in Minecraft with /softwhitelist [on|off]\n" +
                                                "Default: false")
                                        .withOptionalArguments(new BooleanArgument("softwhitelist"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "softwhitelist";
                                            configBoolean(sender, args, key);
                                        }),
                                new CommandAPICommand("playerlimit")
                                        .withHelp("Set playerlimit.", "Player limit override. This will override the player limit, both over and under.\n" +
                                                "This can be set in Minecraft with /playerlimit <int>\n" +
                                                "Default: -1")
                                        .withOptionalArguments(new IntegerArgument("playerlimit", -1))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "playerlimit";
                                            configInt(sender, args, key);
                                        }),
                                new CommandAPICommand("disablepvp")
                                        .withHelp("Set disablepvp.", "Whether to globally disable pvp or not.\n" +
                                                "This can be toggled in Minecraft with /togglepvp\n" +
                                                "Default: false")
                                        .withOptionalArguments(new BooleanArgument("disablepvp"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "disablepvp";
                                            configBoolean(sender, args, key);
                                        }),
                                new CommandAPICommand("hubLocation")
                                        .withHelp("Get hubLocation.", "The location of the recorded hub.\n" +
                                                "This can be set in Minecraft with /sethub\n" +
                                                "Default: none")
                                        .executes((sender, args) -> {
                                            @Nullable String key = "hubLocation";
                                            Location location = plugin.getConfig().getLocation(key);
                                            String reply = "none";
                                            if (location != null) reply = location.toString();
                                            sender.sendMessage(Dreamvisitor.PREFIX + key + " is currently set to\n" + reply);
                                        }),
                                new CommandAPICommand("log-console")
                                        .withHelp("Set log-console.", "Whether to copy the output of the console to the Discord log channel.\n" +
                                                "This will disable the default Dreamvisitor logging in place of the Minecraft server console.\n" +
                                                "Default: false")
                                        .withOptionalArguments(new BooleanArgument("log-console"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "log-console";
                                            configBoolean(sender, args, key);
                                        }),
                                new CommandAPICommand("enable-log-console-commands")
                                        .withHelp("Set enable-log-console-commands.", "Whether to pass messages in the log channel as console commands.\n" +
                                                "If log-console is enabled, this will take messages sent by users with the Discord administrator permission and pass\n" +
                                                "  them as console commands.\n" +
                                                "Default: false")
                                        .withOptionalArguments(new BooleanArgument("enable-log-console-commands"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "enable-log-console-commands";
                                            configBoolean(sender, args, key);
                                        }),
                                new CommandAPICommand("infraction-expire-time-days")
                                        .withHelp("Set infraction-expire-time-days.", "The amount of time in days (as an integer) that infractions take to expire.\n" +
                                                "Expired infractions are not deleted, but they do not count toward a total infraction count.\n" +
                                                "Default: 90")
                                        .withOptionalArguments(new IntegerArgument("infraction-expire-time-days", 0))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "infraction-expire-time-days";
                                            configInt(sender, args, key);
                                        }),
                                new CommandAPICommand("infractions-category-id")
                                        .withHelp("Set infractions-category-id.", "The ID of the category to create infractions channels.\n" +
                                                "They will accumulate here.\n" +
                                                "Default: 1226180189604544593")
                                        .withOptionalArguments(new LongArgument("infractions-category-id"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "infractions-category-id";
                                            configLong(sender, args, key);
                                        }),
                                new CommandAPICommand("shopName")
                                        .withHelp("Set shopName.", "The name of the Discord shop.\n" +
                                                "This will appear at the top of the embed.\n" +
                                                "Default: \"Shop\"")
                                        .withOptionalArguments(new TextArgument("shopName"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "shopName";
                                            configString(sender, args, key);
                                        }),
                                new CommandAPICommand("currencyIcon")
                                        .withHelp("Set currencyIcon.", "The icon used for currency in the Discord economy system.\n" +
                                                "This can be any string, including symbols, letters, emojis, and Discord custom emoji.\n" +
                                                "Default: \"$\"")
                                        .withOptionalArguments(new TextArgument("currencyIcon"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "currencyIcon";
                                            configString(sender, args, key);
                                        }),
                                new CommandAPICommand("dailyBaseAmount")
                                        .withHelp("Set dailyBaseAmount.", "The base amount given by the /daily Discord command.\n" +
                                                "This is the default amount before adding the streak bonus. The total amount is decided by dailyBaseAmount + (user's streak * this).\n" +
                                                "Default: 10.00")
                                        .withOptionalArguments(new LongArgument("dailyBaseAmount"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "dailyBaseAmount";
                                            configLong(sender, args, key);
                                        }),
                                new CommandAPICommand("dailyStreakMultiplier")
                                        .withHelp("Set dailyStreakMultiplier.", "The multiplier of the streak bonus given by the /daily command.\n" +
                                                "This is multiplied by the streak and added to the base amount. The total amount is decided by dailyBaseAmount + (user's streak * this).\n" +
                                                "Default: 5.00")
                                        .withOptionalArguments(new LongArgument("dailyStreakMultiplier"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "dailyStreakMultiplier";
                                            configLong(sender, args, key);
                                        }),
                                new CommandAPICommand("workReward")
                                        .withHelp("Set workReward.", "The amount gained from the /work command.\n" +
                                                "/work can only be run every hour.\n" +
                                                "Default: 20.00")
                                        .withOptionalArguments(new LongArgument("workReward"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "workReward";
                                            configLong(sender, args, key);
                                        }),
                                new CommandAPICommand("mailDeliveryLocationSelectionDistanceWeightMultiplier")
                                        .withHelp("Set mailDeliveryLocationSelectionDistanceWeightMultiplier.", "The multiplier of the distance weight when choosing mail delivery locations.\n" +
                                                "Takes the ratio (between 0 and 1) of the distance to the maximum distance between locations,\n" +
                                                "  multiplies it by this value, and adds it to the mail location weight.\n" +
                                                "This weight is used to randomly choose a mail location to deliver to provide a realistic\n" +
                                                "  relationship between delivery locations.\n" +
                                                "At 0, distance has no effect on location selection.\n" +
                                                "At 1, the weight will have a slight effect on the location selection.\n" +
                                                "At 10, the weight will have a significant effect on the location selection.\n" +
                                                "The weight is applied inversely, making closer distances worth more than further distances.\n" +
                                                "Default: 1.00")
                                        .withOptionalArguments(new LongArgument("mailDeliveryLocationSelectionDistanceWeightMultiplier"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "mailDeliveryLocationSelectionDistanceWeightMultiplier";
                                            configLong(sender, args, key);
                                        }),
                                new CommandAPICommand("mailDistanceToRewardMultiplier")
                                        .withHelp("Set mailDistanceToRewardMultiplier.", "Mail delivery reward is calculated by multiplying the distance by this number.\n" +
                                                "The result is then rounded to the nearest ten.\n" +
                                                "At 0, the reward given is 0.\n" +
                                                "At 1, the reward given will be the distance in blocks.\n" +
                                                "Default: 0.05")
                                        .withOptionalArguments(new LongArgument("mailDistanceToRewardMultiplier"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "mailDistanceToRewardMultiplier";
                                            configLong(sender, args, key);
                                        }),
                                new CommandAPICommand("currentYear")
                                        .withHelp("Set currentYear.", "The current in-universe year.\n" +
                                                "This will be incremented by Dreamvisitor automatically.\n" +
                                                "Default: 0")
                                        .withOptionalArguments(new LongArgument("currentYear"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "currentYear";
                                            configLong(sender, args, key);
                                        }),
                                new CommandAPICommand("minutesPerYear")
                                        .withHelp("Set minutesPerYear.", "The number of minutes in an in-universe year.\n" +
                                                "Default: 43200")
                                        .withOptionalArguments(new LongArgument("minutesPerYear"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "minutesPerYear";
                                            configLong(sender, args, key);
                                        }),
                                new CommandAPICommand("lastUpdateMilli")
                                        .withHelp("Set lastUpdateMilli.", "The last time the year was updated.\n" +
                                                "This is used to keep time accurately, even if the server goes offline.\n" +
                                                "Do not change this value.\n" +
                                                "Default: 0")
                                        .withOptionalArguments(new LongArgument("lastUpdateMilli"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "lastUpdateMilli";
                                            configLong(sender, args, key);
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
}

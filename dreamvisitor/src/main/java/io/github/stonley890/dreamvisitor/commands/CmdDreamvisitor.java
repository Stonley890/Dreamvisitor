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
                                            configInt(sender, args, key);
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
                                            configInt(sender, args, key);
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
                                            configInt(sender, args, key);
                                        }),
                                new CommandAPICommand("softwhitelist")
                                        .withHelp("Set softwhitelist.", "Whether the soft whitelist is enabled or not\n" +
                                                "This can be set in Minecraft with /softwhitelist [on|off]\n" +
                                                "Default: false")
                                        .withOptionalArguments(new BooleanArgument("softwhitelist"))
                                        .executes((sender, args) -> {
                                            @Nullable String key = "softwhitelist";
                                            configInt(sender, args, key);
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
                                            configInt(sender, args, key);
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

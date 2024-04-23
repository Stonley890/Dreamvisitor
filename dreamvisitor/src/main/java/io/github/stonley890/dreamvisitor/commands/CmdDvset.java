package io.github.stonley890.dreamvisitor.commands;

import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.data.PlayerMemory;
import io.github.stonley890.dreamvisitor.data.PlayerUtility;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public class CmdDvset implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        // dvset [user|admin [<option> (<action>)]]

        if (args.length == 0) {
            if (sender instanceof Player player && sender.hasPermission("dreamvisitor.userset")) {
                // Player GUI
                sendUserGui(player);
            }
        } else {
            if (args[0].equals("user") && sender.hasPermission("dreamvisitor.userset")) {

                if (sender instanceof Player player) {
                    if (args.length > 1) {

                        PlayerMemory memory = PlayerUtility.getPlayerMemory(player.getUniqueId());

                        switch (args[1]) {
                            case "discord" -> {
                                if (sender.hasPermission("dreamvisitor.set.discord")) {

                                    if (args.length == 3) {
                                        memory.discordToggled = Boolean.parseBoolean(args[2]);
                                        sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.GRAY + "Discord Visibility toggled to " + ChatColor.WHITE + memory.discordToggled);
                                    } else sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.GRAY + "Discord Visibility is currently set to " + ChatColor.WHITE + memory.discordToggled);

                                } else sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "Invalid arguments or insufficient permissions!");
                            }
                            case "vanished" -> {
                                if (sender.hasPermission("dreamvisitor.set.zoop")) {

                                    if (args.length == 3) {
                                        memory.vanished = Boolean.parseBoolean(args[2]);

                                        if (memory.vanished) {
                                            String chatMessage = "**" + player.getName() + " left the game**";
                                            Bot.getGameChatChannel().sendMessage(chatMessage).queue();
                                            Bot.sendLog(chatMessage);
                                        } else {
                                            String chatMessage = "**" + player.getName() + " joined the game**";
                                            Bot.getGameChatChannel().sendMessage(chatMessage).queue();
                                            Bot.sendLog(chatMessage);
                                        }

                                        sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.GRAY + "Discord Vanish toggled to " + ChatColor.WHITE + memory.vanished);
                                    } else sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.GRAY + "Discord Vanish is currently set to " + ChatColor.WHITE + memory.vanished);

                                } else sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "Invalid arguments or insufficient permissions!");
                            }
                            case "autoinvswap" -> {
                                if (sender.hasPermission("dreamvisitor.set.autoinvswap")) {

                                    if (args.length == 3) {
                                        memory.autoinvswap = Boolean.parseBoolean(args[2]);
                                        sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.GRAY + "Automatic Inventory Swap toggled to " + ChatColor.WHITE + memory.autoinvswap);
                                    } else sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.GRAY + "Automatic Inventory Swap is currently set to " + ChatColor.WHITE + memory.autoinvswap);

                                } else sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "Invalid arguments or insufficient permissions!");
                            }
                            case "autoradio" -> {
                                if (sender.hasPermission("dreamvisitor.set.autoradio")) {

                                    if (args.length == 3) {
                                        memory.autoRadio = Boolean.parseBoolean(args[2]);
                                        sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.GRAY + "Automatic Radio toggled to " + ChatColor.WHITE + memory.autoRadio);
                                    } else sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.GRAY + "Automatic Radio is currently set to " + ChatColor.WHITE + memory.autoRadio);

                                } else sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "Invalid arguments or insufficient permissions!");
                            }
                            default -> sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "Invalid arguments or insufficient permissions!");
                        }

                        PlayerUtility.setPlayerMemory(player.getUniqueId(), memory);

                    }

                    // Player GUI
                    sendUserGui(player);

                } else sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "This command must be run by a player.");

            } else if (args[0].equals("admin") && sender.hasPermission("dreamvisitor.adminset")) {
                if (args.length > 1) {

                    switch (args[1]) {
                        case "pausechat" -> {
                            if (sender.hasPermission("dreamvisitor.pausechat")) {
                                if (args.length == 3) {

                                    Dreamvisitor.chatPaused = Boolean.parseBoolean(args[2]);

                                    if (Dreamvisitor.chatPaused) {
                                        // Broadcast to server
                                        Bukkit.getServer().broadcastMessage(org.bukkit.ChatColor.BLUE + "Chat has been paused.");
                                        // Broadcast to chat channel
                                        Bot.getGameChatChannel().sendMessage("**Chat has been paused. Messages will not be sent to Minecraft**").queue();


                                    } else {
                                        // Broadcast to server
                                        Bukkit.getServer().broadcastMessage(org.bukkit.ChatColor.BLUE + "Chat has been unpaused.");
                                        // Broadcast to chat channel
                                        Bot.getGameChatChannel().sendMessage("**Chat has been unpaused. Messages will now be sent to Minecraft**").queue();
                                    }
                                    Dreamvisitor.getPlugin().getConfig().set("chatPaused", Dreamvisitor.chatPaused);
                                    Dreamvisitor.getPlugin().saveConfig();

                                    sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.GRAY + "Chat Pause toggled to " + ChatColor.WHITE + Dreamvisitor.chatPaused);
                                } else sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.GRAY + "Chat Pause is currently set to " + ChatColor.WHITE + Dreamvisitor.chatPaused);
                            }

                        }
                        case "softwhitelist" -> {
                            if (sender.hasPermission("dreamvisitor.softwhitelist")) {
                                if (args.length == 3) {

                                    // Set config
                                    Dreamvisitor.getPlugin().getConfig().set("softwhitelist", Boolean.parseBoolean(args[2]));
                                    Dreamvisitor.getPlugin().saveConfig();

                                    sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.GRAY + "Soft Whitelist toggled to " + ChatColor.WHITE + Boolean.parseBoolean(args[2]));
                                } else sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.GRAY + "Soft Whitelist is currently set to " + ChatColor.WHITE + Boolean.parseBoolean(args[2]));
                            }

                        }
                        default -> sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "Invalid arguments or insufficient permissions!");
                    }

                }

                if (sender instanceof Player player) {
                    // Player GUI
                    sendAdminGui(player);
                }

            } else sender.sendMessage(Dreamvisitor.PREFIX + ChatColor.RED + "Invalid arguments or insufficient permissions!");
        }

        return true;
    }

    private static void sendUserGui(@NotNull Player player) {

        PlayerMemory memory = PlayerUtility.getPlayerMemory(player.getUniqueId());

        ComponentBuilder builder = new ComponentBuilder(Dreamvisitor.PREFIX);
        builder.append("User Options ");

        if (player.hasPermission("dreamvisitor.adminset")) {
            TextComponent adminButton = new TextComponent("Switch to Admin");
            adminButton.setUnderlined(true);
            adminButton.setColor(ChatColor.GRAY);
            adminButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.GRAY + "Switch to administrator options.")));
            adminButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/dvset admin"));

            builder.append("[").color(ChatColor.DARK_GRAY).append(adminButton).append("").reset().append("]").color(ChatColor.DARK_GRAY);
        }


        if (player.hasPermission("dreamvisitor.set.discord")) {
            builder.append("\n\nDiscord Visibility: ").color(ChatColor.WHITE)
                    .append("\nWhether to show messages from Discord's chat bridge.").color(ChatColor.GRAY)
                    .append("\n[").color(ChatColor.DARK_GRAY)
                    .append(booleanToggle(memory.discordToggled, true, "discord"))
                    .append("").reset().append("]").color(ChatColor.DARK_GRAY);
        }

        if (player.hasPermission("dreamvisitor.set.zoop")) {
            builder.append("\n\nDiscord Vanish: ").color(ChatColor.WHITE)
                    .append("\nWhether to appear offline in Dreamvisitor.").color(ChatColor.GRAY)
                    .append("\n[").color(ChatColor.DARK_GRAY)
                    .append(booleanToggle(memory.vanished, true, "vanished"))
                    .append("").reset().append("]").color(ChatColor.DARK_GRAY);
        }

        if (player.hasPermission("dreamvisitor.set.autoinvswap")) {
            builder.append("\n\nAutomatic Inventory Swap: ").color(ChatColor.WHITE)
                    .append("\nWhether to automatically swap inventories on game mode change.").color(ChatColor.GRAY)
                    .append("\n[").color(ChatColor.DARK_GRAY)
                    .append(booleanToggle(memory.autoinvswap, true, "autoinvswap"))
                    .append("").reset().append("]").color(ChatColor.DARK_GRAY);
        }

        if (player.hasPermission("dreamvisitor.set.autoradio")) {
            builder.append("\n\nAutomatic Radio: ").color(ChatColor.WHITE)
                    .append("\nWhether to send all messages to staff radio.").color(ChatColor.GRAY)
                    .append("\n[").color(ChatColor.DARK_GRAY)
                    .append(booleanToggle(memory.autoRadio, true, "autoradio"))
                    .append("").reset().append("]").color(ChatColor.DARK_GRAY);
        }

        builder.append("").reset().append("\n");
        player.spigot().sendMessage(builder.create());

    }

    private static void sendAdminGui(@NotNull Player player) {

        ComponentBuilder builder = new ComponentBuilder(Dreamvisitor.PREFIX);
        builder.append("Admin Options ").color(ChatColor.DARK_AQUA);

        if (player.hasPermission("dreamvisitor.userset")) {
            TextComponent adminButton = new TextComponent("Switch to User");
            adminButton.setUnderlined(true);
            adminButton.setColor(ChatColor.GRAY);
            adminButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.GRAY + "Switch to user options.")));
            adminButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/dvset user"));

            builder.append("[").color(ChatColor.DARK_GRAY).append(adminButton).append("").reset().append("]").color(ChatColor.DARK_GRAY).underlined(false);
        }

        if (player.hasPermission("dreamvisitor.pausechat")) {
            builder.append("\n\nChat Pause: ").color(ChatColor.WHITE)
                    .append("\nWhether to stop player messages.").color(ChatColor.DARK_AQUA)
                    .append("\n[").color(ChatColor.DARK_GRAY)
                    .append(booleanToggle(Dreamvisitor.chatPaused, false, "pausechat"))
                    .append("").reset().append("]").color(ChatColor.DARK_GRAY);
        }

        if (player.hasPermission("dreamvisitor.softwhitelist")) {
            builder.append("\n\nSoft Whitelist: ").color(ChatColor.WHITE)
                    .append("\nWhether to enforce the soft whitelist.").color(ChatColor.DARK_AQUA)
                    .append("\n[").color(ChatColor.DARK_GRAY)
                    .append(booleanToggle(Dreamvisitor.getPlugin().getConfig().getBoolean("softwhitelist"), false, "softwhitelist"))
                    .append("").reset().append("]").color(ChatColor.DARK_GRAY);

            builder.append("\n\nSoft Whitelist Players: ").color(ChatColor.WHITE)
                    .append("\nThe players allowed by the soft whitelist.").color(ChatColor.DARK_AQUA);

            // Load softWhitelist.yml
            File file = new File(Dreamvisitor.getPlugin().getDataFolder().getAbsolutePath() + "/softWhitelist.yml");
            FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);

            BaseComponent[] playerList = playerList(fileConfig.getStringList("players"));

            if (playerList == null || playerList.length == 0) {
                TextComponent none = new TextComponent("None");
                none.setColor(ChatColor.GRAY);
                none.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.GRAY + "Click to add a player")));
                none.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/softwhitelist add "));
                builder.append("\n[").color(ChatColor.DARK_GRAY).append(none).append("").reset().append("]").color(ChatColor.DARK_GRAY);

            } else {

                builder.append("\n[ ").color(ChatColor.DARK_GRAY);
                for (BaseComponent baseComponent : playerList) {
                    baseComponent.setColor(ChatColor.YELLOW);
                    baseComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.GRAY + "Click to remove player")));
                    baseComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/softwhitelist remove " + baseComponent.toPlainText()));
                    builder.append(baseComponent).append(" ").reset();
                }
                builder.append("").reset().append("]").color(ChatColor.DARK_GRAY);

            }

        }

        if (player.hasPermission("dreamvisitor.playerlimit")) {
            builder.append("\n\nPlayer Limit Override: ").color(ChatColor.WHITE)
                    .append("\nOverride server player limit. Set to -1 to use default.").color(ChatColor.DARK_AQUA)
                    .append("\n[").color(ChatColor.DARK_GRAY);
            TextComponent value = intToggle(Dreamvisitor.playerLimit);
            value.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/playerlimit "));
            builder.append(value)
                    .append("").reset().append("]").color(ChatColor.DARK_GRAY).underlined(false);
        }

        builder.append("").reset().append("\n");
        player.spigot().sendMessage(builder.create());

    }

    private static BaseComponent[] playerList(@NotNull List<String> players) {

        ComponentBuilder componentBuilder = new ComponentBuilder();

        for (String player : players) {
            TextComponent playerButton = new TextComponent(PlayerUtility.getUsernameOfUuid(player));
            componentBuilder.append(playerButton);
        }

        return componentBuilder.create();
    }

    /**
     * Creates a {@link TextComponent} representing an {@code int} value with a command to change it.
     *
     * @param value the {@code int} to display.
     * @return a {@link TextComponent} representing the value with a command to change it.
     */
    private static @NotNull TextComponent intToggle(int value) {
        TextComponent toggle = new TextComponent(String.valueOf(value));
        toggle.setUnderlined(true);

        String state;
        state = "user";

        toggle.setColor(ChatColor.YELLOW);
        toggle.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.GRAY + "Currently set to " + ChatColor.WHITE + value + ".")));
        toggle.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/dvset " + state + " " + "autoinvswap" + " "));

        return toggle;
    }

    /**
     * Creates a {@link TextComponent} representing a {@code boolean} value with a command to change it.
     * @param value the {@code boolean} to display.
     * @param user whether the value is user-based or admin-based.
     * @param cmdName the command to run. This will be formatted as {@code /dvset <state> <cmdName> !value}
     * @return a {@link TextComponent} representing the value with a command to change it.
     */
    private static @NotNull TextComponent booleanToggle(boolean value, boolean user, String cmdName) {
        TextComponent toggle = new TextComponent();
        toggle.setUnderlined(true);

        if (value) {
            toggle.setText("-O");
            toggle.setColor(ChatColor.GREEN);
        } else {
            toggle.setText("O-");
            toggle.setColor(ChatColor.RED);
        }

        String state;
        if (user) state = "user";
        else state = "admin";

        toggle.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.GRAY + "Currently toggled to " + ChatColor.WHITE + String.valueOf(value).toUpperCase() + ".")));
        toggle.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/dvset " + state + " " + cmdName + " " + String.valueOf(!value).toLowerCase()));


        return toggle;
    }
}
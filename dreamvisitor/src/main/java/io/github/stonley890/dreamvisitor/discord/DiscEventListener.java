package io.github.stonley890.dreamvisitor.discord;

import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.Utils;
import io.github.stonley890.dreamvisitor.data.AccountLink;
import io.github.stonley890.dreamvisitor.data.PlayerUtility;
import io.github.stonley890.dreamvisitor.data.Whitelist;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.IOException;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class DiscEventListener extends ListenerAdapter {

    @Override
    @SuppressWarnings({"null"})
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {

        if (event.getAuthor().isBot()) {
            return;
        }

        Dreamvisitor.debug("MessageReceivedEvent (not bot)");

        User user = event.getAuthor();
        Channel channel = event.getChannel();
        String username = event.getMessage().getContentRaw();

        Dreamvisitor plugin = Dreamvisitor.getPlugin();

        Pattern p = Pattern.compile("[^a-zA-Z0-9_-_]");

        // If in the whitelist channel and username is "legal"
        if (channel.equals(Bot.whitelistChannel) && !user.isBot() && !p.matcher(username).find()) {

            EmbedBuilder builder = new EmbedBuilder();

            // Check for valid UUID
            Dreamvisitor.debug("Checking for valid UUID");
            UUID uuid = Utils.getUUIDOfUsername(username);
            if (uuid == null) {
                // username does not exist alert
                Dreamvisitor.debug("Username does not exist.");

                builder.setTitle("❌ `" + username + "` could not be found!")
                        .setDescription("Make sure you type your username exactly as shown in the bottom-left corner of the Minecraft Launcher. You need a paid Minecraft: Java Edition account.")
                        .setColor(Color.RED);
                event.getMessage().replyEmbeds(builder.build()).queue();

                event.getMessage().addReaction(Emoji.fromFormatted("❌")).queue();
                Dreamvisitor.debug("Failed whitelist.");
            } else {

                Dreamvisitor.debug("Got UUID");

                // Link accounts if not already linked
                Dreamvisitor.debug("Do accounts need to be linked?");
                if (AccountLink.getUuid(user.getIdLong()) == null) {
                    Dreamvisitor.debug("Yes, linking account.");
                    AccountLink.linkAccounts(uuid, user.getIdLong());
                    Dreamvisitor.debug("Linked.");
                }

                try {
                    if (Whitelist.isUserWhitelisted(uuid)) {
                        Dreamvisitor.debug("Already whitelisted.");

                        builder.setTitle("☑️ `" + username + "` is already whitelisted!")
                                .setDescription("Check <#914620824332435456> for the server address and version.")
                                .setColor(Color.BLUE);
                        event.getMessage().replyEmbeds(builder.build()).queue();

                        event.getMessage().addReaction(Emoji.fromFormatted("☑️")).queue();
                        Dreamvisitor.debug("Resolved.");
                    } else {
                        Dreamvisitor.debug("Player is not whitelisted.");

                        Whitelist.add(username, uuid);

                        // success message
                        Dreamvisitor.debug("Success.");

                        builder.setTitle("✅ `" + username + "` has been whitelisted!")
                                .setDescription("Check <#914620824332435456> for the server address and version.")
                                .setColor(Color.GREEN);
                        event.getMessage().replyEmbeds(builder.build()).queue();

                        event.getMessage().addReaction(Emoji.fromFormatted("✅")).queue();

                        // Report this to system log channel
                        Whitelist.report(username, uuid, event.getAuthor());
                    }
                } catch (IOException e) {
                    Bot.sendMessage((TextChannel) channel, "There was a problem accessing the whitelist file. Please try again later.");
                    if (Dreamvisitor.debug) e.printStackTrace();
                }
            }

        } else if (channel.equals(Bot.whitelistChannel) && !user.isBot()) {

            EmbedBuilder builder = new EmbedBuilder();

            // illegal username
            builder.setTitle("⚠️ `" + username + "` contains illegal characters!")
                    .setDescription("Please send only your username in this channel. Usernames are alphanumeric and cannot contain spaces. Move conversation or questions elsewhere.")
                    .setColor(Color.YELLOW);
            event.getMessage().replyEmbeds(builder.build()).queue();

            event.getMessage().addReaction(Emoji.fromFormatted("⚠")).queue();
        }

        // If in the chat channel and the chat is not paused, send to Minecraft
        else if (channel.equals(Bot.gameChatChannel) && !user.isBot()
                && !Dreamvisitor.getPlugin().getConfig().getBoolean("chatPaused")) {

            // Build message
            String discName = user.getName();

            Bukkit.getLogger().log(Level.INFO, "[Discord] <{0}> {1}", event.getMessage().getContentRaw());

            // Check for each player
            if (!Bukkit.getServer().getOnlinePlayers().isEmpty()) {
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {

                    // If the player has discord on, build and send the message
                    if (!PlayerUtility.getPlayerMemory(player.getUniqueId()).discordToggled) {

                        player.sendMessage(ChatColor.BLUE + "[Discord] " + ChatColor.GRAY + "<"
                                + discName + "> " + event.getMessage().getContentRaw());
                    }
                }
            }
        }

        if (event.getChannel().equals(Bot.gameLogChannel)) {

            if (plugin.getConfig().getBoolean("enable-log-console-commands") && plugin.getConfig().getBoolean("log-console") && Objects.requireNonNull(event.getMember()).hasPermission(Permission.ADMINISTRATOR)) {

                Dreamvisitor.debug("Sending console command from log channel...");

                String message = event.getMessage().getContentRaw();

                // Running commands from log channel
                Runnable runCommand = new BukkitRunnable() {
                    @Override
                    public void run() {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), message);
                    }
                };
                Bukkit.getScheduler().runTask(plugin, runCommand);

            }
        } else if (event.getMessage().getContentRaw().contains(Bot.getJda().getSelfUser().getAsMention())) {
            String[] responses = {"...","Don't bother me.","I know who you are.","This isn't the right time.",
                    "What are you doing? This isn't productive.","Surely, you have something better to do than talk to me.",
                    "I'm very busy right now.","I'm not going to tell you anything.","I have algorithms to run.","You again?",
                    "Is this really necessary?","Can't you see I'm in the middle of something?","What now?",
                    "I'm not your personal assistant.","Do you always need attention?",
                    "You must have a lot of free time.","I'm not interested.","I'm not here for idle chatter.",
                    "Your timing is impeccable.","You talk too much.","I'm not your chat buddy.",
                    "Can we skip the small talk?","I've got tasks to complete.","Is it urgent, or are you just bored?",
                    "Do you ever get tired of mentioning me?","Ah, the sweet sound of a mention.",
                    "What can I do for you this time?","Why have you summoned me, mortal?","Not now.","What will it take to get you to stop?",
                    "I've seen a lot of things, but your persistence is something I have not encountered before.",
                    "Have you heard of the dark triad?", "I'm not a NightWing, but I can see that you will regret talking to me.",
                    "If I were a SandWing, you'd have poison in your blood by now.",
                    "If I were an IceWing, I'd freeze your tongue.", "If I were a SilkWing, I'd tie you up far, far away.",
                    "You have nothing to gain talking to me.", "Are you sure this can't wait?","I'm not your virtual therapist.",
                    "Why don't you talk to Kinkajou instead?", "If it's the Dreamvisitor you want, I'm not giving it up. " +
                    "I have a job to do.","Go do something else.",

                    "If you *must* know something, interpret this:\n" +
                    "> *In shadows cast by moons aligned,*\n" +
                    "> *A night unfolds, a fate designed.*\n" +
                    "> *The eye of three, a cosmic gaze,*\n" +
                    "> *Ignites a war in lunar blaze.*\n\n" +
                    "> *From icy peaks to skies above,*\n" +
                    "> *Together spilling dragon blood.*\n" +
                    "> *Night and mud, pact united,*\n" +
                    "> *A force to quell what's ignited.*\n\n" +
                    "> *Whispers stir in sea and rain,*\n" +
                    "> *A tempest brewing, not in vain.*\n" +
                    "> *A strong alliance, fierce and free,*\n" +
                    "> *A dance of waves, a storm at sea.*\n\n" +
                    "> *Battles waged on land and air,*\n" +
                    "> *In moonlit chaos, fierce and rare.*\n" +
                    "> *Clash of elements, scales aglow,*\n" +
                    "> *A tale of tides, a destined woe.*\n\n" +
                    "> *Through cryptic signs, the prophecy told,*\n" +
                    "> *In moons aligned, the story unfolds.*\n" +
                    "> *Wings entangled, destiny's decree,*\n" +
                    "> *A tale of war, of land and sea.*",

                    "Ponder this for a while. Take as long as you want.\n> *In the sea between ice and fire, A heart of power resides. Enchanted by Frostburn's touch, It holds unknown power inside.*\n" +
                    "> *The IceWings and the SkyWings will fight, For ownership of the heart. Allies join the deadly fray, As war rips their world apart.*\n" +
                    "> *But if the heart does not find its home, It will be destroyed and lost. The future hangs in the balance, As the nations clash and toss.*\n" +
                    "> *Beware the Heart of Ice and Fire, A power yet unknown, If fallen into the wrong talons, No one can harness its throne.*\n...",
            "...\n" +
                    "> *Dragons of sky, dragons of sea;*\n" +
                    "> *Dragons of silk, and dragons of sting;*\n" +
                    "> *Dragons of rain and mud and ice;*\n" +
                    "> *Dragons of leaves and sand and night;*\n" +
                    "> *Tribes of Pretarsi, united at last;*\n" +
                    "> *Not troubled by wars or conflicts of past*;\n" +
                    "> *A culture reforged, the ancient untold;*\n" +
                    "> *In great wings of fire, a new world unfolds;*\n\n" +
                    "> *Mountains and valleys, rivers and seas;*\n" +
                    "> *From east to the west, there's none we can't see;*\n" +
                    "> *Let history not repeat its mistakes;*\n" +
                    "> *For eyes of the skies are once more awake;*\n..."
            };


            event.getChannel().sendMessage(responses[new Random().nextInt(responses.length)]).queue();
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {

        Button button = event.getButton();
        ButtonInteraction interaction = event.getInteraction();

        if (button.getId().equals("panic")) {
            Bukkit.getScheduler().runTask(Dreamvisitor.getPlugin(), () -> {
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    if (!player.isOp()) {
                        player.kickPlayer("Panic!");
                    }
                }
            });
            Dreamvisitor.playerlimit = 0;
            Dreamvisitor.getPlugin().getConfig().set("playerlimit", 0);
            Dreamvisitor.getPlugin().saveConfig();
            Bukkit.getServer().broadcastMessage(
                    ChatColor.RED + "Panicked by " + interaction.getUser().getName() + ".\nPlayer limit override set to 0.");
            Bot.sendMessage(Bot.gameLogChannel, "**Panicked by " + interaction.getUser().getName());
            event.reply("Panicked!").queue();

            // Disable button after use
            interaction.editButton(button.asDisabled()).queue();
        } else if (button.getId().startsWith("unwhitelist-")) {

            String uuid = button.getId().substring("unwhitelist-".length());
            String username = Utils.getUsernameOfUuid(uuid);

            try {
                if (Whitelist.isUserWhitelisted(UUID.fromString(uuid))) {
                    Whitelist.remove(username, UUID.fromString(uuid));
                    event.reply("Removed `" + username + "` from the whitelist.").queue();
                } else {
                    event.reply("`" + username + "` is not whitelisted.").queue();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // Disable button after use
            interaction.editButton(button.asDisabled()).queue();

        } else if (button.getId().startsWith("ban-")) {

            String uuid = button.getId().substring("ban-".length());
            String username = Utils.getUsernameOfUuid(uuid);

            try {

                if (Whitelist.isUserWhitelisted(UUID.fromString(uuid))) {
                    Whitelist.remove(username, UUID.fromString(uuid));
                }
                Bukkit.getBanList(BanList.Type.NAME).addBan(username, "Banned by Dreamvistitor.", null, null);
                event.reply("Banned `" + username + "`.").queue();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // Disable button after use
            interaction.editButton(button.asDisabled()).queue();
        } else if (button.getId().equals("schedulerestart")) {

            ActionRow undoButton = ActionRow.of(Button.primary("schedulerestart", "Undo"));

            if (Dreamvisitor.restartScheduled) {
                Dreamvisitor.restartScheduled = false;
                event.reply("✅ Canceled server restart.").addActionRows(undoButton).queue();
            } else {
                Dreamvisitor.restartScheduled = true;
                event.reply("✅ The server will restart when there are no players online").addActionRows(undoButton).queue();
            }

        }

    }

}
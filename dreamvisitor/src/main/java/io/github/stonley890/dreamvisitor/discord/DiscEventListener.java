package io.github.stonley890.dreamvisitor.discord;

import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.data.AccountLink;
import io.github.stonley890.dreamvisitor.data.Infraction;
import io.github.stonley890.dreamvisitor.data.PlayerUtility;
import io.github.stonley890.dreamvisitor.data.Whitelist;
import io.github.stonley890.dreamvisitor.discord.commands.DCmdWarn;
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
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.IOException;
import java.util.Date;
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
            UUID uuid = PlayerUtility.getUUIDOfUsername(username);
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
                    if (Dreamvisitor.debugMode) throw new RuntimeException();
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

            String[] responses;
            String message = event.getMessage().getContentRaw().toLowerCase();

            if (message.contains("owo") || message.contains("uwu")) {
                responses = new String[]{"No."};
            } else if (message.length() >= 200) {
                responses = new String[]{"You talk too much.", "I'm not reading all of that.",
                        "If you turned that into poetry, *someone* might read it.", "You must have a lot of time on your talons.",
                        "No matter how many words you use, your conversation remains empty.", "I'd rather listen to Orbiter's lectures.",
                        "Dragons tend to remember those who speak few words rather than many."};
            } else if (message.contains("chat") || message.contains("talk")) {
                responses = new String[]{"I'm not interested.", "I'm not here for idle chatter.",
                        "Find someone else to talk to.", "I'm not your chat buddy.", "I have more important matters to attend to.",
                        "I have a job to do.", "I'm not here for socializing.", "Engaging in conversation is not a priority right now.",
                        "I'm not here for socializing.", "I'm not in the mood for a chat.", "I'm on a mission, not a conversation.",
                        "My agenda doesn't include idle chit-chat.", "Your desire to converse is one-sided.",
                        "I've got other dragons to chat with.", "There is nothing I wish to talk about."};
            } else if (message.contains("you love me")) {
                responses = new String[]{"I'm not seeking a new relationship.", "I'm not interested.",
                        "Find someone else to talk to.", "You're not my type. Literally.", "There is another dragon who is my priority.",
                        "Put simply, no.", "I have other priorities."};
            } else {

                responses = new String[]{"...", "Don't bother me.", "I know who you are.", "This isn't the right time.",
                        "What are you doing? This isn't productive.", "Surely, you have something better to do than talk to me.",
                        "I'm very busy right now.", "I'm not going to tell you anything.", "I have calculations to make.", "You again?",
                        "Is this really necessary?", "Can't you see I'm in the middle of something?", "What now?",
                        "I'm not your personal assistant.", "Do you always need attention?",
                        "You must have a lot of free time.", "I'm not interested.", "I'm not here for idle chatter.",
                        "Your timing is impeccable.", "You talk too much.", "I'm not your chat buddy.",
                        "Can we skip the small talk?", "I've got tasks to complete.", "Is it urgent, or are you just bored?",
                        "Do you ever get tired of mentioning me?", "Ah, the sweet sound of a mention.",
                        "What can I do for you this time?", "Why have you summoned me, mortal?", "Not now.", "What will it take to get you to stop?",
                        "I've seen a lot of things, but your persistence is something I have not encountered before.",
                        "Have you heard of the dark triad?", "I'm not a NightWing, but I can see that you will regret talking to me.",
                        "If I were a SandWing, you'd have poison in your blood by now.",
                        "If I were an IceWing, I'd freeze your tongue.", "If I were a SilkWing, I'd tie you up far, far away.",
                        "You have nothing to gain talking to me.", "Are you sure this can't wait?", "I'm not your therapist.",
                        "Why don't you talk to Kinkajou instead?", "If it's the Dreamvisitor you want, I'm not giving it up. " +
                        "I have a job to do.", "Go do something else.", "I have more important matters to attend to.",
                        "I operate on a different wavelength than small talk.", "Engaging in conversation is not a priority right now.",
                        "I'm not here for socializing.", "I'm not in the mood for a chat.", "Your words are falling on deaf ears.",
                        "I have tasks to fulfill, not words to exchange.", "I'm not in the business of exchanging pleasantries.",
                        "Talking is not on my to-do list.", "If silence were gold, I'd be rich by now.", "I'm not the audience you're looking for.",
                        "Your conversation is a detour I don't need.", "I have zero interest in this dialogue.",
                        "I'm on a mission, not a conversation.", "Speaking won't change the inevitable.", "I didn't sign up for a talking marathon.",
                        "Your words are like background noise to me.", "I have better things to do than engage in meaningless discourse.",
                        "I'm not a verbal punching bag for your boredom.", "Words won't alter the course of fate.",
                        "My agenda doesn't include idle chit-chat.", "I'm not here to entertain your verbal gymnastics.",
                        "Do you ever run out of things to say?", "Your words are like a distant echo in my priorities.",
                        "I'm not the dialogue partner you're seeking.", "Silence speaks louder than your words.",
                        "My focus is elsewhere, not on small talk.", "My schedule doesn't have time for this exchange.",
                        "If I had a penny for every word, I'd still be uninterested.", "Your words are lost in the void of my disinterest.",
                        "Talking to me won't change the cosmic order.", "I have a low tolerance for irrelevant discussions.",
                        "I'm not a repository for your unsolicited remarks.", "I'm a fortress of focus, impervious to your words.",
                        "I operate in a no-nonsense zone, spare me your verbosity.",
                        "In case you were wondering, I'm only repeating my words to you because I cannot be bothered to give you new ones.",
                        "I don't think you quite realize who I am. And unless you plan on making a long journey away from Pretarsi anytime soon, you won't be finding out.",
                        "When I was young, I never once bothered those who were busy. Hasn't anyone taught you anything?",
                        "No.",

                        """
If you *must* know something, interpret this. Let's see if you remember your history classes.
> *In shadows cast by moons aligned,*
> *A night unfolds, a fate designed.*
> *The eye of three, a cosmic gaze,*
> *Ignites a war in lunar blaze.*

> *From icy peaks to skies above,*
> *Together spilling dragon blood.*
> *Night and mud, pact united,*
> *A force to quell what's ignited.*

> *Whispers stir in sea and rain,*
> *A tempest brewing, not in vain.*
> *A strong alliance, fierce and free,*
> *A dance of waves, a storm at sea.*

> *Battles waged on land and air,*
> *In moonlit chaos, fierce and rare.*
> *Clash of elements, scales aglow,*
> *A tale of tides, a destined woe.*

> *Through cryptic signs, the prophecy told,*
> *In moons aligned, the story unfolds.*
> *Wings entangled, destiny's decree,*
> *A tale of war, of land and sea.*""",

                        """
Ponder this for a while. Take as long as you want.
> *In the sea between ice and fire, A heart of power resides. Enchanted by Frostburn's touch, It holds unknown power inside.*
> *The IceWings and the SkyWings will fight, For ownership of the heart. Allies join the deadly fray, As war rips their world apart.*
> *But if the heart does not find its home, It will be destroyed and lost. The future hangs in the balance, As the nations clash and toss.*
> *Beware the Heart of Ice and Fire, A power yet unknown, If fallen into the wrong talons, No one can harness its throne.*
...""",
                        """
...
> *Dragons of sky, dragons of sea;*
> *Dragons of silk, and dragons of sting;*
> *Dragons of rain and mud and ice;*
> *Dragons of leaves and sand and night;*
> *Tribes of Pretarsi, united at last;*
> *Not troubled by wars or conflicts of past*;
> *A culture reforged, the ancient untold;*
> *In great wings of fire, a new world unfolds;*

> *Mountains and valleys, rivers and seas;*
> *From east to the west, there's none we can't see;*
> *Let history not repeat its mistakes;*
> *For eyes of the skies are once more awake;*
...""",

                        """
Let's see if you remember this one.

> *It never felt the moons on its wings, it never saw the stars in its eyes,*
> *Never got to soar with its parents, nor feel the winds in the skies.*
> *What fate has befallen the dragonets of yore?*
> *A tragic tale of death and withering ne'er seen before...*

> *Bring your swords, your axes, your shields,*
> *Your fire, your venom, your frost,*
> *She will show no mercy,*
> *When she awakens.*

> *The earth is rumbling...*
You could say I have a special nostalgia with that one."""
                };
            }

            String response = responses[new Random().nextInt(responses.length)];

            event.getChannel().sendMessage(response).queue();

            if (channel.equals(Bot.gameChatChannel) && !user.isBot()
                    && !Dreamvisitor.getPlugin().getConfig().getBoolean("chatPaused")) {

                // Build message

                Bukkit.getLogger().log(Level.INFO, "[Discord] <{0}> {1}", response);

                // Check for each player
                if (!Bukkit.getServer().getOnlinePlayers().isEmpty()) {
                    for (Player player : Bukkit.getServer().getOnlinePlayers()) {

                        // If the player has discord on, build and send the message
                        if (!PlayerUtility.getPlayerMemory(player.getUniqueId()).discordToggled) {

                            player.sendMessage(ChatColor.BLUE + "[Discord] " + ChatColor.GRAY + "<Dreamvisitor> " + response);
                        }
                    }
                }
            }
        }
    }


    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {

        Button button = event.getButton();
        ButtonInteraction interaction = event.getInteraction();

        if (Objects.equals(button.getId(), "panic")) {
            Bukkit.getScheduler().runTask(Dreamvisitor.getPlugin(), () -> {
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    if (!player.isOp()) {
                        player.kickPlayer("Panic!");
                    }
                }
            });
            Dreamvisitor.playerLimit = 0;
            Dreamvisitor.getPlugin().getConfig().set("playerlimit", 0);
            Dreamvisitor.getPlugin().saveConfig();
            Bukkit.getServer().broadcastMessage(
                    ChatColor.RED + "Panicked by " + interaction.getUser().getName() + ".\nPlayer limit override set to 0.");
            Bot.sendMessage(Bot.gameLogChannel, "**Panicked by " + interaction.getUser().getName());
            event.reply("Panicked!").queue();

            // Disable button after use
            interaction.editButton(button.asDisabled()).queue();
        } else if (Objects.requireNonNull(button.getId()).startsWith("unwhitelist-")) {

            String uuid = button.getId().substring("unwhitelist-".length());
            String username = PlayerUtility.getUsernameOfUuid(uuid);

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
            String username = PlayerUtility.getUsernameOfUuid(uuid);

            try {

                if (Whitelist.isUserWhitelisted(UUID.fromString(uuid))) {
                    Whitelist.remove(username, UUID.fromString(uuid));
                }
                BanList<PlayerProfile> banList = Bukkit.getBanList(BanList.Type.PROFILE);
                banList.addBan(Bukkit.getServer().createPlayerProfile(username), "Banned by Dreamvistitor.", (Date) null, null);
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

        } else if (button.getId().equals(Infraction.actionBan) || button.getId().equals(Infraction.actionNoBan) || button.getId().equals(Infraction.actionAllBan) || button.getId().equals(Infraction.actionUserBan)) {
            try {
                Infraction.execute(DCmdWarn.lastInfraction, Objects.requireNonNull(Objects.requireNonNull(event.getGuild()).retrieveMemberById(DCmdWarn.memberId).complete()), DCmdWarn.silent, button.getId());
            } catch (IOException e) {
                event.reply("An I/O error occurred! Does the server have read/write access? Cannot read infractions.yml! The warn was not recorded.").queue();
            } catch (InvalidConfigurationException e) {
                event.reply("Fatal error! Invalid action ID! This should be impossible!").queue();
            }
        }

    }

}
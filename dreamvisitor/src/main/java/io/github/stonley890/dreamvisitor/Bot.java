package io.github.stonley890.dreamvisitor;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import org.bukkit.Bukkit;

import io.github.stonley890.dreamvisitor.discord.DiscCommandsManager;
import io.github.stonley890.dreamvisitor.discord.DiscEventListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static io.github.stonley890.dreamvisitor.Dreamvisitor.debug;
import static io.github.stonley890.dreamvisitor.Dreamvisitor.plugin;

public class Bot {

    public static final String[] TRIBE_NAMES = {"HiveWing", "IceWing", "LeafWing", "MudWing", "NightWing", "RainWing", "SandWing", "SeaWing", "SilkWing", "SkyWing"};
    public static TextChannel gameChatChannel;
    public static TextChannel gameLogChannel;
    public static TextChannel whitelistChannel;
    public static List<Role> tribeRole = new ArrayList<>();
    static JDA jda;

    private Bot() {
        throw new IllegalStateException("Utility class.");
    }

    public static void startBot() {

        // Build JDA
        String token = Dreamvisitor.getPlugin().getConfig().getString("bot-token");
        // Try to create a bot
        debug("Attempting to create a bot...");
        try {
            jda = JDABuilder.createLight(token, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                    .disableCache(CacheFlag.VOICE_STATE, CacheFlag.EMOJI, CacheFlag.STICKER)
                    .build();
            debug("Bot created.");
        } catch (LoginException e) {
            Bukkit.getLogger().severe(
                    "BOT LOGIN FAILED: You need a valid bot token in dreamvisitor/config.yml. Dreamvisitor will not work properly unless there is a valid bot token.");
            Dreamvisitor.botFailed = true;
        }

        if (!Dreamvisitor.botFailed) {
            jda.addEventListener(new DiscEventListener(), new DiscCommandsManager());

            // Wait for bot ready
            try {
                jda.awaitReady();
                debug("Bot is ready.");
                
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }

    }

    public static JDA getJda() {
        return jda;
    }

    public static void sendMessage(TextChannel channel, @Nonnull String message) {
        if (!Dreamvisitor.botFailed && channel != null) {

            if ((channel == gameLogChannel && !plugin.getConfig().getBoolean("log-console")) || channel == gameChatChannel) {
                channel.sendMessage(message.replaceAll("_", "\\\\_")).queue();
            } else {
                channel.sendMessage(message).queue();
            }
        }
    }

    public static User getUser(@NotNull String discordId) {
        return jda.retrieveUserById(discordId).complete();
    }


}

package io.github.stonley890.dreamvisitor;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
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

import static io.github.stonley890.dreamvisitor.Dreamvisitor.PLUGIN;

public class Bot {

    public static final String[] TRIBE_NAMES = {"HiveWing", "IceWing", "LeafWing", "MudWing", "NightWing", "RainWing", "SandWing", "SeaWing", "SilkWing", "SkyWing"};
    public static TextChannel gameChatChannel;
    public static TextChannel gameLogChannel;
    public static TextChannel whitelistChannel;
    public static final List<Role> tribeRole = new ArrayList<>();
    static JDA jda;

    private Bot() {
        throw new IllegalStateException("Utility class.");
    }

    public static void startBot() {

        // Build JDA
        String token = Dreamvisitor.getPlugin().getConfig().getString("bot-token");
        // Try to create a bot
        Dreamvisitor.debug("Attempting to create a bot...");
        try {

            jda = JDABuilder.createLight(token, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
                    .disableCache(CacheFlag.VOICE_STATE, CacheFlag.EMOJI, CacheFlag.STICKER)
                    .build();
            Dreamvisitor.debug("Bot created.");
            Dreamvisitor.botFailed = false;

        } catch (LoginException e) {

            Bukkit.getLogger().severe(
                    "BOT LOGIN FAILED: You need a valid bot token in dreamvisitor/config.yml. Dreamvisitor will not work properly unless there is a valid bot token.");
            Dreamvisitor.botFailed = true;

        } catch (ErrorResponseException exception) {

            if (exception.getErrorCode() == -1) {
                Bukkit.getLogger().severe("BOT LOGIN FAILED: Dreamvisitor is unable to connect to the Discord server. Dreamvisitor functionality will not work properly.");
                Dreamvisitor.botFailed = true;
            }

        }

        if (!Dreamvisitor.botFailed) {
            jda.addEventListener(new DiscEventListener(), new DiscCommandsManager());

            // Wait for bot ready
            try {
                jda.awaitReady();
                Dreamvisitor.debug("Bot is ready.");

            } catch (InterruptedException exception) {
                throw new RuntimeException();
            }
        }

    }

    public static JDA getJda() {
        return jda;
    }

    public static void sendMessage(TextChannel channel, @Nonnull String message) {
        if (!Dreamvisitor.botFailed && channel != null) {

            if (channel == gameLogChannel) {
                if (!PLUGIN.getConfig().getBoolean("log-console")) channel.sendMessage(message).queue();
            } else {
                channel.sendMessage(message).queue();
            }
        }
    }


    /**
     * Escapes all Discord markdown elements in a {@link String}.
     * @param string the {@link String} to format.
     * @return the formatted {@link String}.
     */
    public static @NotNull String escapeMarkdownFormatting(@NotNull String string) {
        return string.replaceAll("_","\\\\_").replaceAll("\\*","\\\\*").replaceAll("\\|","\\\\|");
    }
}
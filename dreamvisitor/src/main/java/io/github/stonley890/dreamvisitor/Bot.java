package io.github.stonley890.dreamvisitor;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;

import org.bukkit.Bukkit;

import io.github.stonley890.dreamvisitor.commands.discord.DiscCommandsManager;
import io.github.stonley890.dreamvisitor.commands.discord.DiscEventListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class Bot {

    private static JDA jda;

    private Bot() {
        throw new IllegalStateException("Utility class.");
    }

    public static void startBot() {

        // Build JDA
        String token = Dreamvisitor.getPlugin().getConfig().getString("bot-token");
        // Try to create a bot
        try {
            jda = JDABuilder.createLight(token, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                    .disableCache(CacheFlag.VOICE_STATE, CacheFlag.EMOJI, CacheFlag.STICKER)
                    .build();
        } catch (LoginException e) {
            Bukkit.getLogger().severe(
                    "BOT LOGIN FAILED: You need a valid bot token in dreamvisitor/config.yml. Dreamvisitor will not work properly unless there is a valid bot token. Add a token and execute /reloadbot");
            Dreamvisitor.botFailed = true;
        }

        if (!Dreamvisitor.botFailed) {
            jda.addEventListener(new DiscEventListener(), new DiscCommandsManager());

            // Wait for bot ready
            try {
                jda.awaitReady();
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
            channel.sendMessage(message).queue();
        }
    }
}

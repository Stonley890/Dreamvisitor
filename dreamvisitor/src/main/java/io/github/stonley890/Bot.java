package io.github.stonley890;

import javax.security.auth.login.LoginException;

import org.bukkit.Bukkit;

import io.github.stonley890.commands.DiscCommandsManager;
import io.github.stonley890.listeners.DiscEventListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class Bot {

    private static JDA jda;

    private Bot() {
        throw new IllegalStateException("Utility class.");
    }

    public static void startBot() {

        // Build JDA
        String token = Main.getPlugin().getConfig().getString("bot-token");
        try {
            jda = JDABuilder.createLight(token, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                    .disableCache(CacheFlag.VOICE_STATE, CacheFlag.EMOJI, CacheFlag.STICKER)
                    .build();
        } catch (LoginException e) {
            Bukkit.getLogger().severe("Bot login failed: You need a valid bot token.");
        }

        jda.addEventListener(new DiscEventListener(), new DiscCommandsManager());

        // Wait for bot ready
        try {
            jda.awaitReady();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    public static JDA getJda() {
        return jda;
    }
}

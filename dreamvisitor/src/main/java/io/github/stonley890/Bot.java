package io.github.stonley890;

import javax.security.auth.login.LoginException;

import io.github.stonley890.commands.CommandsManager;
import io.github.stonley890.listeners.EventListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class Bot {
    private static JDA jda;

    public Bot() throws LoginException {
        String token = App.getPlugin().getConfig().getString("bot-token");

        JDABuilder builder = JDABuilder.createLight(token, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT,
                GatewayIntent.GUILD_MEMBERS);
        builder.disableCache(CacheFlag.VOICE_STATE, CacheFlag.EMOJI, CacheFlag.STICKER);
        builder.setStatus(OnlineStatus.ONLINE);
        builder.setActivity(Activity.watching("Wings of Fire: The New World"));

        jda = builder.build();

        jda.addEventListener(new EventListener(), new CommandsManager());
    }

    public static JDA getJDA() {
        return jda;
    }
}
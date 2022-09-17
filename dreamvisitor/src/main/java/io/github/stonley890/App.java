package io.github.stonley890;

//import java.util.UUID;
//import java.util.logging.Logger;

import javax.security.auth.login.LoginException;

//import org.bukkit.OfflinePlayer;
//import org.bukkit.command.Command;
//import org.bukkit.command.CommandSender;
//import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
//import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.java.JavaPlugin;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;

//import com.lishid.openinv.IOpenInv;
//import com.lishid.openinv.internal.IAnySilentContainer;
//import com.lishid.openinv.internal.ISpecialEnderChest;
//import com.lishid.openinv.internal.ISpecialInventory;
//import com.lishid.openinv.internal.ISpecialPlayerInventory;

import io.github.stonley890.commands.CommandsManager;
import net.dv8tion.jda.api.entities.Guild;
//import net.dv8tion.jda.api.entities.TextChannel;

public class App extends JavaPlugin implements Listener {

    String token = "BOT_TOKEN";
    private static App plugin;

    @Override
    public void onEnable() {

        getLogger()
                .info("Dreamvisitor: A plugin created by Bog for WoF:TNW to add Discord automation.");
        getServer().getPluginManager().registerEvents(this, this);
        // Start Discord bot
        try {
            new Bot();
        } catch (LoginException e) {
            System.out.println("ERROR: Bot login failed!");
            e.printStackTrace();
        }
        try {
            Bot.getJDA().awaitReady();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Bot.getJDA().getGuilds().forEach((Guild guild) -> guild.getSystemChannel().sendMessage("Server has been started.").queue());

        plugin = this;

    }

    public static App getPlugin() {
        return plugin;
    }

//    @EventHandler
//    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
//        if (command.getName() == "offlineinv") {
//            if (sender instanceof Player) {
//                Player player = (Player) sender;
//                try {
//                    IOpenInv openinv = new IOpenInv() {
//                    };
//                    openinv.getSpecialEnderChest(player, true);
//                } catch (InstantiationException e) {
//                    e.printStackTrace();
//                }
//                return true;
//            } else {
//                return false;
//            }
//        } else {
//            return false;
//        }
//    }

    @EventHandler
    public void onPlayerChatEvent(AsyncPlayerChatEvent event) {
        // Send chat messages to Discord
        String chatMessage = "**" + event.getPlayer().getName() + "**: " + event.getMessage();
        String channelId = CommandsManager.getChatChannel();
        if (channelId != "none") {
            io.github.stonley890.Bot.getJDA().getTextChannelById(channelId).sendMessage(chatMessage).queue();
        }
        

    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        // Send player joins to Discord
        String chatMessage = "**" + event.getPlayer().getName() + " joined the game**";
        String channelId = CommandsManager.getChatChannel();
        if (channelId != "none") {
            io.github.stonley890.Bot.getJDA().getTextChannelById(channelId).sendMessage(chatMessage).queue();
        }
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        // Send player quits to Discord
        String chatMessage = "**" + event.getPlayer().getName() + " left the game**";
        String channelId = CommandsManager.getChatChannel();
        if (channelId != "none") {
            io.github.stonley890.Bot.getJDA().getTextChannelById(channelId).sendMessage(chatMessage).queue();
        }
    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        // Send death messages
        String chatMessage = "**" + event.getDeathMessage() + "**";
        String channelId = CommandsManager.getChatChannel();
        if (channelId != "none") {
            io.github.stonley890.Bot.getJDA().getTextChannelById(channelId).sendMessage(chatMessage).queue();
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("Dreamvisitor disable process.");
        Bot.getJDA().getGuilds().forEach((Guild guild) -> guild.getSystemChannel().sendMessage("Server has been shutdown.").queue());
        // Shut down bot
        Bot.getJDA().shutdown();
        
    }

}
package io.github.stonley890.dreamvisitor.listeners;

import io.github.stonley890.dreamvisitor.data.PlayerMemory;
import io.github.stonley890.dreamvisitor.data.PlayerUtility;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.jetbrains.annotations.NotNull;

public class ListenPlayerGameModeChange implements Listener {

    @EventHandler
    public void onPlayerGameModeChangeEvent(@NotNull PlayerGameModeChangeEvent event) {

        Player player = event.getPlayer();
        PlayerMemory memory = PlayerUtility.getPlayerMemory(player.getUniqueId());

        if (memory.autoinvswap) if ((player.getGameMode().equals(GameMode.SURVIVAL) && event.getNewGameMode().equals(GameMode.CREATIVE)) || (player.getGameMode().equals(GameMode.CREATIVE) && event.getNewGameMode().equals(GameMode.SURVIVAL))) Bukkit.dispatchCommand(player, "invswap");

    }

}

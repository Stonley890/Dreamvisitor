package io.github.stonley890.dreamvisitor.data;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

public class PlayerUtility {
    private static final Map<String, PlayerMemory> playerMemory = new HashMap<>();

    private PlayerUtility() {
        throw new IllegalStateException("Utility class");
    }

    public static PlayerMemory getPlayerMemory(Player player) {
        if(!playerMemory.containsKey(player.getUniqueId().toString())) {
            PlayerMemory memory = new PlayerMemory();
            playerMemory.put(player.getUniqueId().toString(), memory);
            return memory;
        }
        return playerMemory.get(player.getUniqueId().toString());
    }

    public static void setPlayerMemory(Player player, PlayerMemory memory) {
        if (memory == null) playerMemory.remove(player.getUniqueId().toString());
        else playerMemory.put(player.getUniqueId().toString(), memory);
    }
}

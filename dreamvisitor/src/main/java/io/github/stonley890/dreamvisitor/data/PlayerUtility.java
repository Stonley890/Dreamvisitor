package io.github.stonley890.dreamvisitor.data;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class PlayerUtility {
    private static final Map<String, PlayerMemory> MEMORY_MAP = new HashMap<>();

    private PlayerUtility() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Get the specified player's memory from the file.
     * Creates a new configuration with default values if one does not exist.
     * @param uuid The UUID of the player whose data to fetch.
     * @return The {@link PlayerMemory} of the given player.
     */
    private static PlayerMemory fetchPlayerMemory(UUID uuid) {
        File file = new File(Dreamvisitor.getPlayerPath(uuid));
        FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);
        return PlayerMemory.getFromFileConfig(fileConfig);
    }

    /**
     * Saves the specified player's memory to file. Does nothing if the player is not in memory.
     * @param uuid The UUID of the player whose data to save.
     */
    public static void savePlayerMemory(UUID uuid) throws IOException {
        if(MEMORY_MAP.containsKey(uuid.toString())) {
            PlayerMemory memory = getPlayerMemory(uuid);
            memory.toFileConfig().save(Dreamvisitor.getPlayerPath(uuid));
        }
    }

    /**
     * Get the specified player's memory. If it is not in memory, it will be fetched from file.
     * @param uuid The UUID of the player whose data to get.
     * @return The {@link PlayerMemory} of the given player.
     */
    public static PlayerMemory getPlayerMemory(UUID uuid) {
        // If it does not exist in memory, add it
        if(!MEMORY_MAP.containsKey(uuid.toString())) {
            PlayerMemory memory = fetchPlayerMemory(uuid);
            MEMORY_MAP.put(uuid.toString(), memory);
            return memory;
        }
        return MEMORY_MAP.get(uuid.toString());
    }

    public static void clearPlayerMemory(UUID uuid) {
        MEMORY_MAP.remove(uuid.toString());
    }

    public static void setPlayerMemory(UUID uuid, PlayerMemory memory) {
        if (memory == null) MEMORY_MAP.remove(uuid.toString());
        else MEMORY_MAP.put(uuid.toString(), memory);
    }
}

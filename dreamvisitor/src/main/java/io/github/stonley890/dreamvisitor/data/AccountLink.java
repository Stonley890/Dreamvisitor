package io.github.stonley890.dreamvisitor.data;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.Utils;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.github.stonley890.dreamvisitor.Dreamvisitor.debug;

public class AccountLink {

    static Dreamvisitor plugin = Dreamvisitor.getPlugin();
    static File accountFile = new File(plugin.getDataFolder().getPath() + "/accountLink.txt");

    static Map<UUID, Long> uuidToDiscordIdMap = new HashMap<>();
    static Map<Long, UUID> discordIdToUuidMap = new HashMap<>();

    public static void init() {
        // If file does not exist, create one
        if (!accountFile.exists()) {
            debug("accountLink.txt does not exist. Creating one now...");
            try {
                if (!accountFile.createNewFile()) {
                    Bukkit.getLogger().warning("Unable to create accountLink.txt!");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        loadFromFile();
    }

    private static void loadFromFile() {
        debug("Loading accountLink.txt");
        try (BufferedReader reader = new BufferedReader(new FileReader(accountFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    UUID uuid = UUID.fromString(Utils.formatUuid(parts[0]));
                    long discordID = Long.parseLong(parts[1]);
                    uuidToDiscordIdMap.put(uuid, discordID);
                    discordIdToUuidMap.put(discordID, uuid);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(accountFile))) {
            for (Map.Entry<UUID, Long> entry : uuidToDiscordIdMap.entrySet()) {
                UUID uuid = entry.getKey();
                long discordId = entry.getValue();
                writer.write(uuid.toString().replaceAll("-","") + ":" + discordId);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void linkAccounts(@NotNull UUID minecraftUUID, @NotNull Long discordId) {
        uuidToDiscordIdMap.put(minecraftUUID, discordId);
        discordIdToUuidMap.put(discordId, minecraftUUID);
        Bukkit.getScheduler().runTaskAsynchronously(Dreamvisitor.getPlugin(), AccountLink::saveFile);
    }

    /**
     * Get the Discord ID of the given {@link UUID}.
     * @param minecraftUUID the {@link UUID} to get the Discord ID of.
     * @return the {@code long} Discord ID.
     * @throws NullPointerException if the given {@link UUID} does not have an associated Discord ID.
     */
    public static long getDiscordId(@NotNull UUID minecraftUUID) throws NullPointerException {
        return uuidToDiscordIdMap.get(minecraftUUID);
    }

    /**
     * Get the {@link UUID} of the given Discord ID.
     * @param discordId the {@code long} Discord ID to get the {@link UUID} of.
     * @return the {@link UUID} associated with this Discord ID or {@code null} if it does not exist.
     */
    public static @Nullable UUID getUuid(long discordId) {
        return discordIdToUuidMap.get(discordId);
    }
}

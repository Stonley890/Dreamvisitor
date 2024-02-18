package io.github.stonley890.dreamvisitor.data;

import io.github.stonley890.dreamvisitor.Main;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AccountLink {

    static Main plugin = Main.getPlugin();
    static File accountFile = new File(plugin.getDataFolder().getPath() + "/accountLink.txt");

    static Map<UUID, Long> uuidToDiscordIdMap = new HashMap<>();
    static Map<Long, UUID> discordIdToUuidMap = new HashMap<>();

    public static void init() throws IOException {
        // If the file does not exist, create one
        if (!accountFile.exists()) {
            Main.debug("accountLink.txt does not exist. Creating one now...");
            if (!accountFile.createNewFile()) Bukkit.getLogger().warning("Unable to create accountLink.txt!");
        }
        loadFromFile();
    }

    private static void loadFromFile() throws IOException {
        Main.debug("Loading accountLink.txt");
        BufferedReader reader = new BufferedReader(new FileReader(accountFile));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(":");
            if (parts.length == 2) {
                UUID uuid = UUID.fromString(PlayerUtility.formatUuid(parts[0]));
                long discordID = Long.parseLong(parts[1]);
                uuidToDiscordIdMap.put(uuid, discordID);
                discordIdToUuidMap.put(discordID, uuid);
            }
        }

    }

    public static void saveFile() throws IOException {
        Main.debug("Saving...");
        BufferedWriter writer = new BufferedWriter(new FileWriter(accountFile));
        for (Map.Entry<UUID, Long> entry : uuidToDiscordIdMap.entrySet()) {
            UUID uuid = entry.getKey();
            Main.debug("UUID for this entry: " + uuid.toString());
            long discordId = entry.getValue();
            Main.debug("Discord ID for this entry: " + discordId);

            writer.write(uuid.toString().replaceAll("-","") + ":" + discordId);
            writer.newLine();
            Main.debug("Line written");
        }
        writer.close();
    }

    public static void linkAccounts(@NotNull UUID minecraftUUID, @NotNull Long discordId) {

        // remove existing values
        for (UUID uuid : uuidToDiscordIdMap.keySet()) {
            if (uuidToDiscordIdMap.get(uuid).equals(discordId)) {
                uuidToDiscordIdMap.remove(uuid);
            }
        }
        for (Long id : discordIdToUuidMap.keySet()) {
            if (discordIdToUuidMap.get(id).equals(minecraftUUID)) {
                discordIdToUuidMap.remove(discordId);
            }
        }

        // set values
        uuidToDiscordIdMap.put(minecraftUUID, discordId);
        discordIdToUuidMap.put(discordId, minecraftUUID);
        Bukkit.getScheduler().runTaskAsynchronously(Main.getPlugin(), () -> {
            try {
                AccountLink.saveFile();
            } catch (IOException e) {
                Bukkit.getLogger().severe("Unable to save accountLink.txt!");
            }
        });
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

package io.github.stonley890.dreamvisitor.data;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public class PlayerTribe {

    private static final Dreamvisitor plugin = Dreamvisitor.getPlugin();
    private static final File file = new File(plugin.getDataFolder(), "player-tribes.yml");

    /**
     * Initializes the player tribe storage.
     *
     * @throws IOException If the file could not be created.
     */
    public static void setup() throws IOException {

        if (!file.exists()) {
            Bukkit.getLogger().info("player-tribes.yml does not exist. Creating one...");
            file.createNewFile();
        }
    }

    @Nullable
    private static Tribe getPlayer(@NotNull UUID uuid) {
        try {
            YamlConfiguration configuration = new YamlConfiguration();
            configuration.load(file);
            return Tribe.valueOf(configuration.getString(uuid.toString()));
        } catch (IOException | InvalidConfigurationException e) {
            Bukkit.getLogger().severe("Unable to load " + file.getName() + "!");
            Bukkit.getPluginManager().disablePlugin(plugin);
            throw new RuntimeException();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Saves the current file configuration to disk.
     */
    private static void savePlayer(@NotNull UUID uuid, @NotNull Tribe tribe) {
        try {
            YamlConfiguration configuration = new YamlConfiguration();
            configuration.load(file);
            configuration.set(uuid.toString(), tribe.toString());
            configuration.save(file);
        } catch (IOException | InvalidConfigurationException e) {
            Bukkit.getLogger().severe("Unable to load " + file.getName() + "!");
            Bukkit.getPluginManager().disablePlugin(plugin);
            throw new RuntimeException();
        }
    }

    /**
     * Gets the stored tribe of a given player. If it does not exist, this method will try to update it.
     *
     * @param playerUuid The player UUID to search for.
     * @return The index of their tribe.
     * @throws NullPointerException The given player does not have a recorded tribe.
     */
    @Nullable
    public static Tribe getTribeOfPlayer(@NotNull UUID playerUuid) throws NullPointerException {

        Tribe tribe = getPlayer(playerUuid);

        // If not in file, try to update
        if (tribe == null) {
            try {
                updateTribeOfPlayer(playerUuid);
                tribe = getPlayer(playerUuid);
            } catch (Exception e) {
                return tribe;
            }
        }

        return tribe;

    }

    /**
     * Attempts to update a player's recorded tribe by team.
     * If not found on a team, tags will be checked if player is online.
     * @param uuid the UUID of the player to update, online or offline.
     * @throws NullPointerException given player does not have a valid team or tag associated with a tribe.
     */
    public static void updateTribeOfPlayer(@NotNull UUID uuid) throws NullPointerException {

        boolean online;
        Player player = Bukkit.getPlayer(uuid);

        online = player != null;

        Scoreboard scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard();

        int playerTribe;

        String username;
        if (online) username = player.getName();
        else username = PlayerUtility.getUsernameOfUuid(uuid);

        if (username == null) throw new NullPointerException("Player is null");

        // Check by team
        Dreamvisitor.debug("Checking by team...");
        Tribe[] tribes = Tribe.values();
        for (int i = 0; i < tribes.length; i++) {
            Tribe tribe = tribes[i];
            Team team = scoreboard.getTeam(tribe.getTeamName());
            if (team != null && team.hasEntry(username)) {
                Dreamvisitor.debug("Found tribe " + i);
                playerTribe = i;
                savePlayer(uuid, tribes[playerTribe]);
                return;
            }
        }

        if (online) {
            // If no matching team, check by tags
            Dreamvisitor.debug("Checking by tag...");
            for (int i = 0; i < tribes.length; i++) {
                Tribe tribe = tribes[i];
                if (player.getScoreboardTags().contains(tribe.getTeamName())) {
                    Dreamvisitor.debug("Found tag " + i);
                    playerTribe = i;
                    savePlayer(uuid, tribes[playerTribe]);
                    return;
                }
            }
        }

        throw new NullPointerException("Given player does nozt have a valid team or tag associated with a tribe!");
    }
}

package io.github.stonley890.dreamvisitor.data;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Mail {

    static final File file = new File(Dreamvisitor.getPlugin().getDataFolder().getPath() + "/mail.yml");
    static List<Player> activeDeliverers = new ArrayList<>();

    public static void init() throws IOException {
        // If the file does not exist, create one
        if (!file.exists()) {
            Dreamvisitor.debug(file.getName() + " does not exist. Creating one now...");
            try {
                if (!file.createNewFile())
                    throw new IOException("The existence of " + file.getName() + " cannot be verified!", null);
            } catch (IOException e) {
                throw new IOException("Dreamvisitor tried to create " + file.getName() + ", but it cannot be read/written! Does the server have read/write access?", e);
            }
        }
    }

    @NotNull
    private static YamlConfiguration getConfig() {
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(file);
        } catch (IOException e) {
            Bukkit.getLogger().severe(file.getName() + " cannot be read! Does the server have read/write access? " + e.getMessage());
            Bukkit.getPluginManager().disablePlugin(Dreamvisitor.getPlugin());
        } catch (InvalidConfigurationException e) {
            Bukkit.getLogger().severe(file.getName() + " is not a valid configuration! Is it formatted correctly? " + e.getMessage());
            Bukkit.getPluginManager().disablePlugin(Dreamvisitor.getPlugin());
        }
        return config;
    }

    private static void saveConfig(@NotNull YamlConfiguration config) {
        try {
            config.save(file);
        } catch (IOException e) {
            Bukkit.getLogger().severe( file.getName() + " cannot be written! Does the server have read/write access? " + e.getMessage() + "\nHere is the data that was not saved:\n" + config.saveToString());
            Bukkit.getPluginManager().disablePlugin(Dreamvisitor.getPlugin());
        }
    }

    @NotNull
    public static List<MailLocation> getLocations() {
        List<Map<?, ?>> locations = getConfig().getMapList("locations");
        List<MailLocation> parsedLocations = new ArrayList<>();
        for (Map<?, ?> location : locations) {
            parsedLocations.add(MailLocation.deserialize((Map<String, Object>) location));
        }
        return parsedLocations;
    }

    @Nullable
    public static MailLocation getLocationByName(String name) {
        for (MailLocation location : getLocations()) {
            if (location.getName().equals(name)) return location;
        }
        return null;
    }

    @Nullable
    public static MailLocation getNearestLocation(Location queryLocation) {
        double shortestDistance = 50000.0;
        MailLocation nearest = null;
        for (MailLocation location : getLocations()) {
            if (!Objects.equals(location.getLocation().getWorld(), queryLocation.getWorld())) continue;
            double distance = location.getLocation().distance(queryLocation);
            if (distance < shortestDistance) nearest = location;
        }
        return nearest;
    }

    public static class MailLocation implements ConfigurationSerializable {

        @NotNull private Location location;
        @NotNull private String name;
        private int weight;

        public MailLocation(@NotNull Location location, @NotNull String name, int weight) {
            this.location = location;
            this.name = name;
            this.weight = weight;
        }

        @NotNull
        public Location getLocation() {
            return location;
        }

        public int getWeight() {
            return weight;
        }

        @NotNull
        public String getName() {
            return name;
        }

        public void setName(@NotNull String name) {
            this.name = name;
        }

        public void setLocation(@NotNull Location location) {
            this.location = location;
        }

        public void setWeight(int weight) {
            this.weight = weight;
        }

        @NotNull
        @Contract("_ -> new")
        public static MailLocation deserialize(@NotNull Map<String, Object> map) {
            return new MailLocation((Location) map.get("location"), (String) map.get("name"), (Integer) map.get("weight"));
        }

        @NotNull
        @Override
        public Map<String, Object> serialize() {
            return Map.of("location", location, "name", name, "weight", weight);
        }
    }

    public static class Deliverer {

        private MailLocation startLoc;
        private MailLocation endLoc;
        private LocalDateTime startTime;

    }

}

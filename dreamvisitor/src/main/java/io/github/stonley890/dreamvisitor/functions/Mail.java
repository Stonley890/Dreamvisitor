package io.github.stonley890.dreamvisitor.functions;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.data.Tribe;
import io.github.stonley890.dreamvisitor.data.TribeUtil;
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class Mail {

    static final File file = new File(Dreamvisitor.getPlugin().getDataFolder().getPath() + "/mail.yml");
    static List<Deliverer> activeDeliverers = new ArrayList<>();
    static double maxDistance;

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
        calculateMaxDistance();
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

    public static List<Deliverer> getDeliverers() {
        return activeDeliverers;
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

    public static void saveLocations(@NotNull List<MailLocation> mailLocations) {
        YamlConfiguration config = getConfig();
        List<Map<String, Object>> serializedMailLocations = new ArrayList<>();
        for (MailLocation mailLocation : mailLocations) {
            serializedMailLocations.add(mailLocation.serialize());
        }
        config.set("locations", serializedMailLocations);
        saveConfig(config);
    }

    /**
     * Get a {@link MailLocation} by its name.
     * @param name The name to search for.
     * @return The first {@link MailLocation} found with that name, or null if none are found.
     */
    @Nullable
    public static MailLocation getLocationByName(String name) {
        for (MailLocation location : getLocations()) {
            if (location.getName().equals(name)) return location;
        }
        return null;
    }

    /**
     * Get the {@link MailLocation} nearest to a given {@link Location} (within 50000 blocks).
     * @param queryLocation Any {@link Location} to query.
     * @return The closest {@link MailLocation} in the same dimension, or null if none are found.
     */
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

    public static double getMaxDistance() {
        return maxDistance;
    }

    @NotNull
    @Contract("_ -> param1")
    private static Location pantalaTransform(@NotNull Location location) {
        location.setWorld(Bukkit.getWorlds().get(0));
        location.add(-6015, 0, 0);
        return location;
    }

    public static void calculateMaxDistance() {
        List<MailLocation> locations = getLocations();
        double maxDistance = 0;
        for (MailLocation location : locations) {
            for (MailLocation mailLocation : locations) {
                Location location1 = location.location;
                Location location2 = mailLocation.location;
                if (Objects.requireNonNull(location2.getWorld()).getName().contains("pantala")) location2 = pantalaTransform(mailLocation.location.clone());
                double distance = location1.distance(location2);
                if (distance > maxDistance) maxDistance = distance;
            }
        }
        Mail.maxDistance = maxDistance;
    }

    public static double getDistanceWeightMultiplier() {
        return Dreamvisitor.getPlugin().getConfig().getDouble("mailDeliveryLocationSelectionDistanceWeightMultiplier");
    }

    public static double getDistanceRewardMultiplier() {
        return Dreamvisitor.getPlugin().getConfig().getDouble("mailDistanceToRewardMultiplier");
    }

    @NotNull
    public static MailLocation chooseDeliveryLocation(@NotNull MailLocation startPos) throws InvalidConfigurationException {
        List<MailLocation> locations = getLocations();
        locations.remove(startPos);
        if (locations.isEmpty()) throw new InvalidConfigurationException("There aren't enough mail locations!");

        // Compute the total weight of all items together.
        // This can be skipped of course if sum is already 1.
        double totalWeight = 0.0;
        for (MailLocation location : locations) {
            totalWeight += location.getWeight();
            double distance = MailLocation.getDistance(startPos, location);
            double distanceWeight = distance / maxDistance; // get distance ratio
            totalWeight += distanceWeight * -1 * getDistanceWeightMultiplier();
        }

        // Now choose a random item.
        int index = 0;
        MailLocation mailLocation = locations.get(index);
        for (double r = Math.random() * totalWeight; index < locations.size() - 1; ++index) {
            double weight = mailLocation.getWeight();
            weight += (MailLocation.getDistance(startPos, mailLocation) / maxDistance) * -1 * getDistanceWeightMultiplier();
            r -= weight;
            if (r <= 0.0) break;
        }
        return mailLocation;
    }

    @NotNull
    public static String chooseName(@NotNull MailLocation startLoc, @NotNull MailLocation endLoc) {

        String placeholder = "Dreamvisitor";

        InputStream namesStream = Dreamvisitor.getPlugin().getResource("names.yml");
        if (namesStream == null) return placeholder;

        Random random = new Random();
        List<String> nameList;
        YamlConfiguration nameConfig = new YamlConfiguration();
        InputStreamReader inputStreamReader = new InputStreamReader(namesStream);
        try {
            nameConfig.load(inputStreamReader);
        } catch (IOException | InvalidConfigurationException e) {
            Bukkit.getLogger().warning("There was a problem accessing embedded names.yml: " + e.getMessage());
            return placeholder;
        }
        int roll = random.nextInt(100);
        if (roll < 10) {

            int tribeIndex = random.nextInt(10);
            Tribe tribe = TribeUtil.tribes[tribeIndex];

            nameList = nameConfig.getStringList(tribe.getTeamName());

        } else if (roll < 55) {
            nameList = nameConfig.getStringList(startLoc.homeTribe.getTeamName());
        } else {
            nameList = nameConfig.getStringList(endLoc.homeTribe.getTeamName());
        }

        int name = random.nextInt(nameList.size());
        return nameList.get(name);

    }

    public static void saveLocation(MailLocation mailLocation) {
        List<MailLocation> locations = getLocations();
        for (MailLocation location : locations) {
            if (location.name.equals(mailLocation.name)) {
                locations.remove(location);
                break;
            }
        }
        locations.add(mailLocation);
        saveLocations(locations);
    }

    public static void removeLocation(MailLocation location) {
        List<MailLocation> locations = getLocations();
        for (MailLocation mailLocation : locations) {
            if (mailLocation.name.equals(location.name)) {
                locations.remove(mailLocation);
                break;
            }
        }
        saveLocations(locations);
    }

    public static class MailLocation implements ConfigurationSerializable {

        /**
         * The in-game location.
         */
        @NotNull private Location location;
        /**
         * The display name/title of this location that is displayed to players. This should be unique.
         */
        @NotNull private String name;
        /**
         * The weight/importance of this location. Central locations should be weighted more heavily.
         */
        private int weight;
        /**
         * The home tribe of this location. Mail will more like be addressed to/from this tribe.
         */
        private Tribe homeTribe;

        public MailLocation(@NotNull Location location, @NotNull String name, int weight, @NotNull Tribe homeTribe) {
            this.location = location;
            this.name = name;
            this.weight = weight;
            this.homeTribe = homeTribe;
        }

        public static double getDistance(@NotNull MailLocation location1, @NotNull MailLocation location2) {
            if (Objects.equals(location1.location.getWorld(), location2.location.getWorld())) return location1.location.distance(location2.location);

            if (Objects.requireNonNull(location1.location.getWorld()).getName().contains("pantala")) return pantalaTransform(location1.location).distance(location2.location);
            else if (Objects.requireNonNull(location2.location.getWorld()).getName().contains("pantala")) return pantalaTransform(location2.location).distance(location1.location);
            return location1.location.distance(location2.location);
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

        public Tribe getHomeTribe() {
            return homeTribe;
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

        public void setHomeTribe(@NotNull Tribe homeTribe) {
            this.homeTribe = homeTribe;
        }

        @NotNull
        @Contract("_ -> new")
        public static MailLocation deserialize(@NotNull Map<String, Object> map) {
            return new MailLocation((Location) map.get("location"), (String) map.get("name"), (Integer) map.get("weight"), Tribe.valueOf((String) map.get("home")));
        }

        @NotNull
        @Override
        public Map<String, Object> serialize() {
            return Map.of("location", location, "name", name, "weight", weight, "home", homeTribe.toString());
        }
    }

    public static class Deliverer {

        @NotNull private final Player player;
        @NotNull private final MailLocation startLoc;
        @NotNull private final MailLocation endLoc;
        @Nullable private LocalDateTime startTime;

        public Deliverer(@NotNull Player player, @NotNull MailLocation startLoc, @NotNull MailLocation endLoc) {
            this.player = player;
            this.startLoc = startLoc;
            this.endLoc = endLoc;
        }

        public boolean started() {
            return startTime != null;
        }

        @NotNull
        public Player getPlayer() {
            return player;
        }

        @NotNull
        public MailLocation getStartLoc() {
            return startLoc;
        }

        @NotNull
        public MailLocation getEndLoc() {
            return endLoc;
        }

        /**
         * Starts the time for this Deliverer.
         * This will throw {@link UnsupportedOperationException} if already started.
         */
        public void start() {
            if (startTime != null) throw new UnsupportedOperationException("Already started time!");
            startTime = LocalDateTime.now();
        }

        /**
         * The time this deliverer has spent delivering their parcel.
         * @return The time spent or zero if not started.
         */
        public Duration timeSpent() {
            if (startTime == null) return Duration.ZERO;
            return Duration.between(startTime, LocalDateTime.now());
        }

        /**
         * Get the expected reward for the completion of this delivery.
         * @return a double representing the reward amount.
         */
        public double getReward() {
            return Math.round((MailLocation.getDistance(startLoc, endLoc) * getDistanceRewardMultiplier())/10.0) * 10;
        }
    }

}

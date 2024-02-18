package io.github.stonley890.dreamvisitor.data;

import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import spark.Spark;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

public class Whitelist {

    private static @NotNull JSONArray get() throws IOException {

        // Access whitelist.json file
        Main.debug("Trying to access whitelist file");
        String whitelistPath = Bukkit.getServer().getWorldContainer().getPath() + "/whitelist.json";
        // Parse whitelist.json to a string list
        List<String> lines = Files.readAllLines(new File(whitelistPath).toPath());
        Main.debug("Success");

        // Format the string list to StringBuilder
        StringBuilder fileString = new StringBuilder();
        for (String line : lines) {
            fileString.append(line);
        }
        Main.debug("Strings joined to StringBuilder");

        // Format string to JSONArray
        JSONArray whitelist = new JSONArray(fileString.toString());
        Main.debug("String Builder parsed as JSON");
        return whitelist;
    }

    public static boolean isUserWhitelisted(@NotNull UUID uuid) throws IOException {
        JSONArray whitelist = get();
        for (Object entry : whitelist) {
            JSONObject object = (JSONObject) entry;
            if (object.get("uuid").equals(uuid.toString())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds the specified details to the whitelist file and reloads the whitelist.
     * @param username The username to add.
     * @param uuid The {@link UUID} to add.
     * @throws IOException If there is an issue accessing the whitelist file.
     */
    public static void add(@NotNull String username, @NotNull UUID uuid) throws IOException {

        Main.debug("Adding " + username + " to the whitelist.");

        JSONArray whitelist = get();

        // Create entry
        Main.debug("Creating entry...");
        JSONObject whitelistEntry = new JSONObject();
        whitelistEntry.put("uuid", uuid.toString());
        whitelistEntry.put("name", username);

        // Add to whitelist.json
        Main.debug("Adding to JSON...");
        whitelist.put(whitelistEntry);

        // Write to whitelist.json file
        Main.debug("Attempting to write to file...");

        Files.writeString(new File(Bukkit.getServer().getWorldContainer().getPath() + "/whitelist.json").toPath(), whitelist.toString(4));
        Main.debug("Success.");

        // reload whitelist
        Main.debug("Reloading whitelist");
        Bukkit.reloadWhitelist();
        Main.debug("Whitelist reloaded");
    }

    /**
     * Removes the specified details from the whitelist file and reloads the whitelist.
     * @param username The username to remove.
     * @param uuid The {@link UUID} to remove.
     * @throws IOException If there is an issue accessing the whitelist file.
     */
    public static void remove(@NotNull String username, @NotNull UUID uuid) throws IOException {

        Main.debug("Removing " + username + " to the whitelist.");

        JSONArray whitelist = get();

        // Search for and remove entry
        for (int i = 0; i < whitelist.length(); i++) {
            JSONObject object = (JSONObject) whitelist.get(i);

            Main.debug("Checking " + object.get("uuid") + " with " + uuid);

            if (object.get("uuid").equals(uuid.toString())) {

                Main.debug("Found match! " + whitelist.remove(i));
            }
        }

        // Write to whitelist.json file
        Main.debug("Attempting to write to file...");

        Files.writeString(new File(Bukkit.getServer().getWorldContainer().getPath() + "/whitelist.json").toPath(), whitelist.toString(4));
        Main.debug("Success.");

        // reload whitelist
        Main.debug("Reloading whitelist");
        Bukkit.reloadWhitelist();
        Main.debug("Whitelist reloaded");
    }

    public static void startWeb() {

        // Web whitelist server

        Spark.port(4567); // Choose a port for your API
        Spark.before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Request-Method", "*");
            response.header("Access-Control-Allow-Headers", "*");
        });
        Spark.post("/process-username", (request, response) -> {
            String username = request.queryParams("username");

            Main.debug("Username from web form: " + username);

            // Process the username
            boolean success = processUsername(username);

            Main.debug("Processed. Success: " + success);

            // Send a response back to the web page
            Main.debug("response.header");
            response.type("application/json");
            Main.debug("response.type");
            return "{\"success\": " + success + "}";
        });
    }

    public static void stopWeb() {
        Spark.stop();
    }

    private static boolean processUsername(@NotNull String username) throws IOException {

        // Check for valid UUID
        Main.debug("Checking for valid UUID");
        UUID uuid = PlayerUtility.getUUIDOfUsername(username);
        if (uuid == null) {
            // username does not exist alert
            Main.debug("Username does not exist.");
            Main.debug("Failed whitelist.");
        } else {

            Main.debug("Got UUID");

            // No account to link

            // Check if already whitelisted
            Main.debug("Is user already whitelisted?");

            if (isUserWhitelisted(uuid)) {
                Main.debug("Already whitelisted.");
                Main.debug("Resolved.");

                return true;
            } else {
                Main.debug("Player is not whitelisted.");

                add(username, uuid);

                // success message
                Main.debug("Success.");

                report(username, uuid, null);

                return true;
            }
        }
        return false;
    }

    /**
     * Report a whitelist to the system log channel.
     * @param username the username that is being whitelisted.
     * @param source the {@link User} (or {@code null} if by web) that caused this whitelist.
     */
    public static void report(String username, UUID uuid, User source) {

        String sourceName = "web whitelist";
        if (source != null) sourceName = source.getName();

        TextChannel systemChannel = Bot.gameLogChannel.getGuild().getSystemChannel();
        if (systemChannel != null) {
            EmbedBuilder logEmbed = getEmbedBuilder(username, source, sourceName);

            ActionRow buttons = ActionRow.of(Button.secondary("unwhitelist-" + uuid, "Unwhitelist"), Button.danger("ban-" + uuid, "Ban"));
            systemChannel.sendMessageEmbeds(logEmbed.build()).setActionRows(buttons).queue();
        }
    }

    @NotNull
    private static EmbedBuilder getEmbedBuilder(String username, User source, String sourceName) {
        EmbedBuilder logEmbed = new EmbedBuilder();
        logEmbed.setTitle("Whitelisted " + username + " from " + sourceName);

        if (source != null) logEmbed.setDescription(source.getAsMention() + " added " + username + " to the whitelist with Dreamvisitor. Use the buttons below to undo this action or `/link <username> <member>` to link this user to a different member.");
        else logEmbed.setDescription("Added " + username + " to the whitelist via the web whitelist. Use the buttons below to undo this action or `/link <username> <member>` to link this user to a Discord member.");
        return logEmbed;
    }

}
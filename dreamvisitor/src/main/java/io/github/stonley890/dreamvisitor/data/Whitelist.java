package io.github.stonley890.dreamvisitor.data;

import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.Utils;
import net.dv8tion.jda.api.entities.TextChannel;
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
        Dreamvisitor.debug("Trying to access whitelist file");
        String whitelistPath = Bukkit.getServer().getWorldContainer().getPath() + "/whitelist.json";
        // Parse whitelist.json to a string list
        List<String> lines = Files.readAllLines(new File(whitelistPath).toPath());
        Dreamvisitor.debug("Success");

        // Format the string list to StringBuilder
        StringBuilder fileString = new StringBuilder();
        for (String line : lines) {
            fileString.append(line);
        }
        Dreamvisitor.debug("Strings joined to StringBuilder");

        // Format string to JSONArray
        JSONArray whitelist = new JSONArray(fileString.toString());
        Dreamvisitor.debug("String Builder parsed as JSON");
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

        Dreamvisitor.debug("Adding " + username + " to the whitelist.");

        JSONArray whitelist = get();

        // Create entry
        Dreamvisitor.debug("Creating entry...");
        JSONObject whitelistEntry = new JSONObject();
        whitelistEntry.put("uuid", uuid.toString());
        whitelistEntry.put("name", username);

        // Add to whitelist.json
        Dreamvisitor.debug("Adding to JSON...");
        whitelist.put(whitelistEntry);

        // Write to whitelist.json file
        Dreamvisitor.debug("Attempting to write to file...");

        Files.writeString(new File(Bukkit.getServer().getWorldContainer().getPath() + "/whitelist.json").toPath(), whitelist.toString(4));
        Dreamvisitor.debug("Success.");

        // reload whitelist
        Dreamvisitor.debug("Reloading whitelist");
        Bukkit.reloadWhitelist();
        Dreamvisitor.debug("Whitelist reloaded");
    }

    /**
     * Removes the specified details from the whitelist file and reloads the whitelist.
     * @param username The username to remove.
     * @param uuid The {@link UUID} to remove.
     * @throws IOException If there is an issue accessing the whitelist file.
     */
    public static void remove(@NotNull String username, @NotNull UUID uuid) throws IOException {

        Dreamvisitor.debug("Removing " + username + " to the whitelist.");

        JSONArray whitelist = get();

        // Search for and remove entry
        for (int i = 0; i < whitelist.length(); i++) {
            JSONObject object = (JSONObject) whitelist.get(i);

            Dreamvisitor.debug("Checking " + object.get("uuid") + " with " + uuid);

            if (object.get("uuid").equals(uuid.toString())) {

                Dreamvisitor.debug("Found match! " + whitelist.remove(i));
            }
        }

        // Write to whitelist.json file
        Dreamvisitor.debug("Attempting to write to file...");

        Files.writeString(new File(Bukkit.getServer().getWorldContainer().getPath() + "/whitelist.json").toPath(), whitelist.toString(4));
        Dreamvisitor.debug("Success.");

        // reload whitelist
        Dreamvisitor.debug("Reloading whitelist");
        Bukkit.reloadWhitelist();
        Dreamvisitor.debug("Whitelist reloaded");
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

            Dreamvisitor.debug("Username from web form: " + username);

            // Process the username
            boolean success = processUsername(username);

            Dreamvisitor.debug("Processed. Success: " + success);

            // Send a response back to the web page
            Dreamvisitor.debug("response.header");
            response.type("application/json");
            Dreamvisitor.debug("response.type");
            return "{\"success\": " + success + "}";
        });
    }

    public static void stopWeb() {
        Spark.stop();
    }

    private static boolean processUsername(@NotNull String username) throws IOException {

        // Check for valid UUID
        Dreamvisitor.debug("Checking for valid UUID");
        UUID uuid = Utils.getUUIDOfUsername(username);
        if (uuid == null) {
            // username does not exist alert
            Dreamvisitor.debug("Username does not exist.");
            Dreamvisitor.debug("Failed whitelist.");
        } else {

            Dreamvisitor.debug("Got UUID");

            // No account to link

            // Check if already whitelisted
            Dreamvisitor.debug("Is user already whitelisted?");

            if (isUserWhitelisted(uuid)) {
                Dreamvisitor.debug("Already whitelisted.");
                Dreamvisitor.debug("Resolved.");

                return true;
            } else {
                Dreamvisitor.debug("Player is not whitelisted.");

                add(username, uuid);

                // success message
                Dreamvisitor.debug("Success.");

                TextChannel systemChannel = Bot.gameLogChannel.getGuild().getSystemChannel();
                if (systemChannel != null) systemChannel.sendMessage("Whitelisted `" + username + "` from web whitelist. Use **/unwhitelist <username>** to undo this action or **/toggleweb** to disable web whitelisting.").queue();

                return true;
            }
        }
        return false;
    }

}
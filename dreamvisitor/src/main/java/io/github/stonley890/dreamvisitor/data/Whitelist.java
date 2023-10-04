package io.github.stonley890.dreamvisitor.data;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import org.bukkit.Bukkit;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

public class Whitelist {

    private static JSONArray get() throws IOException {

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

    public static boolean isUserWhitelisted(UUID uuid) throws IOException {
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
    public static void add(String username, UUID uuid) throws IOException {
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

}
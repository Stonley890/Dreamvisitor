package io.github.stonley890.dreamvisitor.functions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

public class ResourcePack {

    public static void update() throws IOException, NoSuchAlgorithmException {
        URL resourcePackURL;
        try {
            resourcePackURL = ResourcePack.getLatestReleaseURL();
            Dreamvisitor.debug("Found URL.");
        } catch (IOException e) {
            throw new IOException(Dreamvisitor.TITLE + " was unable to find the resource pack URL: " + e.getMessage());
        }

        Dreamvisitor.debug("Attempting to download the resource pack.");
        HttpURLConnection connection = (HttpURLConnection) resourcePackURL.openConnection();
        connection.setRequestMethod("GET");
        connection.setReadTimeout(10000); // timeout
        connection.connect();

        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            Dreamvisitor.debug("Generating hash.");
            InputStream is = connection.getInputStream();
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = is.read(buffer)) != -1) {
                sha1.update(buffer, 0, bytesRead);
            }

            byte[] hashBytes = sha1.digest();
            StringBuilder hash = new StringBuilder();

            for (byte hashByte : hashBytes) {
                hash.append(Integer.toString((hashByte & 0xff) + 0x100, 16).substring(1));
            }

            String newHash = hash.toString();
            Dreamvisitor.debug("New hash: " + newHash);
            Dreamvisitor.debug("Writing changes to server.properties.");

            Properties properties = getProperties(resourcePackURL, newHash);

            // Save the updated properties back to the file with UTF-8 encoding
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("server.properties"), StandardCharsets.UTF_8));
            properties.store(writer, null);
            writer.close();

            Dreamvisitor.debug("Success.");
        }
    }

    private static @NotNull Properties getProperties(URL resourcePackURL, String newHash) throws IOException {
        // Load the server.properties file with UTF-8 encoding
        Properties properties = new Properties();
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("server.properties"), StandardCharsets.UTF_8));
        properties.load(reader);
        reader.close();

        properties.setProperty("resource-pack", String.valueOf(resourcePackURL)); // Update the resource pack property
        properties.setProperty("resource-pack-sha1", newHash); // Update the hash property
        return properties;
    }

    @NotNull
    public static URL getLatestReleaseURL() throws IOException {
        // Create a URL object
        URL url = new URL("https://api.github.com/repos/" + Dreamvisitor.getPlugin().getConfig().getString("resourcePackRepo") + "/releases/latest");
        Dreamvisitor.debug("Finding latest artifact at " + url);

        // Open a connection to the URL
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");

        // Read the response
        Dreamvisitor.debug("Sending request.");
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        Dreamvisitor.debug("Parsing response.");
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }

        // Close connections
        in.close();
        connection.disconnect();

        // Parse the JSON response using Gson
        Dreamvisitor.debug("Converting to JSON.");
        JsonElement jsonElement = JsonParser.parseString(content.toString());
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        // Access the assets array
        Dreamvisitor.debug("Locating assets[0].browser_download_url");
        JsonArray assets = jsonObject.getAsJsonArray("assets");
        if (!assets.isEmpty()) {
            // Get the first asset's browser_download_url
            JsonObject firstAsset = assets.get(0).getAsJsonObject();

            return new URL(firstAsset.get("browser_download_url").getAsString());
        } else {
            throw new FileNotFoundException();
        }
    }

    @NotNull
    @Contract(" -> new")
    public static URL getCurrentURL() throws IOException {
        // Load the server.properties file with UTF-8 encoding
        Properties properties = new Properties();
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("server.properties"), StandardCharsets.UTF_8));
        properties.load(reader);
        reader.close();

        String property = properties.getProperty("resource-pack");
        return new URL(property);
    }
}

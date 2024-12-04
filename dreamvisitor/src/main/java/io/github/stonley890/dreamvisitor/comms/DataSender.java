package io.github.stonley890.dreamvisitor.comms;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.data.Tribe;
import io.github.stonley890.dreamvisitor.functions.ResourcePack;
import io.github.stonley890.dreamvisitor.functions.ScheduleRestart;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

public class DataSender {

    public static final String STATS = "stats";
    public static final String CONFIG = "config";
    public static final String USERS = "users";
    public static final String BOT = "bot";

    public static void sendPlayerCount() {
        sendPost(STATS, new JSONObject().put("playerCount", Bukkit.getOnlinePlayers().size()));
    }

    public static void sendMaxPlayerCount() {
        sendPost(STATS, new JSONObject().put("maxPlayerCount", Dreamvisitor.playerLimit));
    }

    public static void sendRestartStatus() {
        sendPost(STATS, new JSONObject().put("restart", ScheduleRestart.isRestartScheduled()));
    }

    public static void sendResourcePack() throws IOException {
        boolean newResourcePack;
        newResourcePack = !ResourcePack.getLatestReleaseURL().equals(ResourcePack.getCurrentURL());
        sendPost(CONFIG, new JSONObject().put("newResourcePack", newResourcePack));
    }

    public static void sendConsoleLine(String line) {
        sendPost(STATS, new JSONObject().put("consoleLine", line));
    }

    public static void sendChatPause() {
        sendPost(CONFIG, new JSONObject().put("chatPause", Dreamvisitor.chatPaused));
    }

    public static void sendPlayerTribe(@NotNull UUID uuid, @Nullable Tribe tribe) {
        sendPost(USERS, new JSONObject().put("playerTribe", uuid).put("tribe", tribe));
    }

    public static void sendPost(String context, JSONObject data) {
        try {
            // Construct the URL for the web app's API
            URL url = new URL(Dreamvisitor.getPlugin().getConfig().getString("core-url") + "/api/" + context);

            // Open a connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");

            // Write
            OutputStream outputStream = connection.getOutputStream();
            byte[] input = data.toString().getBytes(StandardCharsets.UTF_8);
            outputStream.write(input, 0, input.length);

            // Read the response from the web app
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            // Print or process the response from the web app
            Dreamvisitor.getPlugin().getLogger().info("Response from web app: " + content);

        } catch (Exception e) {
            Dreamvisitor.debug("Could not send data to web app.");
        }
    }

    public static void sendGet(String context, String data) {
        try {
            // Construct the URL for the web app's API
            URL url = new URL(Dreamvisitor.getPlugin().getConfig().getString("core-url") + "/api/" + context + "?data=" + data);

            // Open a connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET"); // Use POST for sensitive data in production

            // Read the response from the web app
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            // Print or process the response from the web app
            Dreamvisitor.getPlugin().getLogger().info("Response from web app: " + content);

        } catch (Exception e) {
            Dreamvisitor.debug("Could not send data to web app.");
        }
    }

    public static void requestDataFromWebApp() {
        try {
            // Construct the URL for the web app's API
            URL url = new URL(Dreamvisitor.getPlugin().getConfig().getString("core-url") + "/api/request");

            // Open a connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Read the response from the web app
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            // Print or process the data received from the web app
            System.out.println("Data from web app: " + content);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

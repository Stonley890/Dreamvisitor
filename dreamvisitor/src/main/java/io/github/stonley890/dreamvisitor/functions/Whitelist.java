package io.github.stonley890.dreamvisitor.functions;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.data.PlayerUtility;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.profile.PlayerProfile;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import spark.Spark;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Whitelist extends ListenerAdapter {

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

    public static void startWeb(int port) {

        // Web whitelist server
        String websiteUrl = Dreamvisitor.getPlugin().getConfig().getString("website-url");

        Spark.port(port); // Choose a port for your API
        Spark.before((request, response) -> {
            response.header("Access-Control-Allow-Origin", websiteUrl);
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
        UUID uuid = PlayerUtility.getUUIDOfUsername(username);
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

        // TODO: Send whitelist embed
//        TextChannel systemChannel = Bot.getGameLogChannel().getGuild().getSystemChannel();
//        if (systemChannel != null) {
//            EmbedBuilder logEmbed = getEmbedBuilder(username, source, sourceName);
//
//            Button ban = Button.danger("ban-" + uuid, "Ban");
//            Button unwhitelist = Button.secondary("unwhitelist-" + uuid, "Unwhitelist");
//            systemChannel.sendMessageEmbeds(logEmbed.build()).setActionRow(ban, unwhitelist).queue();
//        }
    }

    @NotNull
    private static EmbedBuilder getEmbedBuilder(String username, User source, String sourceName) {
        EmbedBuilder logEmbed = new EmbedBuilder();
        logEmbed.setTitle("Whitelisted " + username + " from " + sourceName);

        if (source != null) logEmbed.setDescription(source.getAsMention() + " added " + username + " to the whitelist with Dreamvisitor. Use the buttons below to undo this action or `/link <username> <member>` to link this user to a different member.");
        else logEmbed.setDescription("Added " + username + " to the whitelist via the web whitelist. Use the buttons below to undo this action or `/link <username> <member>` to link this user to a Discord member.");
        return logEmbed;
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (Objects.requireNonNull(event.getButton().getId()).startsWith("unwhitelist")) {
            String uuid = Objects.requireNonNull(event.getButton().getId()).substring("unwhitelist-".length());
            String username = PlayerUtility.getUsernameOfUuid(uuid);

            try {
                if (Whitelist.isUserWhitelisted(UUID.fromString(uuid))) {
                    assert username != null;
                    Whitelist.remove(username, UUID.fromString(uuid));
                    event.reply("Removed `" + username + "` from the whitelist.").queue();
                } else {
                    event.reply("`" + username + "` is not whitelisted.").queue();
                }
            } catch (IOException e) {
                event.reply("Unable to read or write the whitelist file: " + e.getMessage()).queue();
            }

            // Disable button after use
            event.editButton(event.getButton().asDisabled()).queue();
        } else if (event.getButton().getId().startsWith("ban")) {
            String uuid = event.getButton().getId().substring("ban-".length());
            String username = PlayerUtility.getUsernameOfUuid(uuid);

            try {

                if (Whitelist.isUserWhitelisted(UUID.fromString(uuid))) {
                    assert username != null;
                    Whitelist.remove(username, UUID.fromString(uuid));
                }
                BanList<PlayerProfile> banList = Bukkit.getBanList(BanList.Type.PROFILE);
                assert username != null;
                banList.addBan(Bukkit.getServer().createPlayerProfile(username), "Banned by Dreamvistitor.", (Date) null, null);
                event.reply("Banned `" + username + "`.").queue();

            } catch (IOException e) {
                event.reply("Unable to read or write the whitelist file: " + e.getMessage()).queue();
            }

            // Disable button after use
            event.editButton(event.getButton().asDisabled()).queue();
        }

    }

}
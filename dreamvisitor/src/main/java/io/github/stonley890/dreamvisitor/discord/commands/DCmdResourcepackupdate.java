package io.github.stonley890.dreamvisitor.discord.commands;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Properties;

public class DCmdResourcepackupdate implements DiscordCommand {
    @Override
    public @NotNull SlashCommandData getCommandData() {
        return Commands.slash("resourcepackupdate", "Update the resource pack hash to prompt clients to download the pack.")
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED);
    }

    @Override
    public void onCommand(@NotNull SlashCommandInteractionEvent event) {
        String resourcePackURL = null;

        try (InputStream input = new FileInputStream("server.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            resourcePackURL = prop.getProperty("resource-pack");
        } catch (IOException e) {
            if (Dreamvisitor.debugMode) throw new RuntimeException();
        }

        if (resourcePackURL != null) {

            event.deferReply().queue();

            try {
                URL url = new URL(resourcePackURL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setReadTimeout(10000); // timeout
                connection.connect();

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
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

                    try (InputStream input = new FileInputStream("server.properties")) {
                        Properties prop = new Properties();
                        prop.load(input);

                        prop.setProperty("resource-pack-sha1", newHash); // Update the hash property

                        try (OutputStream output = new FileOutputStream("server.properties")) {
                            prop.store(output, null);
                            event.getHook().editOriginal("Hash updated to " + newHash + "!").queue();
                        }
                    } catch (IOException e) {
                        if (Dreamvisitor.debugMode) throw new RuntimeException();
                    }
                }
            } catch (Exception e) {
                if (Dreamvisitor.debugMode) throw new RuntimeException();
            }


        } else {
            event.reply("Could not get URL of resource pack.").queue();
        }
        Dreamvisitor.getPlugin().saveConfig();
    }
}

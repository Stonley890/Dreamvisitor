package io.github.stonley890.dreamvisitor.google;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.commands.discord.DiscCommandsManager;
import net.dv8tion.jda.api.entities.User;
import org.bukkit.Bukkit;
import org.junit.BeforeClass;
import org.mortbay.util.IO;
import org.shanerx.mojang.Mojang;

import java.io.*;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class UserTracker {

    private static Sheets sheetsService;
    private static final String APPLICATION_NAME = "Google Sheets API";
    private static final String SPREADSHEET_ID = Dreamvisitor.getPlugin().getConfig().getString("userSpreadsheet");

    private static Credential authorize() throws IOException, GeneralSecurityException {
        // InputStream in = Files.newInputStream(new File(Dreamvisitor.getPlugin().getDataFolder().getAbsolutePath() + "/credentials.json").toPath());

        Reader targerReader = new FileReader(Dreamvisitor.getPlugin().getDataFolder().getAbsolutePath() + "/google_secret.json");

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                JacksonFactory.getDefaultInstance(), targerReader
        );

        List<String> scopes = Collections.singletonList(SheetsScopes.SPREADSHEETS);

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                clientSecrets,
                scopes
        ).setDataStoreFactory(new FileDataStoreFactory(new File("tokens")))
                .setAccessType("offline")
                .build();

        Credential credential = new AuthorizationCodeInstalledApp(
                flow, new LocalServerReceiver())
                .authorize("user");

        return credential;
    }

    public static Sheets getSheetsService() throws IOException, GeneralSecurityException {
        Credential credential = authorize();
        return new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public static List<List<Object>> getRange(String range) throws GeneralSecurityException, IOException {
        sheetsService = getSheetsService();
        // String range = "Users!A3:D3";

        ValueRange response = sheetsService.spreadsheets().values()
                .get(SPREADSHEET_ID, range)
                .execute();

        return response.getValues();

    }

    public static void initWhitelistPlayer(String minecraftUsername, String uuid, User user) throws  GeneralSecurityException, IOException {
        sheetsService = getSheetsService();

        Dreamvisitor.debug("Adding entry to the User Tracker...");
        // Prepare an array of values
        ValueRange body = new ValueRange().setValues(Collections.singletonList(
                Arrays.asList(minecraftUsername, uuid, user.getName(), user.getId(), "", "civilian")
        ));

        List<List<Object>> rows = getRange("Users!B3:B1000");

        // Check if UUID is already registered
        for (List<Object> row : rows) {
            if (row.get(0).equals(uuid)) {
                Dreamvisitor.debug("Minecraft user already exists. Skipping.");
                return;
            }
        }

        // Only rows with content will be returned. If only one player is recorded, only one row is in the array
        // # of rows (+ 3) = index of next empty row
        String range = "A" + (rows.size() + 3) + ":F" + (rows.size() + 3);
        Dreamvisitor.debug(range + " is next empty range. Adding values...");

        UpdateValuesResponse result = sheetsService.spreadsheets().values()
                .update(SPREADSHEET_ID, range, body)
                .setValueInputOption("RAW")
                .execute();
    }

    public static boolean linkAccount(String uuid, User user) throws GeneralSecurityException, IOException {
        sheetsService = getSheetsService();

        Mojang mojang = new Mojang().connect();
        String username;
        if (mojang.getPlayerProfile(uuid) != null) {
            username = mojang.getPlayerProfile(uuid).getUsername();
        } else {
            return false;
        }


        // Prepare array of values
        ValueRange body = new ValueRange().setValues(Collections.singletonList(
                Arrays.asList(username, uuid, user.getName(), user.getId())
        ));

        List<List<Object>> rows = getRange("Users!B3:B1000");

        // Check if UUID is already registered
        for (int i = 0; i < rows.size(); i++) {
            if (rows.get(i).get(0).equals(uuid)) {
                // i + 3 = index of target row
                String range = "A" + (i + 3) + ":D" + (i + 3);
                Dreamvisitor.debug("Minecraft user already exists. Applying change to range " + range);

                UpdateValuesResponse result = sheetsService.spreadsheets().values()
                        .update(SPREADSHEET_ID, range, body)
                        .setValueInputOption("RAW")
                        .execute();

                return true;
            }
        }

        // Only rows with content will be returned. If only one player is recorded, only one row is in the array
        // # of rows (+ 3) = index of next empty row
        String range = "A" + (rows.size() + 3) + ":F" + (rows.size() + 3);
        Dreamvisitor.debug(range + " is next empty range. Adding values...");

        // Prepare array of values
        body = new ValueRange().setValues(Collections.singletonList(
                Arrays.asList(username, uuid, user.getName(), user.getId(), "", "civilian")
        ));

        UpdateValuesResponse result = sheetsService.spreadsheets().values()
                .update(SPREADSHEET_ID, range, body)
                .setValueInputOption("RAW")
                .execute();

        return true;

    }

    public static void updateUsername(String uuid, String username) throws GeneralSecurityException, IOException {
        sheetsService = getSheetsService();

        // Prepare an array of values
        ValueRange body = new ValueRange().setValues(Collections.singletonList(
                Arrays.asList(username, uuid)
        ));

        List<List<Object>> rows = getRange("Users!B3:B1000");

        // Iterate through rows to find UUID
        for (int i = 0; i < rows.size(); i++) {

            if (rows.get(i).get(0).equals(uuid)) {
                // i + 3 = index of target row
                String range = "A" + (i + 3) + ":B" + (i + 3);
                Dreamvisitor.debug(range + " matches target UUID. Updating this username...");

                UpdateValuesResponse result = sheetsService.spreadsheets().values()
                        .update(SPREADSHEET_ID, range, body)
                        .setValueInputOption("RAW")
                        .execute();
            }
        }
    }

    public static void updateTribe(String uuid, int tribeIndex) throws GeneralSecurityException, IOException {
        sheetsService = getSheetsService();

        List<List<Object>> rows = getRange("Users!B3:B1000");

        // Iterate through rows to find UUID
        for (int i = 0; i < rows.size(); i++) {

            if (rows.get(i).get(0).equals(uuid)) {
                // i + 3 = index of target row
                String range = "G" + (i + 3) + ":G" + (i + 3);
                Dreamvisitor.debug(range + " matches target UUID. Updating this tribe...");

                // Prepare array of values
                ValueRange body = new ValueRange().setValues(Collections.singletonList(
                        Collections.singletonList(DiscCommandsManager.TRIBES[tribeIndex])
                ));

                UpdateValuesResponse result = sheetsService.spreadsheets().values()
                        .update(SPREADSHEET_ID, range, body)
                        .setValueInputOption("RAW")
                        .execute();
            }
        }
    }

    public static void disableGoogle() {
        Bukkit.getLogger().severe("Dreamvisitor cannot reach Google Services. This is likely due to bad authentication. Google integration will be disabled until restart.");
        Dreamvisitor.googleFailed = true;
    }
}
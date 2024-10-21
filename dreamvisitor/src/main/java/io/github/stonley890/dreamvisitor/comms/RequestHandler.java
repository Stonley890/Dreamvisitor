package io.github.stonley890.dreamvisitor.comms;

import fi.iki.elonen.NanoHTTPD;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.functions.ResourcePack;
import io.github.stonley890.dreamvisitor.functions.ScheduleRestart;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class RequestHandler extends NanoHTTPD {
    public RequestHandler() throws IOException {
        super(8081);  // Set the port for the server
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println("Spigot Plugin HTTP server started on port 8081");
    }

    @Override
    public Response serve(@NotNull IHTTPSession session) {
        String uri = session.getUri();
        Method method = session.getMethod();
        String response;

        Dreamvisitor.debug("Request to " + uri + " via method " + method + ": " + session.getQueryParameterString());

        Dreamvisitor.debug("uri is /task-action? " + ("/task-action".equals(uri)) + ": " + uri);
        Dreamvisitor.debug("method is POST? " + (Method.POST.equals(method)) + ": " + method);

        if ("/task-action".equals(uri) && Method.POST.equals(method)) {
            Map<String, String> bodyParams = new HashMap<>();
            try {
                session.parseBody(bodyParams);
            } catch (IOException | ResponseException e) {
                e.printStackTrace();
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "failure: Server error");
            }

            String action = session.getParms().get("action");

            switch (action) {
                case "maxPlayerCountUp" -> {
                    Dreamvisitor.playerLimit++;
                    response = "success";
                    DataSender.sendMaxPlayerCount();
                }
                case "maxPlayerCountDown" -> {
                    Dreamvisitor.playerLimit--;
                    response = "success";
                    DataSender.sendMaxPlayerCount();
                }
                case "scheduleRestart" -> {
                    ScheduleRestart.setRestartScheduled(!ScheduleRestart.isRestartScheduled());
                    response = "success";
                }
                case "updateResourcePack" -> {
                    try {
                        ResourcePack.update();
                        response = "success";
                        DataSender.sendResourcePack();
                    } catch (IOException | NoSuchAlgorithmException e) {
                        response = "failure: " + e.getMessage();
                    }
                }
                default -> response = "failure: Action " + action + " not found.";
            }

        } else if ("/request-data".equals(uri) && Method.GET.equals(method)) {
            // Send some data back to the web app
            response = "Specific data from the Spigot plugin!";
            // Add your logic to return data
        } else {
            response = "failure: Unknown request";
        }

        return newFixedLengthResponse(response);
    }
}

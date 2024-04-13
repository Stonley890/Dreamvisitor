package io.github.stonley890.dreamvisitor.functions;

import io.github.stonley890.dreamvisitor.Bot;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ConsoleLogger extends AbstractAppender {

    public static final StringBuilder messageBuilder = new StringBuilder();
    public static final List<String> overFlowMessages = new ArrayList<>();

    public ConsoleLogger() {
        super("MyLogAppender", null, null, false, null);
        start();
    }

    @Override
    public void append(@NotNull LogEvent event) {
        // if you don't make it immutable, then you may have some unexpected behaviors
        LogEvent log = event.toImmutable();

        StringBuilder builder = new StringBuilder(log.getMessage().getFormattedMessage());

        if (log.getThrown() != null) {
            builder.append("\n").append(log.getThrown().getMessage());
            for (StackTraceElement stackTraceElement : log.getThrown().getStackTrace()) {
                builder.append("\n").append(stackTraceElement.toString());
            }

        }

        String message = builder.toString();

        // Remove Minecraft formatting codes
        message = message.replaceAll("\u001B?(\\W1B)?\\[([0-9,;]+)m", "");
        message = "[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + " " + event.getLevel().toString() + "] " + Bot.escapeMarkdownFormatting(message);

        // Truncate messages over 2000 characters
        if (message.length() >= 2000) {
            String tooLongMessage = "**This message was too long! Here is the shorter version:**\n";
            message = message.substring(0, (1998 - tooLongMessage.length())).concat(tooLongMessage);
        }

        // Pause adding strings if the new message will be > 2000
        if (messageBuilder.toString().length() + message.length() + "\n".length() <= 2000) {

            if (!messageBuilder.isEmpty()) {
                messageBuilder.append("\n");
            }
            messageBuilder.append(message);

        } else {
            overFlowMessages.add(message);
        }


    }

}

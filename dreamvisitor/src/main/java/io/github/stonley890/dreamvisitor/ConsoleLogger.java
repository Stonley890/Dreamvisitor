package io.github.stonley890.dreamvisitor;

import io.github.stonley890.dreamvisitor.commands.discord.DiscCommandsManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.logging.Formatter;
// other imports that you need here

public class ConsoleLogger extends AbstractAppender {

    // your variables

    public ConsoleLogger() {
        // do your calculations here before starting to capture
        super("MyLogAppender", null, null);
        start();
    }

    protected ConsoleLogger(String name, Filter filter, Layout<? extends Serializable> layout) {
        super(name, filter, layout);
    }

    @Override
    public void append(LogEvent event) {
        // if you don`t make it immutable, then you may have some unexpected behaviours
        LogEvent log = event.toImmutable();

        // do what you have to do with the log

        // you can get only the log message like this:
        String message = log.getMessage().getFormattedMessage();

        // and you can construct your whole log message like this:
        message = "[" + event.getLevel().toString() + "] " + message;

        if (Dreamvisitor.getPlugin().getConfig().getBoolean("log-console")) {
            DiscCommandsManager.gameLogChannel.sendMessage(message).queue();
        }

    }

}

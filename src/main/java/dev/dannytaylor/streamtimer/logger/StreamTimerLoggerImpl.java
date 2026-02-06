package dev.dannytaylor.streamtimer.logger;

import dev.dannytaylor.logger.log.Logger;
import dev.dannytaylor.logger.log.handler.Handler;
import dev.dannytaylor.logger.log.handler.handlers.ConsoleHandler;
import dev.dannytaylor.logger.log.handler.handlers.FileHandler;
import dev.dannytaylor.logger.util.Timestamp;
import dev.dannytaylor.streamtimer.config.StreamTimerConfig;
import dev.dannytaylor.streamtimer.data.StaticVariables;
import dev.dannytaylor.streamtimer.logger.handler.MiniUIHandler;
import dev.dannytaylor.streamtimer.logger.handler.UIHandler;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class StreamTimerLoggerImpl {
    private static final StreamTimerLogger instance;
    private static final UIHandler uiHandler = new UIHandler();

    public static void bootstrap() {
        instance.registerHandler(addConsoleHandler());
        instance.registerHandler(new MiniUIHandler());
        instance.registerHandler(uiHandler);
        if (StreamTimerConfig.instance.debug.value()) {
            try {
                instance.registerHandler(new FileHandler(new File(StaticVariables.name  + "Assets/logs/" + Timestamp.get(DateTimeFormatter.ofPattern("yyyy-MM-dd.HH-mm-ss")) + ".txt")));
            } catch (IOException error) {
                get().error("Failed to setup file log handler: " + error);
            }
        }
    }

    public static UIHandler getUiHandler() {
        return uiHandler;
    }

    private static ConsoleHandler addConsoleHandler() {
        ConsoleHandler handler = new ConsoleHandler();
        handler.addStyle(Logger.Type.warn.name(), new Color(255, 176, 32));
        handler.addStyle(Logger.Type.error.name(), new Color(255, 107, 107));
        handler.addStyle(Logger.Type.debug.name(), new Color(244, 114, 192));
        return handler;
    }

    public static StreamTimerLogger get() {
        return instance;
    }

    public static List<Handler> getHandlers() {
        return get().getHandlers();
    }

    public static void registerHandler(Handler handler) {
        get().registerHandler(handler);
    }

    public static void log(String message, String style) {
        get().log(message, style);
    }

    public static void log(StringBuilder message, String style) {
        get().log(message, style);
    }

    public static void unformatted(String message) {
        get().unformatted(message);
    }

    public static void unformatted(StringBuilder message) {
        get().unformatted(message);
    }

    public static void info(String message) {
        get().info(message);
    }

    public static void info(StringBuilder message) {
        get().info(message);
    }

    public static void warn(String message) {
        get().warn(message);
    }

    public static void warn(StringBuilder message) {
        get().warn(message);
    }

    public static void error(String message) {
        get().error(message);
    }

    public static void error(StringBuilder message) {
        get().error(message);
    }

    public static void debug(String message) {
        get().debug(message);
    }

    public static void debug(StringBuilder message) {
        get().debug(message);
    }

    public static void close() {
        get().close();
    }

    static {
        instance = new StreamTimerLogger();
    }
}

package dev.dannytaylor.streamtimer.logger;

import dev.dannytaylor.logger.log.Logger;
import dev.dannytaylor.logger.log.handler.handlers.ConsoleHandler;
import dev.dannytaylor.logger.log.handler.handlers.FileHandler;
import dev.dannytaylor.logger.util.Timestamp;
import dev.dannytaylor.streamtimer.config.StreamTimerConfig;
import dev.dannytaylor.streamtimer.data.StaticVariables;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class StreamTimerLogger extends Logger {
    public void debug(String message) {
        if (StreamTimerConfig.instance.debug.value()) super.debug(message);
    }

    public void debug(StringBuilder message) {
        if (StreamTimerConfig.instance.debug.value()) super.debug(message);
    }
}

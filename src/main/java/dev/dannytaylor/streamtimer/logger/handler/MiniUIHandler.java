package dev.dannytaylor.streamtimer.logger.handler;

import dev.dannytaylor.logger.log.Logger;
import dev.dannytaylor.logger.log.handler.Handler;
import dev.dannytaylor.streamtimer.StreamTimerMain;

public class MiniUIHandler extends Handler {
    public void log(Logger.Type type, String log) {
        log(log);
    }

    public void log(String log, String style) {
        log(log);
    }

    private void log(String log) {
        if (StreamTimerMain.gui != null) {
            if (StreamTimerMain.gui.messageText != null) StreamTimerMain.gui.messageText.setText(log);
            else StreamTimerMain.gui.initMessageText = log;
        }
    }
}
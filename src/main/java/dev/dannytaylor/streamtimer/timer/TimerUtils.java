/*
    StreamTimer
    Contributor(s): dannytaylor
    Github: https://github.com/legotaylor/StreamTimer
    Licence: LGPL-3.0
*/

package dev.dannytaylor.streamtimer.timer;

import dev.dannytaylor.streamtimer.StreamTimerMain;
import dev.dannytaylor.streamtimer.config.StreamTimerConfig;

public class TimerUtils {
    public static String getTime(long millis, boolean showMillis) {
        long totalMillis = Math.max(0, millis);
        long hours = totalMillis / 3_600_000;
        long minutes = (totalMillis % 3_600_000) / 60_000;
        long seconds = (totalMillis % 60_000) / 1_000;
        long milliseconds = totalMillis % 1_000;
        return showMillis ? String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, milliseconds) : String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static String getTime(long millis) {
        return getTime(millis, false);
    }

    public static String getTime() {
        return getTime(getMillis(), StreamTimerConfig.instance.showMillis.value());
    }

    public static long getMillis() {
        return Math.max(0, StreamTimerMain.timer.get());
    }

    public static void setTimer(long seconds, boolean add, boolean save) {
        System.out.println("[StreamTimer] Added " + getTime(seconds * 1000L) + " to the timer");
        StreamTimerMain.timer.set(Math.max(0, (add ? getMillis() : 0) + seconds * 1000L), save); // we only save on start/pause/set/add/remove otherwise it is a lot of writes.
    }

    public static long getSeconds(long hours, long minutes, long seconds) {
        long h = Math.max(0, hours);
        long m = Math.max(0, minutes);
        long s = Math.max(0, seconds);
        return s + (m * 60) + ((h * 60) * 60);
    }

    public static long getSecondsFromString(String hours, String minutes, String seconds) {
        return getSecondsFromString(hours, minutes, seconds, true);
    }

    public static long getSecondsFromString(String hours, String minutes, String seconds, boolean saveToConfig) {
        long h = 0;
        if (hours != null && !hours.isBlank()) {
            try {
                h = Integer.parseInt(hours);
            } catch (NumberFormatException numberError) {
                hours = StreamTimerConfig.instance.addHours.getDefaultValue();
            }
        } // it's probably just empty.
        long m = 0;
        if (minutes != null && !minutes.isBlank()) {
            try {
                m = Integer.parseInt(minutes);
            } catch (NumberFormatException numberError) {
                minutes = StreamTimerConfig.instance.addMinutes.getDefaultValue();
            } // it's probably just empty.
        }
        long s = 0;
        if (seconds != null && !seconds.isBlank()) {
            try {
                s = Integer.parseInt(seconds);
            } catch (NumberFormatException numberError) {
                seconds = StreamTimerConfig.instance.addSeconds.getDefaultValue();
            } // it's probably just empty.
        }
        if (saveToConfig) updateConfig(hours, minutes, seconds);
        return getSeconds(h, m, s);
    }

    public static String getTimeFromString(String hours, String minutes, String seconds) {
        long h = 0;
        try {
            h = Integer.parseInt(hours);
        } catch (NumberFormatException numberError) {} // it's probably just empty.
        long m = 0;
        try {
            m = Integer.parseInt(minutes);
        } catch (NumberFormatException numberError) {} // it's probably just empty.
        long s = 0;
        try {
            s = Integer.parseInt(seconds);
        } catch (NumberFormatException numberError) {} // it's probably just empty.
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    public static void updateConfig(String hours, String minutes, String seconds) {
        long h = 0;
        try {
            h = Integer.parseInt(hours);
        } catch (NumberFormatException numberError) {} // it's probably just empty.
        long m = 0;
        try {
            m = Integer.parseInt(minutes);
        } catch (NumberFormatException numberError) {} // it's probably just empty.
        long s = 0;
        try {
            s = Integer.parseInt(seconds);
        } catch (NumberFormatException numberError) {} // it's probably just empty.
        StreamTimerConfig.instance.addHours.setValue(String.format("%02d", h), false);
        StreamTimerConfig.instance.addMinutes.setValue(String.format("%02d", m), false);
        StreamTimerConfig.instance.addSeconds.setValue(String.format("%02d", s), false);
        StreamTimerConfig.instance.save();
    }

    public static void toggleTimer() {
        if (StreamTimerMain.timer.isRunning()) {
            StreamTimerMain.timer.stop();
            if (StreamTimerMain.gui != null) {
                if (StreamTimerMain.gui.messageText != null) StreamTimerMain.gui.messageText.setText("Stopped timer!");
                if (StreamTimerMain.gui.toggleButton != null) {
                    StreamTimerMain.gui.toggleButton.setText("START");
                    StreamTimerMain.gui.toggleButton.setToolTipText("Starts the timer");
                }
            }
        } else {
            StreamTimerMain.timer.start();
            if (StreamTimerMain.gui != null) {
                if (StreamTimerMain.gui.messageText != null) StreamTimerMain.gui.messageText.setText("Started timer!");
                if (StreamTimerMain.gui.toggleButton != null) {
                    StreamTimerMain.gui.toggleButton.setText("STOP");
                    StreamTimerMain.gui.toggleButton.setToolTipText("Stops the timer");
                }
            }
        }
    }
}

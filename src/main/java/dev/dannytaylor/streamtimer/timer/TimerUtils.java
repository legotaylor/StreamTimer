package dev.dannytaylor.streamtimer.timer;

import dev.dannytaylor.streamtimer.StreamTimerMain;
import dev.dannytaylor.streamtimer.config.StreamTimerConfig;

public class TimerUtils {
    public static String getTime(long millis) {
        long totalSeconds = Math.max(0, millis / 1000);
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static String getTime() {
        return getTime(getMillis());
    }

    public static long getMillis() {
        return Math.max(0, StreamTimerMain.timer.get());
    }

    public static void setTimer(long seconds, boolean add, boolean save) {
        StreamTimerMain.timer.set(Math.max(0, (add ? getMillis() : 0) + seconds * 1000L), save); // we only save on start/pause/set/add/remove otherwise it is a lot of writes.
    }

    public static long getSeconds(long hours, long minutes, long seconds) {
        long h = Math.max(0, hours);
        long m = Math.max(0, minutes);
        long s = Math.max(0, seconds);
        return s + (m * 60) + ((h * 60) * 60);
    }

    public static long getSecondsFromString(String hours, String minutes, String seconds) {
        long h = 0;
        try {
            h = Integer.parseInt(hours);
        } catch (NumberFormatException numberError) {
            hours = StreamTimerConfig.instance.addHours.getDefaultValue();
        } // it's probably just empty.
        long m = 0;
        try {
            m = Integer.parseInt(minutes);
        } catch (NumberFormatException numberError) {
            minutes = StreamTimerConfig.instance.addMinutes.getDefaultValue();
        } // it's probably just empty.
        long s = 0;
        try {
            s = Integer.parseInt(seconds);
        } catch (NumberFormatException numberError) {
            seconds = StreamTimerConfig.instance.addSeconds.getDefaultValue();
        } // it's probably just empty.
        updateConfig(hours, minutes, seconds);
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
}

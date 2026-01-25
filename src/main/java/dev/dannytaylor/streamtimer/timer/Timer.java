package dev.dannytaylor.streamtimer.timer;

import dev.dannytaylor.streamtimer.StreamTimerMain;
import dev.dannytaylor.streamtimer.audio.SoundPlayer;
import dev.dannytaylor.streamtimer.config.StreamTimerConfig;
import dev.dannytaylor.streamtimer.data.StaticVariables;

import java.io.File;
import java.nio.file.Path;

public class Timer {
    private boolean running;
    private long time = StreamTimerConfig.instance.time.value();
    private long lastTickTime = 0L;

    public Timer() {
    }

    public void tick() {
        if (!this.running) {
            lastTickTime = 0;
            return;
        }
        if (this.time < 0) {
            this.time = 0;
        }
        if (this.time == 0) {
            if (!StreamTimerConfig.instance.reversed.value()) {
                this.running = false;
                this.time = 0;
                if (StreamTimerConfig.instance.finishSound.value()) {
                    SoundPlayer.playSound(new File(Path.of(StaticVariables.name + "Assets").toFile(), "finishSound.wav"));
                }
                StreamTimerMain.gui.messageText.setText("Timer finished!");
                StreamTimerMain.gui.toggleButton.setText("START");
                return;
            }
        }

        long now = System.currentTimeMillis();
        if (this.lastTickTime == 0L) {
            this.lastTickTime = System.currentTimeMillis();
            return;
        }

        long delta = now - this.lastTickTime;
        this.time = StreamTimerConfig.instance.reversed.value() ? this.time + delta : this.time - delta;
        this.lastTickTime = now;
    }

    public void start() {
        running = true;
        save(true);
        System.out.println("[Stream Timer] Started Timer");
    }

    public void stop() {
        running = false;
        save(true);
        System.out.println("[Stream Timer] Stopped Timer");
    }

    public void set(long time, boolean save) {
        this.time = time;
        save(save);
        System.out.println("[Stream Timer] Set Timer");
    }

    public long get() {
        return this.time;
    }

    public boolean isRunning() {
        return this.running;
    }

    public void save(boolean toFile) {
        if (toFile) System.out.println("[Stream Timer] Saving Timer");
        StreamTimerConfig.instance.time.setValue(time, toFile);
    }
}

package dev.dannytaylor.streamtimer.timer;

import dev.dannytaylor.streamtimer.config.StreamTimerConfig;

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
        if (time <= 0) {
            this.running = false;
            this.time = 0;
            return;
        }

        long now = System.currentTimeMillis();
        if (this.lastTickTime == 0L) {
            this.lastTickTime = System.currentTimeMillis();
            return;
        }

        long delta = now - this.lastTickTime;
        this.time -= delta;
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

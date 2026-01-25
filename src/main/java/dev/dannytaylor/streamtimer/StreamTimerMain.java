package dev.dannytaylor.streamtimer;

import dev.dannytaylor.streamtimer.config.StreamTimerConfig;
import dev.dannytaylor.streamtimer.render.GUI;
import dev.dannytaylor.streamtimer.render.TextRenderer;
import dev.dannytaylor.streamtimer.timer.Timer;
import dev.dannytaylor.streamtimer.timer.TimerUtils;

public class StreamTimerMain {
    public static boolean running = true;
    public static Timer timer = new Timer();
    public static GUI gui = new GUI();
    public static TextRenderer textRenderer = new TextRenderer(576, 144);

    public static void main(String[] args) {
        try {
            StreamTimerResources.extract();
            StreamTimerConfig.bootstrap();
            gui.latch.await();
            long nextTick = System.nanoTime();
            while (running) {
                try {
                    long now = System.nanoTime();
                    if (now >= nextTick) {
                        tick();
                        long tickRate = 1000000000 / 20;
                        nextTick += tickRate;
                        if (System.nanoTime() > nextTick + tickRate * 20) nextTick = System.nanoTime();
                    } else {
                        long sleepTime = (nextTick - now) / 1000000L;
                        if (sleepTime > 0) Thread.sleep(sleepTime);
                    }
                } catch (Exception error) {
                    System.err.println("Error whilst ticking: " + error);
                    running = false;
                }
            }
            StreamTimerMain.timer.stop();
            System.exit(0);
        } catch (InterruptedException error) {
            System.err.println("Failed to start gui: " + error);
        }
    }

    public static void tick() {
        timer.tick();
        String time = TimerUtils.getTime();
        StreamTimerMain.textRenderer.render(time);
        gui.updateTimer(time);
    }
}
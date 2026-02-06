/*
    StreamTimer
    Contributor(s): dannytaylor
    Github: https://github.com/legotaylor/StreamTimer
    Licence: LGPL-3.0
*/

package dev.dannytaylor.streamtimer;

import dev.dannytaylor.streamtimer.config.StreamTimerConfig;
import dev.dannytaylor.streamtimer.data.StaticVariables;
import dev.dannytaylor.streamtimer.integration.IntegrationRegistry;
import dev.dannytaylor.streamtimer.integration.twitch.TwitchIntegration;
import dev.dannytaylor.streamtimer.logger.StreamTimerLoggerImpl;
import dev.dannytaylor.streamtimer.render.GUI;
import dev.dannytaylor.streamtimer.render.TextRenderer;
import dev.dannytaylor.streamtimer.timer.Timer;
import dev.dannytaylor.streamtimer.timer.TimerUtils;
import dev.dannytaylor.streamtimer.util.Resources;

import javax.swing.*;

public class StreamTimerMain {
    public static boolean running;
    public static Timer timer;
    public static GUI gui;
    public static ImageIcon icon;
    public static TextRenderer textRenderer;

    public static void main(String[] args) {
        try {
            StreamTimerResources.extract();
            StreamTimerResources.latch.await();
            icon = Resources.getTexture(StreamTimerMain.class.getResource(StaticVariables.logo), 64, 64);
            StreamTimerConfig.bootstrap();
            IntegrationRegistry.bootstrap();
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
                    StreamTimerLoggerImpl.error("Error whilst ticking: " + error);
                    running = false;
                }
            }
            StreamTimerMain.timer.stop();
            System.exit(0);
        } catch (InterruptedException error) {
            StreamTimerLoggerImpl.error("Failed to start gui: " + error);
        }
    }

    public static void tick() {
        timer.tick();
        String time = TimerUtils.getTime();
        StreamTimerMain.textRenderer.render(time);
        gui.updateTimer(time);

        TwitchIntegration.setIdSecretEnabled(!TwitchIntegration.twitch.hasClient());
    }

    static {
        StreamTimerLoggerImpl.bootstrap();
        running = true;
        timer = new Timer();
        gui = new GUI();
        textRenderer = new TextRenderer(576, 144);
    }
}
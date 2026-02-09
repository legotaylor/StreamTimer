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
import dev.dannytaylor.streamtimer.render.Renderer;
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
    public static long lastSaved = System.nanoTime();
    public static volatile boolean configSaveRequested;
    public static boolean canStart;

    public static void main(String[] args) {
        try {
            StreamTimerResources.extract();
            StreamTimerResources.latch.await();
            icon = Resources.getTexture(StreamTimerMain.class.getResource(StaticVariables.logo), 64, 64);
            StreamTimerConfig.bootstrap();
            IntegrationRegistry.bootstrap();
            gui = new GUI();
            gui.latch.await();
            if (canStart) {
                long nextTick = System.nanoTime();
                while (running) {
                    try {
                        if (StreamTimerConfig.instance.iPaidForTheWholeDamnCpuGiveMeTheWholeDamnCpu.value()) tick();
                        else {
                            long now = System.nanoTime();
                            if (now >= nextTick) {
                                tick();
                                long tickRate = 1000000000L / StreamTimerConfig.instance.tps.value();
                                nextTick += tickRate;
                                if (System.nanoTime() > nextTick + 1000000000L) nextTick = System.nanoTime();
                            } else {
                                long sleepTime = (nextTick - now) / 1000000L;
                                if (sleepTime > 0) Thread.sleep(sleepTime);
                            }
                        }
                    } catch (Exception error) {
                        StreamTimerLoggerImpl.error("Error whilst ticking: " + error);
                        running = false;
                    }
                }
            }
        } catch (InterruptedException error) {
            StreamTimerLoggerImpl.error("Failed to start gui: " + error);
        }
        exit();
    }

    public static void close() {
        running = false;
    }

    public static void exit() {
        StreamTimerLoggerImpl.info("Closing " + StaticVariables.name + "!");
        timer.stop(false);
        IntegrationRegistry.close();
        if (gui != null) {
            if (gui.configWindow != null) gui.configWindow.dispose();
            if (gui.window != null) gui.window.dispose();
            if (gui.setupGUI != null) {
                if (gui.setupGUI.window != null) gui.setupGUI.window.dispose();
            }
            gui = null;
        }
        StreamTimerConfig.toFile();
        System.exit(0);
    }

    public static void tick() {
        timer.tick();
        Renderer.tick(TimerUtils.getTime());
        gui.tick();
        TwitchIntegration.setIdSecretEnabled(!TwitchIntegration.twitch.hasClient());
        autoSave();
    }

    public static void autoSave() {
        long now = System.nanoTime();
        if (configSaveRequested || now >= lastSaved + (Math.max(StreamTimerConfig.instance.saveSeconds.value(), 1) * 1000000000L)) {
            StreamTimerConfig.toFile();
            configSaveRequested = false;
            lastSaved = now;
        }
    }

    static {
        StreamTimerLoggerImpl.bootstrap();
        running = true;
        timer = new Timer();
        textRenderer = new TextRenderer(StreamTimerConfig.instance.renderWidth.value(), StreamTimerConfig.instance.renderHeight.value());
    }
}
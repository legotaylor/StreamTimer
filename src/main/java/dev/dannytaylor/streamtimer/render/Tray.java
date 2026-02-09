/*
    StreamTimer
    Contributor(s): dannytaylor
    Github: https://github.com/legotaylor/StreamTimer
    Licence: LGPL-3.0
*/

package dev.dannytaylor.streamtimer.render;

import dev.dannytaylor.streamtimer.StreamTimerMain;
import dev.dannytaylor.streamtimer.config.StreamTimerConfig;
import dev.dannytaylor.streamtimer.data.StaticVariables;
import dev.dannytaylor.streamtimer.timer.TimerUtils;

import java.awt.*;

public class Tray {
    public SystemTray systemTray;
    public TrayIcon trayIcon;

    public Tray() {
        if (!SystemTray.isSupported()) return;

        this.systemTray = SystemTray.getSystemTray();
        PopupMenu menu = new PopupMenu();

        MenuItem open = new MenuItem("Open " + StaticVariables.name);
        open.addActionListener(e -> {
            StreamTimerMain.gui.window.setVisible(true);
            StreamTimerMain.gui.window.toFront();
        });

        MenuItem startStop = new MenuItem("Start/Stop");
        startStop.addActionListener(e -> TimerUtils.toggleTimer());

        MenuItem exit = new MenuItem("Exit");
        exit.addActionListener(e -> System.exit(0));

        menu.add(startStop);
        menu.add(open);
        menu.addSeparator();
        menu.add(exit);

        this.trayIcon = new TrayIcon(StreamTimerMain.icon.getImage(), StaticVariables.name, menu);
        this.trayIcon.setImageAutoSize(true);

        this.trayIcon.addActionListener(l -> {
            StreamTimerMain.gui.window.setVisible(true);
            StreamTimerMain.gui.window.toFront();
        });

        try {
            this.systemTray.add(this.trayIcon);
        } catch (Exception ignored) {}
    }

    private long lastUpdated = System.nanoTime();
    public void updateTimer(String time) {
        if (this.trayIcon != null) {
            long now = System.nanoTime();
            if (now >= (this.lastUpdated + 1000000000L)) { // limit updates to every second
                this.trayIcon.setToolTip(time != null ? time : StaticVariables.name);
                this.lastUpdated = now;
            }
        }
    }

    public void close() {
        if (!SystemTray.isSupported()) return;
        try {
            this.systemTray.remove(this.trayIcon);
        } catch (Exception ignored) {}
    }
}

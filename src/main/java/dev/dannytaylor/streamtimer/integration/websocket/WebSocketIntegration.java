/*
    StreamTimer
    Contributor(s): dannytaylor
    Github: https://github.com/legotaylor/StreamTimer
    Licence: LGPL-3.0
*/

package dev.dannytaylor.streamtimer.integration.websocket;

import dev.dannytaylor.streamtimer.StreamTimerMain;
import dev.dannytaylor.streamtimer.config.RenderMode;
import dev.dannytaylor.streamtimer.config.StreamTimerConfig;
import dev.dannytaylor.streamtimer.data.StaticVariables;
import dev.dannytaylor.streamtimer.render.GUI;
import dev.dannytaylor.streamtimer.render.GUIWidgets;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Paths;

public class WebSocketIntegration {
    private static WebSocketConnection connection;
    private static JButton connectButton;

    public static void bootstrap() {
        GUI.runBeforeVisible.add(WebSocketIntegration::runBeforeVisible);
    }

    private static void runBeforeVisible(RenderMode renderMode) {
        StreamTimerMain.gui.configureTabs.addTab("WebSocket", getConnectionTab2(renderMode));
        if (StreamTimerConfig.instance.webSocketAutoConnect.value()) connect();
    }

    private static JPanel getConnectionTab2(RenderMode renderMode) {
        JPanel layout = new JPanel(new GridBagLayout());
        JPanel tab = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        int row = 0;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 1.0;
        tab.add(Box.createHorizontalGlue(), gbc);

        // Name (for reference)
        gbc.gridy = row++;
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        JLabel name = new JLabel("Use this information to create a browser source in your recording/broadcasting software");
        name.setFont(name.getFont().deriveFont(Font.BOLD));
        name.setHorizontalAlignment(SwingConstants.CENTER);
        tab.add(name, gbc);

        gbc.gridwidth = 2;

        // Browser Source URL (for reference)
        gbc.gridy = row++;
        gbc.gridx = 1;
        JTextField browserSourceUrl = new JTextField("file:///" + Paths.get(StaticVariables.name + "Assets").toAbsolutePath() + "\\WebSocketClient.html" + "?type=ws&host=localhost&port=" + StreamTimerConfig.instance.webSocketPort.value());
        browserSourceUrl.setEditable(false);
        tab.add(browserSourceUrl, gbc);

        gbc.gridwidth = 1;

        // Width (for reference)
        gbc.gridy = row++;
        gbc.weightx = 0;
        gbc.gridx = 1;
        JLabel widthLabel = new JLabel("Width:");
        widthLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        tab.add(widthLabel, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0;
        JTextField width = new JTextField("576");
        width.setEditable(false);
        tab.add(width, gbc);

        // Height (for reference)
        gbc.gridy = row++;
        gbc.weightx = 0;
        gbc.gridx = 1;
        JLabel heightLabel = new JLabel("Height:");
        heightLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        tab.add(heightLabel, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0;
        JTextField height = new JTextField("144");
        height.setEditable(false);
        tab.add(height, gbc);

        // Auto Connect
        gbc.gridy = row++;
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        JCheckBox autoConnect = GUIWidgets.createCheckbox("Automatically connect on Startup", StreamTimerConfig.instance.webSocketAutoConnect.value());
        autoConnect.addChangeListener(l -> {
            StreamTimerConfig.instance.webSocketAutoConnect.setValue(autoConnect.isSelected(), true);
        });
        tab.add(autoConnect, gbc);

        // Connect/Disconnect
        gbc.gridy = row++;
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        connectButton = new JButton(isConnected() ? "Disconnect" : "Connect");
        connectButton.addActionListener(l -> {
            connectButton.setEnabled(false);
            connectButton.setText(connectButton.getText() + "ing...");
            toggleConnected();
        });
        tab.add(connectButton, gbc);

        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.gridheight = row + 1;
        gbc.weightx = 1.0;
        tab.add(Box.createHorizontalGlue(), gbc);

        layout.add(tab, new GridBagConstraints());
        return layout;
    }


    public static void toggleConnected() {
        if (isConnected()) disconnect();
        else connect();
    }

    public static void connect() {
        if (connection == null) (connection = new WebSocketConnection()).start();
        else System.err.println("[Stream Timer/WebSocket Integration] Server already connected!");
        enableWidgets();
    }

    public static void disconnect() {
        if (connection != null) {
            try {
                connection.stop();
            } catch (Exception error) {
                System.err.println("[Stream Timer/WebSocket Integration] Failed to stop server: " + error);
            }
            connection = null;
        } else System.err.println("[Stream Timer/WebSocket Integration] Server doesn't exist!");
        enableWidgets();
    }

    public static void enableWidgets() {
        if (connectButton != null) {
            connectButton.setText(isConnected() ? "Disconnect" : "Connect");
            connectButton.setEnabled(true);
        }
    }

    public static boolean isConnected() {
        return connection != null;
    }

    public static void sendFrame(byte[] frame) {
        if (isConnected()) connection.sendFrame(frame);
    }
}

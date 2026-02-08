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
import dev.dannytaylor.streamtimer.logger.StreamTimerLoggerImpl;
import dev.dannytaylor.streamtimer.render.GUI;
import dev.dannytaylor.streamtimer.render.GUIWidgets;
import dev.dannytaylor.streamtimer.util.IntegerFilter;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import java.awt.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Paths;

public class WebSocketIntegration {
    private static WebSocketConnection connection;
    private static JButton connectButton;
    private static JTextField portField;
    private static JTextField browserSourceUrl;

    public static void bootstrap() {
        String name = "WebSocket Integration";
        GUI.addBeforeVisible(name, WebSocketIntegration::runBeforeVisible);
    }

    private static void runBeforeVisible(RenderMode renderMode) {
        try {
            StreamTimerMain.gui.configureTabs.addTab("WebSocket", getConnectionTab2(renderMode));
            if (StreamTimerConfig.instance.webSocketAutoConnect.value()) connect();
        } catch (Exception error) {
            StreamTimerLoggerImpl.error("[WebSocket Integration] Failed to initialize: " + error);
        }
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
        browserSourceUrl = new JTextField();
        updateUrl(true);
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
        JTextField width = new JTextField(String.valueOf(StreamTimerMain.textRenderer.getWidth()));
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
        JTextField height = new JTextField(String.valueOf(StreamTimerMain.textRenderer.getHeight()));
        height.setEditable(false);
        tab.add(height, gbc);

        // Port
        gbc.gridy = row++;
        gbc.weightx = 0;
        gbc.gridx = 1;
        JLabel portLabel = new JLabel("Port:");
        portLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        tab.add(portLabel, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0;
        portField = new JTextField(String.valueOf(StreamTimerConfig.instance.webSocketPort.value()));
        ((AbstractDocument) portField.getDocument()).setDocumentFilter(new IntegerFilter());
        portField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateUrl(false);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateUrl(false);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateUrl(false);
            }
        });
        portField.setEditable(!WebSocketIntegration.isConnected());
        tab.add(portField, gbc);

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
            disableWidgets();
            if (portField != null) {
                StreamTimerConfig.instance.webSocketPort.setValue(portField.getText().isBlank() ? StreamTimerConfig.instance.webSocketPort.getDefaultValue() : Integer.parseInt(portField.getText()), true);
                portField.setText(String.valueOf(StreamTimerConfig.instance.webSocketPort.value()));
            }
            updateUrl(true);
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
        int port = StreamTimerConfig.instance.webSocketPort.value();
        if (isPortAvailable(port)) {
            try {
                if (connection == null) (connection = new WebSocketConnection(port)).start();
                else StreamTimerLoggerImpl.error("[WebSocket Integration] Server already connected!");
            } catch (Exception error) {
                StreamTimerLoggerImpl.error("[WebSocket Integration] Failed to start web socket server: " + error);
                connection = null;
            }
        } else {
            StreamTimerLoggerImpl.error("[WebSocket Integration] Port is already bound!");
        }
        enableWidgets();
    }

    public static void disconnect() {
        if (connection != null) {
            close();
        } else StreamTimerLoggerImpl.error("[WebSocket Integration] Server doesn't exist!");
        enableWidgets();
    }

    public static boolean close() {
        if (connection != null) {
            try {
                connection.stop();
                StreamTimerLoggerImpl.info("[WebSocket Integration] Closed server!");
            } catch (Exception error) {
                StreamTimerLoggerImpl.error("[WebSocket Integration] Failed to stop server: " + error);
            }
            connection = null;
            return true;
        } else {
            return false;
        }
    }

    public static void disableWidgets() {
        if (portField != null) portField.setEditable(false);
        if (connectButton != null) connectButton.setEnabled(false);
    }

    public static void enableWidgets() {
        if (connectButton != null) {
            connectButton.setText(isConnected() ? "Disconnect" : "Connect");
            connectButton.setEnabled(true);
        }
        if (portField != null) portField.setEditable(!isConnected());
    }

    public static void updateUrl(boolean useConfig) {
        if (browserSourceUrl != null) {
            browserSourceUrl.setText("file:///" + Paths.get(StaticVariables.name + "Assets").toAbsolutePath() + "\\WebSocketClient.html" + "?type=ws&host=localhost&port=" + (!useConfig && portField != null ? (portField.getText().isBlank() ? StreamTimerConfig.instance.webSocketPort.getDefaultValue() : portField.getText()) : StreamTimerConfig.instance.webSocketPort.value()) + "&width=" + StreamTimerMain.textRenderer.getHeight() + "&height=" + StreamTimerMain.textRenderer.getHeight());
        }
    }

    public static boolean isConnected() {
        return connection != null;
    }

    public static void sendProcessedFrame(byte[] frame) {
        sendFrame(frame);
    }

    public static void sendFrame(byte[] frame) {
        if (isConnected()) connection.sendFrame(frame);
    }

    public static boolean isPortAvailable(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            socket.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}

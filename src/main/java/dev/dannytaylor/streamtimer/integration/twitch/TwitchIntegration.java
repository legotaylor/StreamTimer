/*
    StreamTimer
    Contributor(s): dannytaylor
    Github: https://github.com/legotaylor/StreamTimer
    Licence: LGPL-3.0
*/

package dev.dannytaylor.streamtimer.integration.twitch;

import dev.dannytaylor.streamtimer.StreamTimerMain;
import dev.dannytaylor.streamtimer.config.RenderMode;
import dev.dannytaylor.streamtimer.config.StreamTimerConfig;
import dev.dannytaylor.streamtimer.data.StaticVariables;
import dev.dannytaylor.streamtimer.integration.AuthConfig;
import dev.dannytaylor.streamtimer.logger.StreamTimerLoggerImpl;
import dev.dannytaylor.streamtimer.render.GUI;
import dev.dannytaylor.streamtimer.render.GUIWidgets;
import dev.dannytaylor.streamtimer.util.FloatFilter;
import dev.dannytaylor.streamtimer.util.IntegerFilter;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.DefaultEditorKit;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;

public class TwitchIntegration {
    public static TwitchConnection twitch = new TwitchConnection();
    public static JButton connectButton;

    private static JTextField clientId;
    private static JTextField clientSecret;

    public static void bootstrap() {
        String name = "Twitch Integration";
        GUI.addBeforeVisible(name, TwitchIntegration::runBeforeVisible);
        GUI.addOnConfigClose(name, (renderMode) -> save());
    }

    public static void close() {
        twitch.closeConnections();
    }

    private static void runBeforeVisible(RenderMode renderMode) {
        try {
            JTabbedPane tabs = new JTabbedPane();
            tabs.addTab("Times", getTimesTab(renderMode));
            tabs.addTab("Connection", getConnectionTab(renderMode));
            StreamTimerMain.gui.configureTabs.addTab("Twitch Integration", tabs);

            if (AuthConfig.instance.twitchAutoConnect.value()) twitch.connect();
        } catch (Exception error) {
            StreamTimerLoggerImpl.error("[Twitch Integration] Failed to initialize: " + error);
        }
    }

    private static JPanel getTimesTab(RenderMode renderMode) {
        JPanel layout = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        JPanel tab = new JPanel(new GridBagLayout());
        int row = 0;

        JCheckBox follow = GUIWidgets.createCheckbox(StreamTimerConfig.instance.twitchTimes.followEnabled.value());
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 1;
        tab.add(follow, gbc);

        JTextField followSeconds = GUIWidgets.createText(String.valueOf(StreamTimerConfig.instance.twitchTimes.followSeconds.value()));
        ((AbstractDocument) followSeconds.getDocument()).setDocumentFilter(new IntegerFilter());
        gbc.gridx = 1;
        tab.add(followSeconds, gbc);

        JLabel secondsPerFollowLabel = new JLabel("seconds per follow");
        gbc.gridx = 2;
        tab.add(secondsPerFollowLabel, gbc);

        JCheckBox bits = GUIWidgets.createCheckbox(StreamTimerConfig.instance.twitchTimes.bitsEnabled.value());
        gbc.gridx = 0;
        gbc.gridy = row++;
        tab.add(bits, gbc);

        JTextField bitsSeconds = GUIWidgets.createText(String.valueOf(StreamTimerConfig.instance.twitchTimes.bitsSeconds.value()));
        ((AbstractDocument) bitsSeconds.getDocument()).setDocumentFilter(new IntegerFilter());
        gbc.gridx = 1;
        tab.add(bitsSeconds, gbc);

        JLabel secondsPerBitsLabel = new JLabel("seconds per");
        gbc.gridx = 2;
        tab.add(secondsPerBitsLabel, gbc);

        JTextField perBits = GUIWidgets.createText(String.valueOf(StreamTimerConfig.instance.twitchTimes.bits.value()));
        ((AbstractDocument) perBits.getDocument()).setDocumentFilter(new IntegerFilter());
        gbc.gridx = 3;
        tab.add(perBits, gbc);

        JLabel bitsLabel = new JLabel("bits");
        gbc.gridx = 4;
        tab.add(bitsLabel, gbc);

        JComboBox<TwitchPermission> commandCombo = new JComboBox<>(TwitchPermission.values());
        commandCombo.setSelectedItem(StreamTimerConfig.instance.twitchTimes.commandEnabled.value());
        gbc.gridx = 0;
        gbc.gridy = row++;
        tab.add(commandCombo, gbc);

        JTextField moneySeconds = GUIWidgets.createText(String.valueOf(StreamTimerConfig.instance.twitchTimes.moneySeconds.value()));
        ((AbstractDocument) moneySeconds.getDocument()).setDocumentFilter(new IntegerFilter());
        gbc.gridx = 1;
        tab.add(moneySeconds, gbc);

        JLabel secondsPerMoneyLabel = new JLabel("seconds per");
        gbc.gridx = 2;
        tab.add(secondsPerMoneyLabel, gbc);

        JTextField perMoney = GUIWidgets.createText(String.valueOf(StreamTimerConfig.instance.twitchTimes.money.value()));
        ((AbstractDocument) perMoney.getDocument()).setDocumentFilter(new FloatFilter());
        gbc.gridx = 3;
        tab.add(perMoney, gbc);

        JLabel moneyLabel = new JLabel("money");
        gbc.gridx = 4;
        tab.add(moneyLabel, gbc);

        JCheckBox tierOne = GUIWidgets.createCheckbox(StreamTimerConfig.instance.twitchTimes.tierOneEnabled.value());
        gbc.gridx = 0;
        gbc.gridy = row++;
        tab.add(tierOne, gbc);

        JTextField tierOneSeconds = GUIWidgets.createText(String.valueOf(StreamTimerConfig.instance.twitchTimes.tierOneSeconds.value()));
        ((AbstractDocument) tierOneSeconds.getDocument()).setDocumentFilter(new IntegerFilter());
        gbc.gridx = 1;
        tab.add(tierOneSeconds, gbc);

        JLabel secondsPerTierOneLabel = new JLabel("seconds per Tier 1/Prime");
        gbc.gridx = 2;
        gbc.gridwidth = 2;
        tab.add(secondsPerTierOneLabel, gbc);

        gbc.gridwidth = 1;

        JCheckBox tierTwo = GUIWidgets.createCheckbox(StreamTimerConfig.instance.twitchTimes.tierTwoEnabled.value());
        gbc.gridx = 0;
        gbc.gridy = row++;
        tab.add(tierTwo, gbc);

        JTextField tierTwoSeconds = GUIWidgets.createText(String.valueOf(StreamTimerConfig.instance.twitchTimes.tierTwoSeconds.value()));
        ((AbstractDocument) tierTwoSeconds.getDocument()).setDocumentFilter(new IntegerFilter());
        gbc.gridx = 1;
        tab.add(tierTwoSeconds, gbc);

        JLabel secondsPerTierTwoLabel = new JLabel("seconds per Tier 2");
        gbc.gridx = 2;
        gbc.gridwidth = 2;
        tab.add(secondsPerTierTwoLabel, gbc);

        gbc.gridwidth = 1;

        JCheckBox tierThree = GUIWidgets.createCheckbox(StreamTimerConfig.instance.twitchTimes.tierThreeEnabled.value());
        gbc.gridx = 0;
        gbc.gridy = row++;
        tab.add(tierThree, gbc);

        JTextField tierThreeSeconds = GUIWidgets.createText(String.valueOf(StreamTimerConfig.instance.twitchTimes.tierThreeSeconds.value()));
        ((AbstractDocument) tierThreeSeconds.getDocument()).setDocumentFilter(new IntegerFilter());
        gbc.gridx = 1;
        tab.add(tierThreeSeconds, gbc);

        JLabel secondsPerTierThreeLabel = new JLabel("seconds per Tier 3");
        gbc.gridx = 2;
        gbc.gridwidth = 2;
        tab.add(secondsPerTierThreeLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = row++;

        JLabel multiplierLabel = new JLabel("Multiplier", SwingConstants.RIGHT);
        gbc.gridwidth = 2;
        tab.add(multiplierLabel, gbc);

        JTextField multiplier = GUIWidgets.createText(String.valueOf(StreamTimerConfig.instance.twitchTimes.multiplier.value()));
        ((AbstractDocument) multiplier.getDocument()).setDocumentFilter(new FloatFilter());
        gbc.gridx = 2;
        gbc.gridwidth = 1;
        tab.add(multiplier, gbc);

        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 5;
        JButton applyChanges = GUIWidgets.createButton("Apply Changes");
        applyChanges.addActionListener(l -> {
            StreamTimerConfig.instance.twitchTimes.followSeconds.setValue(Integer.valueOf(followSeconds.getText()), false);
            StreamTimerConfig.instance.twitchTimes.bitsSeconds.setValue(Integer.valueOf(bitsSeconds.getText()), false);
            StreamTimerConfig.instance.twitchTimes.bits.setValue(Integer.valueOf(perBits.getText()), false);
            StreamTimerConfig.instance.twitchTimes.moneySeconds.setValue(Integer.valueOf(moneySeconds.getText()), false);
            StreamTimerConfig.instance.twitchTimes.money.setValue(Float.valueOf(perMoney.getText()), false);
            StreamTimerConfig.instance.twitchTimes.tierOneSeconds.setValue(Integer.valueOf(tierOneSeconds.getText()), false);
            StreamTimerConfig.instance.twitchTimes.tierTwoSeconds.setValue(Integer.valueOf(tierTwoSeconds.getText()), false);
            StreamTimerConfig.instance.twitchTimes.tierThreeSeconds.setValue(Integer.valueOf(tierThreeSeconds.getText()), false);
            StreamTimerConfig.instance.twitchTimes.multiplier.setValue(Float.valueOf(multiplier.getText()), false);

            StreamTimerConfig.instance.twitchTimes.followEnabled.setValue(follow.isSelected(), false);
            StreamTimerConfig.instance.twitchTimes.bitsEnabled.setValue(bits.isSelected(), false);
            StreamTimerConfig.instance.twitchTimes.commandEnabled.setValue((TwitchPermission) commandCombo.getSelectedItem(), false);
            StreamTimerConfig.instance.twitchTimes.tierOneEnabled.setValue(tierOne.isSelected(), false);
            StreamTimerConfig.instance.twitchTimes.tierTwoEnabled.setValue(tierTwo.isSelected(), false);
            StreamTimerConfig.instance.twitchTimes.tierThreeEnabled.setValue(tierThree.isSelected(), true);
        });
        tab.add(applyChanges, gbc);

        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 5;
        JButton cancelChanges = GUIWidgets.createButton("Cancel");
        cancelChanges.addActionListener(l -> {
            followSeconds.setText(String.valueOf(StreamTimerConfig.instance.twitchTimes.followSeconds.value()));
            bitsSeconds.setText(String.valueOf(StreamTimerConfig.instance.twitchTimes.bitsSeconds.value()));
            perBits.setText(String.valueOf(StreamTimerConfig.instance.twitchTimes.bits.value()));
            moneySeconds.setText(String.valueOf(StreamTimerConfig.instance.twitchTimes.moneySeconds.value()));
            perMoney.setText(String.valueOf(StreamTimerConfig.instance.twitchTimes.money.value()));
            tierOneSeconds.setText(String.valueOf(StreamTimerConfig.instance.twitchTimes.tierOneSeconds.value()));
            tierTwoSeconds.setText(String.valueOf(StreamTimerConfig.instance.twitchTimes.tierTwoSeconds.value()));
            tierThreeSeconds.setText(String.valueOf(StreamTimerConfig.instance.twitchTimes.tierThreeSeconds.value()));
            multiplier.setText(String.valueOf(StreamTimerConfig.instance.twitchTimes.multiplier.value()));

            follow.setSelected(StreamTimerConfig.instance.twitchTimes.followEnabled.value());
            bits.setSelected(StreamTimerConfig.instance.twitchTimes.bitsEnabled.value());
            commandCombo.setSelectedItem(StreamTimerConfig.instance.twitchTimes.commandEnabled.value());
            tierOne.setSelected(StreamTimerConfig.instance.twitchTimes.tierOneEnabled.value());
            tierTwo.setSelected(StreamTimerConfig.instance.twitchTimes.tierTwoEnabled.value());
            tierThree.setSelected(StreamTimerConfig.instance.twitchTimes.tierThreeEnabled.value());
        });
        tab.add(cancelChanges, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        layout.add(tab, gbc);

        return layout;
    }

    private static JPanel getConnectionTab(RenderMode renderMode) {
        JPanel layout = new JPanel(new GridBagLayout());
        JPanel tab = new JPanel(new GridBagLayout());

        String doNotShareSecretMessage = "Sharing your Client Secret can compromise your Twitch account. You can reset your Client Secret in your Twitch Developer Console.";

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
        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        tab.add(nameLabel, gbc);
        gbc.gridx = 2;
        JLabel name = new JLabel("This can be anything as long as it's unique");
        name.setToolTipText("I suggest using \"your_channel_name's " + StaticVariables.name +"\".");
        name.setFont(name.getFont().deriveFont(Font.BOLD));
        tab.add(name, gbc);

        // OAuth Redirect URL (for reference)
        gbc.gridy = row++;
        gbc.gridx = 1;
        JLabel oAuthRedirectUrlLabel = new JLabel("OAuth Redirect URL:");
        oAuthRedirectUrlLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        tab.add(oAuthRedirectUrlLabel, gbc);
        gbc.gridx = 2;
        JTextField oAuthRedirectUrl = new JTextField("http://localhost:" + AuthConfig.instance.twitchPort.value() + "/callback");
        oAuthRedirectUrl.setEditable(false);
        tab.add(oAuthRedirectUrl, gbc);

        // Client Type (for reference)
        gbc.gridy = row++;
        gbc.gridx = 1;
        JLabel clientTypeLabel = new JLabel("Client Type:");
        clientTypeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        tab.add(clientTypeLabel, gbc);
        gbc.gridx = 2;
        JLabel clientType = new JLabel("Confidential");
        clientType.setFont(clientType.getFont().deriveFont(Font.BOLD));
        tab.add(clientType, gbc);

        // Client ID
        gbc.gridy = row++;
        gbc.weightx = 0;
        gbc.gridx = 1;
        JLabel clientIdLabel = new JLabel("Client ID:");
        clientIdLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        tab.add(clientIdLabel, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0;
        clientId = new JTextField(AuthConfig.instance.twitchId.value());
        clientId.setEnabled(TwitchIntegration.twitch.hasClient());
        tab.add(clientId, gbc);

        // DO NOT SHARE
        gbc.gridy = row++;
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        JLabel doNotShareLabel = new JLabel("DO NOT SHARE YOUR CLIENT SECRET WITH ANYONE!");
        doNotShareLabel.setToolTipText(doNotShareSecretMessage);
        doNotShareLabel.setFont(doNotShareLabel.getFont().deriveFont(Font.BOLD));
        doNotShareLabel.setForeground(new Color(255, 127, 127));
        tab.add(doNotShareLabel, gbc);

        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        // Client Secret
        gbc.gridy = row++;
        gbc.gridx = 1;
        JLabel clientSecretLabel = new JLabel("Client Secret:");
        clientSecretLabel.setToolTipText(doNotShareSecretMessage);
        clientSecretLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        tab.add(clientSecretLabel, gbc);
        gbc.gridx = 2;
        String secret = "";
        try {
            secret = AuthConfig.decrypt(AuthConfig.instance.twitchSecret.value());
        } catch (Exception error) {
            System.err.println("Failed to decrypt secret: " + error);
        }
        clientSecret = new JPasswordField(secret);
        clientSecret.setEnabled(TwitchIntegration.twitch.hasClient());
        clientSecret.getActionMap().put(DefaultEditorKit.copyAction, null);
        clientSecret.getActionMap().put(DefaultEditorKit.cutAction, null);
        clientSecret.setToolTipText(doNotShareSecretMessage);
        tab.add(clientSecret, gbc);

        // Client ID/Secret Info
        gbc.gridy = row++;
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        JLabel clientInfoLabel = new JLabel("<html><body style='text-align:center;'><a href=\"https://dev.twitch.tv/console\">You can register/modify your application at dev.twitch.tv/console</a></body></html>");
        clientInfoLabel.setToolTipText("Opens the Twitch Developer Console in your browser.");
        clientInfoLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        clientInfoLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI("https://dev.twitch.tv/console"));
                } catch (Exception error) {
                    clientInfoLabel.setEnabled(false);
                    System.err.println("Failed to open uri!");
                }
            }
        });
        tab.add(clientInfoLabel, gbc);

        // Auto Connect
        gbc.gridy = row++;
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        JCheckBox autoConnect = GUIWidgets.createCheckbox("Automatically connect on Startup", AuthConfig.instance.twitchAutoConnect.value());
        autoConnect.addChangeListener(l -> {
            AuthConfig.instance.twitchAutoConnect.setValue(autoConnect.isSelected(), true);
        });
        tab.add(autoConnect, gbc);

        // Connect/Disconnect
        gbc.gridy = row++;
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        connectButton = new JButton(twitch.hasClient() ? "Disconnect" : "Connect");
        connectButton.addActionListener(l -> {
            save();
            connectButton.setEnabled(false);
            connectButton.setText(connectButton.getText() + "ing...");
            twitch.toggleConnected();
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

    private static void save() {
        String secret = "";
        try {
            if (!clientSecret.getText().isBlank()) secret = AuthConfig.encrypt(clientSecret.getText());
        } catch (Exception error) {
            StreamTimerLoggerImpl.error("[Twitch Integration] Failed to encrypt secret: " + error);
        }
        AuthConfig.instance.twitchId.setValue(clientId.getText(), true);
        AuthConfig.instance.twitchSecret.setValue(secret, true);
    }

    public static void setIdSecretEnabled(boolean value) {
        if (clientId != null) clientId.setEnabled(value);
        if (clientSecret != null) clientSecret.setEnabled(value);
    }
}

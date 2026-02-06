/*
    StreamTimer
    Contributor(s): dannytaylor
    Github: https://github.com/legotaylor/StreamTimer
    Licence: LGPL-3.0
*/

package dev.dannytaylor.streamtimer.render;

import dev.dannytaylor.streamtimer.StreamTimerMain;
import dev.dannytaylor.streamtimer.config.RenderMode;
import dev.dannytaylor.streamtimer.config.StreamTimerConfig;
import dev.dannytaylor.streamtimer.data.StaticVariables;
import dev.dannytaylor.streamtimer.logger.StreamTimerLoggerImpl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.nio.file.Path;

public class SetupGUI {
    public JFrame window;

    public SetupGUI() {
        SwingUtilities.invokeLater(this::create);
    }
    
    private void create() {
        StreamTimerLoggerImpl.info("Initializing GUI...");
        if (StreamTimerConfig.instance.skipSetupScreen.value()) launch(StreamTimerConfig.instance.previousRenderMode.value());
        else {
            RenderMode previousRenderType = StreamTimerConfig.instance.previousRenderMode.value();
            StreamTimerLoggerImpl.info("Launching Setup GUI...");
            this.window = new JFrame(StaticVariables.name + ": Setup");
            this.window.setLayout(new BorderLayout());

            JPanel textPanel = new JPanel();
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            textPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

            JLabel title = new JLabel("How would you like to open " + StaticVariables.name + "?");
            title.setFont(title.getFont().deriveFont(Font.BOLD, 24.0F));
            GUI.setCentered(title);

            JLabel description = new JLabel("You can hover your mouse over the options to see a description.");
            description.setFont(title.getFont().deriveFont(Font.PLAIN, 12.0F));
            GUI.setCentered(description);

            textPanel.add(title);
            textPanel.add(Box.createVerticalStrut(4));
            textPanel.add(description);

            JPanel renderModePanel = new JPanel();
            renderModePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 24, 0));

            JRadioButton dialogButton = GUIWidgets.createRadioButton("Dialog");
            dialogButton.setToolTipText("Runs as a dialog.");
            GUI.setCentered(dialogButton);
            dialogButton.setSelected(previousRenderType.getRenderType().equals(RenderMode.RenderType.DIALOG));

            JRadioButton frameButton = GUIWidgets.createRadioButton("Window");
            frameButton.setToolTipText("Runs in a window.");
            GUI.setCentered(frameButton);
            frameButton.setSelected(!dialogButton.isSelected() && previousRenderType.getRenderType().equals(RenderMode.RenderType.FRAME));

            ButtonGroup renderMode = new ButtonGroup();
            renderMode.add(dialogButton);
            renderMode.add(frameButton);
            renderModePanel.add(dialogButton);
            renderModePanel.add(frameButton);

            JPanel buttonsPanel = new JPanel();
            buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
            buttonsPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

            JCheckBox legacyMode = GUIWidgets.createCheckbox("Use Legacy Renderer", !previousRenderType.usesGL());
            legacyMode.setToolTipText("Opens using the legacy rendering system. Features requiring OpenGL will not be available!\nHowever you can minimize when opened as a Window whilst using WebSocket!");
            GUI.setCentered(legacyMode);

            JButton launchButton = GUIWidgets.createButton("Launch!");
            launchButton.setToolTipText("Opens " + StaticVariables.name + " using the selected settings!");
            GUI.setCentered(launchButton);

            JCheckBox dontShowAgain = GUIWidgets.createCheckbox("Don't show this screen again!");
            dontShowAgain.setToolTipText("If enabled, this screen will be skipped on future start ups.");
            GUI.setCentered(dontShowAgain);
            dontShowAgain.addChangeListener(l -> StreamTimerConfig.instance.skipSetupScreen.setValue(dontShowAgain.isSelected(), true));

            launchButton.addActionListener(l -> {
                launchButton.setEnabled(false);
                if (dialogButton.isSelected() || frameButton.isSelected()) {
                    frameButton.setEnabled(false);
                    dialogButton.setEnabled(false);
                    if (dialogButton.isSelected()) {
                        launch(legacyMode.isSelected() ? RenderMode.TEXT_DIALOG : RenderMode.GL_DIALOG);
                    } else if (frameButton.isSelected()) {
                        launch(legacyMode.isSelected() ? RenderMode.TEXT_FRAME : RenderMode.GL_FRAME);
                    }
                }
            });

            buttonsPanel.add(legacyMode);
            buttonsPanel.add(Box.createVerticalStrut(8));
            buttonsPanel.add(launchButton);
            buttonsPanel.add(Box.createVerticalStrut(12));
            buttonsPanel.add(dontShowAgain);

            this.window.add(textPanel, BorderLayout.NORTH);
            this.window.add(renderModePanel, BorderLayout.CENTER);
            this.window.add(buttonsPanel, BorderLayout.SOUTH);

            this.window.pack();
            this.window.setResizable(false);
            this.window.setLocationRelativeTo(null);
            if (StreamTimerMain.icon != null) this.window.setIconImage(StreamTimerMain.icon.getImage());
            this.window.setVisible(true);

            this.window.addWindowListener(new WindowListener() {
                @Override
                public void windowOpened(WindowEvent e) {
                }

                @Override
                public void windowClosing(WindowEvent e) {
                    StreamTimerMain.canStart = false;
                    StreamTimerMain.gui.latch.countDown();
                }

                @Override
                public void windowClosed(WindowEvent e) {
                }

                @Override
                public void windowIconified(WindowEvent e) {
                }

                @Override
                public void windowDeiconified(WindowEvent e) {
                }

                @Override
                public void windowActivated(WindowEvent e) {
                }

                @Override
                public void windowDeactivated(WindowEvent e) {
                }
            });
        }
    }

    private void launch(RenderMode renderMode) {
        StreamTimerConfig.instance.previousRenderMode.setValue(renderMode, true);
        if (this.window != null) this.window.setVisible(false);

        RenderMode verifiedRenderMode = renderMode;
        if (renderMode.usesGL()) {
            if (!Path.of(StaticVariables.name + "Assets/vertex.glsl").toFile().exists() || !Path.of(StaticVariables.name + "Assets/fragment.glsl").toFile().exists()) {
                if (renderMode.getRenderType().equals(RenderMode.RenderType.FRAME)) verifiedRenderMode = RenderMode.TEXT_FRAME;
                else if (renderMode.getRenderType().equals(RenderMode.RenderType.DIALOG)) verifiedRenderMode = RenderMode.TEXT_DIALOG;
                StreamTimerLoggerImpl.warn("Failed to load assets: Using legacy renderer instead!");
            }
        }

        StreamTimerMain.gui.create(verifiedRenderMode);
    }
}

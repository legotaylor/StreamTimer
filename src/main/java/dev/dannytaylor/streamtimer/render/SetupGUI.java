package dev.dannytaylor.streamtimer.render;

import dev.dannytaylor.streamtimer.StreamTimerMain;
import dev.dannytaylor.streamtimer.config.RenderMode;
import dev.dannytaylor.streamtimer.config.StreamTimerConfig;
import dev.dannytaylor.streamtimer.data.StaticVariables;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class SetupGUI {
    private JFrame frame;

    public SetupGUI() {
        SwingUtilities.invokeLater(this::init);
    }
    
    private void init() {
        System.out.println("[Stream Timer] Initializing GUI...");
        if (StreamTimerConfig.instance.skipSetupScreen.value()) launch(StreamTimerConfig.instance.previousRenderMode.value());
        else {
            System.out.println("[Stream Timer] Launching Setup GUI...");
            this.frame = new JFrame(StaticVariables.name + ": Setup");
            Dimension size = new Dimension(576, 200);
            JPanel textPanel = new JPanel();
            textPanel.setLayout(new BorderLayout());

            JLabel title = new JLabel("How would you like to open " + StaticVariables.name + "?");
            title.setFont(title.getFont().deriveFont(Font.BOLD, 24));
            GUI.setCentered(title);

            JLabel description = new JLabel("You can hover over the buttons to see a description.");
            description.setFont(title.getFont().deriveFont(Font.PLAIN, 12));
            GUI.setCentered(description);

            textPanel.add(title, BorderLayout.NORTH);
            textPanel.add(description, BorderLayout.SOUTH);

            JPanel buttonsPanel = new JPanel();
            buttonsPanel.setLayout(new BorderLayout());

            JPanel glButtonsPanel = new JPanel();
            glButtonsPanel.setLayout(new BorderLayout());

            JButton frameModeGL = GUIWidgets.createButton("Window");
            frameModeGL.setToolTipText("Runs in a window. This won't always prevent minimizing, and you'll see the icon in your taskbar.");

            JButton dialogModeGL = GUIWidgets.createButton("Dialog");
            dialogModeGL.setToolTipText("Runs as a dialog. This prevents minimising, however you will have use the tray icon to bring the window to the front.");

            JCheckBox legacyMode = GUIWidgets.createCheckbox("Use Legacy Renderer");
            legacyMode.setToolTipText("Opens using the legacy rendering system. Features requiring OpenGL will not be available!");

            frameModeGL.addActionListener(l -> {
                StreamTimerConfig.instance.previousRenderMode.setValue(RenderMode.GL_FRAME, true);
                frameModeGL.setEnabled(false);
                dialogModeGL.setEnabled(false);
                launch(legacyMode.isSelected() ? RenderMode.TEXT_FRAME : RenderMode.GL_FRAME);
            });

            dialogModeGL.addActionListener(l -> {
                StreamTimerConfig.instance.previousRenderMode.setValue(RenderMode.GL_DIALOG, true);
                frameModeGL.setEnabled(false);
                dialogModeGL.setEnabled(false);
                launch(legacyMode.isSelected() ? RenderMode.TEXT_DIALOG : RenderMode.GL_DIALOG);
            });

            JPanel legacyModePanel = new JPanel();
            legacyModePanel.add(legacyMode);
            GUI.setCentered(legacyModePanel);

            JPanel dontShowAgainPanel = new JPanel();
            JCheckBox dontShowAgain = GUIWidgets.createCheckbox("Don't show this screen again!");
            dontShowAgain.setToolTipText("If enabled, this screen will be skipped and the render mode will be remembered for future start ups.");
            dontShowAgain.addChangeListener(l -> StreamTimerConfig.instance.skipSetupScreen.setValue(dontShowAgain.isSelected(), true));
            dontShowAgainPanel.add(dontShowAgain);
            GUI.setCentered(dontShowAgainPanel);

            glButtonsPanel.add(dialogModeGL, BorderLayout.NORTH);
            glButtonsPanel.add(frameModeGL, BorderLayout.SOUTH);

            buttonsPanel.add(glButtonsPanel, BorderLayout.NORTH);
            buttonsPanel.add(legacyModePanel, BorderLayout.CENTER);
            buttonsPanel.add(dontShowAgainPanel, BorderLayout.SOUTH);

            this.frame.add(textPanel, BorderLayout.NORTH);
            this.frame.add(buttonsPanel, BorderLayout.SOUTH);

            this.frame.pack();
            this.frame.setMinimumSize(size);
            this.frame.setSize(size.width, size.height);
            this.frame.setResizable(false);

            this.frame.setLocationRelativeTo(null);
            this.frame.setVisible(true);

            this.frame.addWindowListener(new WindowListener() {
                @Override
                public void windowOpened(WindowEvent e) {

                }

                @Override
                public void windowClosing(WindowEvent e) {
                    System.exit(1);
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
        this.frame.setVisible(false);
        StreamTimerMain.gui.init(renderMode);
    }
}

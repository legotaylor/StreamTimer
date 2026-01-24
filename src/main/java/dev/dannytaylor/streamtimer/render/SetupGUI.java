package dev.dannytaylor.streamtimer.render;

import dev.dannytaylor.streamtimer.StreamTimerMain;
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
        if (StreamTimerConfig.instance.skipSetupScreen.value()) launch(StreamTimerConfig.instance.previouslyStartedAsDialog.value());
        else {
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

            JButton frameMode = new JButton("Open as Frame");
            frameMode.setToolTipText("Runs as a frame. This won't always prevent minimizing, and you'll see the icon in your taskbar.");

            JButton dialogMode = new JButton("Open as Dialog");
            dialogMode.setToolTipText("Runs as a dialog. This prevents minimising, however you will have use the tray icon to bring the window to the front.");

            frameMode.addActionListener(l -> {
                frameMode.setEnabled(false);
                dialogMode.setEnabled(false);
                launch(false);
            });
            dialogMode.addActionListener(l -> {
                StreamTimerConfig.instance.previouslyStartedAsDialog.setValue(true, true);
                frameMode.setEnabled(false);
                dialogMode.setEnabled(false);
                launch(true);
            });

            JPanel dontShowAgainPanel = new JPanel();
            JCheckBox dontShowAgain = new JCheckBox();
            dontShowAgain.addChangeListener(l -> StreamTimerConfig.instance.skipSetupScreen.setValue(dontShowAgain.isSelected(), true));
            dontShowAgainPanel.add(dontShowAgain);
            dontShowAgainPanel.add(new JLabel("Don't show this screen again!"));
            GUI.setCentered(dontShowAgainPanel);

            buttonsPanel.add(frameMode, BorderLayout.NORTH);
            buttonsPanel.add(dialogMode, BorderLayout.CENTER);
            buttonsPanel.add(dontShowAgainPanel, BorderLayout.SOUTH);

            this.frame.add(textPanel, BorderLayout.NORTH);
            this.frame.add(buttonsPanel, BorderLayout.SOUTH);

            this.frame.pack();
            this.frame.setMinimumSize(size);
            this.frame.setSize(size.width, size.height);
            this.frame.setResizable(false);

            try {
                if (StreamTimerMain.gui.icon != null) this.frame.setIconImage(StreamTimerMain.gui.icon.getImage());
            } catch (Exception error) {
                System.err.println("[Stream Timer] Failed to set icon: " + error);
            }

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

    private void launch(boolean dialog) {
        StreamTimerConfig.instance.previouslyStartedAsDialog.setValue(dialog, true);
        this.frame.setVisible(false);
        StreamTimerMain.gui.init(dialog);
    }
}

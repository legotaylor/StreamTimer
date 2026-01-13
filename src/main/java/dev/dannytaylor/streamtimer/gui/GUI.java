package dev.dannytaylor.streamtimer.gui;

import com.formdev.flatlaf.FlatDarculaLaf;
import dev.dannytaylor.streamtimer.StreamTimerMain;
import dev.dannytaylor.streamtimer.config.StreamTimerConfig;
import dev.dannytaylor.streamtimer.data.StaticVariables;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.CountDownLatch;
import dev.dannytaylor.streamtimer.timer.TimerUtils;

public class GUI {
    private JFrame frame;
    public JLabel timerText;
    public JLabel messageText;
    public JButton toggleButton;
    public CountDownLatch latch = new CountDownLatch(1);

    public GUI() {
        SwingUtilities.invokeLater(this::init);
    }

    private void init() {
        System.out.println("[Stream Timer] Launching GUI...");
        setTheme(frame);
        frame = new JFrame(StaticVariables.name);

        JPanel timerPanel = new JPanel();

        timerPanel.setBackground(new Color(0x00FF00));
        timerText = new JLabel(TimerUtils.getTime(StreamTimerConfig.instance.time.value()));
        timerText.setForeground(new Color(0xffffff));
        timerText.setFont(timerText.getFont().deriveFont(Font.BOLD, 72f));
        timerPanel.add(setCentered(timerText));
        frame.add(timerPanel, BorderLayout.NORTH);

        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));

        JPanel togglePanel = new JPanel();
        toggleButton = new JButton("START");
        toggleButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (StreamTimerMain.timer.isRunning()) {
                    StreamTimerMain.timer.stop();
                    messageText.setText("Stopped timer!");
                    toggleButton.setText("START");
                } else {
                    StreamTimerMain.timer.start();
                    messageText.setText("Started timer!");
                    toggleButton.setText("STOP");
                }
            }
        });
        toggleButton.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                messageText.setText("⠀");
            }
        });
        togglePanel.add(toggleButton);
        settingsPanel.add(togglePanel);

        JPanel timerSettings = new JPanel();

        JTextField hours = new JTextField("01", 2);
        ((AbstractDocument)hours.getDocument()).setDocumentFilter(new NumberFilter());
        timerSettings.add(hours);
        timerSettings.add(new JLabel(":"));
        JTextField minutes = new JTextField("00", 2);
        ((AbstractDocument)minutes.getDocument()).setDocumentFilter(new NumberFilter());
        timerSettings.add(minutes);
        timerSettings.add(new JLabel(":"));
        JTextField seconds = new JTextField("00", 2);
        ((AbstractDocument)seconds.getDocument()).setDocumentFilter(new NumberFilter());
        timerSettings.add(seconds);

        JButton addButton = new JButton("ADD");
        addButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateTimer(TimerUtils.getSecondsFromString(hours.getText(), minutes.getText(), seconds.getText()), true, true);
                messageText.setText("Added " + TimerUtils.getTimeFromString(hours.getText(), minutes.getText(), seconds.getText()) + " to timer!");
            }
        });
        addButton.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                messageText.setText("⠀");
            }
        });
        timerSettings.add(addButton);

        JButton removeButton = new JButton("SUBTRACT");
        removeButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateTimer(-TimerUtils.getSecondsFromString(hours.getText(), minutes.getText(), seconds.getText()), true, true);
                messageText.setText("Subtracted " + TimerUtils.getTimeFromString(hours.getText(), minutes.getText(), seconds.getText()) + " from timer!");
            }
        });
        removeButton.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                messageText.setText("⠀");
            }
        });
        timerSettings.add(removeButton);

        JButton setButton = new JButton("SET");
        setButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateTimer(TimerUtils.getSecondsFromString(hours.getText(), minutes.getText(), seconds.getText()), false, true);
                messageText.setText("Set timer to " + TimerUtils.getTimeFromString(hours.getText(), minutes.getText(), seconds.getText()));
            }
        });
        setButton.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                messageText.setText("⠀");
            }
        });
        timerSettings.add(setButton);

        settingsPanel.add(timerSettings);

        frame.add(settingsPanel, BorderLayout.CENTER);

        JPanel messagePanel = new JPanel();
        messagePanel.add(messageText = new JLabel("⠀"));
        frame.add(messagePanel, BorderLayout.SOUTH);

        frame.pack();
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        Dimension size = new Dimension(576, 288);
        frame.setMinimumSize(size);
        frame.setSize(size.width, size.height);
        frame.setResizable(false);

        try {
            ImageIcon logo = Texture.getTexture(this.getClass().getResource(StaticVariables.logo), 64, 64);
            if (logo != null) frame.setIconImage(logo.getImage());
        } catch (Exception error) {
            System.err.println("[Stream Timer] Failed to set icon: " + error);
        }

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        frame.addWindowListener(new WindowListener() {
            public void windowOpened(WindowEvent e) {
            }
            public void windowClosing(WindowEvent e) {
                StreamTimerMain.timer.stop();
                StreamTimerMain.running = false;
            }
            public void windowClosed(WindowEvent e) {
            }
            public void windowIconified(WindowEvent e) {
            }
            public void windowDeiconified(WindowEvent e) {
            }
            public void windowActivated(WindowEvent e) {
            }
            public void windowDeactivated(WindowEvent e) {
            }
        });

        latch.countDown();
    }

    public JLabel setCentered(JLabel label) {
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        return label;
    }

    private void setTheme(Frame frame) {
        try {
            UIManager.setLookAndFeel(new FlatDarculaLaf());
            if (frame != null) {
                Dimension size = frame.getSize();
                SwingUtilities.updateComponentTreeUI(frame);
                frame.setSize(size);
            }
        } catch (Exception error) {
            System.err.println("[Stream Timer] Error setting theme: " + error);
        }
    }

    public void updateTimer(long seconds, boolean add, boolean save) {
        TimerUtils.setTimer(seconds, add, save);
        updateTimer();
    }

    public void updateTimer() {
        String time = TimerUtils.getTime();
        if (!timerText.getText().equals(time)) timerText.setText(time);

        if (StreamTimerMain.timer.isRunning() && TimerUtils.getMillis() <= 0) {
            StreamTimerMain.timer.stop();
            messageText.setText("Timer finished!");
            toggleButton.setText("START");
        }
    }
}

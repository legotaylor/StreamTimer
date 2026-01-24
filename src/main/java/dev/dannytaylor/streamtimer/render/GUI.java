package dev.dannytaylor.streamtimer.render;

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
import dev.dannytaylor.streamtimer.util.NumberFilter;

public class GUI {
    private JFrame frame;
    public TextRendererPanel timer;
    public JLabel messageText;
    public JButton toggleButton;
    public CountDownLatch latch = new CountDownLatch(1);

    public GUI() {
        SwingUtilities.invokeLater(this::init);
    }

    private void init() {
        System.out.println("[Stream Timer] Launching GUI...");
        setTheme(this.frame);
        Dimension size = new Dimension(576, 320);
        this.frame = new JFrame(StaticVariables.name);
        this.frame.setLayout(new BorderLayout());

        JPanel timerPanel = new JPanel();
        timerPanel.setLayout(new BorderLayout());

        timerPanel.setBackground(new Color(StreamTimerConfig.instance.backgroundColor.value()));
        this.timer = new TextRendererPanel();
        this.timer.setPreferredSize(new Dimension(576, 144));
        this.timer.setBackground(new Color(StreamTimerConfig.instance.backgroundColor.value()));
        timerPanel.add(this.timer, BorderLayout.CENTER);

        this.frame.add(timerPanel, BorderLayout.NORTH);

        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));

        JPanel togglePanel = new JPanel();
        this.toggleButton = new JButton("START");
        this.toggleButton.addActionListener(new AbstractAction() {
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
        this.toggleButton.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                messageText.setText("⠀");
            }
        });
        togglePanel.add(setCentered(this.toggleButton));
        settingsPanel.add(togglePanel);

        JPanel timerSettings = new JPanel();

        JTextField hours = new JTextField(StreamTimerConfig.instance.addHours.value(), 2);
        ((AbstractDocument)hours.getDocument()).setDocumentFilter(new NumberFilter());
        timerSettings.add(hours);
        timerSettings.add(new JLabel(":"));
        JTextField minutes = new JTextField(StreamTimerConfig.instance.addMinutes.value(), 2);
        ((AbstractDocument)minutes.getDocument()).setDocumentFilter(new NumberFilter());
        timerSettings.add(minutes);
        timerSettings.add(new JLabel(":"));
        JTextField seconds = new JTextField(StreamTimerConfig.instance.addSeconds.value(), 2);
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

        JPanel forceFocusSettings = new JPanel();
        JCheckBox forceFocus = new JCheckBox();
        forceFocus.setSelected(StreamTimerConfig.instance.forceFocus.value());
        forceFocus.addChangeListener(listener -> {
            StreamTimerConfig.instance.forceFocus.setValue(forceFocus.isSelected(), true);
        });
        forceFocusSettings.add(new JLabel("Prevent minimize"));
        forceFocusSettings.add(forceFocus);
        setCentered(forceFocusSettings);

        settingsPanel.add(forceFocusSettings);
        settingsPanel.setPreferredSize(new Dimension(576, 176));
        this.frame.add(settingsPanel, BorderLayout.CENTER);

        JPanel messagePanel = new JPanel();
        messagePanel.add(this.messageText = new JLabel("⠀"));
        this.frame.add(messagePanel, BorderLayout.SOUTH);

        this.frame.pack();
        this.frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        this.frame.setMinimumSize(size);
        this.frame.setSize(size.width, size.height);
        this.frame.setResizable(false);

        try {
            ImageIcon logo = Texture.getTexture(this.getClass().getResource(StaticVariables.logo), 64, 64);
            if (logo != null) this.frame.setIconImage(logo.getImage());
        } catch (Exception error) {
            System.err.println("[Stream Timer] Failed to set icon: " + error);
        }

        this.frame.setLocationRelativeTo(null);
        this.frame.setVisible(true);

        this.frame.addWindowListener(new WindowListener() {
            public void windowOpened(WindowEvent e) {
            }
            public void windowClosing(WindowEvent e) {
                StreamTimerMain.timer.stop();
                StreamTimerMain.running = false;
            }
            public void windowClosed(WindowEvent e) {
            }
            public void windowIconified(WindowEvent e) {
                if (StreamTimerConfig.instance.forceFocus.value()) {
                    frame.setExtendedState(Frame.NORMAL);
                    frame.requestFocus();
                }
            }
            public void windowDeiconified(WindowEvent e) {
            }
            public void windowActivated(WindowEvent e) {
            }
            public void windowDeactivated(WindowEvent e) {
            }
        });

        this.latch.countDown();
    }

    public JComponent setCentered(JComponent component) {
        component.setAlignmentX(Component.CENTER_ALIGNMENT);
        if (component instanceof JLabel) {
            JLabel label = ((JLabel)component);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setVerticalAlignment(SwingConstants.CENTER);
        }
        return component;
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
        this.timer.setImage(StreamTimerMain.textRenderer.getFramebuffer());

        if (StreamTimerMain.timer.isRunning() && TimerUtils.getMillis() <= 0) {
            StreamTimerMain.timer.stop();
            messageText.setText("Timer finished!");
            toggleButton.setText("START");
        }
    }
}

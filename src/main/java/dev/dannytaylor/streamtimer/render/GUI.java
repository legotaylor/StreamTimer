package dev.dannytaylor.streamtimer.render;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import dev.dannytaylor.streamtimer.StreamTimerMain;
import dev.dannytaylor.streamtimer.config.RenderMode;
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
    public Window window;
    public JPanel timer;
    public JLabel messageText;
    public JButton toggleButton;

    public SystemTray systemTray;
    public TrayIcon trayIcon;

    public ImageIcon icon;

    public CountDownLatch latch = new CountDownLatch(1);

    public SetupGUI setupGUI;

    public GUI() {
        setup();
        setupGUI = new SetupGUI();
    }

    public void setup() {
        setTheme(this.window);
        this.icon = Resources.getTexture(this.getClass().getResource(StaticVariables.logo), 64, 64);
    }

    public void init(RenderMode renderMode) {
        System.out.println("[Stream Timer] Launching GUI...");
        Dimension size = new Dimension(576, 320);
        boolean isDialog = renderMode.getRenderType().equals(RenderMode.RenderType.DIALOG);
        this.window = isDialog ? new JDialog((Frame) null, StaticVariables.name) : new JFrame(StaticVariables.name);
        this.window.setLayout(new BorderLayout());
        initTrayIcon();

        JPanel timerPanel = new JPanel();
        timerPanel.setLayout(new BorderLayout());
        timerPanel.setBackground(new Color(StreamTimerConfig.instance.backgroundColor.value()));
        this.timer = renderMode.usesGL() ? new GLRendererPanel(new GLCapabilities(GLProfile.get(GLProfile.GL2))) : new TextRendererPanel();
        this.timer.setPreferredSize(new Dimension(576, 144));
        this.timer.setBackground(new Color(StreamTimerConfig.instance.backgroundColor.value()));
        timerPanel.add(this.timer, BorderLayout.CENTER);

        this.window.add(timerPanel, BorderLayout.NORTH);

        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));

        JPanel togglePanel = new JPanel();
        this.toggleButton = GUIWidgets.createButton("START");
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

        JTextField hours = GUIWidgets.createText(StreamTimerConfig.instance.addHours.value(), 2);
        ((AbstractDocument)hours.getDocument()).setDocumentFilter(new NumberFilter());
        timerSettings.add(hours);
        timerSettings.add(new JLabel(":"));
        JTextField minutes = GUIWidgets.createText(StreamTimerConfig.instance.addMinutes.value(), 2);
        ((AbstractDocument)minutes.getDocument()).setDocumentFilter(new NumberFilter());
        timerSettings.add(minutes);
        timerSettings.add(new JLabel(":"));
        JTextField seconds = GUIWidgets.createText(StreamTimerConfig.instance.addSeconds.value(), 2);
        ((AbstractDocument)seconds.getDocument()).setDocumentFilter(new NumberFilter());
        timerSettings.add(seconds);

        JButton addButton = GUIWidgets.createButton("ADD");
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

        JButton removeButton = GUIWidgets.createButton("SUBTRACT");
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

        JButton setButton = GUIWidgets.createButton("SET");
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

        JPanel extraSettings = new JPanel();
        if (!isDialog) {
            JCheckBox forceFocus = GUIWidgets.createCheckbox("Prevent minimize");
            forceFocus.setSelected(StreamTimerConfig.instance.forceFocus.value());
            forceFocus.addChangeListener(listener -> {
                StreamTimerConfig.instance.forceFocus.setValue(forceFocus.isSelected(), true);
            });
            extraSettings.add(forceFocus);
            setCentered(extraSettings);
            settingsPanel.add(extraSettings);
        }

        if (this.timer instanceof GLRendererPanel) {
//            JCheckBox spout = GUIWidgets.createCheckbox("Spout Broadcasting");
//            spout.setSelected(StreamTimerConfig.instance.spout.value());
//            spout.addChangeListener(listener -> {
//                StreamTimerConfig.instance.spout.setValue(spout.isSelected(), true);
//            });
//            extraSettings.add(spout);

            JCheckBox reversed = GUIWidgets.createCheckbox("Count up");
            reversed.setSelected(StreamTimerConfig.instance.reversed.value());
            reversed.addChangeListener(listener -> {
                StreamTimerConfig.instance.reversed.setValue(reversed.isSelected(), true);
            });
            extraSettings.add(reversed);

            JCheckBox background = GUIWidgets.createCheckbox("Render Background");
            background.setSelected(StreamTimerConfig.instance.background.value());
            background.addChangeListener(listener -> {
                StreamTimerConfig.instance.background.setValue(background.isSelected(), true);
            });
            extraSettings.add(background);

            JCheckBox rainbow = GUIWidgets.createCheckbox("Rainbow Mode");
            rainbow.setSelected(StreamTimerConfig.instance.rainbow.value());
            rainbow.addChangeListener(listener -> {
                StreamTimerConfig.instance.rainbow.setValue(rainbow.isSelected(), true);
            });
            extraSettings.add(rainbow);
        }

        setCentered(extraSettings);
        settingsPanel.add(extraSettings);

        settingsPanel.setPreferredSize(new Dimension(576, 176));
        this.window.add(settingsPanel, BorderLayout.CENTER);

        JPanel messagePanel = new JPanel();
        messagePanel.add(this.messageText = new JLabel("⠀"));
        this.window.add(messagePanel, BorderLayout.SOUTH);

        this.window.pack();
        if (this.window instanceof JFrame) {
            ((JFrame)this.window).setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        } else if (this.window instanceof JDialog) {
            ((JDialog)this.window).setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        }
        this.window.setMinimumSize(size);
        this.window.setSize(size.width, size.height);

        if (this.window instanceof JFrame) {
            //((JFrame)this.window).setResizable(false);
        } else if (this.window instanceof JDialog) {
            //((JDialog)this.window).setResizable(false);
        }

        try {
            if (this.icon != null) this.window.setIconImage(this.icon.getImage());
        } catch (Exception error) {
            System.err.println("[Stream Timer] Failed to set icon: " + error);
        }

        this.window.setLocationRelativeTo(null);
        this.window.setVisible(true);

        this.window.addWindowListener(new WindowListener() {
            public void windowOpened(WindowEvent e) {
            }
            public void windowClosing(WindowEvent e) {
                StreamTimerMain.timer.stop();
                StreamTimerMain.running = false;
            }
            public void windowClosed(WindowEvent e) {
            }
            public void windowIconified(WindowEvent e) {
                if (window instanceof JFrame) {
                    JFrame frame = (JFrame) window;
                    frame.setVisible(true);
                    if (StreamTimerConfig.instance.forceFocus.value()) {
                        frame.setExtendedState(Frame.NORMAL);
                        frame.requestFocus();
                    }
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

    public static JComponent setCentered(JComponent component) {
        component.setAlignmentX(Component.CENTER_ALIGNMENT);
        if (component instanceof JLabel) {
            JLabel label = ((JLabel)component);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setVerticalAlignment(SwingConstants.CENTER);
        }
        return component;
    }

    private void setTheme(Window frame) {
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
        updateTimer(null);
    }

    public void updateTimer(String time) {
        if (this.timer instanceof TimerPanel) {
            if (this.timer instanceof TextRendererPanel) ((TextRendererPanel) this.timer).setImage(StreamTimerMain.textRenderer.getFramebuffer());
            ((TimerPanel) this.timer).update();
        }
        if (time != null) {
            if (this.trayIcon != null) this.trayIcon.setToolTip(time);
        }
    }

    public void initTrayIcon() {
        if (SystemTray.isSupported()) {
            System.out.println("[Stream Timer] Initializing System Tray Icon...");
            systemTray = SystemTray.getSystemTray();

            PopupMenu popupMenu = new PopupMenu();

            MenuItem titleText = new MenuItem(StaticVariables.name);
            titleText.setEnabled(false);
            popupMenu.add(titleText);

            popupMenu.addSeparator();

            MenuItem startStopButton = new MenuItem("Start/Stop");
            startStopButton.addActionListener(l -> {
                if (StreamTimerMain.timer.isRunning()) StreamTimerMain.timer.stop();
                else StreamTimerMain.timer.start();
            });
            popupMenu.add(startStopButton);

            MenuItem openButton = new MenuItem("Open Window");
            openButton.addActionListener(l -> {
                this.window.setVisible(true);
                this.window.toFront();
            });
            popupMenu.add(openButton);

            popupMenu.addSeparator();

            MenuItem exitButton = new MenuItem("Exit");
            exitButton.addActionListener(l -> {
                StreamTimerMain.timer.stop();
                StreamTimerMain.running = false;
                this.systemTray.remove(this.trayIcon);
                System.exit(0);
            });
            popupMenu.add(exitButton);

            this.trayIcon = new TrayIcon(this.icon.getImage(), StaticVariables.name, popupMenu);
            this.trayIcon.setImageAutoSize(true);

            this.trayIcon.addActionListener(l -> {
                this.window.setVisible(true);
                this.window.toFront();
            });
            try {
                this.systemTray.add(this.trayIcon);
            } catch (Exception error) {
                System.out.println("[Stream Timer] Failed to init system tray: " + error);
            }
        }
    }
}

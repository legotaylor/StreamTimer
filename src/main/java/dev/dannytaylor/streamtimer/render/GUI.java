package dev.dannytaylor.streamtimer.render;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import dev.dannytaylor.streamtimer.StreamTimerMain;
import dev.dannytaylor.streamtimer.config.RenderMode;
import dev.dannytaylor.streamtimer.config.StreamTimerConfig;
import dev.dannytaylor.streamtimer.data.StaticVariables;
import dev.dannytaylor.streamtimer.timer.TimerUtils;
import dev.dannytaylor.streamtimer.util.NumberFilter;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.CountDownLatch;

public class GUI {
    public Window window;
    public JPanel timer;
    public JLabel messageText;
    public String initMessageText = "⠀";
    public JButton toggleButton;
    public JButton configureButton;

    public CountDownLatch latch = new CountDownLatch(1);

    public SetupGUI setupGUI;
    public static Tray tray;

    public GUI() {
        setup();
        setupGUI = new SetupGUI();
    }

    public void setup() {
        setTheme(this.window);
    }

    public void init(RenderMode renderMode) {
        boolean isDialog = renderMode.getRenderType().equals(RenderMode.RenderType.DIALOG);
        this.window = isDialog ? new JDialog((Frame) null, StaticVariables.name, false) : new JFrame(StaticVariables.name);
        this.window.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;
        this.timer = renderMode.usesGL() ? new GLRendererPanel(new GLCapabilities(GLProfile.get(GLProfile.GL2))) : new TextRendererPanel();
        this.timer.setPreferredSize(new Dimension(576, 144));
        this.window.add(this.timer, gbc);
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weighty = 0;
        JPanel configureRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 0));
        this.toggleButton = GUIWidgets.createButton("START");
        this.toggleButton.setToolTipText("Starts the timer");
        this.toggleButton.addActionListener(e -> toggleTimer());
        configureRow.add(this.toggleButton);
        configureButton = GUIWidgets.createButton("...");
        configureButton.setPreferredSize(new Dimension(26, 26));
        configureButton.setToolTipText("Configure");
        configureButton.addActionListener(e -> {
            configureButton.setEnabled(false);
            JDialog configureDialog = new JDialog(this.window);
            configureDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            configureDialog.setTitle(StaticVariables.name + ": Configure");
            JTabbedPane tabs = new JTabbedPane();
            JPanel fontTab = new JPanel(new GridBagLayout());
            GridBagConstraints fontTabGbc = new GridBagConstraints();
            fontTabGbc.insets = new Insets(8, 8, 8, 8);
            fontTabGbc.fill = GridBagConstraints.HORIZONTAL;
            fontTabGbc.anchor = GridBagConstraints.CENTER;
            fontTabGbc.gridx = 0;
            fontTabGbc.gridy = 0;
            JLabel fontLabel = new JLabel("Font:");
            fontLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            fontTab.add(fontLabel, fontTabGbc);
            fontTabGbc.gridx = 1;
            JComboBox<String> fontCombo = new JComboBox<>(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
            fontCombo.setSelectedItem(StreamTimerConfig.instance.font.value());
            fontTab.add(fontCombo, fontTabGbc);
            fontTabGbc.gridx = 0;
            fontTabGbc.gridy++;
            JLabel styleLabel = new JLabel("Style:");
            styleLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            fontTab.add(styleLabel, fontTabGbc);
            fontTabGbc.gridx = 1;
            String[] styles = {"Plain", "Bold", "Italic", "Bold Italic"};
            JComboBox<String> styleCombo = new JComboBox<>(styles);
            styleCombo.setSelectedIndex(StreamTimerConfig.instance.style.value());
            fontTab.add(styleCombo, fontTabGbc);
            fontTabGbc.gridx = 0;
            fontTabGbc.gridy++;
            JLabel sizeLabel = new JLabel("Size:");
            sizeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            fontTab.add(sizeLabel, fontTabGbc);
            fontTabGbc.gridx = 1;
            JSpinner sizeSpinner = new JSpinner(new SpinnerNumberModel(StreamTimerConfig.instance.size.value().intValue(), 8, 200, 1));
            fontTab.add(sizeSpinner, fontTabGbc);

            fontCombo.addActionListener(f -> StreamTimerConfig.instance.font.setValue((String) fontCombo.getSelectedItem(), true));
            styleCombo.addActionListener(g -> StreamTimerConfig.instance.style.setValue(styleCombo.getSelectedIndex(), true));
            sizeSpinner.addChangeListener(h -> StreamTimerConfig.instance.size.setValue((Integer) sizeSpinner.getValue(), true));
            tabs.addTab("Font", fontTab);

            JPanel textColorTab = new JPanel();
            textColorTab.setLayout(new BoxLayout(textColorTab, BoxLayout.Y_AXIS));
            textColorTab.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

            JPanel textCheckboxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

            JCheckBox rainbow = GUIWidgets.createCheckbox("Rainbow Mode");
            rainbow.setToolTipText("When enabled, the timer text renders with a coloured effect.");
            rainbow.setSelected(StreamTimerConfig.instance.rainbow.value());
            rainbow.addChangeListener(i -> StreamTimerConfig.instance.rainbow.setValue(rainbow.isSelected(), true));
            textCheckboxPanel.add(rainbow);
            JCheckBox dimWhenStopped = GUIWidgets.createCheckbox("Dim when Timer Stopped");
            dimWhenStopped.setToolTipText("When enabled, the timer text renders at 50% colour intensity.");
            dimWhenStopped.setSelected(StreamTimerConfig.instance.dimWhenStopped.value());
            dimWhenStopped.addChangeListener(i -> StreamTimerConfig.instance.dimWhenStopped.setValue(dimWhenStopped.isSelected(), true));
            textCheckboxPanel.add(dimWhenStopped);

            Color textColor = new Color(StreamTimerConfig.instance.textColor.value(), true);
            JColorChooser textColorChooser = new JColorChooser(textColor);
            textColorChooser.setBorder(BorderFactory.createEmptyBorder());
            textColorChooser.getSelectionModel().addChangeListener(l -> StreamTimerConfig.instance.textColor.setValue(textColorChooser.getColor().getRGB(), false));
            textColorTab.add(textCheckboxPanel);
            textColorTab.add(Box.createVerticalStrut(6));
            textColorTab.add(textColorChooser);
            tabs.addTab("Text Colour", textColorTab);

            JPanel backgroundColorTab = new JPanel();
            backgroundColorTab.setLayout(new BoxLayout(backgroundColorTab, BoxLayout.Y_AXIS));
            backgroundColorTab.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
            JPanel backgroundCheckboxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            JCheckBox background = GUIWidgets.createCheckbox("Render Background");
            background.setToolTipText("When enabled, renders a solid coloured background which can be chroma-keyed out in some window capturing applications.");
            background.setSelected(StreamTimerConfig.instance.background.value());
            background.addChangeListener(f -> StreamTimerConfig.instance.background.setValue(background.isSelected(), true));
            backgroundCheckboxPanel.add(background);
            Color backgroundColor = new Color(StreamTimerConfig.instance.backgroundColor.value(), true);
            JColorChooser backgroundColorChooser = new JColorChooser(backgroundColor);
            backgroundColorChooser.setBorder(BorderFactory.createEmptyBorder());
            backgroundColorChooser.getSelectionModel().addChangeListener(l -> StreamTimerConfig.instance.backgroundColor.setValue(backgroundColorChooser.getColor().getRGB(), false));
            backgroundColorTab.add(backgroundCheckboxPanel);
            backgroundColorTab.add(Box.createVerticalStrut(6));
            backgroundColorTab.add(backgroundColorChooser);
            tabs.addTab("Background Colour", backgroundColorTab);

            configureDialog.add(tabs);
            configureDialog.setResizable(false);
            configureDialog.pack();
            configureDialog.setMinimumSize(new Dimension(700, 400));
            configureDialog.setLocationRelativeTo(this.window);
            configureDialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    StreamTimerConfig.instance.textColor.setValue(textColorChooser.getColor().getRGB(), true);
                    StreamTimerConfig.instance.backgroundColor.setValue(backgroundColorChooser.getColor().getRGB(), true);
                    configureButton.setEnabled(true);
                }
            });
            configureDialog.setVisible(true);
        });
        configureRow.add(configureButton);
        this.window.add(configureRow, gbc);
        gbc.gridy = 2;
        JPanel timerRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 0));
        JTextField hours = GUIWidgets.createText(StreamTimerConfig.instance.addHours.value(), 2);
        hours.setToolTipText("Seconds");
        JTextField minutes = GUIWidgets.createText(StreamTimerConfig.instance.addMinutes.value(), 2);
        minutes.setToolTipText("Minutes");
        JTextField seconds = GUIWidgets.createText(StreamTimerConfig.instance.addSeconds.value(), 2);
        seconds.setToolTipText("Seconds");
        ((AbstractDocument) hours.getDocument()).setDocumentFilter(new NumberFilter());
        ((AbstractDocument) minutes.getDocument()).setDocumentFilter(new NumberFilter());
        ((AbstractDocument) seconds.getDocument()).setDocumentFilter(new NumberFilter());
        JButton addButton = GUIWidgets.createButton("+");
        addButton.setToolTipText("Add time");
        addButton.addActionListener(e -> {
            updateTimer(TimerUtils.getSecondsFromString(hours.getText(), minutes.getText(), seconds.getText()), true, true);
            messageText.setText("Added time to timer!");
        });
        JButton removeButton = GUIWidgets.createButton("-");
        removeButton.setToolTipText("Subtract time");
        removeButton.addActionListener(e -> {
            updateTimer(-TimerUtils.getSecondsFromString(hours.getText(), minutes.getText(), seconds.getText()), true, true);
            messageText.setText("Subtracted time from timer!");
        });
        JButton setButton = GUIWidgets.createButton("=");
        setButton.setToolTipText("Set time");
        setButton.addActionListener(e -> {
            updateTimer(TimerUtils.getSecondsFromString(hours.getText(), minutes.getText(), seconds.getText()), false, true);
            messageText.setText("Set timer!");
        });
        timerRow.add(hours);
        timerRow.add(new JLabel(":"));
        timerRow.add(minutes);
        timerRow.add(new JLabel(":"));
        timerRow.add(seconds);
        timerRow.add(addButton);
        timerRow.add(removeButton);
        timerRow.add(setButton);
        this.window.add(timerRow, gbc);
        gbc.gridy = 3;
        JPanel optionsRow = new JPanel(new FlowLayout(FlowLayout.CENTER));

        if (!isDialog) {
            JCheckBox forceFocus = GUIWidgets.createCheckbox("Prevent minimize");
            forceFocus.setToolTipText("When enabled, the window will unminimize itself when minimized.");
            forceFocus.setSelected(StreamTimerConfig.instance.forceFocus.value());
            forceFocus.addChangeListener(e -> StreamTimerConfig.instance.forceFocus.setValue(forceFocus.isSelected(), true));
            optionsRow.add(forceFocus);
        }

        if (this.timer instanceof GLRendererPanel) {
            JCheckBox reversed = GUIWidgets.createCheckbox("Count up");
            reversed.setToolTipText("When enabled, the timer counts up instead of down.");
            reversed.setSelected(StreamTimerConfig.instance.reversed.value());
            reversed.addChangeListener(e -> StreamTimerConfig.instance.reversed.setValue(reversed.isSelected(), true));
            optionsRow.add(reversed);
            JCheckBox finishSound = GUIWidgets.createCheckbox("Finish Sound");
            finishSound.setToolTipText("When enabled, a sound will be played after the timer reaches 0.");
            finishSound.setSelected(StreamTimerConfig.instance.finishSound.value());
            finishSound.addChangeListener(e -> StreamTimerConfig.instance.finishSound.setValue(finishSound.isSelected(), true));
            optionsRow.add(finishSound);
        }

        this.window.add(optionsRow, gbc);
        gbc.gridy = 4;
        this.messageText = new JLabel(initMessageText, SwingConstants.CENTER);
        this.window.add(this.messageText, gbc);
        this.window.pack();
        this.window.setMinimumSize(new Dimension(576, 320));
        this.window.setLocationRelativeTo(null);

        if (this.window instanceof JFrame) {
            ((JFrame) this.window).setResizable(false);
            ((JFrame) this.window).setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        } else {
            ((JDialog) this.window).setResizable(false);
            ((JDialog) this.window).setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        }

        if (StreamTimerMain.icon != null) this.window.setIconImage(StreamTimerMain.icon.getImage());

        tray = new Tray();

        this.window.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {
            }

            @Override
            public void windowClosing(WindowEvent e) {
                StreamTimerMain.running = false;
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

        this.window.setVisible(true);
        this.latch.countDown();
    }

    private void setTheme(Window frame) {
        try {
            UIManager.setLookAndFeel(new FlatDarculaLaf());
            if (frame != null) SwingUtilities.updateComponentTreeUI(frame);
        } catch (Exception error) {
            System.err.println("Failed to set theme: " + error);
        }
    }

    public void updateTimer(long seconds, boolean add, boolean save) {
        TimerUtils.setTimer(seconds, add, save);
        updateTimer(null);
    }

    public void updateTimer(String time) {
        if (this.timer instanceof TimerPanel) ((TimerPanel) this.timer).update();
        tray.updateTimer(time);
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

    public void toggleTimer() {
        if (StreamTimerMain.timer.isRunning()) {
            StreamTimerMain.timer.stop();
            this.messageText.setText("Stopped timer!");
            this.toggleButton.setText("START");
            this.toggleButton.setToolTipText("Starts the timer");
        } else {
            StreamTimerMain.timer.start();
            this.messageText.setText("Started timer!");
            this.toggleButton.setText("STOP");
            this.toggleButton.setToolTipText("Stops the timer");
        }
    }
}

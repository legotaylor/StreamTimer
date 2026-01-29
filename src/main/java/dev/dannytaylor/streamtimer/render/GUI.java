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
import dev.dannytaylor.streamtimer.util.StreamTimerRunnable;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class GUI {
    public Window window;
    public JPanel timer;
    public JLabel messageText;
    public String initMessageText = "⠀";
    public JButton toggleButton;
    public JButton configureButton;
    public JTabbedPane configureTabs;

    public CountDownLatch latch = new CountDownLatch(1);

    public SetupGUI setupGUI;
    public static Tray tray;

    public static final ArrayList<StreamTimerRunnable> runBeforeVisible = new ArrayList<>();
    public static final ArrayList<StreamTimerRunnable> runOnConfigClose = new ArrayList<>();

    public GUI() {
        setup();
        setupGUI = new SetupGUI();
    }

    public void setup() {
        setTheme(this.window);
    }

    public void create(RenderMode renderMode) {
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
        this.toggleButton.addActionListener(e -> TimerUtils.toggleTimer());
        configureRow.add(this.toggleButton);
        this.configureButton = GUIWidgets.createButton("...");
        this.configureButton.setPreferredSize(new Dimension(26, 26));
        this.configureButton.setToolTipText("Configure");

        JDialog configureDialog = new JDialog(this.window);
        configureDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        configureDialog.setTitle(StaticVariables.name + ": Configure");

        if (StreamTimerMain.icon != null) configureDialog.setIconImage(StreamTimerMain.icon.getImage());

        this.configureTabs = new JTabbedPane();

        ArrayList<StreamTimerRunnable> onBeforeVisible = new ArrayList<>();
        ArrayList<StreamTimerRunnable> onConfigClose = new ArrayList<>();

        onBeforeVisible.add((mode) -> StreamTimerMain.gui.createFontConfigTab(mode));
        onBeforeVisible.add((mode) -> StreamTimerMain.gui.createTextColorConfigTab(mode));
        onBeforeVisible.add((mode) -> StreamTimerMain.gui.createBackgroundColorConfigTab(mode));
        onBeforeVisible.addAll(GUI.runBeforeVisible);

        onConfigClose.add((mode) -> StreamTimerMain.gui.onTextColorConfigClose(mode));
        onConfigClose.add((mode) -> StreamTimerMain.gui.onBackgroundColorConfigClose(mode));
        onConfigClose.addAll(GUI.runOnConfigClose);

        configureDialog.add(this.configureTabs);
        configureDialog.setResizable(false);
        configureDialog.pack();
        configureDialog.setMinimumSize(new Dimension(700, 500));
        configureDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                for (StreamTimerRunnable runnable : onConfigClose) runnable.run(renderMode);
                configureButton.setEnabled(true);
            }
        });
        this.configureButton.addActionListener(e -> {
            this.configureButton.setEnabled(false);
            configureDialog.setLocationRelativeTo(this.window);
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

        for (StreamTimerRunnable runnable : onBeforeVisible) runnable.run(renderMode);

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

    private JColorChooser textColorChooser;
    private JColorChooser backgroundColorChooser;

    private void createFontConfigTab(RenderMode renderMode) {
        JPanel tab = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel fontLabel = new JLabel("Font:");
        fontLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        tab.add(fontLabel, gbc);
        gbc.gridx = 1;
        JComboBox<String> fontCombo = new JComboBox<>(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        fontCombo.setSelectedItem(StreamTimerConfig.instance.font.value());
        tab.add(fontCombo, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel styleLabel = new JLabel("Style:");
        styleLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        tab.add(styleLabel, gbc);
        gbc.gridx = 1;
        String[] styles = {"Plain", "Bold", "Italic", "Bold Italic"};
        JComboBox<String> styleCombo = new JComboBox<>(styles);
        styleCombo.setSelectedIndex(StreamTimerConfig.instance.style.value());
        tab.add(styleCombo, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel sizeLabel = new JLabel("Size:");
        sizeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        tab.add(sizeLabel, gbc);
        gbc.gridx = 1;
        JSpinner sizeSpinner = new JSpinner(new SpinnerNumberModel(StreamTimerConfig.instance.size.value().intValue(), 8, 200, 1));
        tab.add(sizeSpinner, gbc);

        fontCombo.addActionListener(f -> StreamTimerConfig.instance.font.setValue((String) fontCombo.getSelectedItem(), true));
        styleCombo.addActionListener(g -> StreamTimerConfig.instance.style.setValue(styleCombo.getSelectedIndex(), true));
        sizeSpinner.addChangeListener(h -> StreamTimerConfig.instance.size.setValue((Integer) sizeSpinner.getValue(), true));
        this.configureTabs.addTab("Font", tab);
    }

    private void createTextColorConfigTab(RenderMode renderMode) {
        JPanel textColorTab = new JPanel();
        textColorTab.setLayout(new BoxLayout(textColorTab, BoxLayout.Y_AXIS));
        textColorTab.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        if (renderMode.usesGL()) {
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
            textColorTab.add(textCheckboxPanel);
            textColorTab.add(Box.createVerticalStrut(6));
        }

        Color textColor = new Color(StreamTimerConfig.instance.textColor.value(), true);
        this.textColorChooser = new JColorChooser(textColor);
        this.textColorChooser.setBorder(BorderFactory.createEmptyBorder());
        this.textColorChooser.getSelectionModel().addChangeListener(l -> StreamTimerConfig.instance.textColor.setValue(this.textColorChooser.getColor().getRGB(), false));
        textColorTab.add(this.textColorChooser);
        this.configureTabs.addTab("Text Colour", textColorTab);
    }

    private void onTextColorConfigClose(RenderMode renderMode) {
        StreamTimerConfig.instance.textColor.setValue(this.textColorChooser.getColor().getRGB(), true);
    }

    private void createBackgroundColorConfigTab(RenderMode renderMode) {
        JPanel backgroundColorTab = new JPanel();
        backgroundColorTab.setLayout(new BoxLayout(backgroundColorTab, BoxLayout.Y_AXIS));
        backgroundColorTab.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        if (renderMode.usesGL()) {
            JPanel backgroundCheckboxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            JCheckBox background = GUIWidgets.createCheckbox("Render Background");
            background.setToolTipText("When enabled, renders a solid coloured background which can be chroma-keyed out in some window capturing applications.");
            background.setSelected(StreamTimerConfig.instance.background.value());
            background.addChangeListener(f -> StreamTimerConfig.instance.background.setValue(background.isSelected(), true));
            backgroundCheckboxPanel.add(background);
            backgroundColorTab.add(backgroundCheckboxPanel);
            backgroundColorTab.add(Box.createVerticalStrut(6));
        }
        Color backgroundColor = new Color(StreamTimerConfig.instance.backgroundColor.value(), true);
        this.backgroundColorChooser = new JColorChooser(backgroundColor);
        this.backgroundColorChooser.setBorder(BorderFactory.createEmptyBorder());
        this.backgroundColorChooser.getSelectionModel().addChangeListener(l -> StreamTimerConfig.instance.backgroundColor.setValue(this.backgroundColorChooser.getColor().getRGB(), false));
        backgroundColorTab.add(this.backgroundColorChooser);
        this.configureTabs.addTab("Background Colour", backgroundColorTab);
    }

    private void onBackgroundColorConfigClose(RenderMode renderMode) {
        StreamTimerConfig.instance.backgroundColor.setValue(this.backgroundColorChooser.getColor().getRGB(), true);
    }
}

/*
    StreamTimer
    Contributor(s): dannytaylor
    Github: https://github.com/legotaylor/StreamTimer
    Licence: LGPL-3.0
*/

package dev.dannytaylor.streamtimer.render;

import com.jthemedetecor.OsThemeDetector;
import dev.dannytaylor.streamtimer.StreamTimerMain;
import dev.dannytaylor.streamtimer.config.RenderMode;
import dev.dannytaylor.streamtimer.config.StreamTimerConfig;
import dev.dannytaylor.streamtimer.config.WindowTheme;
import dev.dannytaylor.streamtimer.data.StaticVariables;
import dev.dannytaylor.streamtimer.logger.StreamTimerLoggerImpl;
import dev.dannytaylor.streamtimer.timer.TimerUtils;
import dev.dannytaylor.streamtimer.util.IntegerFilter;
import dev.dannytaylor.streamtimer.util.StreamTimerRunnable;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

public class GUI {
    public Window window;
    public JDialog configWindow;
    public JPanel timer;
    public JLabel messageText;
    public String initMessageText = "⠀";
    public JButton toggleButton;
    public JButton configureButton;
    public JTabbedPane configureTabs;
    public boolean isDark;

    public Renderer glRenderer;

    public CountDownLatch latch = new CountDownLatch(1);

    public SetupGUI setupGUI;
    public static Tray tray;

    private static final ArrayList<StreamTimerRunnable> runBeforeVisible = new ArrayList<>();
    private static final ArrayList<StreamTimerRunnable> runOnConfigClose = new ArrayList<>();

    public static boolean addBeforeVisible(String name, StreamTimerRunnable runnable) {
        StreamTimerLoggerImpl.debug("[Run Before Visible] Added " + name);
        return runBeforeVisible.add(runnable);
    }

    public static boolean addOnConfigClose(String name, StreamTimerRunnable runnable) {
        StreamTimerLoggerImpl.debug("[Run On Config Close] Added " + name);
        return runOnConfigClose.add(runnable);
    }

    public GUI() {
        setup();
        setupGUI = new SetupGUI();
    }

    public void setup() {
        updateTheme(this.window, OsThemeDetector.getDetector().isDark());
        OsThemeDetector.getDetector().registerListener(this::updateThemes);
    }

    public void updateThemes(boolean isDark) {
        SwingUtilities.invokeLater(() -> {
            if (this.setupGUI != null && this.setupGUI.window != null) updateTheme(this.setupGUI.window, isDark);
            if (this.window != null) updateTheme(this.window, isDark);
            if (this.configWindow != null) updateTheme(this.configWindow, isDark);
            this.isDark = isDark;
        });
    }

    public void create(RenderMode renderMode) {
        if (renderMode.usesGL()) this.glRenderer = new Renderer();
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
        this.timer = new TimerRendererPanel(renderMode.usesGL());
        this.timer.setPreferredSize(new Dimension(576, 144));
        this.window.add(this.timer, gbc);
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weighty = 0;
        JPanel configureRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 0));
        this.toggleButton = GUIWidgets.createButton("START");
        this.toggleButton.setToolTipText("Starts the timer");
        this.toggleButton.addActionListener(e -> {
            TimerUtils.toggleTimer();
        });
        configureRow.add(this.toggleButton);
        this.configureButton = GUIWidgets.createButton("...");
        this.configureButton.setMinimumSize(new Dimension(26, 26));
        this.configureButton.setToolTipText("Configure");

        configWindow = new JDialog(this.window);
        configWindow.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        configWindow.setTitle(StaticVariables.name + ": Configure");

        if (StreamTimerMain.icon != null) configWindow.setIconImage(StreamTimerMain.icon.getImage());

        this.configureTabs = new JTabbedPane();

        ArrayList<StreamTimerRunnable> onBeforeVisible = new ArrayList<>();
        ArrayList<StreamTimerRunnable> onConfigClose = new ArrayList<>();

        onBeforeVisible.add((mode) -> StreamTimerMain.gui.createFontTab(mode));
        onBeforeVisible.add((mode) -> StreamTimerMain.gui.createTextColorTab(mode));
        onBeforeVisible.add((mode) -> StreamTimerMain.gui.createBackgroundColorTab(mode));

        onBeforeVisible.addAll(runBeforeVisible);

        onBeforeVisible.add((mode) -> StreamTimerMain.gui.createMiscTab(mode));
        onBeforeVisible.add((mode) -> StreamTimerMain.gui.createLogTab(mode));
        onBeforeVisible.add((mode) -> StreamTimerMain.gui.createLicenseTab(mode));

        onConfigClose.add((mode) -> StreamTimerMain.gui.onTextColorTabClose(mode));
        onConfigClose.add((mode) -> StreamTimerMain.gui.onBackgroundColorTabClose(mode));
        onConfigClose.addAll(GUI.runOnConfigClose);

        configWindow.add(this.configureTabs);
        configWindow.setResizable(false);
        configWindow.pack();
        configWindow.setMinimumSize(new Dimension(700, 500));
        configWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                for (StreamTimerRunnable runnable : onConfigClose) {
                    try {
                        runnable.run(renderMode);
                    } catch (Exception error) {
                        StreamTimerLoggerImpl.error("Failed to run onConfigClose runnable: " + error);
                    }
                }
                configureButton.setEnabled(true);
            }
        });
        this.configureButton.addActionListener(e -> {
            this.configureButton.setEnabled(false);
            configWindow.setLocationRelativeTo(this.window);
            configWindow.setVisible(true);
        });
        configureRow.add(configureButton);
        this.window.add(configureRow, gbc);
        gbc.gridy = 2;
        JPanel timerRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 0));
        JTextField hours = GUIWidgets.createText(StreamTimerConfig.instance.addHours.value(), 2);
        hours.setToolTipText("Hours");
        JTextField minutes = GUIWidgets.createText(StreamTimerConfig.instance.addMinutes.value(), 2);
        minutes.setToolTipText("Minutes");
        JTextField seconds = GUIWidgets.createText(StreamTimerConfig.instance.addSeconds.value(), 2);
        seconds.setToolTipText("Seconds");
        ((AbstractDocument) hours.getDocument()).setDocumentFilter(new IntegerFilter());
        ((AbstractDocument) minutes.getDocument()).setDocumentFilter(new IntegerFilter());
        ((AbstractDocument) seconds.getDocument()).setDocumentFilter(new IntegerFilter());
        JButton addButton = GUIWidgets.createButton("+");
        addButton.setToolTipText("Add time");
        addButton.addActionListener(e -> {
            long s = TimerUtils.getSecondsFromString(hours.getText(), minutes.getText(), seconds.getText());
            updateTimer(s, true, true);
            messageText.setText("Added " + TimerUtils.getTime(s * 1000L) + " to the timer");
        });
        JButton removeButton = GUIWidgets.createButton("-");
        removeButton.setToolTipText("Subtract time");
        removeButton.addActionListener(e -> {
            long s = -TimerUtils.getSecondsFromString(hours.getText(), minutes.getText(), seconds.getText());
            updateTimer(s, true, true);
            messageText.setText("Subtracted " + TimerUtils.getTime(s * 1000L) + " to the timer");
        });
        JButton setButton = GUIWidgets.createButton("=");
        setButton.setToolTipText("Set time");
        setButton.addActionListener(e -> {
            long s = TimerUtils.getSecondsFromString(hours.getText(), minutes.getText(), seconds.getText());
            updateTimer(s, false, true);
            messageText.setText("Set timer to " + TimerUtils.getTime(s * 1000L));
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

        JCheckBox showMillis = GUIWidgets.createCheckbox("Show Milliseconds");
        showMillis.setSelected(StreamTimerConfig.instance.showMillis.value());
        showMillis.addChangeListener(e -> StreamTimerConfig.instance.showMillis.setValue(showMillis.isSelected(), true));
        optionsRow.add(showMillis);

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
                tray.close();
                StreamTimerMain.close();
            }

            @Override
            public void windowClosed(WindowEvent e) {
            }

            @Override
            public void windowIconified(WindowEvent e) {
                if (window instanceof JFrame && StreamTimerConfig.instance.forceFocus.value()) {
                    window.setVisible(true);
                    ((JFrame)window).setExtendedState(Frame.NORMAL);
                    window.requestFocus();
                }
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

        for (StreamTimerRunnable runnable : onBeforeVisible) {
            try {
                runnable.run(renderMode);
            } catch (Exception error) {
                StreamTimerLoggerImpl.error("Failed to run onBeforeVisible runnable: " + error);
            }
        }

        StreamTimerLoggerImpl.info("Ready!");
        this.window.setVisible(true);
        StreamTimerMain.canStart = true;
        this.latch.countDown();
    }

    private void updateTheme(Window frame, boolean isDark) {
        try {
            UIManager.setLookAndFeel(StreamTimerConfig.instance.theme.value().getTheme(isDark));
            if (frame != null) {
                SwingUtilities.updateComponentTreeUI(frame);
                frame.invalidate();
                frame.validate();
                Dimension size = frame.getSize();
                frame.pack();
                frame.setSize(size);
                frame.repaint();
            }
        } catch (Exception error) {
            StreamTimerLoggerImpl.error("Failed to update theme: " + error);
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

    private void createFontTab(RenderMode renderMode) {
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
        ArrayList<FontValue> fonts = new ArrayList<>();
        FontValue selectedFontValue = null;
        for (String fontName : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {
            Font font = new Font(fontName, Font.PLAIN, 12);
            if (font.canDisplayUpTo("01234567890:.") == -1) {
                FontValue fontValue = new FontValue(fontName, font.canDisplayUpTo(fontName) == - 1);
                if (fontName.equals(StreamTimerConfig.instance.font.value())) selectedFontValue = fontValue;
                fonts.add(fontValue);
            }
        }
        JComboBox<FontValue> fontCombo = new JComboBox<>(fonts.toArray(new FontValue[0]));
        fontCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component label = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof FontValue && ((FontValue)value).canDisplayName) {
                    Font font = label.getFont();
                    label.setFont(new Font(((FontValue)value).name, font.getStyle(), font.getSize()));
                }
                return label;
            }
        });
        if (selectedFontValue != null) fontCombo.setSelectedItem(selectedFontValue);
        else System.err.println("Selected font either could not be found, or is not compatible.");
        tab.add(fontCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        JLabel styleLabel = new JLabel("Style:");
        styleLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        tab.add(styleLabel, gbc);
        gbc.gridx = 1;
        String[] styles = {"Plain", "Bold", "Italic", "Bold Italic"};
        JComboBox<String> styleCombo = new JComboBox<>(styles);
        styleCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component label = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setFont(label.getFont().deriveFont(index));
                return label;
            }
        });
        styleCombo.setSelectedIndex(StreamTimerConfig.instance.style.value());
        tab.add(styleCombo, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel sizeLabel = new JLabel("Size:");
        sizeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        tab.add(sizeLabel, gbc);
        gbc.gridx = 1;
        JSpinner sizeSpinner = new JSpinner(new SpinnerNumberModel(StreamTimerConfig.instance.size.value().intValue(), 1, Integer.MAX_VALUE, 1));
        tab.add(sizeSpinner, gbc);

        fontCombo.addActionListener(f -> {
            FontValue fontValue = (FontValue) fontCombo.getSelectedItem();
            if (fontValue != null) StreamTimerConfig.instance.font.setValue(fontValue.name, true);
        });
        styleCombo.addActionListener(g -> StreamTimerConfig.instance.style.setValue(styleCombo.getSelectedIndex(), true));
        sizeSpinner.addChangeListener(h -> StreamTimerConfig.instance.size.setValue((Integer) sizeSpinner.getValue(), true));
        this.configureTabs.addTab("Font", tab);
    }

    private void createTextColorTab(RenderMode renderMode) {
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

    private void onTextColorTabClose(RenderMode renderMode) {
        StreamTimerConfig.instance.textColor.setValue(this.textColorChooser.getColor().getRGB(), true);
    }

    private void createBackgroundColorTab(RenderMode renderMode) {
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

    private void onBackgroundColorTabClose(RenderMode renderMode) {
        StreamTimerConfig.instance.backgroundColor.setValue(this.backgroundColorChooser.getColor().getRGB(), true);
    }

    private void createLogTab(RenderMode renderMode) {
        JPanel logTab = new JPanel();
        logTab.setLayout(new BorderLayout());
        JTextPane log = new JTextPane();
        log.setEditable(false);
        log.setPreferredSize(null);
        JScrollPane pane = new JScrollPane(log);
        pane.setPreferredSize(null);
        StreamTimerLoggerImpl.getUiHandler().add(log);
        logTab.add(pane, BorderLayout.CENTER);
        this.configureTabs.addTab("Log", logTab);
    }

    private void createLicenseTab(RenderMode renderMode) {
        JPanel licenceTab = new JPanel();
        licenceTab.setLayout(new BorderLayout());
        StringBuilder html = new StringBuilder();
        try (InputStream inputStream = new FileInputStream(Path.of(StaticVariables.name + "Assets/LICENSE").toFile())) {
            try (Scanner reader = new Scanner(inputStream)) {
                html.append("<html><body style='margin:0'><pre style='margin:0'>");
                while (reader.hasNextLine()) {
                    html.append(reader.nextLine()).append("\n");
                }
                html.append("</pre></body></html>");
            }
        } catch (Exception error) {
            StreamTimerLoggerImpl.error("Failed to load licence tab: " + error);
            html = new StringBuilder("<html><body><p style='color:red;'>Failed to load content.</p></body></html>");
        }
        JEditorPane licence = new JEditorPane("text/html", html.toString());
        licence.setEditable(false);
        licence.setCaretPosition(0);
        licenceTab.add(new JScrollPane(licence), BorderLayout.CENTER);
        this.configureTabs.addTab("Licence", licenceTab);
    }

    private void createMiscTab(RenderMode renderMode) {
        JPanel tab = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel themeLabel = new JLabel("Window Theme:");
        themeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        tab.add(themeLabel, gbc);
        gbc.gridx = 1;
        JComboBox<String> themeCombo = new JComboBox<>(WindowTheme.getAllNames());
        themeCombo.setSelectedItem(StreamTimerConfig.instance.theme.value().getName());
        tab.add(themeCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        JLabel tpsLabel = new JLabel("TPS:");
        tpsLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        tab.add(tpsLabel, gbc);
        gbc.gridx = 1;
        JSpinner tpsSpinner = new JSpinner(new SpinnerNumberModel(StreamTimerConfig.instance.tps.value().intValue(), 1, 65536, 1));
        tab.add(tpsSpinner, gbc);

        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.gridy++;
        JCheckBox iPaidForTheWholeDamnCpuGiveMeTheWholeDamnCpuCheckbox = GUIWidgets.createCheckbox("I paid for the whole damn CPU, now give me the whole damn CPU!", StreamTimerConfig.instance.iPaidForTheWholeDamnCpuGiveMeTheWholeDamnCpu.value());
        iPaidForTheWholeDamnCpuGiveMeTheWholeDamnCpuCheckbox.setToolTipText("Removes tps limit. This does not effect timer precision, this is purely for updating/rendering.");
        tab.add(setCentered(iPaidForTheWholeDamnCpuGiveMeTheWholeDamnCpuCheckbox), gbc);
        gbc.gridwidth = 1;

        tpsSpinner.setEnabled(!iPaidForTheWholeDamnCpuGiveMeTheWholeDamnCpuCheckbox.isSelected());

        themeCombo.addActionListener(f -> {
            StreamTimerConfig.instance.theme.setValue(WindowTheme.values()[themeCombo.getSelectedIndex()], true);
            updateThemes(OsThemeDetector.getDetector().isDark());
        });
        tpsSpinner.addChangeListener(f -> {
            StreamTimerConfig.instance.tps.setValue((Integer) tpsSpinner.getValue(), true);
        });
        iPaidForTheWholeDamnCpuGiveMeTheWholeDamnCpuCheckbox.addChangeListener(f -> {
            boolean isSelected = iPaidForTheWholeDamnCpuGiveMeTheWholeDamnCpuCheckbox.isSelected();
            StreamTimerConfig.instance.iPaidForTheWholeDamnCpuGiveMeTheWholeDamnCpu.setValue(isSelected, true);
            tpsSpinner.setEnabled(!iPaidForTheWholeDamnCpuGiveMeTheWholeDamnCpuCheckbox.isSelected());
        });
        this.configureTabs.addTab("Misc", tab);
    }

    public static class FontValue {
        public final String name;
        public final boolean canDisplayName;

        public FontValue(String name, boolean canDisplayName) {
            this.name = name;
            this.canDisplayName = canDisplayName;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}

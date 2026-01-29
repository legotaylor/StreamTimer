/*
    StreamTimer
    Contributor(s): dannytaylor
    Github: https://github.com/legotaylor/StreamTimer
    Licence: LGPL-3.0
*/

package dev.dannytaylor.streamtimer.render;

import javax.swing.*;
import javax.swing.text.Document;
import java.awt.*;

public class GUIWidgets {
    public static JCheckBox createCheckbox(String name, boolean selected) {
        JCheckBox checkBox = new JCheckBox(name);
        checkBox.setSelected(selected);
        return (JCheckBox) setHandCursor(checkBox);
    }

    public static JCheckBox createCheckbox(boolean selected) {
        return createCheckbox(null, selected);
    }

    public static JCheckBox createCheckbox(String name) {
        return createCheckbox(name, false);
    }

    public static JCheckBox createCheckbox() {
        return createCheckbox(false);
    }

    public static JButton createButton() {
        return (JButton) setHandCursor(new JButton());
    }

    public static JButton createButton(Icon icon) {
        return (JButton) setHandCursor(new JButton(icon));
    }

    public static JButton createButton(String text) {
        return (JButton) setHandCursor(new JButton(text));
    }

    public static JButton createButton(Action action) {
        return (JButton) setHandCursor(new JButton(action));
    }

    public static JButton createButton(String text, Icon icon) {
        return (JButton) setHandCursor(new JButton(text, icon));
    }

    public static JTextField createText(Document doc, String text, int columns) {
        return (JTextField) setTextCursor(new JTextField(doc, text, columns));
    }

    public static JTextField createText(String text, int columns) {
        return createText(null, text, columns);
    }

    public static JTextField createText(String text) {
        return createText(text, 0);
    }

    private static JComponent setHandCursor(JComponent component) {
        component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return component;
    }

    private static JComponent setTextCursor(JComponent component) {
        component.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        return component;
    }
}

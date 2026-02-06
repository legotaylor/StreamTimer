package dev.dannytaylor.streamtimer.logger.handler;

import dev.dannytaylor.logger.log.Logger;
import dev.dannytaylor.logger.log.handler.Handler;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.ArrayList;

public class UIHandler extends Handler {
    private final StyledDocument document = new DefaultStyledDocument();
    public final ArrayList<JTextPane> textPanes = new ArrayList<>();

    public UIHandler() {
        addStyles(this.document);
    }

    public void add(JTextPane log) {
        textPanes.add(log);
    }

    public void remove(JTextPane log) {
        textPanes.remove(log);
    }

    private void addStyles(StyledDocument doc) {
        addStyle(doc, Logger.Type.info, new Color(255, 255, 255));
        addStyle(doc, Logger.Type.warn, new Color(255, 176, 32));
        addStyle(doc, Logger.Type.error, new Color(255, 107, 107));
        addStyle(doc, Logger.Type.debug, new Color(244, 114, 192));
        addStyle(doc, Logger.Type.unformatted, new Color(255, 255, 255));
    }

    private void removeStyles(StyledDocument doc) {
        removeStyle(doc, Logger.Type.info);
        removeStyle(doc, Logger.Type.warn);
        removeStyle(doc, Logger.Type.error);
        removeStyle(doc, Logger.Type.debug);
        removeStyle(doc, Logger.Type.unformatted);
    }

    private void addStyle(StyledDocument doc, Logger.Type type, Color color) {
        Style style = doc.addStyle(type.name(), null);
        StyleConstants.setForeground(style, color);
        StyleConstants.setFontFamily(style, "Monospaced");
        StyleConstants.setFontSize(style, 12);
    }

    private void removeStyle(StyledDocument doc, Logger.Type type) {
        doc.removeStyle(type.name());
    }

    public void log(Logger.Type type, String message) {
        log(format(type, message), type.name());
    }

    public void log(String message, String style) {
        try {
            document.insertString(document.getLength(), message + "\n", document.getStyle(style));
        } catch (Exception error) {
            System.out.println("Error sending log to gui: " + error);
        }
        SwingUtilities.invokeLater(() -> {
            for (JTextPane log : textPanes) {
                try {
                    log.setStyledDocument(document);
                    log.setCaretPosition(log.getStyledDocument().getLength());
                } catch (Exception error) {
                    System.out.println("Error sending log to gui: " + error);
                }
            }
        });
    }
}

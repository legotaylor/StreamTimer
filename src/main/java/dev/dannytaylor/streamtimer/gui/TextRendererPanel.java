package dev.dannytaylor.streamtimer.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class TextRendererPanel extends JPanel {
    private BufferedImage image;

    public void setImage(BufferedImage image) {
        this.image = image;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (this.image != null) {
            graphics.drawImage(image, 0, 0, getWidth(), getHeight(), null);
        }
    }
}

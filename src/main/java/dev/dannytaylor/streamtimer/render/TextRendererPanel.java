package dev.dannytaylor.streamtimer.render;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class TextRendererPanel extends JPanel implements TimerPanel {
    private BufferedImage image;

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (this.image != null) {
            int panelWidth = getWidth();
            int panelHeight = getHeight();
            int imageWidth = image.getWidth();
            int imageHeight = image.getHeight();
            double aspectRatio = (double) imageWidth / imageHeight;
            int width = panelWidth;
            int height = (int) (panelWidth / aspectRatio);
            if (height > panelHeight) {
                height = panelHeight;
                width = (int) (panelHeight * aspectRatio);
            }
            int x = (panelWidth - width) / 2;
            int y = (panelHeight - height) / 2;
            graphics.drawImage(image, x, y, width, height, null);
        }
    }

    @Override
    public void update() {
        repaint();
    }
}

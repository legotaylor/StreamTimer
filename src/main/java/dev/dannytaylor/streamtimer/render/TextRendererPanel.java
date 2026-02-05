/*
    StreamTimer
    Contributor(s): dannytaylor
    Github: https://github.com/legotaylor/StreamTimer
    Licence: LGPL-3.0
*/

package dev.dannytaylor.streamtimer.render;

import dev.dannytaylor.streamtimer.StreamTimerMain;
import dev.dannytaylor.streamtimer.config.StreamTimerConfig;
import dev.dannytaylor.streamtimer.integration.websocket.WebSocketIntegration;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public class TextRendererPanel extends JPanel implements TimerPanel {
    private BufferedImage image;

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        this.setBackground(new Color(StreamTimerConfig.instance.backgroundColor.value(), false));
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
        this.image = StreamTimerMain.textRenderer.getFramebuffer();
        repaint();
    }
}

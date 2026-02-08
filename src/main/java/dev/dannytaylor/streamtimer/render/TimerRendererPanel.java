/*
    StreamTimer
    Contributor(s): dannytaylor
    Github: https://github.com/legotaylor/StreamTimer
    Licence: LGPL-3.0
*/

package dev.dannytaylor.streamtimer.render;

import dev.dannytaylor.streamtimer.StreamTimerMain;
import dev.dannytaylor.streamtimer.config.StreamTimerConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class TimerRendererPanel extends JPanel implements TimerPanel {
    private final boolean usesGl;
    private BufferedImage frame;

    public TimerRendererPanel(boolean usesGl) {
        this.usesGl = usesGl;
        Color color;
        if (this.usesGl) {
            this.setOpaque(false);
            this.setDoubleBuffered(true);
            color = new Color(0, 0, 0, 0);
        } else color = new Color(StreamTimerConfig.instance.backgroundColor.value(), false);
        this.setBackground(color);
        this.updateFrame();
    }

    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (this.frame != null) {
            ((Graphics2D)graphics).setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            int panelWidth = getWidth();
            int panelHeight = getHeight();
            int imageWidth = frame.getWidth();
            int imageHeight = frame.getHeight();
            double aspectRatio = (double) imageWidth / imageHeight;
            int width = panelWidth;
            int height = (int) (panelWidth / aspectRatio);
            if (height > panelHeight) {
                height = panelHeight;
                width = (int) (panelHeight * aspectRatio);
            }
            int x = (panelWidth - width) / 2;
            int y = (panelHeight - height) / 2;
            graphics.drawImage(this.frame, x, y, width, height, null);
        }
    }

    @Override
    public void update() {
        if (!this.usesGl) {
            Color color = new Color(StreamTimerConfig.instance.backgroundColor.value(), false);
            if (!this.getBackground().equals(color)) this.setBackground(color);
        }
        this.updateFrame();
        this.repaint();
    }

    private void updateFrame() {
        this.frame = this.usesGl ? Renderer.frame : StreamTimerMain.textRenderer.getFramebuffer();
    }
}
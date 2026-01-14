package dev.dannytaylor.streamtimer.gui;

import java.awt.*;
import java.awt.image.BufferedImage;

public class TextRenderer {
    private final int width;
    private final int height;
    private final Font font;

    private final BufferedImage framebuffer;

    public TextRenderer(int width, int height) {
        this.width = width;
        this.height = height;
        this.font = new Font("SansSerif", Font.BOLD, 72);
        this.framebuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    public BufferedImage render(String timeText) {
        Graphics2D graphics = framebuffer.createGraphics();

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        graphics.setComposite(AlphaComposite.Clear);
        graphics.fill(new Rectangle(0, 0, this.width, this.height));
        graphics.setComposite(AlphaComposite.SrcOver);

        graphics.setFont(this.font);
        graphics.setColor(Color.WHITE);

        FontMetrics fm = graphics.getFontMetrics();
        int x = (this.width - fm.stringWidth(timeText)) / 2;
        int y = (this.height - fm.getHeight()) / 2 + fm.getAscent();

        graphics.drawString(timeText, x, y);
        graphics.dispose();

        return this.framebuffer;
    }
}

package dev.dannytaylor.streamtimer.render;

import dev.dannytaylor.streamtimer.config.StreamTimerConfig;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public class TextRenderer {
    private final int width;
    private final int height;
    private final Font font;

    private final BufferedImage framebuffer;
    private final ByteBuffer byteBuffer;

    public TextRenderer(int width, int height) {
        this.width = width;
        this.height = height;
        this.font = new Font(StreamTimerConfig.instance.font.value(), StreamTimerConfig.instance.style.value(), StreamTimerConfig.instance.size.value());
        this.framebuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        this.byteBuffer = ByteBuffer.allocateDirect(4 * width * height);
    }

    public void render(String timeText) {
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
        updateByteBuffer();
    }

    private void updateByteBuffer() {
        this.byteBuffer.clear();
        BufferedImage framebuffer = getFramebuffer();
        int width = framebuffer.getWidth();
        int height = framebuffer.getHeight();
        int[] pixels = new int[width * height];
        framebuffer.getRGB(0, 0, width, height, pixels, 0, width);
        for (int pixel : pixels) {
            this.byteBuffer.put((byte) ((pixel >> 16) & 0xFF)); // Red
            this.byteBuffer.put((byte) ((pixel >> 8) & 0xFF)); // Green
            this.byteBuffer.put((byte) (pixel & 0xFF)); // Blue
            this.byteBuffer.put((byte) ((pixel >> 24) & 0xFF)); // Alpha
        }
        this.byteBuffer.flip();
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public Font getFont() {
        return this.font;
    }

    public BufferedImage getFramebuffer() {
        return this.framebuffer;
    }

    public ByteBuffer getByteBuffer() {
        return this.byteBuffer;
    }
}

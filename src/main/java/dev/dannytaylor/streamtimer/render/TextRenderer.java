/*
    StreamTimer
    Contributor(s): dannytaylor
    Github: https://github.com/legotaylor/StreamTimer
    Licence: LGPL-3.0
*/

package dev.dannytaylor.streamtimer.render;

import dev.dannytaylor.streamtimer.config.StreamTimerConfig;
import dev.dannytaylor.streamtimer.integration.websocket.WebSocketIntegration;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public class TextRenderer {
    private final int width;
    private final int height;

    private final BufferedImage framebuffer;

    public TextRenderer(int width, int height) {
        this.width = width;
        this.height = height;
        this.framebuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    public void render(String timeText) {
        Graphics2D graphics = framebuffer.createGraphics();

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        graphics.setComposite(AlphaComposite.Clear);
        graphics.fill(new Rectangle(0, 0, this.width, this.height));
        graphics.setComposite(AlphaComposite.SrcOver);

        graphics.setFont(new Font(StreamTimerConfig.instance.font.value(), StreamTimerConfig.instance.style.value(), StreamTimerConfig.instance.size.value()));
        graphics.setColor(new Color(StreamTimerConfig.instance.textColor.value(), true));

        FontMetrics fm = graphics.getFontMetrics();
        int x = (this.width - fm.stringWidth(timeText)) / 2;
        int y = (this.height - fm.getHeight()) / 2 + fm.getAscent();

        graphics.drawString(timeText, x, y);
        graphics.dispose();

        sendFrameToWebSocket(imageToByteArray(this.framebuffer));
    }

    public void updateByteBuffer(ByteBuffer buffer) {
        buffer.clear();
        BufferedImage framebuffer = this.getFramebuffer();
        int width = framebuffer.getWidth();
        int height = framebuffer.getHeight();
        int[] pixels = new int[width * height];
        framebuffer.getRGB(0, 0, width, height, pixels, 0, width);
        for (int pixel : pixels) {
            buffer.put((byte) ((pixel >> 16) & 0xFF)); // Red
            buffer.put((byte) ((pixel >> 8) & 0xFF)); // Green
            buffer.put((byte) (pixel & 0xFF)); // Blue
            buffer.put((byte) ((pixel >> 24) & 0xFF)); // Alpha
        }
        buffer.flip();
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public BufferedImage getFramebuffer() {
        return this.framebuffer;
    }

    public ByteBuffer createByteBuffer() {
        return ByteBuffer.allocateDirect(4 * this.width * this.height);
    }

    public static byte[] imageToByteArray(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);

        ByteBuffer byteBuffer = ByteBuffer.allocate(width * height * 4);
        for (int pixel : pixels) {
            byteBuffer.put((byte) ((pixel >> 16) & 0xFF)); // Red
            byteBuffer.put((byte) ((pixel >> 8) & 0xFF));  // Green
            byteBuffer.put((byte) (pixel & 0xFF));  // Blue
            byteBuffer.put((byte) ((pixel >> 24) & 0xFF)); // Alpha
        }
        return byteBuffer.array();
    }

    private static void sendFrameToWebSocket(byte[] frame) {
        if (WebSocketIntegration.isConnected()) WebSocketIntegration.sendFrame(frame);
    }
}

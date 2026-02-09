package dev.dannytaylor.streamtimer.render;

import com.jogamp.opengl.*;
import dev.dannytaylor.streamtimer.StreamTimerMain;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public class Renderer {
    public static final Renderer instance = new Renderer();
    public static volatile BufferedImage frame;

    private final GLProfile profile;
    private final GLCapabilities capabilities;
    private final GLDrawableFactory factory;
    private final GLAutoDrawable drawable;

    public Renderer() {
        this.profile = GLProfile.get(GLProfile.GL2);
        this.capabilities = new GLCapabilities(this.profile);
        this.capabilities.setAlphaBits(8);
        this.capabilities.setOnscreen(false);
        this.capabilities.setPBuffer(true);
        this.capabilities.setDoubleBuffered(false);
        this.factory = GLDrawableFactory.getFactory(this.profile);
        this.drawable = this.factory.createOffscreenAutoDrawable(this.factory.getDefaultDevice(), this.capabilities, null, StreamTimerMain.textRenderer.getWidth(), StreamTimerMain.textRenderer.getHeight());
        this.drawable.addGLEventListener(new TimerRenderer());
        render();
    }

    public void render() {
        this.drawable.display();
    }

    public static void updateFrame() {
        if (instance != null) frame = instance.readFrame();
    }

    public static void updateUIFrame() {
        if (instance != null) {
            BufferedImage updatedFrame = instance.readFrame();
            if (updatedFrame != null) frame = updatedFrame;
        }
    }

    public BufferedImage readFrame() {
        GL2 gl = this.drawable.getGL().getGL2();
        int width = this.drawable.getSurfaceWidth();
        int height = this.drawable.getSurfaceHeight();
        ByteBuffer buffer = ByteBuffer.allocateDirect(width * height * 4);
        gl.glReadPixels(0, 0, width, height, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, buffer);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int i = (y * width + x) * 4;
                int r = buffer.get(i) & 0xFF;
                int g = buffer.get(i + 1) & 0xFF;
                int b = buffer.get(i + 2) & 0xFF;
                int a = buffer.get(i + 3) & 0xFF;
                image.setRGB(x, height - y - 1, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }
        return image;
    }

    public static void tick(String time) {
        StreamTimerMain.textRenderer.render(time);
        instance.render();
        StreamTimerMain.gui.updateTimer();
    }
}
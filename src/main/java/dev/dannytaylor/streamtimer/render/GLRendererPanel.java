package dev.dannytaylor.streamtimer.render;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.awt.GLJPanel;

import java.awt.*;

public class GLRendererPanel extends GLJPanel implements TimerPanel {
    private final GLRenderer glRenderer;

    public GLRendererPanel(GLCapabilities capabilities) {
        super(capabilities);
        this.glRenderer = new GLRenderer();
        this.addGLEventListener(this.glRenderer);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
    }

    public int getTextureID() {
        return this.glRenderer.getTextureID();
    }

    @Override
    public void update() {
        repaint();
    }
}
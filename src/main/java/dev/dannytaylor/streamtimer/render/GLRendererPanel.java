/*
    StreamTimer
    Contributor(s): dannytaylor
    Github: https://github.com/legotaylor/StreamTimer
    Licence: LGPL-3.0
*/

package dev.dannytaylor.streamtimer.render;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.awt.GLJPanel;

import java.awt.*;

public class GLRendererPanel extends GLJPanel implements TimerPanel {
    private final GLRenderer glRenderer;

    public GLRendererPanel(GLCapabilities capabilities) {
        super(capabilities);
        this.setDoubleBuffered(true);
        this.glRenderer = new GLRenderer();
        this.addGLEventListener(this.glRenderer);
        this.setOpaque(false);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        this.setBackground(new Color(0, 0, 0, 0));
    }

    public int getTextureID() {
        return this.glRenderer.getTextureID();
    }

    @Override
    public void update() {
        repaint();
    }
}
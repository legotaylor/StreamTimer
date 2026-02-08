/*
    StreamTimer
    Contributor(s): dannytaylor
    Github: https://github.com/legotaylor/StreamTimer
    Licence: LGPL-3.0
*/

package dev.dannytaylor.streamtimer.render;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import dev.dannytaylor.streamtimer.StreamTimerMain;
import dev.dannytaylor.streamtimer.config.StreamTimerConfig;
import dev.dannytaylor.streamtimer.integration.websocket.WebSocketIntegration;
import dev.dannytaylor.streamtimer.logger.StreamTimerLoggerImpl;

import java.awt.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class TimerRenderer implements GLEventListener {
    private int texID;
    private ByteBuffer buffer;
    private long startTime;

    private int shaderProgram;

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

        try {
            ShaderRegistry shaderRegistry = new ShaderRegistry(gl);
            this.shaderProgram = shaderRegistry.linkProgram(gl);
            this.startTime = System.currentTimeMillis();
        } catch (Exception error) {
            StreamTimerLoggerImpl.error("Failed to register glsl shaders: "+ error);
        }

        int[] ids = new int[1];
        gl.glGenTextures(1, ids, 0);
        this.texID = ids[0];

        gl.glBindTexture(GL2.GL_TEXTURE_2D, this.texID);

        gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
        gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
        gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP);
        gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP);

        gl.glEnable(GL2.GL_TEXTURE_2D);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        if (this.texID != -1) gl.glDeleteTextures(1, new int[]{this.texID}, 0);
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        if (this.buffer == null && StreamTimerMain.textRenderer != null) {
            this.buffer = StreamTimerMain.textRenderer.createByteBuffer();
        }

        if (this.buffer != null) {
            GL2 gl = drawable.getGL().getGL2();
            int windowWidth = drawable.getSurfaceWidth();
            int windowHeight = drawable.getSurfaceHeight();
            gl.glViewport(0, 0, windowWidth, windowHeight);

            StreamTimerMain.textRenderer.updateByteBuffer(buffer);

            gl.glUseProgram(this.shaderProgram);
            gl.glUniform1f(gl.glGetUniformLocation(this.shaderProgram, "uTime"), (System.currentTimeMillis() - this.startTime) / 1000F);
            Color backgroundColor = new Color(StreamTimerConfig.instance.backgroundColor.value(), true);
            gl.glUniform1f(gl.glGetUniformLocation(this.shaderProgram, "uBackground"), StreamTimerConfig.instance.background.value() ? 1.0F : 0.0F);
            gl.glUniform4f(gl.glGetUniformLocation(this.shaderProgram, "uBackgroundColor"), backgroundColor.getRed() / 255.0F, backgroundColor.getGreen() / 255.0F, backgroundColor.getBlue() / 255.0F, backgroundColor.getAlpha() / 255.0F);
            gl.glUniform1f(gl.glGetUniformLocation(this.shaderProgram, "uRainbow"), StreamTimerConfig.instance.rainbow.value() ? 0.0F : 1.0F);
            gl.glUniform1f(gl.glGetUniformLocation(this.shaderProgram, "uShouldDim"), StreamTimerConfig.instance.dimWhenStopped.value() ? 1.0F : 0.0F);
            gl.glUniform1f(gl.glGetUniformLocation(this.shaderProgram, "uActive"), StreamTimerMain.timer.isRunning() ? 1.0F : 0.0F);
            gl.glUniform1f(gl.glGetUniformLocation(this.shaderProgram, "uFinished"), StreamTimerMain.timer.isFinished() ? 1.0F : 0.0F);
            gl.glBindTexture(GL2.GL_TEXTURE_2D, this.texID);

            gl.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
            gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

            int width = StreamTimerMain.textRenderer.getWidth();
            int height = StreamTimerMain.textRenderer.getHeight();

            gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_RGBA, width, height, 0, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, buffer);

            float scaleX = (float) windowWidth / width;
            float scaleY = (float) windowHeight / height;
            float scale = Math.min(scaleX, scaleY);

            float scaledWidth = width * scale;
            float scaledHeight = height * scale;

            float offsetX = (windowWidth - scaledWidth) / 2F;
            float offsetY = (windowHeight - scaledHeight) / 2F;

            float left = -1F + (offsetX / windowWidth) * 2F;
            float right = left + (scaledWidth / windowWidth) * 2F;
            float top = 1F - (offsetY / windowHeight) * 2F;
            float bottom = top - (scaledHeight / windowHeight) * 2F;

            float[] vertices = {
                    left, bottom, 0F, 1F,
                    right, bottom, 1F, 1F,
                    right, top, 1F, 0F,
                    left, top, 0F, 0F
            };

            FloatBuffer vertexBuffer = ByteBuffer
                    .allocateDirect(vertices.length * Float.BYTES)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
            vertexBuffer.put(vertices);
            vertexBuffer.flip();

            int posLoc = gl.glGetAttribLocation(shaderProgram, "aPos");
            int texLoc = gl.glGetAttribLocation(shaderProgram, "aTexCoord");

            gl.glEnableVertexAttribArray(posLoc);
            gl.glEnableVertexAttribArray(texLoc);

            vertexBuffer.position(0);
            gl.glVertexAttribPointer(posLoc, 2, GL.GL_FLOAT, false, 4 * Float.BYTES, vertexBuffer);

            vertexBuffer.position(2);
            gl.glVertexAttribPointer(texLoc, 2, GL.GL_FLOAT, false, 4 * Float.BYTES, vertexBuffer);

            gl.glDrawArrays(GL2.GL_QUADS, 0, 4);

            gl.glDisableVertexAttribArray(posLoc);
            gl.glDisableVertexAttribArray(texLoc);

            gl.glUseProgram(0);

            ByteBuffer frameBuffer = ByteBuffer.allocateDirect(width * height * 4);
            frameBuffer.order(ByteOrder.nativeOrder());
            gl.glReadPixels(0, 0, width, height, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, frameBuffer);

            byte[] frame = new byte[frameBuffer.remaining()];
            frameBuffer.get(frame);
            int rowSize = width * 4;
            byte[] tempRow = new byte[rowSize];
            for (int y = 0; y < height / 2; y++) {
                int topRowStart = y * rowSize;
                int bottomRowStart = (height - y - 1) * rowSize;
                System.arraycopy(frame, topRowStart, tempRow, 0, rowSize);
                System.arraycopy(frame, bottomRowStart, frame, topRowStart, rowSize);
                System.arraycopy(tempRow, 0, frame, bottomRowStart, rowSize);
            }
            this.sendFrameToWebSocket(frame);
        }
        Renderer.updateFrame();
    }

    private void sendFrameToWebSocket(byte[] frame) {
        if (WebSocketIntegration.isConnected()) WebSocketIntegration.sendProcessedFrame(frame);
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glViewport(0, 0, width, height);
    }

    public int getTextureID() {
        return texID;
    }
}
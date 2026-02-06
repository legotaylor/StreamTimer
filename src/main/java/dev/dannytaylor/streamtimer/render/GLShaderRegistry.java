/*
    StreamTimer
    Contributor(s): dannytaylor
    Github: https://github.com/legotaylor/StreamTimer
    Licence: LGPL-3.0
*/

package dev.dannytaylor.streamtimer.render;

import com.jogamp.opengl.GL2;
import dev.dannytaylor.streamtimer.data.StaticVariables;
import dev.dannytaylor.streamtimer.logger.StreamTimerLoggerImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GLShaderRegistry {
    public final int vert;
    public final int frag;

    public GLShaderRegistry(GL2 gl) throws IOException {
        this.vert = compileShader(gl, Path.of(StaticVariables.name + "Assets/vertex.glsl"), GL2.GL_VERTEX_SHADER);
        this.frag = compileShader(gl, Path.of(StaticVariables.name + "Assets/fragment.glsl"), GL2.GL_FRAGMENT_SHADER);
    }

    private static int compileShader(GL2 gl, Path path, int type) throws IOException {
        String source = Files.readString(path);
        int shader = gl.glCreateShader(type);
        gl.glShaderSource(shader, 1, new String[]{source}, null, 0);
        gl.glCompileShader(shader);
        int[] compiled = new int[1];
        gl.glGetShaderiv(shader, GL2.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            byte[] infoLog = new byte[1024];
            gl.glGetShaderInfoLog(shader, infoLog.length, null, 0, infoLog, 0);
            throw new RuntimeException("Error compiling shader: " + new String(infoLog));
        }
        return shader;
    }

    public int linkProgram(GL2 gl) {
        int program = gl.glCreateProgram();
        gl.glAttachShader(program, vert);
        gl.glAttachShader(program, frag);
        gl.glLinkProgram(program);

        int[] linked = new int[1];
        gl.glGetProgramiv(program, GL2.GL_LINK_STATUS, linked, 0);
        if (linked[0] == 0) {
            int[] infoLen = new int[1];
            gl.glGetShaderiv(program, GL2.GL_INFO_LOG_LENGTH, infoLen, 0);
            if (infoLen[0] > 0) {
                byte[] infoLog = new byte[infoLen[0]];
                gl.glGetShaderInfoLog(program, infoLen[0], null, 0, infoLog, 0);
                StreamTimerLoggerImpl.error("Error compiling shader: " + new String(infoLog));
            }
        }

        return program;
    }
}
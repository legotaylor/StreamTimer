package dev.dannytaylor.streamtimer.render;

import dev.dannytaylor.streamtimer.data.StaticVariables;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class Resources {
    public static ImageIcon getTexture(URL location) {
        return getTexture(location, getMissing());
    }

    public static ImageIcon getTexture(URL location, URL fallback) {
        URL imageUrl = location != null && Toolkit.getDefaultToolkit().getImage(location) != null ? location : fallback;
        return imageUrl != null ? new ImageIcon(imageUrl) : null;
    }

    public static ImageIcon getTexture(URL location, int width, int height, int imageScalingAlgorithm, URL fallback) {
        ImageIcon image = getTexture(location, fallback);
        return image != null ? new ImageIcon(image.getImage().getScaledInstance(width, height, imageScalingAlgorithm)) : null;
    }

    public static ImageIcon getTexture(URL location, int width, int height) {
        return getTexture(location, width, height, Image.SCALE_SMOOTH, getMissing());
    }

    public static ImageIcon getTextureWithFallback(URL location, int width, int height, URL fallback) {
        return getTexture(location, width, height, Image.SCALE_SMOOTH, fallback);
    }

    public static URL getMissing() {
        return Resources.class.getResource("/assets/" + StaticVariables.id + "/missing.png");
    }

    public static URL getShader(String filename) {
        return Resources.class.getResource("/assets/" + StaticVariables.id + "/shaders/" + filename);
    }
}

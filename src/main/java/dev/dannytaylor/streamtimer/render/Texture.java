package dev.dannytaylor.streamtimer.render;

import dev.dannytaylor.streamtimer.data.StaticVariables;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class Texture {
    public static ImageIcon getTexture(URL location) {
        URL imageUrl = location != null && Toolkit.getDefaultToolkit().getImage(location) != null ? location : getMissing();
        return imageUrl != null ? new ImageIcon(imageUrl) : null;
    }

    public static ImageIcon getTexture(URL location, int width, int height, int imageScalingAlgorithm) {
        ImageIcon image = getTexture(location);
        return image != null ? new ImageIcon(image.getImage().getScaledInstance(width, height, imageScalingAlgorithm)) : null;
    }

    public static ImageIcon getTexture(URL location, int width, int height) {
        return getTexture(location, width, height, Image.SCALE_SMOOTH);
    }

    public static URL getMissing() {
        return Texture.class.getResource("/assets/" + StaticVariables.id + "/missing.png");
    }
}

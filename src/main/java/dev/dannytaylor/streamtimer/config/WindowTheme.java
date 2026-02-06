package dev.dannytaylor.streamtimer.config;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import org.quiltmc.config.api.values.ComplexConfigValue;
import org.quiltmc.config.api.values.ConfigSerializableObject;

import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.util.ArrayList;

public enum WindowTheme implements ConfigSerializableObject<String> {
    FLAT_AUTO("Flat IntelliJ/Darcula Auto", new FlatIntelliJLaf(), new FlatDarculaLaf()),
    FLAT_INTELLIJ("Flat IntelliJ", new FlatIntelliJLaf()),
    FLAT_DARCULA("Flat Darcula", new FlatDarculaLaf()),
    FLAT_MAC_AUTO("Flat Mac Auto", new FlatMacLightLaf(), new FlatMacDarkLaf()),
    FLAT_MAC_LIGHT("Flat Mac Light", new FlatMacLightLaf()),
    FLAT_MAC_DARK("Flat Mac Dark", new FlatMacDarkLaf()),
    NIMBUS("Nimbus", new NimbusLookAndFeel()),
    METAL("Metal", new MetalLookAndFeel());

    private final String name;
    private final LookAndFeel lightLaf;
    private final LookAndFeel darkLaf;

    WindowTheme(String name, LookAndFeel lightLaf, LookAndFeel darkLaf) {
        this.name = name;
        this.lightLaf = lightLaf;
        this.darkLaf = darkLaf;
    }

    WindowTheme(String name, LookAndFeel laf) {
        this(name, laf, null);
    }

    public String getName() {
        return this.name;
    }

    public LookAndFeel getTheme(boolean isDark) {
        return isDark && this.darkLaf != null ? darkLaf : lightLaf;
    }

    @Override
    public ConfigSerializableObject<String> convertFrom(String representation) {
        return valueOf(representation);
    }

    @Override
    public String getRepresentation() {
        return this.name();
    }

    @Override
    public ComplexConfigValue copy() {
        return this;
    }

    public static String[] getAllNames() {
        ArrayList<String> names = new ArrayList<>();
        for (WindowTheme theme : values()) names.add(theme.getName());
        return names.toArray(new String[0]);
    }
}

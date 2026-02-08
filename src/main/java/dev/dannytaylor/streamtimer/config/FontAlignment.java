/*
    StreamTimer
    Contributor(s): dannytaylor
    Github: https://github.com/legotaylor/StreamTimer
    Licence: LGPL-3.0
*/

package dev.dannytaylor.streamtimer.config;

import org.quiltmc.config.api.values.ComplexConfigValue;
import org.quiltmc.config.api.values.ConfigSerializableObject;

public enum FontAlignment implements ConfigSerializableObject<String> {
    LEFT("Left"),
    CENTER("Center"),
    RIGHT("Right");

    public final String display;

    FontAlignment(String display) {
        this.display = display;
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

    @Override
    public String toString() {
        return this.display;
    }
}

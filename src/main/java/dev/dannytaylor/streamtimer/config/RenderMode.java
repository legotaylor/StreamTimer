/*
    StreamTimer
    Contributor(s): dannytaylor
    Github: https://github.com/legotaylor/StreamTimer
    Licence: LGPL-3.0
*/

package dev.dannytaylor.streamtimer.config;

import org.quiltmc.config.api.values.ComplexConfigValue;
import org.quiltmc.config.api.values.ConfigSerializableObject;

public enum RenderMode implements ConfigSerializableObject<String> {
    GL_FRAME(RenderType.FRAME, true),
    GL_DIALOG(RenderType.DIALOG, true),
    TEXT_FRAME(RenderType.FRAME, false),
    TEXT_DIALOG(RenderType.DIALOG, false);

    private final RenderType renderType;
    private final boolean gl;

    RenderMode(RenderType renderType, boolean gl) {
        this.renderType = renderType;
        this.gl = gl;
    }

    public RenderType getRenderType() {
        return this.renderType;
    }

    public boolean usesGL() {
        return this.gl;
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

    public enum RenderType {
        FRAME,
        DIALOG
    }
}

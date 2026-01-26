package dev.dannytaylor.streamtimer.config;

import dev.dannytaylor.streamtimer.data.StaticVariables;
import org.quiltmc.config.api.ReflectiveConfig;
import org.quiltmc.config.api.serializers.TomlSerializer;
import org.quiltmc.config.api.values.TrackedValue;

import java.io.File;
import java.nio.file.Files;

public class StreamTimerConfig extends ReflectiveConfig {
    public static StreamTimerConfig instance = ConfigHelper.register(StaticVariables.id, StaticVariables.id, StreamTimerConfig.class);

    public static void bootstrap() {
        System.out.println("[Stream Timer] Initialized Config");
    }

    public final TrackedValue<Boolean> skipSetupScreen = this.value(false);
    public final TrackedValue<RenderMode> previousRenderMode = this.value(RenderMode.GL_FRAME);
    public final TrackedValue<Boolean> forceFocus = this.value(true);

    public final TrackedValue<Long> time = this.value(21600000L);
    public final TrackedValue<Boolean> reversed = this.value(false);

    public final TrackedValue<Boolean> background = this.value(true);
    public final TrackedValue<Integer> backgroundColor = this.value(-16711936);
    public final TrackedValue<Integer> textColor = this.value(-1);
    public final TrackedValue<String> font = this.value("SansSerif");
    public final TrackedValue<Integer> style = this.value(1);
    public final TrackedValue<Integer> size = this.value(72);
    public final TrackedValue<Boolean> rainbow = this.value(false);
    public final TrackedValue<Boolean> dimWhenStopped = this.value(true);
    public final TrackedValue<Boolean> finishSound = this.value(false);

    public final TrackedValue<String> addHours = this.value("01");
    public final TrackedValue<String> addMinutes = this.value("00");
    public final TrackedValue<String> addSeconds = this.value("00");

    public static void reload() {
        try {
            TomlSerializer.INSTANCE.deserialize(instance, Files.newInputStream(new File(StaticVariables.name + "Assets/" + StaticVariables.id + ".toml").toPath()));
        } catch (Exception error) {
            System.err.println("[StreamTimer] Error occurred whilst reloading config: " + error);
        }
    }
}

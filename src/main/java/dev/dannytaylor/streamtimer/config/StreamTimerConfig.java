package dev.dannytaylor.streamtimer.config;

import dev.dannytaylor.streamtimer.data.StaticVariables;
import org.quiltmc.config.api.ReflectiveConfig;
import org.quiltmc.config.api.values.TrackedValue;

public class StreamTimerConfig extends ReflectiveConfig {
    public static StreamTimerConfig instance = ConfigHelper.register(StaticVariables.id, StaticVariables.id, StreamTimerConfig.class);

    public static void bootstrap() {
        System.out.println("[Stream Timer] Initialized Config");
    }

    public final TrackedValue<Long> time = this.value(21600000L);
}

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

    public final TrackedValue<Integer> backgroundColor = this.value(0x00FF00);
    public final TrackedValue<String> font = this.value("SansSerif");
    public final TrackedValue<Integer> style = this.value(1);
    public final TrackedValue<Integer> size = this.value(72);

    public final TrackedValue<String> addHours = this.value("01");
    public final TrackedValue<String> addMinutes = this.value("00");
    public final TrackedValue<String> addSeconds = this.value("00");
}

/*
    StreamTimer
    Contributor(s): dannytaylor
    Github: https://github.com/legotaylor/StreamTimer
    Licence: LGPL-3.0
*/

package dev.dannytaylor.streamtimer.config;

import dev.dannytaylor.streamtimer.data.StaticVariables;
import dev.dannytaylor.streamtimer.integration.twitch.TwitchPermission;
import dev.dannytaylor.streamtimer.logger.StreamTimerLoggerImpl;
import org.quiltmc.config.api.ReflectiveConfig;
import org.quiltmc.config.api.annotations.Comment;
import org.quiltmc.config.api.serializers.TomlSerializer;
import org.quiltmc.config.api.values.TrackedValue;
import org.quiltmc.config.api.values.ValueList;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

public class StreamTimerConfig extends ReflectiveConfig {
    public static StreamTimerConfig instance = ConfigHelper.register(StaticVariables.id, StaticVariables.id, StreamTimerConfig.class);

    public static void bootstrap() {
        StreamTimerLoggerImpl.info("Initialized Config");
    }

    public final TrackedValue<Boolean> skipSetupScreen = this.value(false);
    public final TrackedValue<RenderMode> previousRenderMode = this.value(RenderMode.GL_FRAME);
    public final TrackedValue<Boolean> forceFocus = this.value(true);

    public final TrackedValue<Long> time = this.value(21600000L);
    public final TrackedValue<Boolean> reversed = this.value(false);
    public final TrackedValue<Integer> saveTicks = this.value(1200);

    public final TrackedValue<Boolean> background = this.value(false);
    public final TrackedValue<Integer> backgroundColor = this.value(-16711936);
    public final TrackedValue<Integer> textColor = this.value(-1);
    public final TrackedValue<String> font = this.value("SansSerif");
    public final TrackedValue<Integer> style = this.value(1);
    public final TrackedValue<Integer> size = this.value(72);
    public final TrackedValue<Boolean> rainbow = this.value(false);
    public final TrackedValue<Boolean> dimWhenStopped = this.value(true);
    public final TrackedValue<Boolean> finishSound = this.value(false);
    public final TrackedValue<Boolean> showMillis = this.value(false);

    public final TrackedValue<String> addHours = this.value("01");
    public final TrackedValue<String> addMinutes = this.value("00");
    public final TrackedValue<String> addSeconds = this.value("00");

    public final TrackedValue<Integer> webSocketPort = this.value(34251);
    public final TrackedValue<Boolean> webSocketAutoConnect = this.value(false);

    public final TwitchTimeSettings twitchTimes = new TwitchTimeSettings();

    public static class TwitchTimeSettings extends Section {
        public final TrackedValue<TwitchPermission> commandEnabled = this.value(TwitchPermission.MODERATOR);
        @Comment("You can use - to subtract. Characters used before digits will be ignored. The monetary amount must be after !add (separated by a space).")
        public final TrackedValue<ValueList<String>> addCommand = this.list("", "!add");
        @Comment("Example: !set HH:MM:SS")
        public final TrackedValue<ValueList<String>> setCommand = this.list("", "!set");
        public final TrackedValue<ValueList<String>> toggleCommand = this.list("", "!start", "!stop");
        public final TrackedValue<Integer> moneySeconds = this.value(900);
        public final TrackedValue<Float> money = this.value(1.0F);

        public final TrackedValue<Boolean> bitsEnabled = this.value(true);
        public final TrackedValue<Integer> bitsSeconds = this.value(600);
        public final TrackedValue<Integer> bits = this.value(100);

        public final TrackedValue<Boolean> tierOneEnabled = this.value(true);
        public final TrackedValue<Integer> tierOneSeconds = this.value(1200);

        public final TrackedValue<Boolean> tierTwoEnabled = this.value(true);
        public final TrackedValue<Integer> tierTwoSeconds = this.value(2400);

        public final TrackedValue<Boolean> tierThreeEnabled = this.value(true);
        public final TrackedValue<Integer> tierThreeSeconds = this.value(4800);

        public final TrackedValue<Boolean> followEnabled = this.value(false);
        public final TrackedValue<Integer> followSeconds = this.value(0);

        public final TrackedValue<Float> multiplier = this.value(1.0F);
    }

    public final TrackedValue<WindowTheme> theme = this.value(WindowTheme.FLAT_AUTO);

    public final TrackedValue<Boolean> debug = this.value(false);

    public static void reload() {
        try {
            TomlSerializer.INSTANCE.deserialize(instance, Files.newInputStream(new File(StaticVariables.name + "Assets/" + StaticVariables.id + ".toml").toPath()));
        } catch (Exception error) {
            StreamTimerLoggerImpl.error("Error occurred whilst reloading config: " + error);
        }
    }

    public static boolean containsIgnoresCase(List<String> strings, String value) {
        for (String string : strings) {
            if (string.equalsIgnoreCase(value)) return true;
        }
        return false;
    }

    public static void toFile() {
        StreamTimerLoggerImpl.info("Saving config to file!");
        instance.save();
    }
}

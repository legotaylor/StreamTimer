/*
    StreamTimer
    Contributor(s): dannytaylor
    Github: https://github.com/legotaylor/StreamTimer
    Licence: LGPL-3.0
*/

package dev.dannytaylor.streamtimer.integration;

import dev.dannytaylor.streamtimer.config.ConfigHelper;
import dev.dannytaylor.streamtimer.data.StaticVariables;
import dev.dannytaylor.streamtimer.logger.StreamTimerLoggerImpl;
import dev.dannytaylor.streamtimer.util.Crypt;
import org.quiltmc.config.api.ReflectiveConfig;
import org.quiltmc.config.api.annotations.Comment;
import org.quiltmc.config.api.serializers.TomlSerializer;
import org.quiltmc.config.api.values.TrackedValue;
import org.quiltmc.config.api.values.ValueList;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class AuthConfig extends ReflectiveConfig {
    private static final String fileName = "auth-do-not-share";
    private static final Path keyPath = new File(StaticVariables.name + "Assets/" + fileName + ".key").toPath();
    public static AuthConfig instance = ConfigHelper.register(StaticVariables.id, fileName, AuthConfig.class);

    public static void bootstrap() {
        checkEncryption();
        StreamTimerLoggerImpl.info("Initialized AuthConfig");
    }

    @Comment("DO NOT SHARE THIS FILE WITH ANYONE!")
    public final TrackedValue<String> twitchId = this.value("PUT YOUR CLIENT ID HERE");
    public final TrackedValue<String> twitchSecret = this.value("PUT YOUR CLIENT SECRET HERE");
    public final TrackedValue<ValueList<String>> twitchChannels = this.list("", "");
    public final TrackedValue<Integer> twitchPort = this.value(34250);
    public final TrackedValue<Integer> twitchTimeout = this.value(60000); // This gives users one minute before auth times out.
    public final TrackedValue<Boolean> twitchAutoConnect = this.value(false);
    public final TrackedValue<String> twitchAccessToken = this.value("");
    public final TrackedValue<String> twitchRefreshToken = this.value("");
    public final TrackedValue<Long> twitchTokenExpiry = this.value(0L);

    public static void reload() {
        try {
            TomlSerializer.INSTANCE.deserialize(instance, Files.newInputStream(new File(StaticVariables.name + "Assets/" + fileName + ".toml").toPath()));
            checkEncryption();
        } catch (Exception error) {
            StreamTimerLoggerImpl.error("Error occurred whilst reloading config: " + error);
        }
    }

    private static void checkEncryption() {
        try {
            if (!Crypt.fileExists(keyPath)) Crypt.toFile(keyPath, Crypt.createKey());
        } catch (Exception error) {
            StreamTimerLoggerImpl.error("Failed to create key: " + error);
        }
        try {
            encryptIfNeeded(instance.twitchSecret);
            encryptIfNeeded(instance.twitchAccessToken);
            encryptIfNeeded(instance.twitchRefreshToken);
        } catch (Exception error) {
            StreamTimerLoggerImpl.error("Failed to check encryption: " + error);
        }
    }

    public static void encryptIfNeeded(TrackedValue<String> value) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, IOException, InvalidKeyException {
        if (!value.value().isBlank() && !Crypt.isEncrpyted(value.value())) value.setValue(encrypt(value.value()), true);
    }

    public static String encrypt(String data) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IOException {
        return Crypt.encrypt(data, Crypt.fromFile(keyPath));
    }

    public static String decrypt(String data) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, IOException {
        return Crypt.decrypt(data, Crypt.fromFile(keyPath));
    }

    public static void toFile() {
        StreamTimerLoggerImpl.info("Saving auth config to file!");
        instance.save();
    }
}

/*
    StreamTimer
    Contributor(s): dannytaylor
    Github: https://github.com/legotaylor/StreamTimer
    Licence: LGPL-3.0
*/

package dev.dannytaylor.streamtimer.config;

import dev.dannytaylor.streamtimer.data.StaticVariables;
import org.quiltmc.config.api.ReflectiveConfig;
import org.quiltmc.config.api.serializers.TomlSerializer;
import org.quiltmc.config.implementor_api.ConfigEnvironment;
import org.quiltmc.config.implementor_api.ConfigFactory;

import java.nio.file.Paths;

public class ConfigHelper {
	private static ConfigEnvironment environment;

	public static <C extends ReflectiveConfig> C register(String namespace, String id, Class<C> config) {
		return ConfigFactory.create(getConfigEnvironment(), namespace, id, Paths.get(StaticVariables.name + "Assets").toAbsolutePath(), builder -> {}, config, builder -> {});
	}

	public static ConfigEnvironment getConfigEnvironment() {
		if (environment == null) {
			environment = new ConfigEnvironment(Paths.get("").toAbsolutePath(), "toml", TomlSerializer.INSTANCE);
			environment.registerSerializer(TomlSerializer.INSTANCE);
		}
		return environment;
	}
}

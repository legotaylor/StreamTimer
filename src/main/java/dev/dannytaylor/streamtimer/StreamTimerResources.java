/*
    StreamTimer
    Contributor(s): dannytaylor
    Github: https://github.com/legotaylor/StreamTimer
    Licence: LGPL-3.0
*/

package dev.dannytaylor.streamtimer;

import dev.dannytaylor.streamtimer.config.StreamTimerConfig;
import dev.dannytaylor.streamtimer.data.StaticVariables;

import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public class StreamTimerResources {
    private static final String location = "assets/streamtimer/export/";

    public static void extract() {
        try {
            Path outDir = Path.of(StaticVariables.name + "Assets");
            Files.createDirectories(outDir);
            try {
                Path configPath = Paths.get(StaticVariables.id + ".toml");
                Path targetPath = outDir.resolve(StaticVariables.id + ".toml");
                if (Files.exists(configPath)) {
                    boolean shouldReload = Files.exists(targetPath);
                    Files.move(configPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    if (shouldReload) StreamTimerConfig.reload();
                }
            } catch (Exception error) {
                System.err.println("Failed to move config: " + error);
            }
            try {
                ClassLoader loader = StreamTimerResources.class.getClassLoader();
                URL locationUrl = loader.getResource(location);
                if (locationUrl == null) throw new RuntimeException("Assets not found: " + location);
                if ("jar".equals(locationUrl.getProtocol())) extractFromJar(locationUrl, outDir);
                else extractFromFileSystem(Path.of(locationUrl.toURI()), outDir);
            } catch (Exception error) {
                System.err.println("Failed to copy assets: " + error);
            }
        } catch (Exception error) {
            System.err.println("Failed to process resources: " + error);
        }
    }

    private static void extractFromJar(URL resourceUrl, Path outputDir) {
        String jarPath = resourceUrl.getPath().substring(5, resourceUrl.getPath().indexOf("!"));
        jarPath = URLDecoder.decode(jarPath, StandardCharsets.UTF_8);
        try (JarFile jar = new JarFile(jarPath)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (!name.startsWith(location)) continue;
                Path outPath = outputDir.resolve(name.substring(location.length()));
                if (entry.isDirectory()) Files.createDirectories(outPath);
                else {
                    try (InputStream in = jar.getInputStream(entry)) {
                        if (!outPath.toFile().exists()) {
                            Files.createDirectories(outPath.getParent());
                            Files.copy(in, outPath, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                }
            }
        } catch (Exception error) {
            System.err.println("Failed to copy assets from jar: " + error);
        }
    }

    private static void extractFromFileSystem(Path sourceDir, Path outputDir) {
        try (Stream<Path> files = Files.walk(sourceDir)) {
            for (Path path : (Iterable<Path>) files::iterator) {
                try {
                    Path outPath = outputDir.resolve(sourceDir.relativize(path).toString());
                    if (Files.isDirectory(path)) Files.createDirectories(outPath);
                    else {
                        if (!outPath.toFile().exists()) {
                            Files.createDirectories(outPath.getParent());
                            Files.copy(path, outPath, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (Exception error) {
            System.err.println("Failed to copy assets from file system: " + error);
        }
    }
}

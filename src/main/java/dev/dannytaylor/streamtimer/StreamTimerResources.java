package dev.dannytaylor.streamtimer;

import dev.dannytaylor.streamtimer.data.StaticVariables;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class StreamTimerResources {
    public static void extract() {
        try {
            Path jarPath = Paths.get(StreamTimerResources.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Path jarDir = jarPath.getParent();
            Path outputDir = jarDir.resolve(StaticVariables.name + "Assets");
            Files.createDirectories(outputDir);
            try (JarFile jarFile = new JarFile(jarPath.toFile())) {
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();
                    if (!name.startsWith("assets/streamtimer/export/")) continue;
                    Path outPath = outputDir.resolve(name.substring("assets/streamtimer/export/".length()));
                    if (entry.isDirectory()) Files.createDirectories(outPath);
                    else {
                        Files.createDirectories(outPath.getParent());
                        try (InputStream in = jarFile.getInputStream(entry)) {
                            if (!new File(outPath.toUri()).exists()) Files.copy(in, outPath, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                }
            }
        } catch (Exception error) {
            System.err.println("Failed to copy assets: " + error);
        }
    }
}

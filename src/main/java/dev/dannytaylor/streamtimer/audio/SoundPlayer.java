package dev.dannytaylor.streamtimer.audio;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;

public class SoundPlayer {
    public static void playSound(File file) {
        if (file.exists()) {
            new Thread(() -> {
                try {
                    AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
                    Clip clip = AudioSystem.getClip();
                    clip.open(audioStream);
                    clip.start();
                    Thread.sleep(clip.getMicrosecondLength() / 1000);
                } catch (Exception error) {
                    System.err.println("Failed to play sound: " + error);
                }
            }).start();
        } else {
            System.err.println("Failed to play sound: Specified file does not exist " + file);
        }
    }
}

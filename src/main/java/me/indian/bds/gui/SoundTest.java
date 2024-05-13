package me.indian.bds.gui;

import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import me.indian.bds.util.ThreadUtil;

public class SoundTest {
    public static void main(final String[] args) {


        final File soundFile = new File("cos.wav");

        if (!soundFile.exists()) {
            System.out.println("Plik dźwiękowy nie istnieje.");
            return;
        }

        System.out.println(soundFile.getAbsolutePath());


        try (final AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile)) {
            final Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.loop(0);
            clip.start();


            final long length = clip.getMicrosecondLength();
            System.out.println("Length: " + length / 1000000.0 + " seconds");

            while (clip.isRunning()) {
                final long remaining = length - clip.getMicrosecondPosition();
                System.out.println("Remaining: " + remaining / 1000000.0 + " seconds");

                ThreadUtil.sleep(500L);
            }

        } catch (final Exception exception) {
            exception.printStackTrace();
        }


    }
}

package core;

import javax.sound.sampled.AudioInputStream; // for playing sound clips
import javax.sound.sampled.*;
import java.io.*;
import java.util.HashMap; // for storing sound clips

public class JevaSound {
    private Clip source;

    private float volume;

    public JevaSound(String fileName) {
        String path = "sounds/";
        source = loadClip(path.concat(fileName));

        volume = 1.0f;
    }

    private Clip loadClip(String fileName) { // gets clip from the specified file
        AudioInputStream audioIn;
        Clip clip = null;

        try {
            File file = new File(fileName);
            audioIn = AudioSystem.getAudioInputStream(file.toURI().toURL());
            clip = AudioSystem.getClip();
            clip.open(audioIn);
        } catch (Exception e) {
            System.out.println("Error opening sound files: " + e);
        }
        return clip;
    }

    protected Clip getSource() {
        return source;
    }

    public void play(boolean looping) {
        if (source != null) {
            source.setFramePosition(0);
            if (looping)
                source.loop(Clip.LOOP_CONTINUOUSLY);
            else
                source.start();
        }
    }

    public void stop() {
        if (source != null) {
            source.stop();
        }
    }
}
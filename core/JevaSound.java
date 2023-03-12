package core;

import javax.sound.sampled.AudioInputStream; // for playing sound clips
import javax.sound.sampled.*;
import java.io.*;

public class JevaSound {
    private MultiClip source;

    public JevaSound(String fileName, int amount) {
        String path = "sounds/";
        source = new MultiClip(path.concat(fileName), amount);
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

    protected MultiClip getSource() {
        return source;
    }

    public void playOnce() {
        source.playClip(false);
    }

    public void playLoop(boolean looping) {
        source.playClip(looping);
    }

    public void pause() {
        source.pauseClip();
    }

    public void resume() {
        source.resumeClip();
    }

    public void stop() {
        source.stopClip();
    }

    private class MultiClip {
        private Clip[] clipBag;
        private int[] clipFramePos;
        private boolean[] clipWasPlaying;
        private boolean[] clipLooping;

        private int maxClips;
        private int clipNum;

        public MultiClip(String fileName, int amount) {
            clipBag = new Clip[amount];
            clipFramePos = new int[amount];
            clipWasPlaying = new boolean[amount];
            clipLooping = new boolean[amount];
            for (int i = 0; i < amount; i++) {
                Clip clip = loadClip(fileName);
                clipBag[i] = clip;
                clipFramePos[i] = 0;
                clipWasPlaying[i] = false;
                clipLooping[i] = false;
            }

            maxClips = amount;
            clipNum = 0;
        }

        public void playClip(boolean looping) {
            Clip clip = clipBag[clipNum];
            clipNum = (clipNum + 1) % maxClips;

            clip.setFramePosition(0);
            if (looping)
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            else
                clip.start();
            clipWasPlaying[clipNum] = true;
            clipLooping[clipNum] = looping;
        }

        public void pauseClip() {
            for (int i = 0; i < maxClips; i++) {
                clipFramePos[i] = clipBag[i].getFramePosition();
                clipBag[i].stop();
            }
        }

        public void resumeClip() {
            if (maxClips == 1) {
                playClip(false);
            } else
                for (int i = 0; i < maxClips; i++) {
                    if (clipWasPlaying[i]) {
                        clipBag[i].setFramePosition(clipFramePos[i]);
                        if (clipLooping[i])
                            clipBag[i].loop(Clip.LOOP_CONTINUOUSLY);
                        else
                            clipBag[i].start();
                    }
                }
        }

        public void stopClip() {
            for (int i = 0; i < maxClips; i++) {
                clipBag[i].stop();
                clipWasPlaying[i] = false;
                clipLooping[i] = false;
            }
        }
    }
}
package core;

import javax.sound.sampled.AudioInputStream; // for playing sound clips
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.sound.sampled.*;

import java.awt.Label;
import java.io.*;
import java.util.ArrayList;

public class JevaSound {
    protected static String sourcePath = "sounds/";
    private MultiClip source;
    private String _label;
    private String fileName;
    private int _amount;
    private static double masterVolume = 1;
    private static ArrayList<MultiClip> multiClipReferences = new ArrayList<>();

    protected JevaSound(String _label, String fileName) {
        this(_label, fileName, 1);
    }

    protected JevaSound(String _label, String fileName, int amount) {
        String path = sourcePath;
        String filePath = path.concat(fileName);
        source = new MultiClip(filePath, amount);
        this._label = _label;
        this.fileName = fileName;
        this._amount = amount;
        multiClipReferences.add(source);
    }

    protected JevaSound clone() {
        String newLabel = _label.concat(JevaUtils.generateUUID());

        JevaSound sound =  new JevaSound(newLabel, fileName, _amount);
        sound.setVolume(getVolume());

        return sound;
    }

    public String getLabel() {
        return _label;
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

    public JevaSound playOnce() {
        source.playClip(false);
        return this;
    }

    public JevaSound playLoop() {
        source.playClip(true);
        return this;
    }

    public JevaSound pause() {
        source.pauseClip();
        return this;
    }

    public JevaSound resume() {
        source.resumeClip();
        return this;
    }

    public JevaSound setVolume(double volume) {
        if (source.getVolume() != volume)
            source.setVolume(volume);
        return this;
    }

    public double getVolume() {
        return source.getVolume();
    }

    public JevaSound stop() {
        source.stopClip();
        return this;
    }

    private class MultiClip {
        private Clip[] clipBag;
        private int[] clipFramePos;
        private boolean[] clipWasPlaying;
        private boolean[] clipLooping;

        private int maxClips;
        private int clipNum;
        private double volume;

        private MultiClip(String fileName, int amount) {
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
            setVolume(1);
        }

        private void playClip(boolean looping) {
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

        private void pauseClip() {
            for (int i = 0; i < maxClips; i++) {
                clipFramePos[i] = clipBag[i].getFramePosition();
                clipBag[i].stop();
            }
        }

        private void resumeClip() {
            if (maxClips == 1) {
                playClip(false);
            } else {
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
        }

        private double getVolume() {
            return this.volume;
        }

        private void setVolume(double volume) {
            volume = JevaUtils.clampDouble(volume, 0, 1);
            this.volume = volume;
            updateVolume();
        }

        private void updateVolume() {
            double volume = JevaUtils.clampDouble(this.volume * masterVolume, 0, 1);
            double actual = JevaUtils.clampDouble(volume < 0.296 ? volume * 0.1 : Math.exp((volume * 5) - 5), 0, 1);
            for (int i = 0; i < maxClips; i++) {
                FloatControl gainControl = (FloatControl) clipBag[i].getControl(FloatControl.Type.MASTER_GAIN);
                float gainValue = JevaUtils.clampFloat(20f * (float) Math.log10(actual), gainControl.getMinimum(),
                        gainControl.getMaximum());
                gainControl.setValue(gainValue);
            }
        }

        private void stopClip() {
            for (int i = 0; i < maxClips; i++) {
                clipBag[i].stop();
                clipWasPlaying[i] = false;
                clipLooping[i] = false;
            }
        }
    }

    public static double getMasterVolume() {
        return masterVolume;
    }

    public static void setMasterVolume(double volume) {
        volume = JevaUtils.clampDouble(volume, 0, 1);
        masterVolume = volume;

        ArrayList<MultiClip> multiClipReferencesTemp = new ArrayList<>(multiClipReferences);
        for (MultiClip clip : multiClipReferencesTemp)
            clip.updateVolume();
    }
}
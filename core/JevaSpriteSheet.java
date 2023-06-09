package core;

import java.awt.Image;
import java.util.ArrayList;

/**
 * The Animation class manages a series of images (frames) and
 * the amount of time to display each frame.
 */
public class JevaSpriteSheet {
    private JevaR core;
    private ArrayList<AnimFrame> frames; // collection of frames for animation
    private int currFrameIndex; // current frame being displayed
    private long animTime; // time that the animation has run for already
    private long startTime; // start time of the animation or time since last update
    private long totalDuration; // total duration of the animation

    /**
     * Creates a new, empty Animation.
     */
    protected JevaSpriteSheet(JevaR core, JevaScript _init) {
        this.core = core;
        frames = new ArrayList<AnimFrame>();
        totalDuration = 0;
        _init.call(this);
    }

    /**
     * Adds an image to the animation with the specified
     * duration (time to display the image).
     */
    protected synchronized void addFrame(String _label, long duration) {
        JevaGraphic graphic = core.jevagraphicLibrary.get(_label);

        if (graphic == null)
            return;

        Image source = graphic.getSource();

        totalDuration += duration;
        frames.add(new AnimFrame(source, totalDuration));
    }

    /**
     * Starts this animation over from the beginning.
     */
    protected synchronized void reset() {
        animTime = 0; // reset time animation has run for to zero
        currFrameIndex = 0; // reset current frame to first frame
        startTime = System.currentTimeMillis(); // reset start time to current time
    }

    /**
     * Updates this animation's current image (frame), if
     * neccesary.
     */
    protected synchronized void tick() {
        long currTime = System.currentTimeMillis(); // find the current time
        long elapsedTime = currTime - startTime; // find how much time has elapsed since last update
        startTime = currTime; // set start time to current time

        if (frames.size() > 1) {
            animTime += elapsedTime; // add elapsed time to amount of time animation has run for
            if (animTime >= totalDuration) { // if the time animation has run for > total duration
                animTime = animTime % totalDuration; // reset time animation has run for
                currFrameIndex = 0; // reset current frame to first frame
            }

            while (animTime > getFrame(currFrameIndex).endTime) {
                currFrameIndex++; // set frame corresponding to time animation has run for
            }
        }

    }

    /**
     * Gets this Animation's current image. Returns null if this
     * animation has no images.
     */
    protected synchronized Image getSource() {
        if (frames.size() == 0) {
            return null;
        } else {
            return getFrame(currFrameIndex).image;
        }
    }

    protected int getNumFrames() { // find out how many frames in animation
        return frames.size();
    }

    protected AnimFrame getFrame(int i) { // returns ith frame in the collection
        return frames.get(i);
    }

    private class AnimFrame { // inner class for the frames of the animation

        Image image;
        long endTime;

        private AnimFrame(Image image, long endTime) {
            this.image = image;
            this.endTime = endTime;
        }
    }
}

package core;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
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
    private int numLoops;
    private int loopCount;
    private JevaFunction endFunc;

    /**
     * Creates a new, empty Animation.
     */
    protected JevaSpriteSheet(JevaR core, String _source, int frames, int frameSize, int yStart, int fps) {
        this(core, JevaUtils.emptyScript);
        stripUp(_source, frames, frameSize, yStart, fps);
    }

    protected JevaSpriteSheet(JevaR core, JevaScript _init) {
        this.core = core;
        frames = new ArrayList<AnimFrame>();
        totalDuration = 0;
        numLoops = 0;
        loopCount = 0;
        endFunc = null;
        if (_init != null)
            _init.call(this);
    }

    /**
     * Adds an image to the animation with the specified
     * duration (time to display the image).
     */
    public synchronized void addFrame(String _label, long duration) {
        JevaGraphic graphic = core.getJevaGraphic(_label);

        if (graphic == null)
            return;

        Image source = graphic.getSource();

        addFrame(source, duration);
    }

    public synchronized void addFrame(Image source, long duration) {
        if (source == null)
            return;

        totalDuration += duration;
        frames.add(new AnimFrame(source, duration, totalDuration));
    }

    public void stripUp(String _label, int frames, int fps) {
        stripUp(_label, frames, 0, 0, 0, 0, fps);
    }

    public void stripUp(String _label, int frames, int frameSize, int fps) {
        stripUp(_label, frames, frameSize, frameSize, 0, 0, fps);
    }

    public void stripUp(String _label, int frames, int frameSize, int yStart, int fps) {
        stripUp(_label, frames, frameSize, frameSize, 0, yStart, fps);
    }

    public void stripUp(String _label, int frames, int frameSize, int xStart, int yStart, int fps) {
        stripUp(_label, frames, frameSize, frameSize, xStart, yStart, fps);
    }

    public void stripUp(String _label, int frames, int frameWidth, int frameHeight, int xStart, int yStart, int fps) {
        Image stripSheet = core.getImage(_label);
        if (stripSheet == null)
            return;

        if (frames <= 0)
            frames = 1;
        if (frameWidth == 0)
            frameWidth = (int) stripSheet.getWidth(null) / frames;
        if (frameHeight == 0)
            frameHeight = stripSheet.getHeight(null);

        if (frameWidth <= 0 || frameHeight <= 0)
            return;

        int xOffset = xStart * frameWidth;
        int yOffset = yStart * frameHeight;

        int delay = 1000 / fps;

        for (int i = 0; i < frames; i++) {

            BufferedImage frameImage = new BufferedImage(frameWidth, frameHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = (Graphics2D) frameImage.getGraphics();

            g.drawImage(stripSheet,
                    0, 0, frameWidth, frameHeight,
                    xOffset + i * frameWidth, yOffset, xOffset + (i * frameWidth) + frameWidth, yOffset + frameHeight,
                    null);

            this.addFrame(frameImage, delay);
        }
    }

    /**
     * Starts this animation over from the beginning.
     */
    protected synchronized void reset(int numLoops, JevaFunction func) {
        animTime = 0; // reset time animation has run for to zero
        currFrameIndex = 0; // reset current frame to first frame
        startTime = core.currentClockMillis(); // reset start time to current time
        loopCount = 1;
        this.numLoops = numLoops;
        this.endFunc = func;
    }

    /**
     * Updates this animation's current image (frame), if
     * neccesary.
     */
    protected synchronized void tick() {
        long currTime = core.currentClockMillis(); // find the current time
        long elapsedTime = currTime - startTime; // find how much time has elapsed since last update
        startTime = currTime; // set start time to current time

        if (frames.size() > 1) {
            animTime += elapsedTime; // add elapsed time to amount of time animation has run for
            if (animTime >= totalDuration) { // if the time animation has run for > total duration
                animTime = animTime % totalDuration; // reset time animation has run for
                if (numLoops == 0 || loopCount < numLoops) {
                    currFrameIndex = 0; // reset current frame to first frame
                    loopCount++;
                } else if (numLoops != 0 && loopCount >= numLoops) {
                    if (endFunc != null)
                        endFunc.call(core.state);
                }
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
        long duration;
        long endTime;

        private AnimFrame(Image image, long duration, long endTime) {
            this.image = image;
            this.duration = duration;
            this.endTime = endTime;
        }

        protected AnimFrame clone() {
            AnimFrame frame = new AnimFrame(image, duration, endTime);

            return frame;
        }
    }

    protected JevaSpriteSheet clone() {
        JevaSpriteSheet sheet = new JevaSpriteSheet(core, null);

        for (int i = 0; i < frames.size(); i++) {
            AnimFrame frame = frames.get(i);
            sheet.addFrame(frame.image, frame.duration);
        }

        return sheet;
    }
}

package core;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*; // need this to respond to GUI events
import java.awt.image.BufferedImage;
import java.awt.image.BufferStrategy;

import javax.swing.*;

public class JevaScreen extends JFrame implements KeyListener, MouseListener, MouseMotionListener {
    private JevaR core;
    private Canvas gameCanvas;
    private Container gameContainer;

    private static final int NUM_BUFFERS = 2;

    private GraphicsDevice device;
    private boolean isFullScreen;

    protected int _width;
    protected int _height;
    protected int _initwidth;
    protected int _initheight;

    private BufferStrategy offscreenCanvas;

    protected Color backgroundColor;

    protected JevaScreen(JevaR core, int _width, int _height) {
        this.core = core;
        this._width = _width;
        this._height = _height;
        this._initwidth = _width;
        this._initheight = _height;
        setTitle("A JevaR Application");
        setSize(_width, _height + 10);

        backgroundColor = new Color(0, 128, 0);
        backgroundColor = Color.WHITE;

        isFullScreen = false;

        // create game panel

        gameCanvas = new Canvas();
        // gameCanvas.setPreferredSize(new Dimension(_width, _height));
        // gameCanvas.setMinimumSize(new Dimension(_width, _height));
        // gameCanvas.setBounds(0, 0, _width, _height);
        setCanvasSize(_width, _height);
        gameCanvas.setBackground(backgroundColor);

        gameCanvas.addMouseListener(this);
        gameCanvas.addMouseMotionListener(this);
        gameCanvas.addKeyListener(this);

        gameContainer = getContentPane();
        gameContainer.add(gameCanvas, 0);

        // setResizable(false);
        // setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // setLocationRelativeTo(null);
        // pack();
        // setVisible(true);

        initScreen(isFullScreen);

        gameCanvas.requestFocus();

        fetchBufferStrategy();

        // offscreenCanvas = gameCanvas.getBufferStrategy();
        // if (offscreenCanvas == null) {
        // gameCanvas.createBufferStrategy(NUM_BUFFERS);
        // offscreenCanvas = gameCanvas.getBufferStrategy();
        // }
    }

    private void initScreen(boolean fullscreen) {
        dispose();

        if (!fullscreen) {
            setResizable(false);
            setUndecorated(false); // no menu bar, borders, etc.
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            pack();
            setVisible(true);
        } else {
            setUndecorated(true); // no menu bar, borders, etc.
            setIgnoreRepaint(true); // turn off all paint events since doing active rendering
            setResizable(false); // screen cannot be resized
            pack();
            setVisible(true);
        }

        gameCanvas.requestFocus();
    }

    private void fetchBufferStrategy() {
        try {
            gameCanvas.createBufferStrategy(NUM_BUFFERS);
        } catch (Exception e) {
            System.out.println("Error while creating buffer strategy " + e);
            System.exit(0);
        }

        offscreenCanvas = gameCanvas.getBufferStrategy();
    }

    private void setCanvasSize(int width, int height) {
        Dimension size = new Dimension(width, height);
        gameCanvas.setPreferredSize(size);
        gameCanvas.setMinimumSize(size);
        gameCanvas.setBounds(0, 0, width, height);
        _width = width;
        _height = height;
    }

    protected Graphics2D getContext() {
        return (Graphics2D) offscreenCanvas.getDrawGraphics();
    }

    protected void drawScreen() {
        if (!offscreenCanvas.contentsLost())
            offscreenCanvas.show();
    }

    protected void clearScreen() {
        Graphics2D ctx = getContext();

        Rectangle2D.Double screenBackground = new Rectangle2D.Double(0, 0, gameCanvas.getWidth(),
                gameCanvas.getHeight());

        ctx.setColor(Color.BLACK);
        // ctx.setColor(backgroundColor);
        ctx.fill(screenBackground);

        ctx.dispose();
    }

    public void keyTyped(KeyEvent e) {
        // TODO Auto-generated method stub

    }

    public void keyPressed(KeyEvent e) {
        JevaKey key = core.key;
        int code = e.getKeyCode();
        String keyCode = "code_" + code;
        String keyName = "name_" + KeyEvent.getKeyText(code).toLowerCase();

        // System.out.println("Key being pressed- Code: " + keyCode + " Text: " +
        // keyName);

        if (key._keysList.get(keyCode) == JevaKey._keyStates.nil || key._keysList.get(keyCode) == null)
            key._keysList.put(keyCode, JevaKey._keyStates.down);
        if (key._keysList.get(keyName) == JevaKey._keyStates.nil || key._keysList.get(keyName) == null)
            key._keysList.put(keyName, JevaKey._keyStates.down);

        if (key._keysPressed.get(keyCode) == JevaKey._keyStates.nil || key._keysPressed.get(keyCode) == null)
            key._keysPressed.put(keyCode, JevaKey._keyStates.down);
        if (key._keysPressed.get(keyName) == JevaKey._keyStates.nil || key._keysPressed.get(keyName) == null)
            key._keysPressed.put(keyName, JevaKey._keyStates.down);
    }

    public void keyReleased(KeyEvent e) {
        JevaKey key = core.key;
        int code = e.getKeyCode();
        String keyCode = "code_" + code;
        String keyName = "name_" + KeyEvent.getKeyText(code).toLowerCase();

        if (key._keysList.get(keyCode) == JevaKey._keyStates.down)
            key._keysList.put(keyCode, JevaKey._keyStates.nil);
        if (key._keysList.get(keyName) == JevaKey._keyStates.down)
            key._keysList.put(keyName, JevaKey._keyStates.nil);

        if (key._keysPressed.get(keyCode) != JevaKey._keyStates.nil) {
            if ((key._keysPressed.get(keyCode) == JevaKey._keyStates.up
                    || key._keysPressed.get(keyCode) == JevaKey._keyStates.down)
                    && (key._keysReleased.get(keyCode) == JevaKey._keyStates.nil
                            || key._keysReleased.get(keyCode) == null)) {
                key._keysReleased.put(keyCode, JevaKey._keyStates.down);
            }
            key._keysPressed.put(keyCode, JevaKey._keyStates.nil);
        }
        if (key._keysPressed.get(keyName) != JevaKey._keyStates.nil) {
            if ((key._keysPressed.get(keyName) == JevaKey._keyStates.up
                    || key._keysPressed.get(keyName) == JevaKey._keyStates.down)
                    && (key._keysReleased.get(keyName) == JevaKey._keyStates.nil
                            || key._keysReleased.get(keyName) == null)) {
                key._keysReleased.put(keyName, JevaKey._keyStates.down);
            }
            key._keysPressed.put(keyName, JevaKey._keyStates.nil);
        }
    }

    // implement methods in MouseListener interface

    public void mouseClicked(MouseEvent e) {
        JevaMouse mouse = core.mouse;
        int x = e.getX();
        int y = e.getY();

        mouse.setMouseCoords(x, y);
    }

    public void mouseEntered(MouseEvent e) {

    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        JevaMouse mouse = core.mouse;
        int code = e.getButton();
        String mouseCode = "code_" + code;
        String mouseName = code == MouseEvent.BUTTON1 ? "name_left"
                : code == MouseEvent.BUTTON2 ? "name_middle" : code == MouseEvent.BUTTON3 ? "name_right" : "name_nil";

        // System.out.println("Mouse being pressed- Code: " + mouseCode + " Text: " +
        // mouseName);

        if (mouse._mouseList.get(mouseCode) == JevaMouse._mouseStates.nil
                || mouse._mouseList.get(mouseCode) == null)
            mouse._mouseList.put(mouseCode, JevaMouse._mouseStates.down);
        if (mouse._mouseList.get(mouseName) == JevaMouse._mouseStates.nil
                || mouse._mouseList.get(mouseName) == null)
            mouse._mouseList.put(mouseName, JevaMouse._mouseStates.down);

        if (mouse._mousePressed.get(mouseCode) == JevaMouse._mouseStates.nil
                || mouse._mousePressed.get(mouseCode) == null)
            mouse._mousePressed.put(mouseCode, JevaMouse._mouseStates.down);
        if (mouse._mousePressed.get(mouseName) == JevaMouse._mouseStates.nil
                || mouse._mousePressed.get(mouseName) == null)
            mouse._mousePressed.put(mouseName, JevaMouse._mouseStates.down);
    }

    public void mouseReleased(MouseEvent e) {
        JevaMouse mouse = core.mouse;
        int code = e.getButton();
        String mouseCode = "code_" + code;
        String mouseName = code == MouseEvent.BUTTON1 ? "name_left"
                : code == MouseEvent.BUTTON2 ? "name_middle" : code == MouseEvent.BUTTON3 ? "name_right" : "name_nil";

        if (mouse._mouseList.get(mouseCode) == JevaMouse._mouseStates.down)
            mouse._mouseList.put(mouseCode, JevaMouse._mouseStates.nil);
        if (mouse._mouseList.get(mouseName) == JevaMouse._mouseStates.down)
            mouse._mouseList.put(mouseName, JevaMouse._mouseStates.nil);

        if (mouse._mousePressed.get(mouseCode) != JevaMouse._mouseStates.nil) {
            if ((mouse._mousePressed.get(mouseCode) == JevaMouse._mouseStates.up
                    || mouse._mousePressed.get(mouseCode) == JevaMouse._mouseStates.down)
                    && (mouse._mouseReleased.get(mouseCode) == JevaMouse._mouseStates.nil
                            || mouse._mouseReleased.get(mouseCode) == null)) {
                mouse._mouseReleased.put(mouseCode, JevaMouse._mouseStates.down);
            }
            mouse._mousePressed.put(mouseCode, JevaMouse._mouseStates.nil);
        }
        if (mouse._mousePressed.get(mouseName) != JevaMouse._mouseStates.nil) {
            if ((mouse._mousePressed.get(mouseName) == JevaMouse._mouseStates.up
                    || mouse._mousePressed.get(mouseName) == JevaMouse._mouseStates.down)
                    && (mouse._mouseReleased.get(mouseName) == JevaMouse._mouseStates.nil
                            || mouse._mouseReleased.get(mouseName) == null)) {
                mouse._mouseReleased.put(mouseName, JevaMouse._mouseStates.down);
            }
            mouse._mousePressed.put(mouseName, JevaMouse._mouseStates.nil);
        }
    }
    // implement methods in MouseMotionListener interface

    public void mouseMoved(MouseEvent e) {
        JevaMouse mouse = core.mouse;
        int x = e.getX();
        int y = e.getY();

        mouse.setMouseCoords(x, y);
    }

    public void mouseDragged(MouseEvent e) {
        JevaMouse mouse = core.mouse;
        int x = e.getX();
        int y = e.getY();

        mouse.setMouseCoords(x, y);
    }

    protected Rectangle2D.Double getBoundingRectangle() {
        return new Rectangle2D.Double(0, 0, getWidth(), getHeight());
    }

    protected boolean hitTest(Rectangle2D.Double targetRect) {
        Rectangle2D.Double thisRect = getBoundingRectangle();

        return thisRect.intersects(targetRect);
    }

    protected boolean hitTest(JevaClip other) {
        Rectangle2D.Double thisRect = getBoundingRectangle();
        Rectangle2D.Double otherRect = other.getBoundingRectangle();

        return thisRect.intersects(otherRect);
    }

    protected void requestFullscreen() { // standard procedure to get into FSEM
        isFullScreen = true;

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        device = ge.getDefaultScreenDevice();

        initScreen(isFullScreen);

        if (!device.isFullScreenSupported()) {
            System.out.println("Full-screen exclusive mode not supported");
            System.exit(0);
        }

        device.setFullScreenWindow(this); // switch on full-screen exclusive mode

        setCanvasSize(getBounds().width, getBounds().height);

        fetchBufferStrategy();
    }

    protected void exitFullscreen() {
        isFullScreen = false;

        Window w = device.getFullScreenWindow();

        if (w != null)
            w.dispose();

        device.setFullScreenWindow(null);

        setCanvasSize(_initwidth, _initheight);

        initScreen(isFullScreen);

        fetchBufferStrategy();
    }

    protected String _getCursorType() {
        int type = getCursor().getType();

        if (type == Cursor.HAND_CURSOR)
            return "pointer";
        // else if (type == Cursor.DEFAULT_CURSOR)
        //     return "default";
        else
            return "default";
    }

    protected void _setCursorType(String value) {
        if (value == "pointer" || value == "p") {
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            // } else if (value == "default" || value == "d") {
            // setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        } else {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }

    protected boolean isFullscreen() {
        return isFullScreen;
    }

    protected int getScreenWidth() {
        return _width;
    }

    protected int getScreenHeight() {
        return _height;
    }
}

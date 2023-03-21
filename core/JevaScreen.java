package core;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*; // need this to respond to GUI events
import java.awt.image.BufferedImage;

import javax.swing.*;

public class JevaScreen extends JFrame implements KeyListener, MouseListener, MouseMotionListener {
    private JevaR core;
    private JPanel gamePanel;
    private Container gameContainer;

    private BufferedImage offscreenCanvas;

    protected Color backgroundColor;

    protected JevaScreen(JevaR core, int _width, int _height) {
        this.core = core;
        setTitle("A JevaR Application");
        setSize(_width, _height + 10);

        backgroundColor = new Color(0, 128, 0);
        backgroundColor = Color.WHITE;

        offscreenCanvas = new BufferedImage(_width, _height, BufferedImage.TYPE_INT_RGB);

        // create game panel

        gamePanel = new JPanel();
        // gamePanel = new JPanel(new FlowLayout());
        gamePanel.setPreferredSize(new Dimension(_width, _height));
        gamePanel.setMinimumSize(new Dimension(_width, _height));
        gamePanel.setBounds(0, 0, _width, _height);
        // gamePanel.setMaximumSize(new Dimension(_width, _height));
        gamePanel.setBackground(backgroundColor);

        gamePanel.addMouseListener(this);
        gamePanel.addMouseMotionListener(this);
        gamePanel.addKeyListener(this);

        gameContainer = getContentPane();
        // gameContainer.setLayout(new FlowLayout(FlowLayout.CENTER));
        // gameContainer.setPreferredSize(new Dimension(_width, _height));
        gameContainer.add(gamePanel, 0);
        // setLayout(new FlowLayout());
        // setPreferredSize(new Dimension(_width, _height));
        // add(gamePanel);
        // pack();

        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);

        gamePanel.requestFocus();
    }

    protected Graphics2D getContext() {
        return offscreenCanvas.createGraphics();
    }

    protected void drawScreen() {
        Graphics2D g2 = (Graphics2D) gamePanel.getGraphics();
        g2.drawImage(offscreenCanvas, 0, 0, null);
        g2.dispose();
    }

    protected void clearScreen() {
        Graphics2D ctx = offscreenCanvas.createGraphics();

        Rectangle2D.Double screenBackground = new Rectangle2D.Double(0, 0, gamePanel.getWidth(), gamePanel.getHeight());

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

        System.out.println("Key being pressed- Code: " + keyCode + " Text: " + keyName);

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

        System.out.println("Mouse being pressed- Code: " + mouseCode + " Text: " + mouseName);

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
}

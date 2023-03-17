package core;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*; // need this to respond to GUI events
import java.awt.image.BufferedImage;

import javax.swing.*;

public class JevaScreen extends JFrame implements KeyListener, MouseListener, MouseMotionListener {
    private JPanel gamePanel;
    private Container gameContainer;

    private BufferedImage offscreenCanvas;

    protected Color backgroundColor;

    public JevaScreen(int _width, int _height) {
        setTitle("A JevaR Application");
        setSize(_width, _height);

        backgroundColor = new Color(0, 128, 0);
        backgroundColor = Color.WHITE;

        offscreenCanvas = new BufferedImage(_width, _height, BufferedImage.TYPE_INT_RGB);

        // create game panel

        gamePanel = new JPanel();
        gamePanel.setPreferredSize(new Dimension(_width, _height));
        gamePanel.setBackground(backgroundColor);

        gamePanel.addMouseListener(this);
        gamePanel.addMouseMotionListener(this);
        gamePanel.addKeyListener(this);

        gameContainer = getContentPane();
        gameContainer.add(gamePanel);

        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);

        gamePanel.requestFocus();
    }

    public Graphics2D getContext() {
        return offscreenCanvas.createGraphics();
    }

    public void drawScreen() {
        Graphics2D g2 = (Graphics2D) gamePanel.getGraphics();
        g2.drawImage(offscreenCanvas, 0, 0, null);
        g2.dispose();
    }

    public void clearScreen() {
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
        int code = e.getKeyCode();
        String keyCode = "code_" + code;
        String keyName = "name_" + KeyEvent.getKeyText(code).toLowerCase();

        System.out.println("Key being pressed- Code: " + keyCode + " Text: " + keyName);

        if (JevaKey._keysList.get(keyCode) == JevaKey._keyStates.nil || JevaKey._keysList.get(keyCode) == null)
            JevaKey._keysList.put(keyCode, JevaKey._keyStates.down);
        if (JevaKey._keysList.get(keyName) == JevaKey._keyStates.nil || JevaKey._keysList.get(keyName) == null)
            JevaKey._keysList.put(keyName, JevaKey._keyStates.down);

        if (JevaKey._keysPressed.get(keyCode) == JevaKey._keyStates.nil || JevaKey._keysPressed.get(keyCode) == null)
            JevaKey._keysPressed.put(keyCode, JevaKey._keyStates.down);
        if (JevaKey._keysPressed.get(keyName) == JevaKey._keyStates.nil || JevaKey._keysPressed.get(keyName) == null)
            JevaKey._keysPressed.put(keyName, JevaKey._keyStates.down);
    }

    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        String keyCode = "code_" + code;
        String keyName = "name_" + KeyEvent.getKeyText(code).toLowerCase();

        if (JevaKey._keysList.get(keyCode) == JevaKey._keyStates.down)
            JevaKey._keysList.put(keyCode, JevaKey._keyStates.nil);
        if (JevaKey._keysList.get(keyName) == JevaKey._keyStates.down)
            JevaKey._keysList.put(keyName, JevaKey._keyStates.nil);

        if (JevaKey._keysPressed.get(keyCode) != JevaKey._keyStates.nil) {
            if ((JevaKey._keysPressed.get(keyCode) == JevaKey._keyStates.up
                    || JevaKey._keysPressed.get(keyCode) == JevaKey._keyStates.down)
                    && (JevaKey._keysReleased.get(keyCode) == JevaKey._keyStates.nil
                            || JevaKey._keysReleased.get(keyCode) == null)) {
                JevaKey._keysReleased.put(keyCode, JevaKey._keyStates.down);
            }
            JevaKey._keysPressed.put(keyCode, JevaKey._keyStates.nil);
        }
        if (JevaKey._keysPressed.get(keyName) != JevaKey._keyStates.nil) {
            if ((JevaKey._keysPressed.get(keyName) == JevaKey._keyStates.up
                    || JevaKey._keysPressed.get(keyName) == JevaKey._keyStates.down)
                    && (JevaKey._keysReleased.get(keyName) == JevaKey._keyStates.nil
                            || JevaKey._keysReleased.get(keyName) == null)) {
                JevaKey._keysReleased.put(keyName, JevaKey._keyStates.down);
            }
            JevaKey._keysPressed.put(keyName, JevaKey._keyStates.nil);
        }
    }

    // implement methods in MouseListener interface

    public void mouseClicked(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        JevaMouse.setMouseCoords(x, y);
    }

    public void mouseEntered(MouseEvent e) {

    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        int code = e.getButton();
        String mouseCode = "code_" + code;
        String mouseName = code == MouseEvent.BUTTON1 ? "name_left"
                : code == MouseEvent.BUTTON2 ? "name_middle" : code == MouseEvent.BUTTON3 ? "name_right" : "name_nil";

        System.out.println("Mouse being pressed- Code: " + mouseCode + " Text: " + mouseName);

        if (JevaMouse._mouseList.get(mouseCode) == JevaMouse._mouseStates.nil
                || JevaMouse._mouseList.get(mouseCode) == null)
            JevaMouse._mouseList.put(mouseCode, JevaMouse._mouseStates.down);
        if (JevaMouse._mouseList.get(mouseName) == JevaMouse._mouseStates.nil
                || JevaMouse._mouseList.get(mouseName) == null)
            JevaMouse._mouseList.put(mouseName, JevaMouse._mouseStates.down);

        if (JevaMouse._mousePressed.get(mouseCode) == JevaMouse._mouseStates.nil
                || JevaMouse._mousePressed.get(mouseCode) == null)
            JevaMouse._mousePressed.put(mouseCode, JevaMouse._mouseStates.down);
        if (JevaMouse._mousePressed.get(mouseName) == JevaMouse._mouseStates.nil
                || JevaMouse._mousePressed.get(mouseName) == null)
            JevaMouse._mousePressed.put(mouseName, JevaMouse._mouseStates.down);
    }

    public void mouseReleased(MouseEvent e) {
        int code = e.getButton();
        String mouseCode = "code_" + code;
        String mouseName = code == MouseEvent.BUTTON1 ? "name_left"
                : code == MouseEvent.BUTTON2 ? "name_middle" : code == MouseEvent.BUTTON3 ? "name_right" : "name_nil";

        if (JevaMouse._mouseList.get(mouseCode) == JevaMouse._mouseStates.down)
            JevaMouse._mouseList.put(mouseCode, JevaMouse._mouseStates.nil);
        if (JevaMouse._mouseList.get(mouseName) == JevaMouse._mouseStates.down)
            JevaMouse._mouseList.put(mouseName, JevaMouse._mouseStates.nil);

        if (JevaMouse._mousePressed.get(mouseCode) != JevaMouse._mouseStates.nil) {
            if ((JevaMouse._mousePressed.get(mouseCode) == JevaMouse._mouseStates.up
                    || JevaMouse._mousePressed.get(mouseCode) == JevaMouse._mouseStates.down)
                    && (JevaMouse._mouseReleased.get(mouseCode) == JevaMouse._mouseStates.nil
                            || JevaMouse._mouseReleased.get(mouseCode) == null)) {
                JevaMouse._mouseReleased.put(mouseCode, JevaMouse._mouseStates.down);
            }
            JevaMouse._mousePressed.put(mouseCode, JevaMouse._mouseStates.nil);
        }
        if (JevaMouse._mousePressed.get(mouseName) != JevaMouse._mouseStates.nil) {
            if ((JevaMouse._mousePressed.get(mouseName) == JevaMouse._mouseStates.up
                    || JevaMouse._mousePressed.get(mouseName) == JevaMouse._mouseStates.down)
                    && (JevaMouse._mouseReleased.get(mouseName) == JevaMouse._mouseStates.nil
                            || JevaMouse._mouseReleased.get(mouseName) == null)) {
                JevaMouse._mouseReleased.put(mouseName, JevaMouse._mouseStates.down);
            }
            JevaMouse._mousePressed.put(mouseName, JevaMouse._mouseStates.nil);
        }
    }
    // implement methods in MouseMotionListener interface

    public void mouseMoved(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        JevaMouse.setMouseCoords(x, y);
    }

    public void mouseDragged(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        JevaMouse.setMouseCoords(x, y);
    }
}

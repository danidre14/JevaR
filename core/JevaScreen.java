package core;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*; // need this to respond to GUI events

import javax.swing.*;

public class JevaScreen extends JFrame implements KeyListener {
    private JPanel gamePanel;
    private Container gameContainer;

    private Color backgroundColor;

    public JevaScreen(int _width, int _height) {
        setTitle("A JevaR Application");
        setSize(_width, _height);

        backgroundColor = new Color(0, 128, 0);

        // create game panel

        gamePanel = new JPanel();
        gamePanel.setPreferredSize(new Dimension(_width, _height));
        gamePanel.setBackground(backgroundColor);

        gamePanel.addKeyListener(this);

        gameContainer = getContentPane();
        gameContainer.add(gamePanel);

        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);

        gamePanel.requestFocus();
    }

    public Graphics getContext() {
        return gamePanel.getGraphics();
    }

    public void clearScreen() {
        Graphics context = gamePanel.getGraphics();
        Graphics2D ctx = (Graphics2D) context;

        Rectangle2D.Double screenBackground = new Rectangle2D.Double(0, 0, gamePanel.getWidth(), gamePanel.getHeight());

        ctx.setColor(backgroundColor);
        ctx.fill(screenBackground);

        context.dispose();
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
}
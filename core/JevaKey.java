package core;

import java.util.*;
import java.awt.event.*;

public class JevaKey {
    public static int ALT = KeyEvent.VK_ALT;
    public static int BACKSPACE = KeyEvent.VK_BACK_SPACE;
    public static int CAPSLOCK = KeyEvent.VK_CAPS_LOCK;
    public static int CONTROL = KeyEvent.VK_CONTROL;
    public static int DELETE = KeyEvent.VK_DELETE;
    public static int DOWN = KeyEvent.VK_DOWN;
    public static int END = KeyEvent.VK_END;
    public static int ENTER = KeyEvent.VK_ENTER;
    public static int ESCAPE = KeyEvent.VK_ESCAPE;
    public static int HOME = KeyEvent.VK_HOME;
    public static int INSERT = KeyEvent.VK_INSERT;
    public static int LEFT = KeyEvent.VK_LEFT;
    public static int PGDN = KeyEvent.VK_PAGE_DOWN;
    public static int PGUP = KeyEvent.VK_PAGE_UP;
    public static int RIGHT = KeyEvent.VK_RIGHT;
    public static int SHIFT = KeyEvent.VK_SHIFT;
    public static int SPACE = KeyEvent.VK_SPACE;
    public static int TAB = KeyEvent.VK_TAB;
    public static int UP = KeyEvent.VK_UP;
    
    public static int ZERO = KeyEvent.VK_0;
    public static int ONE = KeyEvent.VK_1;
    public static int TWO = KeyEvent.VK_2;
    public static int THREE = KeyEvent.VK_3;
    public static int FOUR = KeyEvent.VK_4;
    public static int FIVE = KeyEvent.VK_5;
    public static int SIX = KeyEvent.VK_6;
    public static int SEVEN = KeyEvent.VK_7;
    public static int EIGHT = KeyEvent.VK_8;
    public static int NINE = KeyEvent.VK_9;
    

    public static int A = KeyEvent.VK_A;
    public static int B = KeyEvent.VK_B;
    public static int C = KeyEvent.VK_C;
    public static int D = KeyEvent.VK_D;
    public static int E = KeyEvent.VK_E;
    public static int F = KeyEvent.VK_F;
    public static int G = KeyEvent.VK_G;
    public static int H = KeyEvent.VK_H;
    public static int I = KeyEvent.VK_I;
    public static int J = KeyEvent.VK_J;
    public static int K = KeyEvent.VK_K;
    public static int L = KeyEvent.VK_L;
    public static int M = KeyEvent.VK_M;
    public static int N = KeyEvent.VK_N;
    public static int O = KeyEvent.VK_O;
    public static int P = KeyEvent.VK_P;
    public static int Q = KeyEvent.VK_Q;
    public static int R = KeyEvent.VK_R;
    public static int S = KeyEvent.VK_S;
    public static int T = KeyEvent.VK_T;
    public static int U = KeyEvent.VK_U;
    public static int V = KeyEvent.VK_V;
    public static int X = KeyEvent.VK_X;
    public static int W = KeyEvent.VK_W;
    public static int Y = KeyEvent.VK_Y;
    public static int Z = KeyEvent.VK_Z;

    public enum _keyStates {
        nil,
        down,
        up,
        expired
    }

    public static HashMap<String, _keyStates> _keysList;
    public static HashMap<String, _keyStates> _keysPressed;
    public static HashMap<String, _keyStates> _keysReleased;
    static {
        _keysList = new HashMap<>();
        _keysPressed = new HashMap<>();
        _keysReleased = new HashMap<>();
    }

    public static boolean isDown(int keyCode) {
        boolean keyDown = _keysList.get("code_" + keyCode) == _keyStates.down;

        return keyDown;
    }

    public static boolean isDown(String keyName) {
        keyName = keyName.toLowerCase();
        boolean keyDown = _keysList.get("name_" + keyName) == _keyStates.down;

        return keyDown;
    }

    public static boolean isUp(int keyCode) {
        return !isDown(keyCode);
    }

    public static boolean isUp(String keyName) {
        keyName = keyName.toLowerCase();
        return !isDown(keyName);
    }

    public static boolean isPressed(int keyCode) {
        boolean keyPressed = _keysPressed.get("code_" + keyCode) == _keyStates.down;

        return keyPressed;
    }

    public static boolean isPressed(String keyName) {
        keyName = keyName.toLowerCase();
        boolean keyPressed = _keysPressed.get("name_" + keyName) == _keyStates.down;

        return keyPressed;
    }

    public static boolean isReleased(int keyCode) {
        boolean keyReleased = _keysReleased.get("code_" + keyCode) == _keyStates.down;

        return keyReleased;
    }

    public static boolean isReleased(String keyName) {
        keyName = keyName.toLowerCase();
        boolean keyReleased = _keysReleased.get("name_" + keyName) == _keyStates.down;

        return keyReleased;
    }


    public static void clearKeyStates(boolean absolute) {
        _keysPressed.forEach((_keyCode, _keyState) -> {
            if (_keyState == _keyStates.down)
                _keysPressed.put(_keyCode, _keyStates.up);
        });
        _keysReleased.forEach((_keyCode, _keyState) -> {
            _keysReleased.put(_keyCode, _keyStates.nil);
        });
        if (absolute)
            _keysList.forEach((_keyCode, _keyState) -> {
                _keysList.put(_keyCode, _keyStates.nil);
            });
    }

    public static void expireKeyPressedStates() {
        _keysPressed.forEach((_keyCode, _keyState) -> {
            _keysPressed.put(_keyCode, _keyStates.expired);
        });
    }

}

package core;

import java.util.*;
import java.awt.event.*;

public class JevaMouse {
    public static int _xmouse;
    public static int _ymouse;

    public static int LEFT = MouseEvent.BUTTON1;
    public static int MIDDLE = MouseEvent.BUTTON2;
    public static int RIGHT = MouseEvent.BUTTON3;

    public enum _mouseStates {
        nil,
        down,
        up,
        expired
    }

    public static HashMap<String, _mouseStates> _mouseList;
    public static HashMap<String, _mouseStates> _mousePressed;
    public static HashMap<String, _mouseStates> _mouseReleased;

    static {
        _xmouse = 0;
        _ymouse = 0;
        _mouseList = new HashMap<>();
        _mousePressed = new HashMap<>();
        _mouseReleased = new HashMap<>();
    }

    public static boolean isDown(int mouseCode) {
        boolean mouseDown = _mouseList.get("code_" + mouseCode) == _mouseStates.down;

        return mouseDown;
    }

    public static boolean isDown(String mouseName) {
        mouseName = mouseName.toLowerCase();
        boolean mouseDown = _mouseList.get("name_" + mouseName) == _mouseStates.down;

        return mouseDown;
    }

    public static boolean isUp(int mouseCode) {
        return !isDown(mouseCode);
    }

    public static boolean isUp(String mouseName) {
        mouseName = mouseName.toLowerCase();
        return !isDown(mouseName);
    }

    public static boolean isPressed(int mouseCode) {
        boolean mousePressed = _mousePressed.get("code_" + mouseCode) == _mouseStates.down;

        return mousePressed;
    }

    public static boolean isPressed(String mouseName) {
        mouseName = mouseName.toLowerCase();
        boolean mousePressed = _mousePressed.get("name_" + mouseName) == _mouseStates.down;

        return mousePressed;
    }

    public static boolean isReleased(int mouseCode) {
        boolean mouseReleased = _mouseReleased.get("code_" + mouseCode) == _mouseStates.down;

        return mouseReleased;
    }

    public static boolean isReleased(String mouseName) {
        mouseName = mouseName.toLowerCase();
        boolean mouseReleased = _mouseReleased.get("name_" + mouseName) == _mouseStates.down;

        return mouseReleased;
    }


    public static void clearMouseStates(boolean absolute) {
        _mousePressed.forEach((_mouseCode, _mouseState) -> {
            if (_mouseState == _mouseStates.down)
                _mousePressed.put(_mouseCode, _mouseStates.up);
        });
        _mouseReleased.forEach((_mouseCode, _mouseState) -> {
            _mouseReleased.put(_mouseCode, _mouseStates.nil);
        });
        if (absolute)
            _mouseList.forEach((_mouseCode, _mouseState) -> {
                _mouseList.put(_mouseCode, _mouseStates.nil);
            });
    }

    public static void expireMousePressedStates() {
        _mousePressed.forEach((_mouseCode, _mouseState) -> {
            _mousePressed.put(_mouseCode, _mouseStates.expired);
        });
    }

    public static void setMouseCoords(int _x, int _y) {
        _xmouse = _x;
        _ymouse = _y;
    }
}

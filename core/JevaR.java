package core;

import java.awt.*;
import java.util.*;
import java.util.List;

public class JevaR implements Runnable {
    private JevaScreen screen;
    private HashMap<String, JevaClip> jevaclipLibrary;

    private HashMap<String, JevaClip> jevaclipHeirarchy;

    protected static HashMap<String, JevaScript> jevascriptLibrary;
    private HashMap<String, JevaScript> jevascriptHeirarchy;

    protected static HashMap<String, JevaGraphic> jevagraphicLibrary;
    protected static HashMap<String, JevaSpriteSheet> jevaspritesheetLibrary;
    protected static HashMap<String, JevaSound> jevasoundLibrary;

    private Thread gameThread;
    private boolean isRunning;

    private int fps;

    public JevaR(int _width, int _height) {
        this(_width, _height, 10);
    }

    public JevaR(int _width, int _height, int fps) {
        screen = new JevaScreen(_width, _height);

        jevaclipLibrary = new HashMap<>();
        jevaclipHeirarchy = new HashMap<>();
        jevascriptLibrary = new HashMap<>();
        jevascriptHeirarchy = new HashMap<>();
        jevagraphicLibrary = new HashMap<>();
        jevaspritesheetLibrary = new HashMap<>();
        jevasoundLibrary = new HashMap<>();

        isRunning = false;
        this.fps = fps;

        gameThread = new Thread(this);
        gameThread.start();
    }

    // creating library jobtives

    public void createGraphic(String _label, String fileName) {
        if (jevagraphicLibrary.get(_label) != null)
            return;

        if (fileName.isEmpty())
            return;

        // add JevaGraphic to game engine
        JevaGraphic graphic = new JevaGraphic(fileName);
        jevagraphicLibrary.put(_label, graphic);
    }

    public void createSpriteSheet(String _label, JevaScript _init) {
        if (jevaspritesheetLibrary.get(_label) != null)
            return;

        // add JevaSpriteSheet to game engine
        JevaSpriteSheet spritesheet = new JevaSpriteSheet(_init);
        jevaspritesheetLibrary.put(_label, spritesheet);
    }



    public void createSound(String _label, String fileName) {
        if (jevasoundLibrary.get(_label) != null)
            return;
        
        if(fileName.isEmpty())
            return;
        
        // add JevaSound to game engine
        JevaSound sound = new JevaSound(fileName);
        jevasoundLibrary.put(_label, sound);
    }

    public JevaSound getSound(String _label) {
        JevaSound sound = jevasoundLibrary.get(_label);

        if(sound == null)
            return null;
        
        return sound;
    }

    public void createJevascript(String _label, JevaScript script) {
        if (jevascriptLibrary.get(_label) != null)
            return;

        // add JevaScript to game engine
        jevascriptLibrary.put(_label, script);
    }

    public void createJevaclip(String _label, double _x, double _y, double _width, double _height) {
        if (jevaclipLibrary.get(_label) != null)
            return;

        // initilize a JevaClip
        JevaClip jevaclip = new JevaClip(_label, _x, _y, _width, _height);

        // add JevaClip to game engine
        jevaclipLibrary.put(_label, jevaclip);
    }

    public void createJevaclip(String _label, double _x, double _y, double _width, double _height, String _scriptName) {
        if (jevaclipLibrary.get(_label) != null)
            return;

        JevaScript script = jevascriptLibrary.get(_scriptName);
        if (script == null)
            return;

        // initilize a JevaClip
        JevaClip jevaclip = new JevaClip(_label, _x, _y, _width, _height, script);

        // add JevaClip to game engine
        jevaclipLibrary.put(_label, jevaclip);
    }

    public void createJevaclip(String _label, double _x, double _y, double _width, double _height, JevaScript onLoad) {
        if (jevaclipLibrary.get(_label) != null)
            return;

        // initilize a JevaClip
        JevaClip jevaclip = new JevaClip(_label, _x, _y, _width, _height, onLoad);

        // add JevaClip to game engine
        jevaclipLibrary.put(_label, jevaclip);
    }

    // attaching jobtives
    public void attachJevascript(String _label) {
        JevaScript script = jevascriptLibrary.get(_label);

        if (script == null)
            return;

        attachJevascript(script);
    }

    public void attachJevascript(JevaScript script) {
        String id = JevaUtils.generateUUID();

        jevascriptHeirarchy.put(id, script);
    }

    public JevaClip attachJevaclip(String _label) {
        JevaClip oldJevaclip = jevaclipLibrary.get(_label);

        if (oldJevaclip == null)
            return oldJevaclip;

        double _x = oldJevaclip._x;
        double _y = oldJevaclip._y;
        double _width = oldJevaclip._width;
        double _height = oldJevaclip._height;

        return this.attachJevaclip(_label, _x, _y, _width, _height, new ArrayList<>(Arrays.asList()));
    }

    public JevaClip attachJevaclip(String _label, double _x, double _y) {
        JevaClip oldJevaclip = jevaclipLibrary.get(_label);

        if (oldJevaclip == null)
            return oldJevaclip;

        double _width = oldJevaclip._width;
        double _height = oldJevaclip._height;

        return this.attachJevaclip(_label, _x, _y, _width, _height, new ArrayList<>(Arrays.asList()));
    }

    public JevaClip attachJevaclip(String _label, double _x, double _y, double _width, double _height) {
        JevaClip oldJevaclip = jevaclipLibrary.get(_label);

        if (oldJevaclip == null)
            return oldJevaclip;

        return this.attachJevaclip(_label, _x, _y, _width, _height, new ArrayList<>(Arrays.asList()));
    }

    public JevaClip attachJevaclip(String _label, double _x, double _y, double _width, double _height,
            ArrayList<JevaScript> _onLoads) {
        JevaClip oldJevaclip = jevaclipLibrary.get(_label);

        if (oldJevaclip == null)
            return oldJevaclip;

        ArrayList<JevaScript> onLoads = oldJevaclip._onLoadScripts;
        for (JevaScript _script : onLoads)
            _onLoads.add(_script);

        JevaClip jevaclip = new JevaClip(_label, _x, _y, _width, _height, _onLoads);

        String id = JevaUtils.generateUUID();

        // add JevaClip to screen's heirarchy
        jevaclipHeirarchy.put(id, jevaclip);

        jevaclip.load();

        return jevaclip;
    }

    public void run() {
        try {
            isRunning = true;
            while (isRunning) {
                tickEngine();
                JevaKey.clearKeyStates(false);
                renderEngine();
                Thread.sleep(1000 / fps);
            }
        } catch (InterruptedException e) {
        }
    }

    private void tickEngine() {
        // run all attached scripts
        jevascriptHeirarchy.forEach((key, script) -> {
            script.call(this);
        });
        // updating all entities
        jevaclipHeirarchy.forEach((key, jevaclip) -> {
            jevaclip.tick();
        });
    }

    private void renderEngine() {
        // TODO find out how to clear screen
        // clear screen
        screen.clearScreen();

        // get context
        Graphics2D ctx = screen.getContext();

        jevaclipHeirarchy.forEach((key, jevaclip) -> {
            jevaclip.render(ctx);
        });

        screen.drawScreen();
        ctx.dispose();
    }

}

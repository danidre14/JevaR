package core;

import java.awt.*;
import java.util.*;

public class JevaR implements Runnable {
    protected JevaScreen screen;
    protected HashMap<String, JevaClip> jevaclipLibrary;
    protected HashMap<String, JevaScript> jevascriptLibrary;
    protected HashMap<String, JevaGraphic> jevagraphicLibrary;
    protected HashMap<String, JevaSpriteSheet> jevaspritesheetLibrary;
    protected HashMap<String, JevaSound> jevasoundLibrary;
    protected HashMap<String, JevaScene> jevasceneLibrary;

    protected LinkedHashMap<String, JevaClip> jevaclipHierarchy;
    private String hierarchySceneName;
    private HashMap<String, JevaScript> jevascriptHierarchy;

    private Thread gameThread;
    private boolean isRunning;

    private boolean isLoaded;
    private JevaRScript initScript;

    public JevaMouse mouse;
    public JevaKey key;
    public JevaMeta meta;

    private int desiredFps;
    private int currentFps;
    private double deltaTime;

    private boolean debugMode;

    public JevaR(int _width, int _height, JevaRScript onLoad) {
        this(_width, _height, 60, onLoad);
    }

    public JevaR(int _width, int _height, int desiredFps, JevaRScript onLoad) {
        screen = new JevaScreen(this, _width, _height);

        jevaclipLibrary = new HashMap<>();
        jevascriptLibrary = new HashMap<>();
        jevagraphicLibrary = new HashMap<>();
        jevaspritesheetLibrary = new HashMap<>();
        jevasoundLibrary = new HashMap<>();
        jevasceneLibrary = new HashMap<>();

        jevascriptHierarchy = new HashMap<>();
        jevaclipHierarchy = new LinkedHashMap<>();
        hierarchySceneName = null;

        mouse = new JevaMouse(this);
        key = new JevaKey(this);
        meta = new JevaMeta(this);

        isRunning = false;
        this.desiredFps = desiredFps;
        this.currentFps = desiredFps;

        initScript = onLoad;
        isLoaded = false;

        deltaTime = 0;

        debugMode = false;

        _startRuntime();
    }

    // creating library jobtives

    public void createGraphic(String _label) {
        String fileName = _label.concat(".png");
        createGraphic(_label, fileName);
    }

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
        JevaSpriteSheet spritesheet = new JevaSpriteSheet(this, _init);
        jevaspritesheetLibrary.put(_label, spritesheet);
    }

    public void createSound(String _label, String fileName) {
        createSound(_label, fileName, 1);
    }

    public void createSound(String _label, String fileName, int amount) {
        if (jevasoundLibrary.get(_label) != null)
            return;

        if (fileName.isEmpty())
            return;

        // add JevaSound to game engine
        JevaSound sound = new JevaSound(fileName, amount);
        jevasoundLibrary.put(_label, sound);
    }

    public void createScene(String _label, JevaScript onLoad) {
        if (jevasceneLibrary.get(_label) != null)
            return;
        // initialize a JevaScene
        JevaScene jevascene = new JevaScene(this, _label, onLoad);

        // add JevaScene to game engine
        jevasceneLibrary.put(_label, jevascene);
    }

    public JevaSound getSound(String _label) {
        JevaSound sound = jevasoundLibrary.get(_label);

        if (sound == null)
            return null;

        return sound;
    }

    public void createJevascript(String _label, JevaScript script) {
        if (jevascriptLibrary.get(_label) != null)
            return;

        // add JevaScript to game engine
        jevascriptLibrary.put(_label, script);
    }

    public void createPrefab(String _label, double _x, double _y, double _width, double _height) {
        if (jevaclipLibrary.get(_label) != null)
            return;

        // initilize a JevaClip
        JevaClip jevaclip = new JevaPrefab(this, _label, _x, _y, _width, _height);

        // add JevaClip to game engine
        jevaclipLibrary.put(_label, jevaclip);
    }

    public void createPrefab(String _label, double _x, double _y, double _width, double _height, String _scriptName) {
        if (jevaclipLibrary.get(_label) != null)
            return;

        JevaScript script = jevascriptLibrary.get(_scriptName);
        if (script == null)
            return;

        // initilize a JevaClip
        JevaClip jevaclip = new JevaPrefab(this, _label, _x, _y, _width, _height, script);

        // add JevaClip to game engine
        jevaclipLibrary.put(_label, jevaclip);
    }

    public void createPrefab(String _label, double _x, double _y, double _width, double _height, JevaScript onLoad) {
        if (jevaclipLibrary.get(_label) != null)
            return;

        // initilize a JevaClip
        JevaClip jevaclip = new JevaPrefab(this, _label, _x, _y, _width, _height, onLoad);

        // add JevaClip to game engine
        jevaclipLibrary.put(_label, jevaclip);
    }

    public void createText(String _label, String _text, double _x, double _y, double _width, double _height) {
        if (jevaclipLibrary.get(_label) != null)
            return;

        // initilize a JevaClip
        JevaClip jevaclip = new JevaText(this, _label, _text, _x, _y, _width, _height);

        // add JevaClip to game engine
        jevaclipLibrary.put(_label, jevaclip);
    }

    public void createText(String _label, String _text, double _x, double _y, double _width, double _height,
            String _scriptName) {
        if (jevaclipLibrary.get(_label) != null)
            return;

        JevaScript script = jevascriptLibrary.get(_scriptName);
        if (script == null)
            return;

        // initilize a JevaClip
        JevaClip jevaclip = new JevaText(this, _label, _text, _x, _y, _width, _height, script);

        // add JevaClip to game engine
        jevaclipLibrary.put(_label, jevaclip);
    }

    public void createText(String _label, String _text, double _x, double _y, double _width, double _height,
            JevaScript onLoad) {
        if (jevaclipLibrary.get(_label) != null)
            return;

        // initilize a JevaClip
        JevaClip jevaclip = new JevaText(this, _label, _text, _x, _y, _width, _height, onLoad);

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

        jevascriptHierarchy.put(id, script);
    }

    public JevaClip attachPrefab(String _label) {
        JevaClip oldJevaclip = jevaclipLibrary.get(_label);

        if (oldJevaclip == null)
            return oldJevaclip;

        double _x = oldJevaclip._x;
        double _y = oldJevaclip._y;
        double _width = oldJevaclip._width;
        double _height = oldJevaclip._height;

        return this.attachPrefab(_label, _x, _y, _width, _height, new ArrayList<>(Arrays.asList()));
    }

    public JevaClip attachPrefab(String _label, double _x, double _y) {
        JevaClip oldJevaclip = jevaclipLibrary.get(_label);

        if (oldJevaclip == null)
            return oldJevaclip;

        double _width = oldJevaclip._width;
        double _height = oldJevaclip._height;

        return this.attachPrefab(_label, _x, _y, _width, _height, new ArrayList<>(Arrays.asList()));
    }

    public JevaClip attachPrefab(String _label, double _x, double _y, double _width, double _height) {
        JevaClip oldJevaclip = jevaclipLibrary.get(_label);

        if (oldJevaclip == null)
            return oldJevaclip;

        return this.attachPrefab(_label, _x, _y, _width, _height, new ArrayList<>(Arrays.asList()));
    }

    public JevaClip attachPrefab(String _label, double _x, double _y, double _width, double _height,
            ArrayList<JevaScript> _onLoads) {
        JevaClip oldJevaclip = jevaclipLibrary.get(_label);

        if (oldJevaclip == null)
            return oldJevaclip;

        ArrayList<JevaScript> onLoads = oldJevaclip._onLoadScripts;
        for (JevaScript _script : onLoads)
            _onLoads.add(_script);

        JevaClip jevaclip = new JevaPrefab(this, _label, _x, _y, _width, _height, _onLoads);

        String id = JevaUtils.generateUUID();

        // add JevaClip to screen's heirarchy
        jevaclipHierarchy.put(id, jevaclip);

        jevaclip.load();

        return jevaclip;
    }

    public JevaClip attachText(String _label) {
        JevaClip oldJevaclip = jevaclipLibrary.get(_label);
        JevaText oldText = (JevaText) oldJevaclip;

        if (oldText == null)
            return oldText;

        double _x = oldText._x;
        double _y = oldText._y;
        String _text = oldText._text;
        double _width = oldText._width;
        double _height = oldText._height;

        return this.attachText(_label, _text, _x, _y, _width, _height, new ArrayList<>(Arrays.asList()));
    }

    public JevaClip attachText(String _label, String _text, double _x, double _y) {
        JevaClip oldJevaclip = jevaclipLibrary.get(_label);

        if (oldJevaclip == null)
            return oldJevaclip;

        double _width = oldJevaclip._width;
        double _height = oldJevaclip._height;

        return this.attachText(_label, _text, _x, _y, _width, _height, new ArrayList<>(Arrays.asList()));
    }

    public JevaClip attachText(String _label, String _text, double _x, double _y, double _width, double _height) {
        JevaClip oldJevaclip = jevaclipLibrary.get(_label);

        if (oldJevaclip == null)
            return oldJevaclip;

        return this.attachText(_label, _text, _x, _y, _width, _height, new ArrayList<>(Arrays.asList()));
    }

    public JevaClip attachText(String _label, String _text, double _x, double _y, double _width, double _height,
            ArrayList<JevaScript> _onLoads) {
        JevaClip oldJevaclip = jevaclipLibrary.get(_label);

        if (oldJevaclip == null)
            return oldJevaclip;

        ArrayList<JevaScript> onLoads = oldJevaclip._onLoadScripts;
        for (JevaScript _script : onLoads)
            _onLoads.add(_script);

        JevaClip jevaclip = new JevaText(this, _label, _text, _x, _y, _width, _height, _onLoads);

        String id = JevaUtils.generateUUID();

        // add JevaClip to screen's heirarchy
        jevaclipHierarchy.put(id, jevaclip);

        jevaclip.load();

        return jevaclip;
    }

    public void removePrefab(String _instanceName) {
        JevaClip jevaClip = getJevaClip(_instanceName);

        if (jevaClip != null && jevaClip instanceof JevaPrefab)
            jevaClip.remove();
    }

    public void removeText(String _instanceName) {
        JevaClip jevaClip = getJevaClip(_instanceName);

        if (jevaClip != null && jevaClip instanceof JevaText)
            jevaClip.remove();
    }

    public void removeClip(String _instanceName) {
        JevaClip jevaClip = getJevaClip(_instanceName);

        if (jevaClip != null)
            jevaClip.remove();
    }

    public JevaVCam getVCam(String _label) {
        if (getCurrentSceneName() == null)
            return null;
        return getCurrentScene().getVCam(_label);
    }

    protected void setVCamMouseCoords(int _x, int _y) {
        if (getCurrentSceneName() != null)
            getCurrentScene().setVCamMouseCoords(_x, _y);
    }

    public void useScene(String _label) {
        if (!hasScene(_label))
            return;

        String currSceneName = getCurrentSceneName();

        if (currSceneName == _label)
            return;

        if (currSceneName != null) {
            getCurrentScene().unload();
        }

        setCurrentSceneName(_label);

        getCurrentScene().load();
    }

    public void resetScene() {
        String currSceneName = getCurrentSceneName();

        if (currSceneName == null)
            return;

        getCurrentScene().unload();

        key.expireKeyPressedStates();
        mouse.expireMousePressedStates();

        getCurrentScene().load();
    }

    public int getFPS() {
        return currentFps;
    }

    public double getDelta() {
        return deltaTime / 1000000000.0;
    }

    private JevaClip getJevaClip(String name) {
        for (JevaClip jevaclip : jevaclipHierarchy.values()) {
            if (jevaclip._instanceName.equals(name) && !jevaclip.shouldRemove())
                return jevaclip;
        }

        if (getCurrentScene() != null) {
            JevaClip clip = getCurrentScene().getJevaClip(name);
            if (clip != null)
                return clip;
        }
        return null;
    }

    private String getCurrentSceneName() {
        return hierarchySceneName;
    }

    private void setCurrentSceneName(String _label) {
        hierarchySceneName = _label;
    }

    protected JevaScene getCurrentScene() {
        if (hierarchySceneName == null)
            return null;

        return jevasceneLibrary.get(hierarchySceneName);
    }

    private boolean hasScene(String _label) {
        return jevasceneLibrary.get(_label) != null;
    }

    public void run() {
        isRunning = true;

        double timePerFrame = 1000000000.0 / desiredFps;
        long lastFrame = System.nanoTime();
        long now = System.nanoTime();

        int frames = 0;
        long lastCheck = System.currentTimeMillis();

        deltaTime = 0;

        while (isRunning) {
            now = System.nanoTime();
            deltaTime = now - lastFrame;
            if (deltaTime >= timePerFrame) {
                lastFrame = now;
                runEngine();
                frames++;
            }

            if (System.currentTimeMillis() - lastCheck >= 1000) {
                lastCheck = System.currentTimeMillis();
                currentFps = frames;
                frames = 0;
            }
        }
    }

    private void runEngine() {
        tickEngine();
        key.clearKeyStates(false);
        mouse.clearMouseStates(false);
        cleanUpEngine();
        renderEngine();
    }

    private void loadEngine() {
        if (isLoaded)
            return;
        initScript.call(this);
        isLoaded = true;
    }

    private void _startRuntime() {
        loadEngine();

        gameThread = new Thread(this);
        gameThread.start();
    }

    private void tickEngine() {
        // run all attached scripts
        for (JevaScript script : jevascriptHierarchy.values()) {
            script.call(this);
        }

        // updating all attached jevaclips
        for (JevaClip jevaclip : jevaclipHierarchy.values()) {
            jevaclip.tick();
        }

        // updating scene
        if (getCurrentSceneName() != null)
            getCurrentScene().tick();
    }

    private void renderEngine() {
        screen.clearScreen();

        // get context
        Graphics2D ctx = screen.getContext();

        // updating scene
        if (getCurrentSceneName() != null)
            getCurrentScene().render(ctx);

        // vcam.render(ctx);
        // for (JevaClip jevaclip : jevaclipHierarchy.values()) {
        // jevaclip.render(ctx);
        // }

        screen.drawScreen();
        ctx.dispose();
    }

    private void cleanUpEngine() {
        // clean up scenes
        if (getCurrentScene() != null) {
            getCurrentScene().cleanUp();
        }

        // delete all jevaclips markedForDeletion from hierarchy
        jevaclipHierarchy.entrySet().removeIf(e -> {
            JevaClip jevaclip = e.getValue();
            return jevaclip.shouldRemove();
        });
    }

    protected boolean isDebugMode() {
        return debugMode;
    }

}

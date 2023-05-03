package core;

import java.awt.*;
import java.util.*;

public class JevaR implements Runnable {
    protected JevaScreen screen;

    protected HashMap<String, JevaClip> jevaclipLibrary;
    protected HashMap<String, JevaScript> jevascriptLibrary;
    protected HashMap<String, JevaFunction> jevafunctionLibrary;
    protected HashMap<String, JevaGraphic> jevagraphicLibrary;
    protected HashMap<String, JevaPainting> jevapaintingLibrary;
    protected HashMap<String, JevaSpriteSheet> jevaspritesheetLibrary;
    protected HashMap<String, JevaSound> jevasoundLibrary;
    protected HashMap<String, JevaScene> jevasceneLibrary;

    protected LinkedHashMap<String, JevaClip> jevaclipHierarchy;
    private String hierarchyActiveSceneName;
    private String hierarchyDesiredSceneName;
    private boolean mustResetScene;
    private HashMap<String, JevaScript> jevascriptHierarchy;

    private Thread gameThread;
    private boolean isRunning;

    private boolean isLoaded;
    private JevaRScript initScript;

    public JevaMouse mouse;
    public JevaKey key;
    public JevaMeta meta;

    private int desiredFps;
    private int refreshRate;
    private int desiredTps;
    private int heartbeatRate;
    private double _dt;

    public JevaState state;

    private double timeScale;
    private double minTimeScale;
    private double maxTimeScale;
    private double clockAccumulator;
    private long processStartTime;

    public boolean debugMode;

    public JevaR(int _width, int _height, JevaRScript onLoad) {
        this(_width, _height, 60, onLoad);
    }

    public JevaR(int _width, int _height, int desiredFps, JevaRScript onLoad) {
        this(_width, _height, desiredFps, desiredFps, onLoad);
    }

    public JevaR(int _width, int _height, int desiredFps, int desiredTps, JevaRScript onLoad) {
        screen = new JevaScreen(this, _width, _height);

        jevaclipLibrary = new HashMap<>();
        jevascriptLibrary = new HashMap<>();
        jevafunctionLibrary = new HashMap<>();
        jevagraphicLibrary = new HashMap<>();
        jevapaintingLibrary = new HashMap<>();
        jevaspritesheetLibrary = new HashMap<>();
        jevasoundLibrary = new HashMap<>();
        jevasceneLibrary = new HashMap<>();

        jevascriptHierarchy = new HashMap<>();
        jevaclipHierarchy = new LinkedHashMap<>();
        hierarchyActiveSceneName = null;
        hierarchyDesiredSceneName = null;
        mustResetScene = false;

        mouse = new JevaMouse(this);
        key = new JevaKey(this);
        meta = new JevaMeta(this);

        state = new JevaState();

        isRunning = false;
        this.desiredFps = desiredFps;
        this.refreshRate = desiredFps;
        this.desiredTps = desiredTps;
        this.heartbeatRate = desiredTps;

        initScript = onLoad;
        isLoaded = false;

        _dt = 0;
        minTimeScale = 0.1;
        maxTimeScale = 10;
        setTimeScale(1);
        clockAccumulator = 0;
        processStartTime = 0;

        debugMode = false;

        _startRuntime();
    }

    // creating library jobtives

    protected JevaPainting getJevaPainting(String _label) {
        return jevapaintingLibrary.get(_label);
    }

    public void createPainting(String _label, JevaPainting painting) {
        if (jevapaintingLibrary.get(_label) != null)
            return;

        // add JevaPainting to game engine
        jevapaintingLibrary.put(_label, painting);
    }

    protected JevaGraphic getJevaGraphic(String _label) {
        JevaGraphic graphic = jevagraphicLibrary.get(_label);

        if (graphic == null) {
            createGraphic(_label);

            graphic = jevagraphicLibrary.get(_label);
        }
        return graphic;
    }

    public Image getImage(String _label) {
        JevaGraphic graphic = getJevaGraphic(_label);

        if (graphic == null)
            return null;

        Image source = graphic.getSource();
        return source;
    }

    public void createGraphic(String _label) {
        String path = JevaGraphic.sourcePath;
        String jpgFileName = _label.concat(".jpg");
        String JPGFileName = _label.concat(".JPG");
        String pngFileName = _label.concat(".png");
        String jpgFileSource = path.concat(jpgFileName);
        String JPGFileSource = path.concat(JPGFileName);
        String pngFileSource = path.concat(pngFileName);
        String fileName = pngFileName;
        if (JevaUtils.fileExists(pngFileSource))
            fileName = pngFileName;
        else if (JevaUtils.fileExists(JPGFileSource))
            fileName = JPGFileName;
        else if (JevaUtils.fileExists(jpgFileSource))
            fileName = jpgFileName;

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

    public void createSpriteSheet(String _label, String _source, int frames, int frameSize, int yStart, int fps) {
        if (jevaspritesheetLibrary.get(_label) != null)
            return;

        // add JevaSpriteSheet to game engine
        JevaSpriteSheet spritesheet = new JevaSpriteSheet(this, _source, frames, frameSize, yStart, fps);
        jevaspritesheetLibrary.put(_label, spritesheet);
    }

    public String copySound(String _label) {

        JevaSound sound = jevasoundLibrary.get(_label);

        if (sound == null) {
            createSound(_label);
            sound = jevasoundLibrary.get(_label);
            if (sound == null)
                return null;
        }

        sound = sound.clone();
        _label = sound.getLabel();
        jevasoundLibrary.put(_label, sound);

        return _label;
    }

    public JevaSound createSound(String _label) {
        return createSound(_label, 1);
    }

    public JevaSound createSound(String _label, int amount) {
        String path = JevaSound.sourcePath;
        String wavFileName = _label.concat(".wav");
        String wavFileSource = path.concat(wavFileName);
        String fileName = wavFileName;

        if (JevaUtils.fileExists(wavFileSource)) {
            return createSound(_label, fileName, amount);
        }
        return null;
    }

    public JevaSound createSound(String _label, String fileName) {
        return createSound(_label, fileName, 1);
    }

    public JevaSound createSound(String _label, String fileName, int amount) {
        if (jevasoundLibrary.get(_label) != null)
            return null;

        if (fileName.isEmpty())
            return null;

        // add JevaSound to game engine
        JevaSound sound = new JevaSound(_label, fileName, amount);
        jevasoundLibrary.put(_label, sound);

        return sound;
    }

    public JevaSound getSound(String _label) {
        JevaSound sound = jevasoundLibrary.get(_label);

        if (sound == null) {
            createSound(_label);

            sound = jevasoundLibrary.get(_label);
        }

        return sound;
    }

    public void createScene(String _label, JevaScript onLoad) {
        if (jevasceneLibrary.get(_label) != null)
            return;
        // initialize a JevaScene
        JevaScene jevascene = new JevaScene(this, _label, onLoad);

        // add JevaScene to game engine
        jevasceneLibrary.put(_label, jevascene);
    }

    public void createJevascript(String _label, JevaScript script) {
        if (jevascriptLibrary.get(_label) != null)
            return;

        // add JevaScript to game engine
        jevascriptLibrary.put(_label, script);
    }

    public void createFunc(String _label, JevaFunction func) {
        // if (jevafunctionLibrary.get(_label) != null)
        //     return;

        // add JevaFunction to game engine
        jevafunctionLibrary.put(_label, func);
    }

    public Object execFunc(String _label, Object ...args) {
        return setTimeout(_label, 0, args);
    }

    public Object execFunc(JevaFunction func, Object ...args) {
        return setTimeout(func, 0, args);
    }

    private static class JevaBatch {
        private JevaR core;
        private JevaFunction func;
        private long deadline;
        private boolean executed;
        private Object[] args;

        private static ArrayList<JevaBatch> batches = new ArrayList<>();

        private static void queueBatch(JevaR core, JevaFunction func, int ms, Object ...args) {
            JevaBatch batch = new JevaBatch(core, func, ms, args);

            batches.add(batch);
        }

        private static void resolveBatches() {
            Iterator<JevaBatch> itr = batches.iterator();
            while (itr.hasNext()) {
                JevaBatch batch = itr.next();
                batch.execute();
                if (batch.executed) {
                    itr.remove();
                }
            }

            // Read more:
            // https://www.java67.com/2018/12/how-to-remove-objects-or-elements-while-iterating-Arraylist-java.html#ixzz80P2jnWqS
        }

        private JevaBatch(JevaR core, JevaFunction func, int ms, Object ...args) {
            this.core = core;
            this.func = func;
            this.deadline = core.currentClockMillis() + ms;
            this.executed = false;
            this.args = args;
        }

        private void execute() {
            long timeNow = core.currentClockMillis();
            if (deadline < timeNow && !executed) {
                executed = true;
                if (func != null)
                    func.call(core.state, this.args);
            }
        }
    }

    public Object setTimeout(String _label, int ms, Object ...args) {
        JevaFunction func = jevafunctionLibrary.get(_label);

        if (func == null)
            return null;

        return setTimeout(func, ms, args);
    }

    public Object setTimeout(JevaFunction func, int ms, Object ...args) {
        if (func == null)
            return null;

        if (ms <= 0)
            return func.call(state, args);
        else
            JevaBatch.queueBatch(this, func, ms, args);
        return null;
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

    public void createPrefab(String _label, JevaScript onLoad) {
        if (jevaclipLibrary.get(_label) != null)
            return;

        // initilize a JevaClip
        JevaClip jevaclip = new JevaPrefab(this, _label, 0, 0, 100, 100, onLoad);

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

    public void createTileMap(String _label, double _x, double _y, int _width, int _height) {
        if (jevaclipLibrary.get(_label) != null)
            return;

        // initilize a JevaClip
        JevaClip jevaclip = new JevaTileMap(this, _label, _x, _y, _width, _height);

        // add JevaClip to game engine
        jevaclipLibrary.put(_label, jevaclip);
    }

    public void createTileMap(String _label, double _x, double _y, int _tileWidth, int _tileHeight,
            String _scriptName) {
        if (jevaclipLibrary.get(_label) != null)
            return;

        JevaScript script = jevascriptLibrary.get(_scriptName);
        if (script == null)
            return;

        // initilize a JevaClip
        JevaClip jevaclip = new JevaTileMap(this, _label, _x, _y, _tileWidth, _tileHeight, script);

        // add JevaClip to game engine
        jevaclipLibrary.put(_label, jevaclip);
    }

    public void createTileMap(String _label, double _x, double _y, int _tileWidth, int _tileHeight,
            JevaScript onLoad) {
        if (jevaclipLibrary.get(_label) != null)
            return;

        // initilize a JevaClip
        JevaClip jevaclip = new JevaTileMap(this, _label, _x, _y, _tileWidth, _tileHeight, onLoad);

        // add JevaClip to game engine
        jevaclipLibrary.put(_label, jevaclip);
    }

    public void createTileMap(String _label, double _x, double _y, int _tileWidth, int _tileHeight, int _mapWidth,
            int _mapHeight,
            JevaScript onLoad) {
        if (jevaclipLibrary.get(_label) != null)
            return;

        // initilize a JevaClip
        JevaClip jevaclip = new JevaTileMap(this, _label, _x, _y, _tileWidth, _tileHeight, _mapWidth, _mapHeight,
                onLoad);

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

        double _x = oldJevaclip.props._x;
        double _y = oldJevaclip.props._y;
        double _width = oldJevaclip.props._width;
        double _height = oldJevaclip.props._height;

        return this.attachPrefab(_label, _x, _y, _width, _height, new ArrayList<>(Arrays.asList()));
    }

    public JevaClip attachPrefab(String _label, double _x, double _y) {
        JevaClip oldJevaclip = jevaclipLibrary.get(_label);

        if (oldJevaclip == null)
            return oldJevaclip;

        double _width = oldJevaclip.props._width;
        double _height = oldJevaclip.props._height;

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

        double _x = oldText.props._x;
        double _y = oldText.props._y;
        String _text = oldText.props._text;
        double _width = oldText.props._width;
        double _height = oldText.props._height;

        return this.attachText(_label, _text, _x, _y, _width, _height, new ArrayList<>(Arrays.asList()));
    }

    public JevaClip attachText(String _label, String _text, double _x, double _y) {
        JevaClip oldJevaclip = jevaclipLibrary.get(_label);

        if (oldJevaclip == null)
            return oldJevaclip;

        double _width = oldJevaclip.props._width;
        double _height = oldJevaclip.props._height;

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

    public JevaClip attachTileMap(String _label) {
        JevaTileMap oldJevaclip = (JevaTileMap) jevaclipLibrary.get(_label);

        if (oldJevaclip == null)
            return oldJevaclip;

        double _x = oldJevaclip.props._x;
        double _y = oldJevaclip.props._y;
        int _tileWidth = JevaUtils.roundInt(oldJevaclip._tileWidth);
        int _tileHeight = JevaUtils.roundInt(oldJevaclip._tileHeight);

        return this.attachTileMap(_label, _x, _y, _tileWidth, _tileHeight, new ArrayList<>(Arrays.asList()));
    }

    public JevaClip attachTileMap(String _label, double _x, double _y) {
        JevaTileMap oldJevaclip = (JevaTileMap) jevaclipLibrary.get(_label);

        if (oldJevaclip == null)
            return oldJevaclip;

        int _tileWidth = JevaUtils.roundInt(oldJevaclip._tileWidth);
        int _tileHeight = JevaUtils.roundInt(oldJevaclip._tileHeight);

        return this.attachTileMap(_label, _x, _y, _tileWidth, _tileHeight, new ArrayList<>(Arrays.asList()));
    }

    public JevaClip attachTileMap(String _label, double _x, double _y, int _tileWidth, int _tileHeight) {
        JevaTileMap oldJevaclip = (JevaTileMap) jevaclipLibrary.get(_label);

        if (oldJevaclip == null)
            return oldJevaclip;

        return this.attachTileMap(_label, _x, _y, _tileWidth, _tileHeight, new ArrayList<>(Arrays.asList()));
    }

    public JevaClip attachTileMap(String _label, double _x, double _y, int _tileWidth, int _tileHeight,
            ArrayList<JevaScript> _onLoads) {
        JevaTileMap oldJevaclip = (JevaTileMap) jevaclipLibrary.get(_label);

        if (oldJevaclip == null)
            return oldJevaclip;

        ArrayList<JevaScript> onLoads = oldJevaclip._onLoadScripts;
        for (JevaScript _script : onLoads)
            _onLoads.add(_script);

        JevaClip jevaclip = new JevaTileMap(this, _label, _x, _y, _tileWidth, _tileHeight, _onLoads);

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

        hierarchyDesiredSceneName = _label;
    }

    public void useScene(String _label, boolean absolute) {
        if (getCurrentSceneName().equals(_label))
            resetScene();
        else
            useScene(_label);
    }

    private void _useScene(String _label) {
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
        mustResetScene = true;
    }

    private void _resetScene() {
        mustResetScene = false;
        String currSceneName = getCurrentSceneName();

        if (currSceneName == null)
            return;

        getCurrentScene().unload();

        key.expireKeyPressedStates();
        mouse.expireMousePressedStates();

        getCurrentScene().load();
    }

    public int getFPS() {
        return refreshRate;
    }

    public int getTPS() {
        return heartbeatRate;
    }

    public double getDelta() {
        return _dt;
    }

    public long currentClockMillis() {
        return Math.round(clockAccumulator / 1000000);
    }

    public long currentProcessMillis() {
        return System.currentTimeMillis() - processStartTime;
    }

    public void setTimeScale(double scale) {
        timeScale = JevaUtils.clampFloat(scale, minTimeScale, maxTimeScale);
    }

    public double alterTimeScale(double diff) {
        return timeScale = JevaUtils.clampFloat(timeScale + diff, minTimeScale, maxTimeScale);
    }

    public double getTimeScale() {
        return timeScale;
    }

    public JevaClip getJevaClip(String name) {
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
        return hierarchyActiveSceneName;
    }

    private void setCurrentSceneName(String _label) {
        hierarchyActiveSceneName = _label;
        hierarchyDesiredSceneName = _label;
    }

    public JevaScene getCurrentScene() {
        if (hierarchyActiveSceneName == null)
            return null;

        return jevasceneLibrary.get(hierarchyActiveSceneName);
    }

    private boolean hasScene(String _label) {
        return jevasceneLibrary.get(_label) != null;
    }

    public void run() {
        isRunning = true;
        if (processStartTime == 0)
            processStartTime = System.currentTimeMillis();

        double t = 0.0;
        double dt = 1.0 / desiredTps;
        double df = 1.0 / desiredFps;
        _dt = dt;

        long currentTime = System.nanoTime();
        double tickAccumulator = 0.0;
        double renderAccumulator = 0.0;

        int frames = 0;
        long lastCheck = System.currentTimeMillis();
        int displayRate = 0;

        while (isRunning) {
            long newTime = System.nanoTime();
            double diffTime = (newTime - currentTime);
            double clockTime = diffTime * timeScale;

            double frameTime = diffTime / 1000000000;
            currentTime = newTime;

            clockAccumulator += clockTime;

            tickAccumulator += frameTime * timeScale;
            renderAccumulator += frameTime;

            while (tickAccumulator >= dt * timeScale) {
                runEngine();
                frames++;
                tickAccumulator -= dt;
                t += dt;
            }

            if (renderAccumulator >= df) {
                renderEngine();
                displayRate++;
                renderAccumulator -= df;
            }

            if (System.currentTimeMillis() - lastCheck >= 1000) {
                lastCheck = System.currentTimeMillis();
                heartbeatRate = frames;
                refreshRate = displayRate;
                frames = 0;
                displayRate = 0;
            }
        }
    }

    private void runEngine() {
        tickEngine();
        checkHoverEngine();
        key.clearKeyStates(false);
        mouse.clearMouseStates(false);

        JevaBatch.resolveBatches();

        cleanUpEngine();
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

    private void checkHoverEngine() {
        if (getCurrentSceneName() != null)
            getCurrentScene().checkHovered();
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

        // deleting all nested jevaclips markedForDeletion from hierarchy
        for (JevaClip jevaclip : jevaclipHierarchy.values()) {
            jevaclip.cleanUp();
        }

        if (hierarchyDesiredSceneName != null && !hierarchyDesiredSceneName.equals(hierarchyActiveSceneName))
            _useScene(hierarchyDesiredSceneName);

        if (mustResetScene)
            _resetScene();
    }

    protected boolean isDebugMode() {
        return debugMode;
    }

}

package core;

import java.awt.Graphics2D;
import java.util.*;

public class JevaScene {
    private String _label;
    protected JevaR core;

    private boolean isLoaded;

    public JevaState state;

    protected ArrayList<JevaScript> _onLoadScripts;
    protected ArrayList<JevaScript> _onUnloadScripts;
    protected ArrayList<JevaScript> _scriptsList;

    protected LinkedHashMap<String, JevaClip> sceneclipHierarchy;
    private HashMap<String, JevaVCam> scenevcamHierarchy;

    protected JevaScene(JevaR core, String _label) {
        this(core, _label, new ArrayList<>(Arrays.asList()));
    }

    protected JevaScene(JevaR core, String _label, JevaScript onLoad) {
        this(core, _label, new ArrayList<>(Arrays.asList(onLoad)));
    }

    protected JevaScene(JevaR core, String _label, ArrayList<JevaScript> onLoads) {
        this.core = core;
        this._label = _label;

        sceneclipHierarchy = new LinkedHashMap<>();
        scenevcamHierarchy = new HashMap<>();

        state = new JevaState();

        _onLoadScripts = onLoads;
        isLoaded = false;

        _scriptsList = new ArrayList<>();
        _onUnloadScripts = new ArrayList<>();
    }

    protected void load() {
        if (isLoaded)
            return;
        for (JevaScript script : _onLoadScripts) {
            script.call(this);
        }

        for (JevaClip jevaclip : sceneclipHierarchy.values()) {
            jevaclip.load();
        }

        isLoaded = true;
    }

    public void addUnload(String _label) {
        JevaScript script = core.jevascriptLibrary.get(_label);

        if (script == null)
            return;

        addUnload(script);
    }

    public void addUnload(JevaScript _unloadScript) {
        _onUnloadScripts.add(_unloadScript);
    }

    protected void unload() {
        if (!isLoaded)
            return;

        for (JevaClip jevaclip : sceneclipHierarchy.values()) {
            if (!jevaclip.preserve) {
                jevaclip.unload();
                // delete from heirarchy
            }
        }

        // delete all unpreserved jevaclips from hierarchy
        sceneclipHierarchy.entrySet().removeIf(e -> {
            JevaClip jevaclip = e.getValue();
            return jevaclip.preserve == false;
        });
        
        // call all unload methods
        for (JevaScript script : _onUnloadScripts) {
            script.call(this);
        }
        _onUnloadScripts = new ArrayList<>();
        
        isLoaded = false;
    }

    // adding jobtives

    public void addJevascript(String _label) {
        JevaScript script = core.jevascriptLibrary.get(_label);

        if (script == null)
            return;

        addJevascript(script);
    }

    public void addJevascript(JevaScript script) {
        _scriptsList.add(script);
    }

    public JevaClip addPrefab(String _label) {
        JevaClip oldJevaclip = core.jevaclipLibrary.get(_label);

        if (oldJevaclip == null)
            return oldJevaclip;

        double _x = oldJevaclip.props._x;
        double _y = oldJevaclip.props._y;
        double _width = oldJevaclip.props._width;
        double _height = oldJevaclip.props._height;

        return this.addPrefab(_label, _x, _y, _width, _height, new ArrayList<>(Arrays.asList()));
    }

    public JevaClip addPrefab(String _label, double _x, double _y) {
        JevaClip oldJevaclip = core.jevaclipLibrary.get(_label);

        if (oldJevaclip == null)
            return oldJevaclip;

        double _width = oldJevaclip.props._width;
        double _height = oldJevaclip.props._height;

        return this.addPrefab(_label, _x, _y, _width, _height, new ArrayList<>(Arrays.asList()));
    }

    public JevaClip addPrefab(String _label, double _x, double _y, double _width, double _height) {
        JevaClip oldJevaclip = core.jevaclipLibrary.get(_label);

        if (oldJevaclip == null)
            return oldJevaclip;

        return this.addPrefab(_label, _x, _y, _width, _height, new ArrayList<>(Arrays.asList()));
    }

    public JevaClip addPrefab(String _label, double _x, double _y, double _width, double _height,
            ArrayList<JevaScript> _onLoads) {
        JevaClip oldJevaclip = core.jevaclipLibrary.get(_label);

        if (oldJevaclip == null)
            return oldJevaclip;

        ArrayList<JevaScript> onLoads = oldJevaclip._onLoadScripts;
        for (JevaScript _script : onLoads)
            _onLoads.add(_script);

        JevaClip jevaclip = new JevaPrefab(core, _label, _x, _y, _width, _height, _onLoads);

        String id = JevaUtils.generateUUID();

        // add JevaClip to scene's heirarchy
        sceneclipHierarchy.put(id, jevaclip);

        if (isLoaded)
            jevaclip.load();

        return jevaclip;
    }

    public JevaClip addPrefab(double _x, double _y, double _width, double _height) {
        return this.addPrefab(_x, _y, _width, _height, JevaUtils.emptyScript);
    }

    public JevaClip addPrefab(double _x, double _y, double _width, double _height, String _scriptName) {
        JevaScript script = core.jevascriptLibrary.get(_scriptName);
        if (script == null)
            script = JevaUtils.emptyScript;

        return this.addPrefab(_x, _y, _width, _height, script);
    }

    public JevaClip addPrefab(double _x, double _y, double _width, double _height,
            JevaScript onLoad) {
        String id = JevaUtils.generateUUID();
        JevaClip jevaclip = new JevaPrefab(core, id, _x, _y, _width, _height, onLoad);

        // add JevaClip to scene's heirarchy
        sceneclipHierarchy.put(id, jevaclip);

        if (isLoaded)
            jevaclip.load();

        return jevaclip;
    }

    public JevaClip addText(String _label) {
        return this.addText(_label, JevaUtils.emptyScript);
    }

    public JevaClip addText(String _label, JevaScript onLoad) {
        JevaClip oldJevaclip = core.jevaclipLibrary.get(_label);
        JevaText oldText = (JevaText) oldJevaclip;

        if (oldText == null)
            return oldText;

        double _x = oldText.props._x;
        double _y = oldText.props._y;
        String _text = oldText.props._text;
        double _width = oldText.props._width;
        double _height = oldText.props._height;

        return this.addText(_label, _text, _x, _y, _width, _height, new ArrayList<>(Arrays.asList(onLoad)));
    }

    public JevaClip addText(String _label, String _text, double _x, double _y) {
        JevaClip oldJevaclip = core.jevaclipLibrary.get(_label);

        if (oldJevaclip == null)
            return oldJevaclip;

        double _width = oldJevaclip.props._width;
        double _height = oldJevaclip.props._height;

        return this.addText(_label, _text, _x, _y, _width, _height, new ArrayList<>(Arrays.asList()));
    }

    public JevaClip addText(String _label, String _text, double _x, double _y, double _width, double _height) {
        JevaClip oldJevaclip = core.jevaclipLibrary.get(_label);

        if (oldJevaclip == null)
            return oldJevaclip;

        return this.addText(_label, _text, _x, _y, _width, _height, new ArrayList<>(Arrays.asList()));
    }

    public JevaClip addText(String _label, String _text, double _x, double _y, double _width, double _height,
            ArrayList<JevaScript> _onLoads) {
        JevaClip oldJevaclip = core.jevaclipLibrary.get(_label);

        if (oldJevaclip == null)
            return oldJevaclip;

        ArrayList<JevaScript> onLoads = oldJevaclip._onLoadScripts;
        for (JevaScript _script : onLoads)
            _onLoads.add(_script);

        JevaClip jevaclip = new JevaText(core, _label, _text, _x, _y, _width, _height, _onLoads);

        String id = JevaUtils.generateUUID();

        // add JevaClip to scene's heirarchy
        sceneclipHierarchy.put(id, jevaclip);

        if (isLoaded)
            jevaclip.load();

        return jevaclip;
    }

    public JevaClip addText(String _text, double _x, double _y, double _width, double _height) {
        return this.addText(_text, _x, _y, _width, _height, JevaUtils.emptyScript);
    }

    public JevaClip addText(String _text, double _x, double _y, double _width, double _height, String _scriptName) {
        JevaScript script = core.jevascriptLibrary.get(_scriptName);
        if (script == null)
            script = JevaUtils.emptyScript;

        return this.addText(_text, _x, _y, _width, _height, script);
    }

    public JevaClip addText(String _text, double _x, double _y, double _width, double _height,
            JevaScript onLoad) {
        String id = JevaUtils.generateUUID();
        JevaClip jevaclip = new JevaText(core, id, _text, _x, _y, _width, _height, onLoad);

        // add JevaClip to scene's heirarchy
        sceneclipHierarchy.put(id, jevaclip);

        if (isLoaded)
            jevaclip.load();

        return jevaclip;
    }

    public JevaClip addTileMap(String _label) {
        JevaTileMap oldJevaclip = (JevaTileMap) core.jevaclipLibrary.get(_label);

        if (oldJevaclip == null)
            return oldJevaclip;

        double _x = oldJevaclip.props._x;
        double _y = oldJevaclip.props._y;
        int _tileWidth = JevaUtils.roundInt(oldJevaclip._tileWidth);
        int _tileHeight = JevaUtils.roundInt(oldJevaclip._tileHeight);

        return this.addTileMap(_label, _x, _y, _tileWidth, _tileHeight, new ArrayList<>(Arrays.asList()));
    }

    public JevaClip addTileMap(String _label, double _x, double _y) {
        JevaTileMap oldJevaclip = (JevaTileMap) core.jevaclipLibrary.get(_label);

        if (oldJevaclip == null)
            return oldJevaclip;

        int _tileWidth = JevaUtils.roundInt(oldJevaclip._tileWidth);
        int _tileHeight = JevaUtils.roundInt(oldJevaclip._tileHeight);

        return this.addTileMap(_label, _x, _y, _tileWidth, _tileHeight, new ArrayList<>(Arrays.asList()));
    }

    public JevaClip addTileMap(String _label, double _x, double _y, int _tileWidth, int _tileHeight) {
        JevaTileMap oldJevaclip = (JevaTileMap) core.jevaclipLibrary.get(_label);

        if (oldJevaclip == null)
            return oldJevaclip;

        return this.addTileMap(_label, _x, _y, _tileWidth, _tileHeight, new ArrayList<>(Arrays.asList()));
    }

    public JevaClip addTileMap(String _label, double _x, double _y, int _tileWidth, int _tileHeight,
            ArrayList<JevaScript> _onLoads) {
        JevaTileMap oldJevaclip = (JevaTileMap) core.jevaclipLibrary.get(_label);

        if (oldJevaclip == null)
            return oldJevaclip;

        ArrayList<JevaScript> onLoads = oldJevaclip._onLoadScripts;
        for (JevaScript _script : onLoads)
            _onLoads.add(_script);

        JevaClip jevaclip = new JevaTileMap(core, _label, _x, _y, _tileWidth, _tileHeight, _onLoads);

        String id = JevaUtils.generateUUID();

        // add JevaClip to scene's heirarchy
        sceneclipHierarchy.put(id, jevaclip);

        jevaclip.load();

        return jevaclip;
    }

    public JevaVCam addVCam(String _label) {
        int defaultStageWidth = core.screen.getWidth();
        int defaultStageHeight = core.screen.getHeight();
        JevaVCam vcam = addVCam(_label, defaultStageWidth / 2, defaultStageHeight / 2, defaultStageWidth,
                defaultStageHeight);
        vcam.projection.setAnchorX(0.5);
        vcam.projection.setAnchorY(0.5);
        vcam.viewport.setAnchorX(0.5);
        vcam.viewport.setAnchorY(0.5);

        return vcam;
    }

    public JevaVCam addVCam(String _label, double _pX, double _pY, double _pWidth, double _pHeight) {
        return addVCam(_label, _pX, _pY, _pWidth, _pHeight, _pX, _pY, _pWidth, _pHeight);
    }

    public JevaVCam addVCam(String _label, double _pX, double _pY, double _pWidth, double _pHeight, double _vX,
            double _vY, double _vWidth, double _vHeight) {
        JevaVCam vcam = scenevcamHierarchy.get(_label);

        if (vcam != null)
            return vcam;

        vcam = new JevaVCam(core, _label, _pX, _pY, _pWidth, _pHeight, _vX, _vY, _vWidth, _vHeight);

        scenevcamHierarchy.put(_label, vcam);

        return vcam;
    }

    public JevaVCam getVCam(String _label) {
        return scenevcamHierarchy.get(_label);
    }

    protected void setVCamMouseCoords(int _x, int _y) {
        for (JevaVCam vcam : scenevcamHierarchy.values())
            vcam.setVCamMouseCoords(_x, _y);
    }

    protected JevaClip getJevaClip(String name) {
        for (JevaClip jevaclip : sceneclipHierarchy.values()) {
            if (jevaclip._instanceName.equals(name) && !jevaclip.shouldRemove())
                return jevaclip;
            JevaClip nestedClip = jevaclip.getJevaClip(name);
            if (nestedClip != null)
                return nestedClip;
        }
        return null;
    }

    protected void checkHovered() {
        for (JevaClip jevaclip : sceneclipHierarchy.values()) {
            if (!jevaclip.shouldRemove()) {
                boolean isVisible = jevaclip.props._visible;
                if (jevaclip instanceof JevaText) {
                    JevaText jevatext = (JevaText) jevaclip;
                    boolean isHovered = false;
                    if (isVisible && jevatext.hitTest(core.mouse._xmouse, core.mouse._ymouse)) {
                        isHovered = true;
                    }
                    if (!isHovered && scenevcamHierarchy.size() > 0) {
                        for (JevaVCam vcam : scenevcamHierarchy.values()) {
                            if (jevatext.hitTest(vcam._xmouse, vcam._ymouse)) {
                                isHovered = isVisible ? true : false;
                                jevatext.checkHovered(vcam._xmouse, vcam._ymouse, isVisible);
                            }
                        }
                    }
                    jevatext.props._hovered = isHovered;
                    jevatext.checkHovered(core.mouse._xmouse, core.mouse._ymouse, isVisible);
                } else {
                    boolean isHovered = false;
                    if (isVisible && jevaclip.hitTest(core.mouse._xmouse, core.mouse._ymouse)) {
                        isHovered = true;
                    }
                    if (!isHovered && scenevcamHierarchy.size() > 0) {
                        for (JevaVCam vcam : scenevcamHierarchy.values()) {
                            if (jevaclip.hitTest(vcam._xmouse, vcam._ymouse)) {
                                isHovered = isVisible ? true : false;
                                jevaclip.checkHovered(vcam._xmouse, vcam._ymouse, isVisible);
                            }
                        }
                    }
                    jevaclip.props._hovered = isHovered;
                    jevaclip.checkHovered(core.mouse._xmouse, core.mouse._ymouse, isVisible);
                }
            }
        }
    }

    protected void tick() {
        if (!isLoaded)
            return;
        // run all added scripts
        for (JevaScript script : _scriptsList) {
            script.call(this);
        }

        // updating all added jevaclips
        LinkedHashMap<String, JevaClip> tempClipHierarchy = new LinkedHashMap<>(sceneclipHierarchy);
        for (JevaClip jevaclip : tempClipHierarchy.values()) {
            jevaclip.tick();
        }
    }

    private JevaClipProps sceneProps = new JevaClipProps(0, 0, 0, 0);

    protected void render(Graphics2D ctx) {
        if (scenevcamHierarchy.size() == 0) {
            // rendering all added jevaclips
            for (JevaClip jevaclip : sceneclipHierarchy.values()) {
                if (core.screen.hitTest(jevaclip)) {
                    if (jevaclip instanceof JevaTileMap)
                        ((JevaTileMap) jevaclip).render(ctx, core.screen.getBoundingRectangle());
                    else if (jevaclip instanceof JevaText)
                        ((JevaText) jevaclip).render(ctx, sceneProps);
                    else
                        jevaclip.render(ctx, sceneProps);
                }
            }
            // rendering added jevaclips
            for (JevaClip jevaclip : core.jevaclipHierarchy.values()) {
                if (core.screen.hitTest(jevaclip)) {
                    if (jevaclip instanceof JevaTileMap)
                        ((JevaTileMap) jevaclip).render(ctx, core.screen.getBoundingRectangle());
                    else if (jevaclip instanceof JevaText)
                        ((JevaText) jevaclip).render(ctx, sceneProps);
                    else
                        jevaclip.render(ctx, sceneProps);
                }
            }
        } else {
            for (JevaVCam vcam : scenevcamHierarchy.values()) {
                if (core.screen.hitTest(vcam.getViewportBoundingRectangle()))
                    vcam.render(ctx, sceneProps);
            }
        }
    }

    protected void cleanUp() {
        if (!isLoaded)
            return;

        // delete all jevaclips markedForDeletion from hierarchy
        for (JevaClip jevaclip : sceneclipHierarchy.values()) {
            if (jevaclip.shouldRemove())
                jevaclip.unload();
        }
        sceneclipHierarchy.entrySet().removeIf(e -> {
            JevaClip jevaclip = e.getValue();
            return jevaclip.shouldRemove();
        });

        // rendering all added jevaclips
        for (JevaClip jevaclip : sceneclipHierarchy.values()) {
            jevaclip.cleanUp();
        }
    }
}

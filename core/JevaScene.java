package core;

import java.awt.Graphics2D;
import java.util.*;

public class JevaScene {
    private String _label;
    protected JevaR core;

    private boolean isLoaded;

    protected ArrayList<JevaScript> _onLoadScripts;
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

        _onLoadScripts = onLoads;
        isLoaded = false;

        _scriptsList = new ArrayList<>();
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
        ;

        isLoaded = true;
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

        double _x = oldJevaclip._x;
        double _y = oldJevaclip._y;
        double _width = oldJevaclip._width;
        double _height = oldJevaclip._height;

        return this.addPrefab(_label, _x, _y, _width, _height, new ArrayList<>(Arrays.asList()));
    }

    public JevaClip addPrefab(String _label, double _x, double _y) {
        JevaClip oldJevaclip = core.jevaclipLibrary.get(_label);

        if (oldJevaclip == null)
            return oldJevaclip;

        double _width = oldJevaclip._width;
        double _height = oldJevaclip._height;

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

        return jevaclip;
    }

    public JevaClip addText(String _label) {
        JevaClip oldJevaclip = core.jevaclipLibrary.get(_label);
        JevaText oldText = (JevaText) oldJevaclip;

        if (oldText == null)
            return oldText;

        double _x = oldText._x;
        double _y = oldText._y;
        String _text = oldText._text;
        double _width = oldText._width;
        double _height = oldText._height;

        return this.addText(_label, _text, _x, _y, _width, _height, new ArrayList<>(Arrays.asList()));
    }

    public JevaClip addText(String _label, String _text, double _x, double _y) {
        JevaClip oldJevaclip = core.jevaclipLibrary.get(_label);

        if (oldJevaclip == null)
            return oldJevaclip;

        double _width = oldJevaclip._width;
        double _height = oldJevaclip._height;

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

        return jevaclip;
    }

    public JevaVCam addVCam(String _label) {
        int defaultStageWidth = core.screen.getWidth();
        int defaultStageHeight = core.screen.getHeight();
        JevaVCam vcam = addVCam(_label, defaultStageWidth / 2, defaultStageHeight / 2, defaultStageWidth, defaultStageHeight);
        vcam.setPAnchorX(0.5);
        vcam.setPAnchorY(0.5);
        vcam.setVAnchorX(0.5);
        vcam.setVAnchorY(0.5);

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
        }
        return null;
    }

    protected void tick() {
        if (!isLoaded)
            return;
        // run all added scripts
        for (JevaScript script : _scriptsList) {
            script.call(this);
        }

        // updating all added jevaclips
        for (JevaClip jevaclip : sceneclipHierarchy.values()) {
            jevaclip.tick();
        }
    }

    protected void render(Graphics2D ctx) {
        if (scenevcamHierarchy.size() == 0) {
            // rendering all added jevaclips
            for (JevaClip jevaclip : sceneclipHierarchy.values()) {
                jevaclip.render(ctx);
            }
            // rendering attached jevaclips
            for (JevaClip jevaclip : core.jevaclipHierarchy.values()) {
                if (core.screen.hitTest(jevaclip.getBoundingRectangle()))
                    jevaclip.render(ctx);

            }
        } else {
            for (JevaVCam vcam : scenevcamHierarchy.values()) {
                if (core.screen.hitTest(vcam.getViewportBoundingRectangle()))
                    vcam.render(ctx);
            }
        }
    }

    protected void cleanUp() {
        if (!isLoaded)
            return;

        // delete all jevaclips markedForDeletion from hierarchy
        sceneclipHierarchy.entrySet().removeIf(e -> {
            JevaClip jevaclip = e.getValue();
            return jevaclip.shouldRemove();
        });
    }
}

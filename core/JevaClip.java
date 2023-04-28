package core;

import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

public class JevaClip {
    private String _label;
    protected JevaR core;

    public JevaClipProps props;

    protected LinkedHashMap<String, JevaClip> clipsContainer;

    private boolean markedForDeletion;

    public boolean preserve;

    protected String _instanceName;

    protected boolean isLoaded;

    public JevaState state;

    protected ArrayList<JevaScript> _onLoadScripts;
    protected ArrayList<JevaScript> _onUnloadScripts;
    protected ArrayList<JevaScript> _scriptsList;

    protected JevaClip(JevaR core, String _label, double _x, double _y, double _width, double _height) {
        this(core, _label, _x, _y, _width, _height, new ArrayList<>(Arrays.asList()));
    }

    protected JevaClip(JevaR core, String _label, double _x, double _y, double _width, double _height,
            JevaScript onLoad) {
        this(core, _label, _x, _y, _width, _height, new ArrayList<>(Arrays.asList(onLoad)));
    }

    protected JevaClip(JevaR core, String _label, double _x, double _y, double _width, double _height,
            ArrayList<JevaScript> onLoads) {
        this.core = core;
        this._label = _label;
        props = new JevaClipProps(_x, _y, _width, _height);

        clipsContainer = new LinkedHashMap<>();

        this._instanceName = "";

        _onLoadScripts = onLoads;
        isLoaded = false;
        preserve = false;
        markedForDeletion = false;

        state = new JevaState();

        _scriptsList = new ArrayList<>();
        _onUnloadScripts = new ArrayList<>();
    }

    public JevaClip extend(String _label) {
        if (isLoaded)
            return this;

        JevaClip superClip = core.jevaclipLibrary.get(_label);
        if (superClip == null)
            return this;

        for (JevaScript script : superClip._onLoadScripts) {
            script.call(this);
        }

        return this;
    }

    protected void load() {
        if (isLoaded)
            return;
        for (JevaScript script : _onLoadScripts) {
            script.call(this);
        }

        for (JevaClip jevaclip : clipsContainer.values()) {
            jevaclip.load();
        }

        isLoaded = true;
        markedForDeletion = false;
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

        for (JevaClip jevaclip : clipsContainer.values()) {
            if (!jevaclip.preserve) {
                jevaclip.unload();
                // delete from heirarchy
            }
        }

        // delete all unpreserved jevaclips from hierarchy
        clipsContainer.entrySet().removeIf(e -> {
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

    protected void tick() {
        if (!isLoaded || shouldRemove())
            return;
        // run all added scripts
        for (JevaScript script : _scriptsList) {
            script.call(this);
        }

        // updating all added jevaclips
        for (JevaClip jevaclip : clipsContainer.values()) {
            jevaclip.tick();
        }
    }

    protected void render(Graphics2D ctx, JevaClipProps parentProps) {
        if (!isLoaded || shouldRemove() || !props._visible)
            return;
        int _x = JevaUtils.roundInt((props._x));
        int _y = JevaUtils.roundInt((props._y));
        int _width = JevaUtils.roundInt((props._width * props._scaleX));
        int _height = JevaUtils.roundInt((props._height * props._scaleY));
        int offsetX = JevaUtils.roundInt((props._anchorX * _width));
        int offsetY = JevaUtils.roundInt((props._anchorY * _height));
        int w = Math.max(Math.abs(_width), 1);
        int h = Math.max(Math.abs(_height), 1);
        int x = _width >= 0 ? _x : _x - w;
        int y = _height >= 0 ? _y : _y - h;

        BufferedImage painting = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        AffineTransform at = new AffineTransform();
        if (_width < 0) {
            at.scale(-1, 1);
            at.translate(_width, 0);
        }
        if (_height < 0) {
            at.scale(1, -1);
            at.translate(0, _height);
        }

        Graphics2D pCtx = (Graphics2D) painting.getGraphics();
        pCtx.transform(at);
        int px = 0;
        int py = 0;
        Rectangle2D.Double body = new Rectangle2D.Double(px, py, w, h);
        Rectangle2D.Double rec1 = new Rectangle2D.Double(px + (w * 3 / 5), py, (w / 5), h);
        Rectangle2D.Double rec2 = new Rectangle2D.Double(px, py + (h * 2 / 5), w, (h / 5));

        pCtx.setColor(Color.GRAY);
        pCtx.fill(body);

        pCtx.setColor(Color.DARK_GRAY);
        pCtx.fill(rec1);
        pCtx.fill(rec2);

        ctx.drawImage(painting, x - offsetX, y - offsetY, null);
        pCtx.dispose();
    }

    protected void cleanUp() {
        if (!isLoaded)
            return;

        // unload and delete all jevaclips markedForDeletion from hierarchy
        for (JevaClip jevaclip : clipsContainer.values()) {
            if (jevaclip.shouldRemove())
                jevaclip.unload();
        }
        clipsContainer.entrySet().removeIf(e -> {
            JevaClip jevaclip = e.getValue();
            return jevaclip.shouldRemove();
        });

        // deleting all nested jevaclips markedForDeletion from hierarchy
        for (JevaClip jevaclip : clipsContainer.values()) {
            jevaclip.cleanUp();
        }
    }

    public void remove() {
        markedForDeletion = true;
    }

    protected boolean shouldRemove() {
        return markedForDeletion;
    }

    protected Rectangle2D.Double getBoundingRectangle() {
        double _width = props._width * props._scaleX;
        double _height = props._height * props._scaleY;
        double offsetX = props._anchorX * _width;
        double offsetY = props._anchorY * _height;
        double w = Math.max(Math.abs(_width), 1);
        double h = Math.max(Math.abs(_height), 1);
        double x = _width >= 0 ? props._x : props._x - w;
        double y = _height >= 0 ? props._y : props._y - h;
        return new Rectangle2D.Double(x - offsetX, y - offsetY, w, h);
    }

    public boolean hitTest(JevaClip other) {
        if (shouldRemove() || other.shouldRemove() || !props._visible || !other.props._visible)
            return false;
        Rectangle2D.Double thisRect = getBoundingRectangle();
        Rectangle2D.Double otherRect = other.getBoundingRectangle();

        return thisRect.intersects(otherRect);
    }

    public boolean hitTest(Rectangle2D.Double targetRect) {
        if (shouldRemove() || !props._visible)
            return false;
        Rectangle2D.Double thisRect = getBoundingRectangle();

        return thisRect.intersects(targetRect);
    }

    public boolean hitTest(double x, double y) {
        if (shouldRemove() || !props._visible)
            return false;
        Rectangle2D.Double thisRect = getBoundingRectangle();
        return thisRect.contains(x, y);
    }

    public boolean hitTest(String _label) {
        return hitTestGet(_label) != null;
    }

    public JevaClip hitTestGet(String _label) {
        if (shouldRemove() || !props._visible)
            return null;
        Rectangle2D.Double thisRect = getBoundingRectangle();
        for (JevaClip jevaclip : core.jevaclipHierarchy.values()) {
            if ((jevaclip._label.equals(_label) || jevaclip._instanceName.equals(_label)) && jevaclip != this
                    && !jevaclip.shouldRemove()) {
                Rectangle2D.Double otherClipsRect = jevaclip.getBoundingRectangle();
                if (thisRect.intersects(otherClipsRect)) {
                    return jevaclip;
                }
            }
        }
        if (core.getCurrentScene() != null)
            for (JevaClip jevaclip : core.getCurrentScene().sceneclipHierarchy.values()) {
                if ((jevaclip._label.equals(_label) || jevaclip._instanceName.equals(_label)) && jevaclip != this
                        && !jevaclip.shouldRemove()) {
                    Rectangle2D.Double otherClipsRect = jevaclip.getBoundingRectangle();
                    if (thisRect.intersects(otherClipsRect)) {
                        return jevaclip;
                    }
                }
            }
        return null;
    }

    public String getInstanceName() {
        return _instanceName;
    }

    public void setInstanceName(String name) {
        _instanceName = name;
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
        clipsContainer.put(id, jevaclip);

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
        clipsContainer.put(id, jevaclip);

        if (isLoaded)
            jevaclip.load();

        return jevaclip;
    }

    public JevaClip addText(String _label) {
        JevaClip oldJevaclip = core.jevaclipLibrary.get(_label);
        JevaText oldText = (JevaText) oldJevaclip;

        if (oldText == null)
            return oldText;

        double _x = oldText.props._x;
        double _y = oldText.props._y;
        String _text = oldText.props._text;
        double _width = oldText.props._width;
        double _height = oldText.props._height;

        return this.addText(_label, _text, _x, _y, _width, _height, new ArrayList<>(Arrays.asList()));
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
        clipsContainer.put(id, jevaclip);

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
        clipsContainer.put(id, jevaclip);

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
        clipsContainer.put(id, jevaclip);

        jevaclip.load();

        return jevaclip;
    }

    protected JevaClip getJevaClip(String name) {
        for (JevaClip jevaclip : clipsContainer.values()) {
            if (jevaclip._instanceName.equals(name) && !jevaclip.shouldRemove())
                return jevaclip;
            JevaClip nestedClip = jevaclip.getJevaClip(name);
            if (nestedClip != null)
                return nestedClip;
        }
        return null;
    }

    protected void checkHovered(double x, double y, boolean isVisible) {
        for (JevaClip jevaclip : clipsContainer.values()) {
            if (!jevaclip.shouldRemove()) {
                if (jevaclip instanceof JevaText) {
                    JevaText jevatext = (JevaText) jevaclip;
                    jevatext.props._hovered = isVisible ? jevatext.hitTest(x + props._x, y + props._y) : false;
                    jevatext.checkHovered(x + props._x, y + props._y, isVisible);
                } else {
                    jevaclip.props._hovered = isVisible ? jevaclip.hitTest(x + props._x, y + props._y) : false;
                    jevaclip.checkHovered(x + props._x, y + props._y, isVisible);
                }
            }
        }
    }
}

package core;

import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

public class JevaClip {
    private String _label;
    protected JevaR core;

    private boolean markedForDeletion;

    public boolean preserve;

    public double _x, _y, _width, _height;
    protected double _anchorX, _anchorY;
    public double _scaleX, _scaleY;
    public String _instanceName;

    protected boolean isLoaded;

    protected ArrayList<JevaScript> _onLoadScripts;
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
        this._x = _x;
        this._y = _y;
        this._width = _width;
        this._height = _height;
        this._anchorX = 0;
        this._anchorY = 0;
        this._scaleX = 1;
        this._scaleY = 1;
        this._instanceName = "";

        _onLoadScripts = onLoads;
        isLoaded = false;
        preserve = false;
        markedForDeletion = false;

        _scriptsList = new ArrayList<>();
    }

    protected void load() {
        if (isLoaded)
            return;
        for (JevaScript script : _onLoadScripts) {
            script.call(this);
        }
        isLoaded = true;
        markedForDeletion = false;
    }

    protected void unload() {
        if (!isLoaded)
            return;

        isLoaded = false;
    }

    // adding jobtives

    public void addJevascript(String _label) {
        JevaScript script = core.jevascriptLibrary.get(_label);

        if (script == null)
            return;

        addJevascript(script);
    }

    public void centerAnchors() {
        double xDiff = 0.5 - _anchorX;
        double yDiff = 0.5 - _anchorY;

        double xDiffOffset = (_width * _scaleX) * xDiff;
        double yDiffOffset = (_height * _scaleY) * yDiff;

        _x += xDiffOffset;
        _y += yDiffOffset;

        setAnchorX(0.5);
        setAnchorY(0.5);
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
    }

    protected void render(Graphics2D ctx) {
        if (!isLoaded || shouldRemove())
            return;
        int _x = JevaUtils.roundInt(this._x);
        int _y = JevaUtils.roundInt(this._y);
        int _width = JevaUtils.roundInt(this._width * this._scaleX);
        int _height = JevaUtils.roundInt(this._height * this._scaleY);
        int offsetX = JevaUtils.roundInt(this._anchorX * _width);
        int offsetY = JevaUtils.roundInt(this._anchorY * _height);
        int w = Math.max(Math.abs(_width), 1);
        int h = Math.max(Math.abs(_height), 1);
        int x = _width >= 0 ? _x : _x - w;
        int y = _height >= 0 ? _y : _y - h;

        BufferedImage painting = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

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

    public void remove() {
        markedForDeletion = true;
    }

    protected boolean shouldRemove() {
        return markedForDeletion;
    }

    protected Rectangle2D.Double getBoundingRectangle() {
        double _width = this._width * this._scaleX;
        double _height = this._height * this._scaleY;
        double offsetX = this._anchorX * _width;
        double offsetY = this._anchorY * _height;
        double w = Math.max(Math.abs(_width), 1);
        double h = Math.max(Math.abs(_height), 1);
        double x = _width >= 0 ? _x : _x - w;
        double y = _height >= 0 ? _y : _y - h;
        return new Rectangle2D.Double(x - offsetX, y - offsetY, w, h);
    }

    public boolean hitTest(JevaClip other) {
        if (shouldRemove() || other.shouldRemove())
            return false;
        Rectangle2D.Double thisRect = getBoundingRectangle();
        Rectangle2D.Double otherRect = other.getBoundingRectangle();

        return thisRect.intersects(otherRect);
    }

    public boolean hitTest(Rectangle2D.Double targetRect) {
        if (shouldRemove())
            return false;
        Rectangle2D.Double thisRect = getBoundingRectangle();

        return thisRect.intersects(targetRect);
    }

    public boolean hitTest(int x, int y) {
        if (shouldRemove())
            return false;
        Rectangle2D.Double thisRect = getBoundingRectangle();
        return thisRect.contains(x, y);
    }

    public boolean hitTest(String _label) {
        if (shouldRemove())
            return false;
        Rectangle2D.Double thisRect = getBoundingRectangle();
        for (JevaClip jevaclip : core.jevaclipHierarchy.values()) {
            if ((jevaclip._label.equals(_label) || jevaclip._instanceName.equals(_label)) && jevaclip != this
                    && !jevaclip.shouldRemove()) {
                Rectangle2D.Double otherClipsRect = jevaclip.getBoundingRectangle();
                if (thisRect.intersects(otherClipsRect)) {
                    return true;
                }
            }
        }
        if (core.getCurrentScene() != null)
            for (JevaClip jevaclip : core.getCurrentScene().sceneclipHierarchy.values()) {
                if ((jevaclip._label.equals(_label) || jevaclip._instanceName.equals(_label)) && jevaclip != this
                        && !jevaclip.shouldRemove()) {
                    Rectangle2D.Double otherClipsRect = jevaclip.getBoundingRectangle();
                    if (thisRect.intersects(otherClipsRect)) {
                        return true;
                    }
                }
            }
        return false;
    }

    public double getAnchorX() {
        return _anchorX;
    }

    public void setAnchorX(double value) {
        _anchorX = Math.min(1, Math.max(0, value));
    }

    public double getAnchorY() {
        return _anchorY;
    }

    public void setAnchorY(double value) {
        _anchorY = Math.min(1, Math.max(0, value));
    }
}

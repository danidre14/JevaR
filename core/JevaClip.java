package core;

import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

public class JevaClip {
    private String _label;
    public double _x, _y, _width, _height;

    private boolean isLoaded;

    protected ArrayList<JevaScript> _onLoadScripts;
    protected ArrayList<JevaScript> _scriptsList;

    protected JevaClip(String _label, double _x, double _y, double _width, double _height) {
        this(_label, _x, _y, _width, _height, new ArrayList<>(Arrays.asList()));
    }

    protected JevaClip(String _label, double _x, double _y, double _width, double _height, JevaScript onLoad) {
        this(_label, _x, _y, _width, _height, new ArrayList<>(Arrays.asList(onLoad)));
    }

    protected JevaClip(String _label, double _x, double _y, double _width, double _height,
            ArrayList<JevaScript> onLoads) {
        this._label = _label;
        this._x = _x;
        this._y = _y;
        this._width = _width;
        this._height = _height;

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
        isLoaded = true;
    }

    // adding jobtives

    public void addJevascript(String _label) {
        JevaScript script = JevaR.jevascriptLibrary.get(_label);

        if (script == null)
            return;

        addJevascript(script);
    }

    public void addJevascript(JevaScript script) {
        _scriptsList.add(script);
    }

    protected void tick() {
        // run all attached scripts
        for (JevaScript script : _scriptsList) {
            script.call(this);
        }
    }

    protected void render(Graphics2D ctx) {
        int _x = JevaUtils.roundInt(this._x);
        int _y = JevaUtils.roundInt(this._y);
        int _width = JevaUtils.roundInt(this._width);
        int _height = JevaUtils.roundInt(this._height);
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

        ctx.drawImage(painting, x, y, null);
        pCtx.dispose();
    }

    private Rectangle2D.Double getBoundingRectangle() {
        return new Rectangle2D.Double(_x, _y, _width, _height);
    }

    public boolean hitTest(JevaClip other) {
        Rectangle2D.Double thisRect = getBoundingRectangle();
        Rectangle2D.Double otherRect = other.getBoundingRectangle();

        return thisRect.intersects(otherRect);
    }

    public boolean hitTest(Rectangle2D.Double targetRect) {
        Rectangle2D.Double thisRect = getBoundingRectangle();

        return thisRect.intersects(targetRect);
    }

    public boolean hitTest(int x, int y) {
        Rectangle2D.Double thisRect = getBoundingRectangle();
        return thisRect.contains(x, y);
    }

    public boolean hitTest(String _label) {
        Rectangle2D.Double thisRect = getBoundingRectangle();
        for (JevaClip jevaclip : JevaR.jevaclipHeirarchy.values()) {
            if (jevaclip._label.equals(_label) && jevaclip != this) {
                Rectangle2D.Double otherClipsRect = jevaclip.getBoundingRectangle();
                if (thisRect.intersects(otherClipsRect)) {
                    return true;
                }
            }
        }
        return false;
    }
}

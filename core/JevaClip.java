package core;

import java.util.*;
import java.awt.*;
import java.awt.geom.*;

public class JevaClip {
    private String _label;
    public double _x, _y, _width, _height;

    private enum appearances {
        painting, graphic, spritesheet
    }

    private appearances appearanceType;
    private Object appearanceSource;

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

        appearanceType = appearances.painting;
        appearanceSource = null;

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
    public void useGraphic(String _label) {
        JevaGraphic graphic = JevaR.jevagraphicLibrary.get(_label);

        if (graphic == null)
            return;

        Image source = graphic.getSource();

        appearanceType = appearances.graphic;
        appearanceSource = source;
    }

    public void useSpriteSheet(String _label) {
        JevaSpriteSheet source = JevaR.jevaspritesheetLibrary.get(_label);

        if (source == null)
            return;

        source.reset();

        appearanceType = appearances.spritesheet;
        appearanceSource = source;
        System.out.println(source);
        System.out.println("Use Sprite Sheet");
    }

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
        if (appearanceType == appearances.spritesheet) {
            JevaSpriteSheet spritesheet = (JevaSpriteSheet) appearanceSource;
            spritesheet.tick();
        }
    }

    protected void render(Graphics2D ctx) {
        int _x = JevaUtils.roundInt(this._x);
        int _y = JevaUtils.roundInt(this._y);
        int _width = JevaUtils.roundInt(this._width);
        int _height = JevaUtils.roundInt(this._height);

        if (appearanceType == appearances.graphic) {
            Image source = (Image) appearanceSource;
            ctx.drawImage(source, _x, _y, _width, _height, null);
        } else if (appearanceType == appearances.spritesheet) {
            Image source = ((JevaSpriteSheet) appearanceSource).getSource();
            ctx.drawImage(source, _x, _y, _width, _height, null);
        } else {
            Rectangle2D.Double body = new Rectangle2D.Double(_x, _y, _width, _height);
            Rectangle2D.Double rec1 = new Rectangle2D.Double(_x + (_width * 2 / 5), _y, (_width / 5), _height);
            Rectangle2D.Double rec2 = new Rectangle2D.Double(_x, _y + (_height * 2 / 5), _width, (_height / 5));

            ctx.setColor(Color.GRAY);
            ctx.fill(body);

            ctx.setColor(Color.DARK_GRAY);
            ctx.fill(rec1);
            ctx.fill(rec2);
        }
    }
}

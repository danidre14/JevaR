package core;

import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

public class JevaPrefab extends JevaClip {
    private enum appearances {
        painting, graphic, spritesheet
    }

    private appearances appearanceType;
    private Object appearanceSource;

    protected JevaPrefab(String _label, double _x, double _y, double _width, double _height) {
        this(_label, _x, _y, _width, _height, new ArrayList<>(Arrays.asList()));
    }

    protected JevaPrefab(String _label, double _x, double _y, double _width, double _height, JevaScript onLoad) {
        this(_label, _x, _y, _width, _height, new ArrayList<>(Arrays.asList(onLoad)));
    }

    protected JevaPrefab(String _label, double _x, double _y, double _width, double _height,
            ArrayList<JevaScript> onLoads) {
        super(_label, _x, _y, _width, _height, onLoads);

        appearanceType = appearances.painting;
        appearanceSource = null;
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
        int w = Math.max(Math.abs(_width), 1);
        int h = Math.max(Math.abs(_height), 1);
        int x = _width >= 0 ? _x : _x - w;
        int y = _height >= 0 ? _y : _y - h;

        if (appearanceType == appearances.graphic) {
            Image source = (Image) appearanceSource;
            ctx.drawImage(source, _x, _y, _width, _height, null);
        } else if (appearanceType == appearances.spritesheet) {
            Image source = ((JevaSpriteSheet) appearanceSource).getSource();
            ctx.drawImage(source, _x, _y, _width, _height, null);
        } else {
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
    }
}

package core;

import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

public class JevaPrefab extends JevaClip {
    private enum appearances {
        painting, graphic, spritesheet, stock
    }

    private String appearanceName;
    private appearances appearanceType;
    private Object appearanceSource;

    protected JevaPrefab(JevaR core, String _label, double _x, double _y, double _width, double _height) {
        this(core, _label, _x, _y, _width, _height, new ArrayList<>(Arrays.asList()));
    }

    protected JevaPrefab(JevaR core, String _label, double _x, double _y, double _width, double _height,
            JevaScript onLoad) {
        this(core, _label, _x, _y, _width, _height, new ArrayList<>(Arrays.asList(onLoad)));
    }

    protected JevaPrefab(JevaR core, String _label, double _x, double _y, double _width, double _height,
            ArrayList<JevaScript> onLoads) {
        super(core, _label, _x, _y, _width, _height, onLoads);

        appearanceName = "stock";
        appearanceType = appearances.stock;
        appearanceSource = null;
    }

    // adding jobtives
    public void usePainting(String _label) {
        if (appearanceName.equals(_label) && appearanceType == appearances.painting)
            return;
        JevaPainting painting = core.getJevaPainting(_label);

        if (painting == null)
            return;

        JevaPainting source = painting;

        appearanceName = _label;
        appearanceType = appearances.painting;
        appearanceSource = source;
    }

    public void usePainting(JevaPainting painting) {
        if (painting == null)
            return;

        JevaPainting source = painting;

        appearanceName = JevaUtils.generateUUID();
        appearanceType = appearances.painting;
        appearanceSource = source;
    }

    public void useGraphic(String _label) {
        if (appearanceName.equals(_label) && appearanceType == appearances.graphic)
            return;
        JevaGraphic graphic = core.getJevaGraphic(_label);

        if (graphic == null)
            return;

        Image source = graphic.getSource();

        appearanceName = _label;
        appearanceType = appearances.graphic;
        appearanceSource = source;
    }

    public void useSpriteSheet(String _label) {
        useSpriteSheet(_label, 0);
    }

    public void useSpriteSheet(String _label, int numLoops) {
        useSpriteSheet(_label, numLoops, null);
    }   
    public void useSpriteSheet(String _label, int numLoops, JevaFunction func) {
        if (appearanceName.equals(_label) && appearanceType == appearances.spritesheet)
            return;
        JevaSpriteSheet source = core.jevaspritesheetLibrary.get(_label);

        if (source == null)
            return;

        source.reset(numLoops, func);

        appearanceName = _label;
        appearanceType = appearances.spritesheet;
        appearanceSource = source;
    }

    protected void tick() {
        if (!isLoaded || shouldRemove())
            return;
        // run all attached scripts
        for (JevaScript script : _scriptsList) {
            script.call(this);
        }
        if (appearanceType == appearances.spritesheet) {
            JevaSpriteSheet spritesheet = (JevaSpriteSheet) appearanceSource;
            spritesheet.tick();
        }

        // updating all added jevaclips
        for (JevaClip jevaclip : clipsContainer.values()) {
            jevaclip.tick();
        }
    }

    @Override
    protected void render(Graphics2D ctx, JevaClipProps parentProps) {
        if (!isLoaded || shouldRemove() || !props._visible)
            return;
        JevaClipProps renderProps = props.clone();
        renderProps._x = parentProps._x + props._x;
        renderProps._y = parentProps._y + props._y;
        renderProps.setAlpha(parentProps._alpha * props._alpha);

        int _x = JevaUtils.roundInt(renderProps._x);
        int _y = JevaUtils.roundInt(renderProps._y);
        int _width = JevaUtils.roundInt(renderProps._width * renderProps._scaleX);
        int _height = JevaUtils.roundInt(renderProps._height * renderProps._scaleY);
        int offsetX = JevaUtils.roundInt(renderProps._anchorX * _width);
        int offsetY = JevaUtils.roundInt(renderProps._anchorY * _height);
        int w = Math.max(Math.abs(_width), 1);
        int h = Math.max(Math.abs(_height), 1);
        int x = _width >= 0 ? _x : _x - w;
        int y = _height >= 0 ? _y : _y - h;

        Composite old = ctx.getComposite();
        ctx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, renderProps._alpha));

        if (appearanceType == appearances.graphic) {
            Image source = (Image) appearanceSource;
            ctx.drawImage(source, _x - offsetX, _y - offsetY, _width, _height, null);
        } else if (appearanceType == appearances.spritesheet) {
            Image source = ((JevaSpriteSheet) appearanceSource).getSource();
            ctx.drawImage(source, _x - offsetX, _y - offsetY, _width, _height, null);
        } else {
            BufferedImage stock = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

            AffineTransform at = new AffineTransform();
            if (_width < 0) {
                at.scale(-1, 1);
                at.translate(_width, 0);
            }
            if (_height < 0) {
                at.scale(1, -1);
                at.translate(0, _height);
            }

            Graphics2D pCtx = (Graphics2D) stock.getGraphics();
            pCtx.transform(at);
            int px = 0;
            int py = 0;
            boolean errorPainting = false;
            if (appearanceType == appearances.painting) {
                try {
                    ((JevaPainting) appearanceSource).call(pCtx, px, py, w, h, state);
                } catch (Exception e) {
                    errorPainting = true;
                    e.printStackTrace();
                }
            }
            if (appearanceType != appearances.painting || errorPainting) {
                Rectangle2D.Double body = new Rectangle2D.Double(px, py, w, h);
                Rectangle2D.Double rec1 = new Rectangle2D.Double(px + (w * 3 / 5), py, (w / 5), h);
                Rectangle2D.Double rec2 = new Rectangle2D.Double(px, py + (h * 2 / 5), w, (h / 5));

                pCtx.setColor(Color.GRAY);
                pCtx.fill(body);

                pCtx.setColor(Color.DARK_GRAY);
                pCtx.fill(rec1);
                pCtx.fill(rec2);
            }

            ctx.drawImage(stock, x - offsetX, y - offsetY, null);
            pCtx.dispose();
        }

        // rendering all added jevaclips
        LinkedHashMap<String, JevaClip> tempClipHierarchy = JevaUtils.sortClipsByDepth(clipsContainer);
        for (JevaClip jevaclip : tempClipHierarchy.values()) {
            jevaclip.render(ctx, renderProps);
        }

        ctx.setComposite(old);

        if (core.isDebugMode()) {
            Rectangle2D.Double anchor = new Rectangle2D.Double(_x - 5, _y - 5, 10, 10);
            ctx.setColor(Color.BLUE);
            ctx.fill(anchor);
            ctx.setColor(Color.ORANGE);
            ctx.drawRect(x - offsetX, y - offsetY, w, h);
        }
    }
}

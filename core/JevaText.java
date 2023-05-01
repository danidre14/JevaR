package core;

import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

public class JevaText extends JevaClip {
    public JevaTextProps props;

    protected JevaText(JevaR core, String _label, String _text, double _x, double _y, double _width, double _height) {
        this(core, _label, _text, _x, _y, _width, _height, new ArrayList<>(Arrays.asList()));
    }

    protected JevaText(JevaR core, String _label, String _text, double _x, double _y, double _width, double _height,
            JevaScript onLoad) {
        this(core, _label, _text, _x, _y, _width, _height, new ArrayList<>(Arrays.asList(onLoad)));
    }

    protected JevaText(JevaR core, String _label, String _text, double _x, double _y, double _width, double _height,
            ArrayList<JevaScript> onLoads) {
        super(core, _label, _x, _y, _width, _height, onLoads);

        this.props = new JevaTextProps(_text, _x, _y, _width, _height);
    }

    @Override
    public JevaClip extend(String _label) {
        if (isLoaded)
            return this;

        JevaText superClip = (JevaText) core.jevaclipLibrary.get(_label);
        if (superClip == null)
            return this;

        for (JevaScript script : superClip._onLoadScripts) {
            script.call(this);
        }

        return this;
    }


    @Override
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

    protected void tick() {
        if (!isLoaded || shouldRemove())
            return;
        // run all attached scripts
        for (JevaScript script : _scriptsList) {
            script.call(this);
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

        JevaTextProps renderProps = props.clone();
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

        pCtx.setColor(props._backgroundColor);
        pCtx.fill(body);

        pCtx.setColor(props._fontColor);
        // pCtx.setFont(new Font(pCtx.getFont().getFontName(), Font.PLAIN, _height));
        pCtx.setFont(new Font(props._fontFamily, Font.PLAIN, (int) Math.max(8, props._fontSize)));
        for (String line : props._text.split("\n")) {
            double alignOffset = 0;
            if (!props.getAlign().equals("left")) {
                int lineWidth = pCtx.getFontMetrics().stringWidth(line);
                if (props.getAlign().equals("right"))
                    alignOffset = w - lineWidth;
                else if (props.getAlign().equals("center"))
                    alignOffset = (w - lineWidth) / 2;
            }
            pCtx.drawString(line, px + (int) alignOffset, py += props._lineHeight);
        }

        ctx.drawImage(painting, x - offsetX, y - offsetY, null);
        pCtx.dispose();

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

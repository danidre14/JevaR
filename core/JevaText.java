package core;

import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

public class JevaText extends JevaClip {
    public String _text;
    public Color _fontColor;
    public Color _backgroundColor;
    public int _fontSize;
    public String _fontFamily;

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

        this._text = _text;
        this._fontColor = Color.WHITE;
        this._backgroundColor = Color.DARK_GRAY;
        this._fontSize = (int) _height;
        this._fontFamily = "Arial";
    }

    // public String getFontFamily() {
    // return _fontFamily;
    // }

    // public Color getFontColor() {
    // return _fontColor;
    // }

    // public double getFontSize() {
    // return _fontSize;
    // }

    // public void setFontFamily(String _fontFamily) {
    // this._fontFamily = _fontFamily;
    // }

    // public void setFontColor(Color color) {
    // this._fontColor = color;
    // }

    // public void setFontSize(int size) {
    // if (size < 8)
    // size = 8;
    // this._fontSize = size;
    // }

    protected void tick() {
        if (!isLoaded || shouldRemove())
            return;
        // run all attached scripts
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

        pCtx.setColor(_backgroundColor);
        pCtx.fill(body);

        pCtx.setColor(_fontColor);
        // pCtx.setFont(new Font(pCtx.getFont().getFontName(), Font.PLAIN, _height));
        pCtx.setFont(new Font(_fontFamily, Font.PLAIN, (int) Math.max(8, _fontSize)));
        for (String line : _text.split("\n"))
            pCtx.drawString(line, px, py += _fontSize);

        ctx.drawImage(painting, x - offsetX, y - offsetY, null);
        pCtx.dispose();

        if (core.isDebugMode()) {
            Rectangle2D.Double anchor = new Rectangle2D.Double(_x - 5, _y - 5, 10, 10);
            ctx.setColor(Color.BLUE);
            ctx.fill(anchor);
            ctx.setColor(Color.ORANGE);
            ctx.drawRect(_x - JevaUtils.roundInt(this._anchorX * this._width),
                    _y - JevaUtils.roundInt(this._anchorY * this._height), (int) this._width, (int) this._height);
        }
    }
}

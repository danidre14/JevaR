package core;

import java.awt.Color;

public class JevaTextProps extends JevaClipProps {
    public String _text;
    public Color _fontColor;
    public Color _backgroundColor;
    public Color _borderColor;
    public String _fontFamily;
    public int _borderSize;

    protected double _lineHeight;
    protected int _fontSize;
    private String _align;

    protected JevaTextProps(String _text, double _x, double _y, double _width, double _height) {
        this(_text, _x, _y, _width, _height, 0, 0, 1, 1, 1, true, Color.WHITE, new Color(0, 0, 0, 0),
                new Color(0, 0, 0, 0), 0,
                (int) (_height * 0.8), "Arial",
                (_height * 0.8), "left");
    }

    private JevaTextProps(String _text, double _x, double _y, double _width, double _height, double _anchorX,
            double _anchorY, double _scaleX, double _scaleY, float _alpha, boolean _visible, Color _fontColor,
            Color _backgroundColor, Color _borderColor, int _borderSize, int _fontSize,
            String _fontFamily, double _lineHeight, String _align) {
        super(_x, _y, _width, _height, _anchorX, _anchorY, _scaleX, _scaleY, _alpha, _visible);

        this._text = _text;
        this._fontColor = _fontColor;
        this._backgroundColor = _backgroundColor;
        this._borderColor = _borderColor;
        this._fontFamily = _fontFamily;

        this._lineHeight = _lineHeight;
        this._fontSize = _fontSize;
        this._borderSize = _borderSize;
        setAlign(_align);
    }

    public String getAlign() {
        return _align;
    }

    public void setAlign(String align) {
        if (align.equals("left") || align.equals("l"))
            this._align = "left";
        else if (align.equals("center") || align.equals("c"))
            this._align = "center";
        else if (align.equals("right") || align.equals("r"))
            this._align = "right";
    }

    public double getLineHeight() {
        return _lineHeight;
    }

    public void setLineHeight(double height) {
        if (height < 8)
            height = 8;
        _lineHeight = height;
    }

    public int getFontSize() {
        return _fontSize;
    }

    public void setFontSize(int size) {
        if (size < 8)
            size = 8;
        int prevSize = Math.max(8, this._fontSize);

        this._fontSize = size;
        this._lineHeight = (this._lineHeight / prevSize) * size;
    }

    @Override
    protected JevaTextProps clone() {
        return new JevaTextProps(_text, _x, _y, _width, _height, _anchorX, _anchorY, _scaleX, _scaleY, _alpha, _visible,
                _fontColor, _backgroundColor, _borderColor, _borderSize, _fontSize, _fontFamily, _lineHeight, _align);
    }
}

package core;

public class JevaClipProps {
    public double _x, _y, _width, _height;
    protected double _anchorX, _anchorY;
    protected float _alpha;
    protected boolean _hovered;
    public double _scaleX, _scaleY;
    public boolean _visible;

    protected JevaClipProps(double _x, double _y, double _width, double _height, double _anchorX, double _anchorY,
            double _scaleX, double _scaleY, float _alpha, boolean _visible) {
        this._x = _x;
        this._y = _y;
        this._width = _width;
        this._height = _height;
        this.setAnchorX(_anchorX);
        this.setAnchorY(_anchorY);
        this.setAlpha(_alpha);
        this._scaleX = _scaleX;
        this._scaleY = _scaleY;
        this._hovered = false;
        this._visible = _visible;
    }

    protected JevaClipProps(double _x, double _y, double _width, double _height) {
        this(_x, _y, _width, _height, 0, 0, 1, 1, 1, true);
    }

    protected JevaClipProps clone() {
        return new JevaClipProps(_x, _y, _width, _height, _anchorX, _anchorY, _scaleX, _scaleY, _alpha, _visible);
    }

    public double getAnchorX() {
        return _anchorX;
    }

    public void setAnchorX(double value) {
        _anchorX = Math.min(1, Math.max(0, value));
    }

    public void shiftAnchorX(double value) {
        value = Math.min(1, Math.max(0, value));

        double xDiff = value - _anchorX;
        double xDiffOffset = (_width * _scaleX) * xDiff;
        _x += xDiffOffset;
        
        setAnchorX(value);
    }

    public void shiftAnchorY(double value) {
        value = Math.min(1, Math.max(0, value));

        double yDiff = value - _anchorY;
        double yDiffOffset = (_height * _scaleY) * yDiff;
        _y += yDiffOffset;

        setAnchorY(value);
    }

    public double getAnchorY() {
        return _anchorY;
    }

    public void setAnchorY(double value) {
        _anchorY = Math.min(1, Math.max(0, value));
    }

    public void centerAnchors() {
        shiftAnchorX(0.5);
        shiftAnchorY(0.5);
    }

    public float getAlpha() {
        return _alpha;
    }

    public void setAlpha(double value) {
        _alpha = (float) Math.min(1, Math.max(0, value));
    }

    public boolean isHovered() {
        return _hovered;
    }
}

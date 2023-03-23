package core;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

public class JevaVCam {
    public int _xmouse;
    public int _ymouse;
    private String _label;
    private JevaR core;
    // projection: the piece of the world being drawn
    public double _pX, _pY, _pWidth, _pHeight, _pScaleX, _pScaleY;
    private double _pAnchorX, _pAnchorY;
    // viewport: the position of the screen displaying drawing
    public double _vX, _vY, _vWidth, _vHeight, _vScaleX, _vScaleY;
    private double _vAnchorX, _vAnchorY;

    private int defaultStageWidth;
    private int defaultStageHeight;

    private BufferedImage vcamCanvas;

    protected JevaVCam(JevaR core, String _label, double _pX, double _pY, double _pWidth, double _pHeight) {
        this(core, _label, _pX, _pY, _pWidth, _pHeight, _pX, _pY, _pWidth, _pHeight);
    }

    protected JevaVCam(JevaR core, String _label, double _pX, double _pY, double _pWidth, double _pHeight, double _vX,
            double _vY, double _vWidth, double _vHeight) {
        this.core = core;
        this._label = _label;
        this._pX = _pX;
        this._pY = _pY;
        this._pWidth = _pWidth;
        this._pHeight = _pHeight;
        this._pAnchorX = 0;
        this._pAnchorY = 0;
        this._pScaleX = 1;
        this._pScaleY = 1;
        this._vX = _vX;
        this._vY = _vY;
        this._vWidth = _vWidth;
        this._vHeight = _vHeight;
        this._vAnchorX = 0;
        this._vAnchorY = 0;
        this._vScaleX = 1;
        this._vScaleY = 1;

        defaultStageWidth = core.screen.getWidth();
        defaultStageHeight = core.screen.getHeight();

        vcamCanvas = new BufferedImage(defaultStageWidth, defaultStageHeight, BufferedImage.TYPE_INT_RGB);
    }

    // adding jobtives
    protected void tick() {
    }

    private Graphics2D getClearViewport() {
        Graphics2D vcamCtx = vcamCanvas.createGraphics();
        // clear screen
        vcamCtx.setColor(core.screen.backgroundColor);
        vcamCtx.fillRect(0, 0, defaultStageWidth, defaultStageHeight);

        return vcamCtx;
    }

    private void setProjector(Graphics2D vcamCtx) {

        double projectorScaleW = defaultStageWidth / _pWidth;
        double projectorScaleH = defaultStageHeight / _pHeight;
        double projectorOffsetX = (defaultStageWidth) * (_pAnchorX / 1.0);
        double projectorOffsetY = (defaultStageHeight) * (_pAnchorY / 1.0);
        double projectorScaleX = 1 / _pScaleX;
        double projectorScaleY = 1 / _pScaleY;

        projectMatrix(vcamCtx,
                projectorOffsetX,
                projectorOffsetY,
                projectorScaleX * projectorScaleW,
                projectorScaleY * projectorScaleH,
                0,
                _pX,
                _pY);
    }

    private void displayViewport(Graphics2D ctx) {
        int _vX = JevaUtils.roundInt(this._vX);
        int _vY = JevaUtils.roundInt(this._vY);
        int _vWidth = JevaUtils.roundInt(this._vWidth * this._vScaleX);
        int _vHeight = JevaUtils.roundInt(this._vHeight * this._vScaleY);
        int vOffsetX = JevaUtils.roundInt(this._vAnchorX * _vWidth);
        int vOffsetY = JevaUtils.roundInt(this._vAnchorY * _vHeight);

        ctx.drawImage(vcamCanvas, _vX - vOffsetX, _vY - vOffsetY, _vWidth, _vHeight, null);
    }

    public void centerPAnchors() {
        double xDiff = 0.5 - _pAnchorX;
        double yDiff = 0.5 - _pAnchorY;

        double xDiffOffset = (_pWidth * _vScaleX) * xDiff;
        double yDiffOffset = (_pHeight * _vScaleY) * yDiff;

        _pX += xDiffOffset;
        _pY += yDiffOffset;

        setPAnchorX(0.5);
        setPAnchorY(0.5);
    }

    public void centerVAnchors() {
        double xDiff = 0.5 - _vAnchorX;
        double yDiff = 0.5 - _vAnchorY;

        double xDiffOffset = (_vWidth * _vScaleX) * xDiff;
        double yDiffOffset = (_vHeight * _vScaleY) * yDiff;

        _vX += xDiffOffset;
        _vY += yDiffOffset;

        setVAnchorX(0.5);
        setVAnchorY(0.5);
    }

    protected void render(Graphics2D ctx) {
        Graphics2D vcamCtx = getClearViewport();

        setProjector(vcamCtx);

        // render scene jevaclips
        if (core.getCurrentScene() != null)
            for (JevaClip jevaclip : core.getCurrentScene().sceneclipHierarchy.values()) {
                if (hitTest(jevaclip)) {
                    jevaclip.render(vcamCtx);
                }
            }
        // render attached jevaclips
        for (JevaClip jevaclip : core.jevaclipHierarchy.values()) {
            if (hitTest(jevaclip)) {
                jevaclip.render(vcamCtx);
            }
        }

        displayViewport(ctx);

        vcamCtx.dispose();
    }

    private void projectMatrix(Graphics2D ctx, double oX, double oY, double scaleX, double scaleY, double r, double x,
            double y) {
        double angle = -(r * Math.PI) / 180;
        double yAx = -Math.sin(angle);
        double yAy = Math.cos(angle);
        AffineTransform projector = new AffineTransform();

        double[] arr1 = { yAy * scaleX, -yAx * scaleX, yAx * scaleY, yAy * scaleY, oX, oY };
        double[] arr2 = { 1, 0, 0, 1, -x, -y };
        double[] arr3 = composeTransform(arr2, arr1);
        // projector.transform
        projector.setTransform(arr3[0], arr3[1], arr3[2], arr3[3], arr3[4], arr3[5]);
        ctx.transform(projector);
    }

    private double[] composeTransform(double[] arr1, double[] arr2) {
        double[] arr3 = new double[6];
        // components of arr1
        var a1 = arr1[0];
        var b1 = arr1[1];
        var c1 = arr1[2];
        var d1 = arr1[3];
        var e1 = arr1[4];
        var f1 = arr1[5];
        // components of arr2
        var a2 = arr2[0];
        var b2 = arr2[1];
        var c2 = arr2[2];
        var d2 = arr2[3];
        var e2 = arr2[4];
        var f2 = arr2[5];
        // components of the resulting array
        var a3 = (a2 * a1) + (c2 * b1);
        var b3 = (b2 * a1) + (d2 * b1);
        var c3 = (a2 * c1) + (c2 * d1);
        var d3 = (b2 * c1) + (d2 * d1);
        var e3 = (a2 * e1) + (c2 * f1) + e2;
        var f3 = (b2 * e1) + (d2 * f1) + f2;
        arr3[0] = a3;
        arr3[1] = b3;
        arr3[2] = c3;
        arr3[3] = d3;
        arr3[4] = e3;
        arr3[5] = f3;
        return arr3;
    }

    protected Rectangle2D.Double getProjectorBoundingRectangle() {
        double _width = this._pWidth * this._pScaleX;
        double _height = this._pHeight * this._pScaleY;
        double offsetX = this._pAnchorX * _width;
        double offsetY = this._pAnchorY * _height;
        double w = Math.max(Math.abs(_width), 1);
        double h = Math.max(Math.abs(_height), 1);
        double x = _width >= 0 ? _pX : _pX - w;
        double y = _height >= 0 ? _pY : _pY - h;
        return new Rectangle2D.Double(x - offsetX, y - offsetY, w, h);
    }

    protected Rectangle2D.Double getViewportBoundingRectangle() {
        double _width = this._vWidth * this._vScaleX;
        double _height = this._vHeight * this._vScaleY;
        double offsetX = this._vAnchorX * _width;
        double offsetY = this._vAnchorY * _height;
        double w = Math.max(Math.abs(_width), 1);
        double h = Math.max(Math.abs(_height), 1);
        double x = _width >= 0 ? _vX : _vX - w;
        double y = _height >= 0 ? _vY : _vY - h;
        return new Rectangle2D.Double(x - offsetX, y - offsetY, w, h);
    }

    protected boolean hitTest(JevaClip other) {
        Rectangle2D.Double thisRect = getProjectorBoundingRectangle();
        Rectangle2D.Double otherRect = other.getBoundingRectangle();

        return thisRect.intersects(otherRect);
    }

    protected boolean hitTest(Rectangle2D.Double targetRect) {
        Rectangle2D.Double thisRect = getProjectorBoundingRectangle();

        return thisRect.intersects(targetRect);
    }

    protected boolean hitTest(int x, int y) {
        Rectangle2D.Double thisRect = getProjectorBoundingRectangle();
        return thisRect.contains(x, y);
    }

    public double getPAnchorX() {
        return _pAnchorX;
    }

    public void setPAnchorX(double value) {
        _pAnchorX = Math.min(1, Math.max(0, value));
    }

    public double getPAnchorY() {
        return _pAnchorY;
    }

    public void setPAnchorY(double value) {
        _pAnchorY = Math.min(1, Math.max(0, value));
    }

    public double getVAnchorX() {
        return _vAnchorX;
    }

    public void setVAnchorX(double value) {
        _vAnchorX = Math.min(1, Math.max(0, value));
    }

    public double getVAnchorY() {
        return _vAnchorY;
    }

    public void setVAnchorY(double value) {
        _vAnchorY = Math.min(1, Math.max(0, value));
    }

    protected void setVCamMouseCoords(double _x, double _y) {
        double x = _x;
        double y = _y;

        double viewportWidth = this._vWidth * this._vScaleX;
        double viewportHeight = this._vHeight * this._vScaleY;

        double viewportOffsetX = viewportWidth * this._vAnchorX;
        double viewportOffsetY = viewportHeight * this._vAnchorY;

        double viewportScaleX = (viewportWidth / defaultStageWidth);
        if (viewportScaleX == 0)
            viewportScaleX = 1;

        double viewportScaleY = (viewportHeight / defaultStageHeight);
        if (viewportScaleY == 0)
            viewportScaleY = 1;

        x -= _vX - viewportOffsetX;
        y -= _vY - viewportOffsetY;

        x /= viewportScaleX;
        y /= viewportScaleY;

        double projectorOffsetX = (defaultStageWidth) * (_pAnchorX / 1.0);
        double projectorOffsetY = (defaultStageHeight) * (_pAnchorY / 1.0);
        x -= projectorOffsetX;
        y -= projectorOffsetY;

        double projectorScaleW = defaultStageWidth / _pWidth;
        double projectorScaleH = defaultStageHeight / _pHeight;

        double projectorScaleX = 1 / _pScaleX;
        double projectorScaleY = 1 / _pScaleY;
        x /= projectorScaleW * projectorScaleX;
        y /= projectorScaleH * projectorScaleY;

        x += _pX;
        y += _pY;

        _xmouse = (int) x;
        _ymouse = (int) y;
    }

    public String toString() {
        String properties = "";
        properties = properties.concat("Label: " + _label + "\n");

        properties = properties.concat("Projector: {\n");
        properties = properties.concat(" - x: " + _pX + "\n");
        properties = properties.concat(" - y: " + _pY + "\n");
        properties = properties.concat(" - w: " + _pWidth + "\n");
        properties = properties.concat(" - h: " + _pHeight + "\n");
        properties = properties.concat(" - anchor x: " + _pAnchorX + "\n");
        properties = properties.concat(" - anchor y: " + _pAnchorY + "\n");
        properties = properties.concat(" - scale x: " + _pScaleX + "\n");
        properties = properties.concat(" - scale y: " + _pScaleY + "\n");
        properties = properties.concat("}\n\n");

        properties = properties.concat("Viewport: {\n");
        properties = properties.concat(" - x: " + _vX + "\n");
        properties = properties.concat(" - y: " + _vY + "\n");
        properties = properties.concat(" - w: " + _vWidth + "\n");
        properties = properties.concat(" - h: " + _vHeight + "\n");
        properties = properties.concat(" - anchor x: " + _vAnchorX + "\n");
        properties = properties.concat(" - anchor y: " + _vAnchorY + "\n");
        properties = properties.concat(" - scale x: " + _vScaleX + "\n");
        properties = properties.concat(" - scale y: " + _vScaleY + "\n");
        properties = properties.concat("}\n\n");

        return properties;
    }
}

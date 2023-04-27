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
    public JevaClipProps projection;
    // viewport: the position of the screen displaying drawing
    public JevaClipProps viewport;

    private int defaultStageWidth;
    private int defaultStageHeight;

    public JevaState state;

    private BufferedImage vcamCanvas;

    protected JevaVCam(JevaR core, String _label, double _pX, double _pY, double _pWidth, double _pHeight) {
        this(core, _label, _pX, _pY, _pWidth, _pHeight, _pX, _pY, _pWidth, _pHeight);
    }

    protected JevaVCam(JevaR core, String _label, double _pX, double _pY, double _pWidth, double _pHeight, double _vX,
            double _vY, double _vWidth, double _vHeight) {
        this.core = core;
        this._label = _label;
        this.projection = new JevaClipProps(_pX, _pY, _pWidth, _pHeight);
        this.viewport = new JevaClipProps(_vX, _vY, _vWidth, _vHeight);

        defaultStageWidth = core.screen.getWidth();
        defaultStageHeight = core.screen.getHeight();

        state = new JevaState();

        vcamCanvas = new BufferedImage(defaultStageWidth, defaultStageHeight, BufferedImage.TYPE_INT_ARGB);
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

        double projectorScaleW = defaultStageWidth / projection._width;
        double projectorScaleH = defaultStageHeight / projection._height;
        double projectorOffsetX = (defaultStageWidth) * (projection._anchorX / 1.0);
        double projectorOffsetY = (defaultStageHeight) * (projection._anchorY / 1.0);
        double projectorScaleX = 1 / projection._scaleX;
        double projectorScaleY = 1 / projection._scaleY;

        projectMatrix(vcamCtx,
                projectorOffsetX,
                projectorOffsetY,
                projectorScaleX * projectorScaleW,
                projectorScaleY * projectorScaleH,
                0,
                projection._x,
                projection._y);
    }

    private void displayViewport(Graphics2D ctx) {
        int _vX = JevaUtils.roundInt(viewport._x);
        int _vY = JevaUtils.roundInt(viewport._y);
        int _vWidth = JevaUtils.roundInt(viewport._width * viewport._scaleX);
        int _vHeight = JevaUtils.roundInt(viewport._height * viewport._scaleY);
        int vOffsetX = JevaUtils.roundInt(viewport._anchorX * _vWidth);
        int vOffsetY = JevaUtils.roundInt(viewport._anchorY * _vHeight);

        ctx.drawImage(vcamCanvas, _vX - vOffsetX, _vY - vOffsetY, _vWidth, _vHeight, null);
    }

    public void centerPAnchors() {
        double xDiff = 0.5 - projection._anchorX;
        double yDiff = 0.5 - projection._anchorY;

        double xDiffOffset = (projection._width * viewport._scaleX) * xDiff;
        double yDiffOffset = (projection._height * viewport._scaleY) * yDiff;

        projection._x += xDiffOffset;
        projection._y += yDiffOffset;

        projection.setAnchorX(0.5);
        projection.setAnchorY(0.5);
    }

    public void centerVAnchors() {
        double xDiff = 0.5 - viewport._anchorX;
        double yDiff = 0.5 - viewport._anchorY;

        double xDiffOffset = (viewport._width * viewport._scaleX) * xDiff;
        double yDiffOffset = (viewport._height * viewport._scaleY) * yDiff;

        viewport._x += xDiffOffset;
        viewport._y += yDiffOffset;

        viewport.setAnchorX(0.5);
        viewport.setAnchorY(0.5);
    }

    protected void render(Graphics2D ctx, JevaClipProps parentProps) {
        Graphics2D vcamCtx = getClearViewport();

        setProjector(vcamCtx);

        // render scene jevaclips
        if (core.getCurrentScene() != null)
            for (JevaClip jevaclip : core.getCurrentScene().sceneclipHierarchy.values()) {
                if (hitTest(jevaclip)) {
                    if (jevaclip instanceof JevaTileMap)
                        ((JevaTileMap) jevaclip).render(vcamCtx, getProjectorBoundingRectangle());
                    else if (jevaclip instanceof JevaText)
                        ((JevaText) jevaclip).render(vcamCtx, parentProps);
                    else
                        jevaclip.render(vcamCtx, parentProps);
                }
            }
        // render attached jevaclips
        for (JevaClip jevaclip : core.jevaclipHierarchy.values()) {
            if (hitTest(jevaclip)) {
                if (jevaclip instanceof JevaTileMap)
                    ((JevaTileMap) jevaclip).render(vcamCtx, getProjectorBoundingRectangle());
                else if (jevaclip instanceof JevaText)
                    ((JevaText) jevaclip).render(vcamCtx, parentProps);
                else
                    jevaclip.render(vcamCtx, parentProps);
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
        double _width = projection._width * projection._scaleX;
        double _height = projection._height * projection._scaleY;
        double offsetX = projection._anchorX * _width;
        double offsetY = projection._anchorY * _height;
        double w = Math.max(Math.abs(_width), 1);
        double h = Math.max(Math.abs(_height), 1);
        double x = _width >= 0 ? projection._x : projection._x - w;
        double y = _height >= 0 ? projection._y : projection._y - h;
        return new Rectangle2D.Double(x - offsetX, y - offsetY, w, h);
    }

    protected Rectangle2D.Double getViewportBoundingRectangle() {
        double _width = viewport._width * viewport._scaleX;
        double _height = viewport._height * viewport._scaleY;
        double offsetX = viewport._anchorX * _width;
        double offsetY = viewport._anchorY * _height;
        double w = Math.max(Math.abs(_width), 1);
        double h = Math.max(Math.abs(_height), 1);
        double x = _width >= 0 ? viewport._x : viewport._x - w;
        double y = _height >= 0 ? viewport._y : viewport._y - h;
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

    protected void setVCamMouseCoords(double _x, double _y) {
        double x = _x;
        double y = _y;

        double viewportWidth = viewport._width * viewport._scaleX;
        double viewportHeight = viewport._height * viewport._scaleY;

        double viewportOffsetX = viewportWidth * viewport._anchorX;
        double viewportOffsetY = viewportHeight * viewport._anchorY;

        double viewportScaleX = (viewportWidth / defaultStageWidth);
        if (viewportScaleX == 0)
            viewportScaleX = 1;

        double viewportScaleY = (viewportHeight / defaultStageHeight);
        if (viewportScaleY == 0)
            viewportScaleY = 1;

        x -= viewport._x - viewportOffsetX;
        y -= viewport._y - viewportOffsetY;

        x /= viewportScaleX;
        y /= viewportScaleY;

        double projectorOffsetX = (defaultStageWidth) * (projection._anchorX / 1.0);
        double projectorOffsetY = (defaultStageHeight) * (projection._anchorY / 1.0);
        x -= projectorOffsetX;
        y -= projectorOffsetY;

        double projectorScaleW = defaultStageWidth / projection._width;
        double projectorScaleH = defaultStageHeight / projection._height;

        double projectorScaleX = 1 / projection._scaleX;
        double projectorScaleY = 1 / projection._scaleY;
        x /= projectorScaleW * projectorScaleX;
        y /= projectorScaleH * projectorScaleY;

        x += projection._x;
        y += projection._y;

        _xmouse = (int) x;
        _ymouse = (int) y;
    }

    public String toString() {
        String properties = "";
        properties = properties.concat("Label: " + _label + "\n");

        properties = properties.concat("Projector: {\n");
        properties = properties.concat(" - x: " + projection._x + "\n");
        properties = properties.concat(" - y: " + projection._y + "\n");
        properties = properties.concat(" - w: " + projection._width + "\n");
        properties = properties.concat(" - h: " + projection._height + "\n");
        properties = properties.concat(" - anchor x: " + projection._anchorX + "\n");
        properties = properties.concat(" - anchor y: " + projection._anchorY + "\n");
        properties = properties.concat(" - scale x: " + projection._scaleX + "\n");
        properties = properties.concat(" - scale y: " + projection._scaleY + "\n");
        properties = properties.concat("}\n\n");

        properties = properties.concat("Viewport: {\n");
        properties = properties.concat(" - x: " + viewport._x + "\n");
        properties = properties.concat(" - y: " + viewport._y + "\n");
        properties = properties.concat(" - w: " + viewport._width + "\n");
        properties = properties.concat(" - h: " + viewport._height + "\n");
        properties = properties.concat(" - anchor x: " + viewport._anchorX + "\n");
        properties = properties.concat(" - anchor y: " + viewport._anchorY + "\n");
        properties = properties.concat(" - scale x: " + viewport._scaleX + "\n");
        properties = properties.concat(" - scale y: " + viewport._scaleY + "\n");
        properties = properties.concat("}\n\n");

        return properties;
    }
}

package core;

import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

public class JevaVCam {
    private String _label;
    private JevaR parent;
    // projection: the piece of the world being drawn
    public double _pX, _pY, _pWidth, _pHeight, _pAnchorX, _pAnchorY, _pScaleX, _pScaleY;
    // viewport: the position of the screen displaying drawing
    public double _vX, _vY, _vWidth, _vHeight, _vAnchorX, _vAnchorY, _vScaleX, _vScaleY;

    private BufferedImage vcamCanvas;

    protected JevaVCam(JevaR parent, String _label, double _pX, double _pY, double _pWidth, double _pHeight) {
        this(parent, _label, _pX, _pY, _pWidth, _pHeight, 0, 0, parent.screen.getWidth(), parent.screen.getHeight());
    }

    protected JevaVCam(JevaR parent, String _label, double _pX, double _pY, double _pWidth, double _pHeight, double _vX,
            double _vY, double _vWidth, double _vHeight) {
        this.parent = parent;
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

        vcamCanvas = new BufferedImage((int) _vWidth, (int) _vHeight, BufferedImage.TYPE_INT_RGB);
    }

    // adding jobtives
    protected void tick() {
    }

    protected void render(Graphics2D ctx) {
        Graphics2D vcamCtx = vcamCanvas.createGraphics();
        // clear screen
        vcamCtx.setColor(parent.screen.backgroundColor);
        vcamCtx.fillRect(0, 0, parent.screen.getWidth(), parent.screen.getHeight());

        double pScaleX = parent.screen.getWidth() / this._pWidth * this._pScaleX;
        double pScaleY = parent.screen.getHeight() / this._pHeight * this._pScaleY;
        double pOffsetX = this._pAnchorX * this._pWidth * pScaleX;
        double pOffsetY = this._pAnchorY * this._pHeight * pScaleY;

        AffineTransform projector = new AffineTransform();
        projector.translate(-_pX + pOffsetX, -_pY + pOffsetY);
        projector.scale(pScaleX, pScaleY);
        vcamCtx.transform(projector);

        parent.jevaclipHeirarchy.forEach((key, jevaclip) -> {
            jevaclip.render(vcamCtx);
        });

        
        int _vX = JevaUtils.roundInt(this._vX);
        int _vY = JevaUtils.roundInt(this._vY);
        int _vWidth = JevaUtils.roundInt(this._vWidth * this._vScaleX);
        int _vHeight = JevaUtils.roundInt(this._vHeight * this._vScaleY);
        int vOffsetX = JevaUtils.roundInt(this._vAnchorX * _vWidth);
        int vOffsetY = JevaUtils.roundInt(this._vAnchorY * _vHeight);

        ctx.drawImage(vcamCanvas, _vX - vOffsetX, _vY - vOffsetY, _vWidth, _vHeight, null);

        vcamCtx.dispose();
    }


    private Rectangle2D.Double getBoundingRectangle() {
        return new Rectangle2D.Double(_pX, _pY, _pWidth, _pHeight);
    }

    // public boolean hitTest(JevaClip other) {
    // Rectangle2D.Double thisRect = getBoundingRectangle();
    // Rectangle2D.Double otherRect = other.getBoundingRectangle();

    // return thisRect.intersects(otherRect);
    // }

    public boolean hitTest(Rectangle2D.Double targetRect) {
        Rectangle2D.Double thisRect = getBoundingRectangle();

        return thisRect.intersects(targetRect);
    }

    public boolean hitTest(int x, int y) {
        Rectangle2D.Double thisRect = getBoundingRectangle();
        return thisRect.contains(x, y);
    }

    // public boolean hitTest(String _label) {
    // Rectangle2D.Double thisRect = getBoundingRectangle();
    // for (JevaClip jevaclip : parent.jevaclipHeirarchy.values()) {
    // if (jevaclip._label.equals(_label) && jevaclip != this) {
    // Rectangle2D.Double otherClipsRect = jevaclip.getBoundingRectangle();
    // if (thisRect.intersects(otherClipsRect)) {
    // return true;
    // }
    // }
    // }
    // return false;
    // }

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
}

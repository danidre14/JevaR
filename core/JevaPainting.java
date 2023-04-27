package core;

import java.awt.Graphics2D;

public interface JevaPainting {
    void call(Graphics2D context, double x, double y, double w, double h, JevaState state);
}

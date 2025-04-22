package org.delta.shapes;
import org.delta.raster.CustomRaster;

import java.awt.Color;
import java.awt.Point;

public interface Shape {
    void draw(CustomRaster raster);
    boolean contains(Point p);
    void move(int dx, int dy);
    void setEndPoint(Point p);
    boolean canBeFilled();
    void setFilled(boolean filled);
    void setFillColor(Color color);
    Point getNearestControlPoint(Point p);
    void resizeByPoint(Point controlPoint, int dx, int dy);
    void drawControlPoints(CustomRaster raster);
}
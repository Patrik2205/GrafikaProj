package org.delta.shapes;
import org.delta.raster.CustomRaster;

import java.awt.Color;
import java.awt.Point;

public class LineShape implements Shape {
    private Point p1;
    private Point p2;
    private Color color;
    private int thickness;
    private int style;

    public LineShape(Point p1, Point p2, Color color, int thickness, int style) {
        this.p1 = p1;
        this.p2 = p2;
        this.color = color;
        this.thickness = thickness;
        this.style = style;
    }

    @Override
    public void draw(CustomRaster raster) {
        raster.drawLine(p1.x, p1.y, p2.x, p2.y, color, style, thickness);
    }

    @Override
    public boolean contains(Point p) {
        // Calculate distance from point to line segment
        double lineLength = distance(p1, p2);
        if (lineLength == 0) return distance(p, p1) <= 5;

        double t = ((p.x - p1.x) * (p2.x - p1.x) + (p.y - p1.y) * (p2.y - p1.y)) / (lineLength * lineLength);
        t = Math.max(0, Math.min(1, t));

        double nearestX = p1.x + t * (p2.x - p1.x);
        double nearestY = p1.y + t * (p2.y - p1.y);

        return distance(p, new Point((int)nearestX, (int)nearestY)) <= 5 + thickness;
    }

    private double distance(Point p1, Point p2) {
        return Math.sqrt((p2.x - p1.x) * (p2.x - p1.x) + (p2.y - p1.y) * (p2.y - p1.y));
    }

    @Override
    public void move(int dx, int dy) {
        p1.x += dx;
        p1.y += dy;
        p2.x += dx;
        p2.y += dy;
    }

    @Override
    public void setEndPoint(Point p) {
        p2 = p;
    }

    @Override
    public boolean canBeFilled() {
        return false;
    }

    @Override
    public void setFilled(boolean filled) {
        // Lines cannot be filled
    }

    @Override
    public void setFillColor(Color color) {
        // Lines cannot be filled
    }

    @Override
    public Point getNearestControlPoint(Point p) {
        double dist1 = distance(p, p1);
        double dist2 = distance(p, p2);

        if (dist1 <= 10) return p1;
        if (dist2 <= 10) return p2;
        return null;
    }

    @Override
    public void resizeByPoint(Point controlPoint, int dx, int dy) {
        if (controlPoint.equals(p1)) {
            p1.x += dx;
            p1.y += dy;
        } else if (controlPoint.equals(p2)) {
            p2.x += dx;
            p2.y += dy;
        }
    }

    @Override
    public void drawControlPoints(CustomRaster raster) {
        raster.drawControlPoint(p1.x, p1.y);
        raster.drawControlPoint(p2.x, p2.y);
    }
}
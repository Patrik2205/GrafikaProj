package org.delta.shapes;
import org.delta.raster.CustomRaster;

import java.awt.Color;
import java.awt.Point;

public class CircleShape implements Shape {
    private Point center;
    private Point radiusPoint;
    private Color color;
    private Color fillColor;
    private int thickness;
    private int style;
    private boolean filled;

    public CircleShape(Point center, Point radiusPoint, Color color, int thickness, int style) {
        this.center = center;
        this.radiusPoint = radiusPoint;
        this.color = color;
        this.thickness = thickness;
        this.style = style;
        this.filled = false;
    }

    @Override
    public void draw(CustomRaster raster) {
        int radius = calculateRadius();

        raster.drawCircle(center.x, center.y, radius, color, style, thickness, filled);

        if (filled) {
            raster.fillCircle(center.x, center.y, radius, fillColor);
        }
    }

    private int calculateRadius() {
        return (int) Math.sqrt(
                Math.pow(radiusPoint.x - center.x, 2) +
                        Math.pow(radiusPoint.y - center.y, 2)
        );
    }

    @Override
    public boolean contains(Point p) {
        int radius = calculateRadius();

        double distance = Math.sqrt(
                Math.pow(p.x - center.x, 2) +
                        Math.pow(p.y - center.y, 2)
        );

        if (filled) {
            return distance <= radius;
        } else {
            return Math.abs(distance - radius) <= 5 + thickness;
        }
    }

    @Override
    public void move(int dx, int dy) {
        center.x += dx;
        center.y += dy;
        radiusPoint.x += dx;
        radiusPoint.y += dy;
    }

    @Override
    public void setEndPoint(Point p) {
        radiusPoint = p;
    }

    @Override
    public boolean canBeFilled() {
        return true;
    }

    @Override
    public void setFilled(boolean filled) {
        this.filled = filled;
    }

    @Override
    public void setFillColor(Color color) {
        this.fillColor = color;
    }

    @Override
    public Point getNearestControlPoint(Point p) {
        // Only two control points: center and radius point
        double distToCenter = distance(p, center);
        double distToRadius = distance(p, radiusPoint);

        if (distToCenter <= 10) {
            return center;
        } else if (distToRadius <= 10) {
            return radiusPoint;
        }

        return null;
    }

    // Check if a point is the radius control point
    public boolean isRadiusPoint(Point p) {
        return distance(p, radiusPoint) <= 10;
    }

    // Check if a point is the center control point
    public boolean isCenterPoint(Point p) {
        return distance(p, center) <= 10;
    }

    // Calculate distance between two points
    private double distance(Point p1, Point p2) {
        return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }

    // Move the radius point (adjusts the circle size)
    public void moveRadiusPoint(int dx, int dy) {
        radiusPoint.x += dx;
        radiusPoint.y += dy;
    }

    @Override
    public void resizeByPoint(Point controlPoint, int dx, int dy) {
        // We only scale the circle if the radius point is selected
        if (isRadiusPoint(controlPoint)) {
            radiusPoint.x += dx;
            radiusPoint.y += dy;
        }
        // If center point is selected, we move the whole circle
        else if (isCenterPoint(controlPoint)) {
            move(dx, dy);
        }
    }

    @Override
    public void drawControlPoints(CustomRaster raster) {
        // Draw only center and radius control points
        raster.drawControlPoint(center.x, center.y);
        raster.drawControlPoint(radiusPoint.x, radiusPoint.y);
    }
}
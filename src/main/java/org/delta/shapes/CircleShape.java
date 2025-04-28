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
        int radius = (int) Math.sqrt(
                Math.pow(radiusPoint.x - center.x, 2) +
                        Math.pow(radiusPoint.y - center.y, 2)
        );

        raster.drawCircle(center.x, center.y, radius, color, style, thickness, filled);

        if (filled) {
            raster.fillCircle(center.x, center.y, radius, fillColor);
        }
    }

    @Override
    public boolean contains(Point p) {
        int radius = (int) Math.sqrt(
                Math.pow(radiusPoint.x - center.x, 2) +
                        Math.pow(radiusPoint.y - center.y, 2)
        );

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
        Point[] controlPoints = {
                new Point(center.x, center.y),            // Center
                new Point(radiusPoint.x, radiusPoint.y)   // Radius point
        };

        Point nearest = null;
        double minDist = 10; // Threshold

        for (Point cp : controlPoints) {
            double dist = Math.sqrt(Math.pow(p.x - cp.x, 2) + Math.pow(p.y - cp.y, 2));
            if (dist < minDist) {
                minDist = dist;
                nearest = cp;
            }
        }

        return nearest;
    }

    // Check if a point is the radius control point
    public boolean isRadiusPoint(Point p) {
        double dist = Math.sqrt(Math.pow(p.x - radiusPoint.x, 2) + Math.pow(p.y - radiusPoint.y, 2));
        return dist < 10; // Using 10px threshold for matching
    }

    // Move just the radius point (for right-drag manipulation)
    public void moveRadiusPoint(int dx, int dy) {
        radiusPoint.x += dx;
        radiusPoint.y += dy;
    }

    @Override
    public void resizeByPoint(Point controlPoint, int dx, int dy) {
        if (isPointNear(controlPoint, center, 10)) {
            // Move center
            center.x += dx;
            center.y += dy;
            radiusPoint.x += dx;
            radiusPoint.y += dy;
        } else if (isPointNear(controlPoint, radiusPoint, 10)) {
            // Resize by moving radius point
            radiusPoint.x += dx;
            radiusPoint.y += dy;
        }
    }

    // Helper method to check if two points are near each other
    private boolean isPointNear(Point p1, Point p2, int threshold) {
        double dist = Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
        return dist <= threshold;
    }

    @Override
    public void drawControlPoints(CustomRaster raster) {
        // Draw only center and radius control points
        raster.drawControlPoint(center.x, center.y);
        raster.drawControlPoint(radiusPoint.x, radiusPoint.y);
    }
}
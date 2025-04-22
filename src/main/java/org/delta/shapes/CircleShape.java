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
        int radius = (int) Math.sqrt(
                Math.pow(radiusPoint.x - center.x, 2) +
                        Math.pow(radiusPoint.y - center.y, 2)
        );

        // Define control points (center and 4 edge points)
        Point[] controlPoints = {
                new Point(center.x, center.y),            // Center
                new Point(center.x + radius, center.y),   // Right
                new Point(center.x - radius, center.y),   // Left
                new Point(center.x, center.y + radius),   // Bottom
                new Point(center.x, center.y - radius)    // Top
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

    @Override
    public void resizeByPoint(Point controlPoint, int dx, int dy) {
        int radius = (int) Math.sqrt(
                Math.pow(radiusPoint.x - center.x, 2) +
                        Math.pow(radiusPoint.y - center.y, 2)
        );

        if (controlPoint.x == center.x && controlPoint.y == center.y) {
            // Moving center
            center.x += dx;
            center.y += dy;
            radiusPoint.x += dx;
            radiusPoint.y += dy;
        } else {
            // Resizing by edge point
            int newX = controlPoint.x + dx;
            int newY = controlPoint.y + dy;

            // Calculate new radius based on the moved point
            radiusPoint = new Point(newX, newY);
        }
    }

    @Override
    public void drawControlPoints(CustomRaster raster) {
        // Draw center control point
        raster.drawControlPoint(center.x, center.y);

        // Draw edge control points
        int radius = (int) Math.sqrt(
                Math.pow(radiusPoint.x - center.x, 2) +
                        Math.pow(radiusPoint.y - center.y, 2)
        );

        raster.drawControlPoint(center.x + radius, center.y);   // Right
        raster.drawControlPoint(center.x - radius, center.y);   // Left
        raster.drawControlPoint(center.x, center.y + radius);   // Bottom
        raster.drawControlPoint(center.x, center.y - radius);   // Top
    }
}
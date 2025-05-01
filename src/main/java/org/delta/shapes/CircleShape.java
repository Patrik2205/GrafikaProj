package org.delta.shapes;
import org.delta.raster.CustomRaster;

import java.awt.Color;
import java.awt.Point;

/**
 * Represents a circle defined by a center point and a radius point.
 * The distance between these two points determines the circle's radius.
 */
public class CircleShape implements Shape {
    private Point center;
    private Point radiusPoint;
    private Color color;
    private Color fillColor;
    private int thickness;
    private int style;
    private boolean filled;

    /**
     * Creates a new circle with the specified parameters
     * @param center Center point of the circle
     * @param radiusPoint Point on the circumference
     * @param color Line color
     * @param thickness Line thickness
     * @param style Line style (solid, dashed, dotted)
     */
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

        // Draw the circle with specified parameters
        raster.drawCircle(center.x, center.y, radius, color, style, thickness, filled);

        // Fill the circle if needed
        if (filled) {
            raster.fillCircle(center.x, center.y, radius, fillColor);
        }
    }

    /**
     * Calculates the radius based on the distance between center and radius points
     * @return The radius in pixels
     */
    private int calculateRadius() {
        return (int) Math.sqrt(
                Math.pow(radiusPoint.x - center.x, 2) +
                        Math.pow(radiusPoint.y - center.y, 2)
        );
    }

    @Override
    public boolean contains(Point p) {
        int radius = calculateRadius();

        // Calculate distance from point to center
        double distance = Math.sqrt(
                Math.pow(p.x - center.x, 2) +
                        Math.pow(p.y - center.y, 2)
        );

        if (filled) {
            // For filled circles, check if the point is inside
            return distance <= radius;
        } else {
            // For outlines, check if the point is near the circumference
            return Math.abs(distance - radius) <= 5 + thickness;
        }
    }

    @Override
    public void move(int dx, int dy) {
        // Move both center and radius point
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

    /**
     * Checks if a point is the radius control point
     */
    public boolean isRadiusPoint(Point p) {
        return distance(p, radiusPoint) <= 10;
    }

    /**
     * Checks if a point is the center control point
     */
    public boolean isCenterPoint(Point p) {
        return distance(p, center) <= 10;
    }

    /**
     * Calculate distance between two points
     */
    private double distance(Point p1, Point p2) {
        return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }

    /**
     * Move the radius point (adjusts the circle size)
     */
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
package org.delta.shapes;
import org.delta.raster.CustomRaster;

import java.awt.Color;
import java.awt.Point;

/**
 * FloodFillShape implements the flood fill algorithm as a shape.
 * Unlike other shapes, FloodFillShape doesn't have a persistent visual representation
 * but rather modifies the raster directly when drawn.
 */
public class FloodFillShape implements Shape {
    private Point seedPoint;
    private Color fillColor;
    private int targetColor; // The color being replaced

    /**
     * Creates a new flood fill shape with the specified seed point and color
     * @param seedPoint Starting point for the flood fill
     * @param fillColor Color to fill with
     */
    public FloodFillShape(Point seedPoint, Color fillColor) {
        this.seedPoint = seedPoint;
        this.fillColor = fillColor;
        this.targetColor = -1; // Will be determined when drawn
    }

    @Override
    public void draw(CustomRaster raster) {
        // The flood fill shape performs its action (filling) when drawn
        // If targetColor wasn't determined yet, get it from the raster
        if (targetColor == -1 && seedPoint != null) {
            targetColor = raster.getPixel(seedPoint.x, seedPoint.y);
        }

        // Only fill if the target color is different from the fill color
        // and if the seed point is within the raster bounds
        if (targetColor != fillColor.getRGB() &&
                seedPoint.x >= 0 && seedPoint.x < raster.getWidth() &&
                seedPoint.y >= 0 && seedPoint.y < raster.getHeight()) {
            raster.floodFill(seedPoint.x, seedPoint.y, fillColor);
        }
    }

    @Override
    public boolean contains(Point p) {
        // Flood fill shapes don't have a persistent representation
        // so they can't contain points after being drawn
        return false;
    }

    @Override
    public void move(int dx, int dy) {
        // Move the seed point
        if (seedPoint != null) {
            seedPoint.x += dx;
            seedPoint.y += dy;
        }
    }

    @Override
    public void setEndPoint(Point p) {
        // Not applicable for flood fill
    }

    @Override
    public boolean canBeFilled() {
        // Flood fill shapes are themselves filling operations
        return false;
    }

    @Override
    public void setFilled(boolean filled) {
        // Not applicable for flood fill
    }

    @Override
    public void setFillColor(Color color) {
        this.fillColor = color;
    }

    @Override
    public Point getNearestControlPoint(Point p) {
        // Flood fill shapes only have one control point (the seed)
        return (seedPoint != null && distance(p, seedPoint) <= 10) ? seedPoint : null;
    }

    @Override
    public void resizeByPoint(Point controlPoint, int dx, int dy) {
        // Can only move the seed point
        if (seedPoint != null && controlPoint.equals(seedPoint)) {
            seedPoint.x += dx;
            seedPoint.y += dy;
        }
    }

    @Override
    public void drawControlPoints(CustomRaster raster) {
        // Draw the seed point as a control point
        if (seedPoint != null) {
            raster.drawControlPoint(seedPoint.x, seedPoint.y);
        }
    }

    /**
     * Calculate distance between two points
     */
    private double distance(Point p1, Point p2) {
        return Math.sqrt((p2.x - p1.x) * (p2.x - p1.x) + (p2.y - p1.y) * (p2.y - p1.y));
    }
}
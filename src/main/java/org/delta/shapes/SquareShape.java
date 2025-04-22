package org.delta.shapes;
import org.delta.raster.CustomRaster;

import java.awt.Color;
import java.awt.Point;

public class SquareShape implements Shape {
    private Point p1;
    private Point p2;
    private Color color;
    private Color fillColor;
    private int thickness;
    private int style;
    private boolean filled;

    public SquareShape(Point p1, Point p2, Color color, int thickness, int style) {
        this.p1 = p1;
        this.p2 = p2;
        this.color = color;
        this.thickness = thickness;
        this.style = style;
        this.filled = false;
    }

    @Override
    public void draw(CustomRaster raster) {
        int left = Math.min(p1.x, p2.x);
        int top = Math.min(p1.y, p2.y);
        int size = Math.max(Math.abs(p2.x - p1.x), Math.abs(p2.y - p1.y));

        int right = p1.x < p2.x ? left + size : left - size;
        int bottom = p1.y < p2.y ? top + size : top - size;

        left = Math.min(left, right);
        top = Math.min(top, bottom);
        right = Math.max(left, right);
        bottom = Math.max(top, bottom);

        raster.drawRectangle(left, top, right, bottom, color, style, thickness, filled);

        if (filled) {
            raster.fillRectangle(left, top, right, bottom, fillColor);
        }
    }

    @Override
    public boolean contains(Point p) {
        int left = Math.min(p1.x, p2.x);
        int top = Math.min(p1.y, p2.y);
        int size = Math.max(Math.abs(p2.x - p1.x), Math.abs(p2.y - p1.y));

        int right = p1.x < p2.x ? left + size : left - size;
        int bottom = p1.y < p2.y ? top + size : top - size;

        left = Math.min(left, right);
        top = Math.min(top, bottom);
        right = Math.max(left, right);
        bottom = Math.max(top, bottom);

        // Check if point is near the edges or inside (if filled)
        if (filled) {
            return p.x >= left && p.x <= right && p.y >= top && p.y <= bottom;
        } else {
            boolean nearLeftEdge = Math.abs(p.x - left) <= 5 && p.y >= top && p.y <= bottom;
            boolean nearRightEdge = Math.abs(p.x - right) <= 5 && p.y >= top && p.y <= bottom;
            boolean nearTopEdge = Math.abs(p.y - top) <= 5 && p.x >= left && p.x <= right;
            boolean nearBottomEdge = Math.abs(p.y - bottom) <= 5 && p.x >= left && p.x <= right;

            return nearLeftEdge || nearRightEdge || nearTopEdge || nearBottomEdge;
        }
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
        // Make a square instead of a rectangle
        int width = Math.abs(p.x - p1.x);
        int height = Math.abs(p.y - p1.y);
        int size = Math.max(width, height);

        int newX = p.x > p1.x ? p1.x + size : p1.x - size;
        int newY = p.y > p1.y ? p1.y + size : p1.y - size;

        p2 = new Point(newX, newY);
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
        int left = Math.min(p1.x, p2.x);
        int top = Math.min(p1.y, p2.y);
        int size = Math.max(Math.abs(p2.x - p1.x), Math.abs(p2.y - p1.y));

        int right = left + size;
        int bottom = top + size;

        if (p1.x > p2.x) {
            int temp = left;
            left = right - size;
            right = temp + size;
        }

        if (p1.y > p2.y) {
            int temp = top;
            top = bottom - size;
            bottom = temp + size;
        }

        Point[] controlPoints = {
                new Point(left, top),        // Top-left
                new Point(right, top),       // Top-right
                new Point(left, bottom),     // Bottom-left
                new Point(right, bottom),    // Bottom-right
                new Point((left + right) / 2, top),       // Top-middle
                new Point((left + right) / 2, bottom),    // Bottom-middle
                new Point(left, (top + bottom) / 2),      // Left-middle
                new Point(right, (top + bottom) / 2)      // Right-middle
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
        int left = Math.min(p1.x, p2.x);
        int top = Math.min(p1.y, p2.y);
        int size = Math.max(Math.abs(p2.x - p1.x), Math.abs(p2.y - p1.y));

        int right = left + size;
        int bottom = top + size;

        if (p1.x > p2.x) {
            int temp = left;
            left = right - size;
            right = temp + size;
        }

        if (p1.y > p2.y) {
            int temp = top;
            top = bottom - size;
            bottom = temp + size;
        }

        // Handle different control points for resizing (keeping square aspect ratio)
        if (controlPoint.x == left && controlPoint.y == top) {
            // Top-left
            int max = Math.max(Math.abs(dx), Math.abs(dy));
            int signX = dx < 0 ? -1 : 1;
            int signY = dy < 0 ? -1 : 1;

            dx = max * signX;
            dy = max * signY;

            if (p1.x == left) p1.x += dx; else p2.x += dx;
            if (p1.y == top) p1.y += dy; else p2.y += dy;
        }
        else if (controlPoint.x == right && controlPoint.y == top) {
            // Top-right
            int max = Math.max(Math.abs(dx), Math.abs(dy));
            int signX = dx < 0 ? -1 : 1;
            int signY = dy < 0 ? -1 : 1;

            dx = max * signX;
            dy = max * signY;

            if (p1.x == right) p1.x += dx; else p2.x += dx;
            if (p1.y == top) p1.y += dy; else p2.y += dy;
        }
        else if (controlPoint.x == left && controlPoint.y == bottom) {
            // Bottom-left
            int max = Math.max(Math.abs(dx), Math.abs(dy));
            int signX = dx < 0 ? -1 : 1;
            int signY = dy < 0 ? -1 : 1;

            dx = max * signX;
            dy = max * signY;

            if (p1.x == left) p1.x += dx; else p2.x += dx;
            if (p1.y == bottom) p1.y += dy; else p2.y += dy;
        }
        else if (controlPoint.x == right && controlPoint.y == bottom) {
            // Bottom-right
            int max = Math.max(Math.abs(dx), Math.abs(dy));
            int signX = dx < 0 ? -1 : 1;
            int signY = dy < 0 ? -1 : 1;

            dx = max * signX;
            dy = max * signY;

            if (p1.x == right) p1.x += dx; else p2.x += dx;
            if (p1.y == bottom) p1.y += dy; else p2.y += dy;
        }
        // For middle points, adjust both corners to maintain square shape
    }

    @Override
    public void drawControlPoints(CustomRaster raster) {
        int left = Math.min(p1.x, p2.x);
        int top = Math.min(p1.y, p2.y);
        int size = Math.max(Math.abs(p2.x - p1.x), Math.abs(p2.y - p1.y));

        int right = p1.x < p2.x ? left + size : left - size;
        int bottom = p1.y < p2.y ? top + size : top - size;

        left = Math.min(left, right);
        top = Math.min(top, bottom);
        right = Math.max(left, right);
        bottom = Math.max(top, bottom);

        // Draw corner control points
        raster.drawControlPoint(left, top);           // Top-left
        raster.drawControlPoint(right, top);          // Top-right
        raster.drawControlPoint(left, bottom);        // Bottom-left
        raster.drawControlPoint(right, bottom);       // Bottom-right

        // Draw middle edge control points
        raster.drawControlPoint((left + right) / 2, top);      // Top-middle
        raster.drawControlPoint((left + right) / 2, bottom);   // Bottom-middle
        raster.drawControlPoint(left, (top + bottom) / 2);     // Left-middle
        raster.drawControlPoint(right, (top + bottom) / 2);    // Right-middle
    }
}
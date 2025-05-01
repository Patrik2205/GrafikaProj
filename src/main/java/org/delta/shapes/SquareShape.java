package org.delta.shapes;
import org.delta.raster.CustomRaster;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a square (a special case of rectangle with equal sides).
 * Provides specialized functionality to maintain equal width and height.
 */
public class SquareShape implements Shape {
    private List<Point> cornerPoints;
    private Point anchorPoint;  // The starting point that remains fixed during drawing
    private Color color;
    private Color fillColor;
    private int thickness;
    private int style;
    private boolean filled;

    // Constants for edge identification (same as Rectangle)
    public static final int NO_EDGE = -1;
    public static final int TOP_EDGE = 0;
    public static final int RIGHT_EDGE = 1;
    public static final int BOTTOM_EDGE = 2;
    public static final int LEFT_EDGE = 3;

    /**
     * Creates a new square with the specified parameters
     * @param p1 First corner point (anchor)
     * @param p2 Point used to determine the size and orientation
     * @param color Outline color
     * @param thickness Line thickness
     * @param style Line style (solid, dashed, dotted)
     */
    public SquareShape(Point p1, Point p2, Color color, int thickness, int style) {
        this.anchorPoint = new Point(p1);
        this.cornerPoints = createSquareCorners(p1, p2);
        this.color = color;
        this.thickness = thickness;
        this.style = style;
        this.filled = false;
    }

    /**
     * Creates square corner points based on two points
     * Uses the maximum of width or height to ensure equal sides
     *
     * @param p1 First corner point (anchor)
     * @param p2 Point used to determine the size and orientation
     * @return List of four corner points in clockwise order
     */
    private List<Point> createSquareCorners(Point p1, Point p2) {
        List<Point> points = new ArrayList<>(4);

        // Calculate the side length based on the maximum difference
        int size = Math.max(Math.abs(p2.x - p1.x), Math.abs(p2.y - p1.y));

        // Determine direction for the square
        int dx = (p2.x >= p1.x) ? size : -size;
        int dy = (p2.y >= p1.y) ? size : -size;

        // Create the four corners
        points.add(new Point(p1.x, p1.y));                 // Anchor corner (0)
        points.add(new Point(p1.x + dx, p1.y));            // Corner 2 (1)
        points.add(new Point(p1.x + dx, p1.y + dy));       // Corner 3 (2) (opposite to anchor)
        points.add(new Point(p1.x, p1.y + dy));            // Corner 4 (3)

        return points;
    }

    @Override
    public void draw(CustomRaster raster) {
        if (cornerPoints.size() != 4) return;

        // Draw filled area first if needed
        if (filled) {
            raster.fillPolygon(cornerPoints, fillColor);
        }

        // Draw the four edges
        for (int i = 0; i < 4; i++) {
            Point p1 = cornerPoints.get(i);
            Point p2 = cornerPoints.get((i + 1) % 4);
            raster.drawLine(p1.x, p1.y, p2.x, p2.y, color, style, thickness);
        }
    }

    @Override
    public boolean contains(Point p) {
        if (cornerPoints.size() != 4) return false;

        // If filled, check if point is inside
        if (filled) {
            return isPointInPolygon(p, cornerPoints);
        }

        // Otherwise check if point is near any edge
        for (int i = 0; i < 4; i++) {
            Point p1 = cornerPoints.get(i);
            Point p2 = cornerPoints.get((i + 1) % 4);

            // Distance from point to line segment
            if (distanceToLineSegment(p, p1, p2) <= 5 + thickness) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if point is inside a polygon using ray casting algorithm
     */
    private boolean isPointInPolygon(Point p, List<Point> polygon) {
        boolean inside = false;
        int n = polygon.size();

        for (int i = 0, j = n - 1; i < n; j = i++) {
            Point pi = polygon.get(i);
            Point pj = polygon.get(j);

            if (((pi.y > p.y) != (pj.y > p.y)) &&
                    (p.x < (pj.x - pi.x) * (p.y - pi.y) / (pj.y - pi.y) + pi.x)) {
                inside = !inside;
            }
        }

        return inside;
    }

    /**
     * Calculate distance from point to line segment
     */
    private double distanceToLineSegment(Point p, Point start, Point end) {
        double lineLength = distance(start, end);
        if (lineLength == 0) return distance(p, start);

        // Calculate projection of point onto line
        double t = ((p.x - start.x) * (end.x - start.x) +
                (p.y - start.y) * (end.y - start.y)) /
                (lineLength * lineLength);

        t = Math.max(0, Math.min(1, t));

        // Calculate closest point on line
        double projX = start.x + t * (end.x - start.x);
        double projY = start.y + t * (end.y - start.y);

        // Return distance to closest point
        return Math.sqrt((p.x - projX) * (p.x - projX) +
                (p.y - projY) * (p.y - projY));
    }

    /**
     * Calculate distance between two points
     */
    private double distance(Point p1, Point p2) {
        return Math.sqrt((p2.x - p1.x) * (p2.x - p1.x) +
                (p2.y - p1.y) * (p2.y - p1.y));
    }

    @Override
    public void move(int dx, int dy) {
        // Move all corner points
        for (Point p : cornerPoints) {
            p.x += dx;
            p.y += dy;
        }

        // Move the anchor point
        anchorPoint.x += dx;
        anchorPoint.y += dy;
    }

    @Override
    public void setEndPoint(Point p) {
        // Create a new square based on the anchor point and new endpoint
        cornerPoints = createSquareCorners(anchorPoint, p);
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
        if (cornerPoints.size() != 4) return null;

        Point nearest = null;
        double minDist = 10; // Threshold

        // Check corner points
        for (Point corner : cornerPoints) {
            double dist = distance(p, corner);
            if (dist < minDist) {
                minDist = dist;
                nearest = corner;
            }
        }

        return nearest;
    }

    /**
     * Identify which edge the point is on (or close to)
     *
     * @param p Point to check
     * @return Edge index or NO_EDGE if not near any edge
     */
    public int getNearestEdge(Point p) {
        if (cornerPoints.size() != 4) return NO_EDGE;

        double minDist = 10 + thickness; // Threshold with some tolerance
        int nearestEdge = NO_EDGE;

        for (int i = 0; i < 4; i++) {
            Point p1 = cornerPoints.get(i);
            Point p2 = cornerPoints.get((i + 1) % 4);

            double dist = distanceToLineSegment(p, p1, p2);
            if (dist < minDist) {
                minDist = dist;
                nearestEdge = i;
            }
        }

        return nearestEdge;
    }

    /**
     * Method to freely move a corner point (for right-drag)
     */
    public void moveCorner(Point corner, int dx, int dy) {
        for (Point p : cornerPoints) {
            if (distance(p, corner) < 10) {
                // Found the corner, move it freely (no constraints)
                p.x += dx;
                p.y += dy;
                return;
            }
        }
    }

    /**
     * Method for scaling by edge while maintaining square shape
     *
     * @param edgeIndex Index of the edge being dragged
     * @param dx Horizontal offset
     * @param dy Vertical offset
     */
    public void resizeByEdge(int edgeIndex, int dx, int dy) {
        if (cornerPoints.size() != 4 || edgeIndex < 0 || edgeIndex > 3) return;

        // We need the center of the square to calculate adjustments
        int centerX = 0, centerY = 0;
        for (Point p : cornerPoints) {
            centerX += p.x;
            centerY += p.y;
        }
        centerX /= 4;
        centerY /= 4;

        // Calculate the current size (half side length)
        int sideLength = (int)distance(cornerPoints.get(0), cornerPoints.get(1));
        int halfSide = sideLength / 2;

        // Use the maximum of dx or dy to maintain square shape
        int change = 0;

        switch (edgeIndex) {
            case TOP_EDGE:
                change = -dy; // Upward movement is negative in screen coordinates
                break;
            case RIGHT_EDGE:
                change = dx;
                break;
            case BOTTOM_EDGE:
                change = dy;
                break;
            case LEFT_EDGE:
                change = -dx; // Leftward movement is negative
                break;
        }

        // Apply the change uniformly to maintain square shape
        if (change != 0) {
            // Calculate new half-side length
            halfSide += change / 2;

            // Ensure minimum size
            halfSide = Math.max(halfSide, 5);

            // Update all corners relative to center
            cornerPoints.get(0).x = centerX - halfSide;
            cornerPoints.get(0).y = centerY - halfSide;

            cornerPoints.get(1).x = centerX + halfSide;
            cornerPoints.get(1).y = centerY - halfSide;

            cornerPoints.get(2).x = centerX + halfSide;
            cornerPoints.get(2).y = centerY + halfSide;

            cornerPoints.get(3).x = centerX - halfSide;
            cornerPoints.get(3).y = centerY + halfSide;
        }
    }

    @Override
    public void resizeByPoint(Point controlPoint, int dx, int dy) {
        if (cornerPoints.size() != 4) return;

        // Find which corner is being manipulated
        int cornerIndex = -1;
        for (int i = 0; i < 4; i++) {
            if (distance(controlPoint, cornerPoints.get(i)) < 10) {
                cornerIndex = i;
                break;
            }
        }

        if (cornerIndex >= 0) {
            // When using left-drag on a corner, maintain square shape
            // Get opposite corner (diagonal)
            int oppositeIndex = (cornerIndex + 2) % 4;
            Point oppositeCorner = cornerPoints.get(oppositeIndex);

            // Calculate the max of dx or dy to maintain square proportions
            int maxChange = Math.max(Math.abs(dx), Math.abs(dy));

            // Apply direction
            int signX = dx >= 0 ? 1 : -1;
            int signY = dy >= 0 ? 1 : -1;

            // Apply the change to the corner
            Point corner = cornerPoints.get(cornerIndex);
            corner.x += signX * maxChange;
            corner.y += signY * maxChange;

            // Get the other two corners
            int adjacentIndex1 = (cornerIndex + 1) % 4;
            int adjacentIndex2 = (cornerIndex + 3) % 4;
            Point adjacent1 = cornerPoints.get(adjacentIndex1);
            Point adjacent2 = cornerPoints.get(adjacentIndex2);

            // Update adjacent corners based on the corner being moved
            // to maintain square shape
            if (cornerIndex == 0) {  // Top-left
                adjacent1.x = oppositeCorner.x;
                adjacent1.y = corner.y;
                adjacent2.x = corner.x;
                adjacent2.y = oppositeCorner.y;
            } else if (cornerIndex == 1) {  // Top-right
                adjacent1.x = corner.x;
                adjacent1.y = oppositeCorner.y;
                adjacent2.x = oppositeCorner.x;
                adjacent2.y = corner.y;
            } else if (cornerIndex == 2) {  // Bottom-right
                adjacent1.x = oppositeCorner.x;
                adjacent1.y = corner.y;
                adjacent2.x = corner.x;
                adjacent2.y = oppositeCorner.y;
            } else if (cornerIndex == 3) {  // Bottom-left
                adjacent1.x = corner.x;
                adjacent1.y = oppositeCorner.y;
                adjacent2.x = oppositeCorner.x;
                adjacent2.y = corner.y;
            }
        }
    }

    @Override
    public void drawControlPoints(CustomRaster raster) {
        if (cornerPoints.size() != 4) return;

        // Draw control points at the four corners
        for (Point corner : cornerPoints) {
            raster.drawControlPoint(corner.x, corner.y);
        }
    }

    /**
     * Get the corner points of the square
     * @return List of corner points
     */
    public List<Point> getCornerPoints() {
        return cornerPoints;
    }
}
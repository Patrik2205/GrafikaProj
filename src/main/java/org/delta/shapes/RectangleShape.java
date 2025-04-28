package org.delta.shapes;
import org.delta.raster.CustomRaster;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class RectangleShape implements Shape {
    private List<Point> cornerPoints;
    private Color color;
    private Color fillColor;
    private int thickness;
    private int style;
    private boolean filled;

    // Constants for edge identification
    public static final int NO_EDGE = -1;
    public static final int TOP_EDGE = 0;
    public static final int RIGHT_EDGE = 1;
    public static final int BOTTOM_EDGE = 2;
    public static final int LEFT_EDGE = 3;

    public RectangleShape(Point p1, Point p2, Color color, int thickness, int style) {
        this.cornerPoints = createRectangleCorners(p1, p2);
        this.color = color;
        this.thickness = thickness;
        this.style = style;
        this.filled = false;
    }

    // Create rectangle corner points from two opposite corners
    private List<Point> createRectangleCorners(Point p1, Point p2) {
        List<Point> points = new ArrayList<>(4);
        int left = Math.min(p1.x, p2.x);
        int top = Math.min(p1.y, p2.y);
        int right = Math.max(p1.x, p2.x);
        int bottom = Math.max(p1.y, p2.y);

        points.add(new Point(left, top));        // Top-left (0)
        points.add(new Point(right, top));       // Top-right (1)
        points.add(new Point(right, bottom));    // Bottom-right (2)
        points.add(new Point(left, bottom));     // Bottom-left (3)

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

    // Check if point is inside a polygon
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

    // Calculate distance from point to line segment
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

    // Calculate distance between two points
    private double distance(Point p1, Point p2) {
        return Math.sqrt((p2.x - p1.x) * (p2.x - p1.x) +
                (p2.y - p1.y) * (p2.y - p1.y));
    }

    @Override
    public void move(int dx, int dy) {
        for (Point p : cornerPoints) {
            p.x += dx;
            p.y += dy;
        }
    }

    @Override
    public void setEndPoint(Point p) {
        // Create a new rectangle by keeping the first point as anchor
        // and using the new point as the opposite corner
        if (cornerPoints.size() > 0) {
            Point anchor = cornerPoints.get(0);
            cornerPoints = createRectangleCorners(anchor, p);
        }
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

    // Identify which edge the point is on (or close to)
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

    // New method to freely move a corner point
    public void moveCorner(Point corner, int dx, int dy) {
        for (Point p : cornerPoints) {
            if (distance(p, corner) < 10) {
                // Found the corner, move it
                p.x += dx;
                p.y += dy;
                return;
            }
        }
    }

    // Method for resizing by edge
    public void resizeByEdge(int edgeIndex, int dx, int dy) {
        if (cornerPoints.size() != 4 || edgeIndex < 0 || edgeIndex > 3) return;

        switch (edgeIndex) {
            case TOP_EDGE:
                // Move top edge (y-direction only)
                cornerPoints.get(0).y += dy;
                cornerPoints.get(1).y += dy;
                break;
            case RIGHT_EDGE:
                // Move right edge (x-direction only)
                cornerPoints.get(1).x += dx;
                cornerPoints.get(2).x += dx;
                break;
            case BOTTOM_EDGE:
                // Move bottom edge (y-direction only)
                cornerPoints.get(2).y += dy;
                cornerPoints.get(3).y += dy;
                break;
            case LEFT_EDGE:
                // Move left edge (x-direction only)
                cornerPoints.get(0).x += dx;
                cornerPoints.get(3).x += dx;
                break;
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
            // Move the corner
            Point corner = cornerPoints.get(cornerIndex);
            corner.x += dx;
            corner.y += dy;

            // Get opposite corner (diagonal) - this one stays fixed
            int oppositeIndex = (cornerIndex + 2) % 4;
            Point opposite = cornerPoints.get(oppositeIndex);

            // Get the other two corners
            int adjacentIndex1 = (cornerIndex + 1) % 4;
            int adjacentIndex2 = (cornerIndex + 3) % 4;
            Point adjacent1 = cornerPoints.get(adjacentIndex1);
            Point adjacent2 = cornerPoints.get(adjacentIndex2);

            // Now update the adjacent corners based on their relative positions
            // Top-left corner (0)
            if (cornerIndex == 0) {
                adjacent1.y = corner.y;    // Top-right: update y
                adjacent2.x = corner.x;    // Bottom-left: update x
            }
            // Top-right corner (1)
            else if (cornerIndex == 1) {
                adjacent1.x = corner.x;    // Bottom-right: update x
                adjacent2.y = corner.y;    // Top-left: update y
            }
            // Bottom-right corner (2)
            else if (cornerIndex == 2) {
                adjacent1.y = corner.y;    // Bottom-left: update y
                adjacent2.x = corner.x;    // Top-right: update x
            }
            // Bottom-left corner (3)
            else if (cornerIndex == 3) {
                adjacent1.x = corner.x;    // Top-left: update x
                adjacent2.y = corner.y;    // Bottom-right: update y
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

    // Getter for corner points (needed for drawing selected edges)
    public List<Point> getCornerPoints() {
        return cornerPoints;
    }
}
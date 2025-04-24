package org.delta.shapes;
import org.delta.raster.CustomRaster;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class PolygonShape implements Shape {
    private List<Point> points;
    private Color color;
    private Color fillColor;
    private int thickness;
    private int style;
    private boolean filled;

    public PolygonShape(List<Point> points, Color color, int thickness, int style) {
        this.points = new ArrayList<>(points);
        this.color = color;
        this.thickness = thickness;
        this.style = style;
        this.filled = false;
    }

    public void setPoints(List<Point> points) {
        this.points = new ArrayList<>(points);
    }

    @Override
    public void draw(CustomRaster raster) {
        // Don't attempt to draw if we don't have enough points
        if (points.size() < 2) return;

        // Draw the filled region if needed
        if (filled && points.size() >= 3) {
            raster.fillPolygon(points, fillColor);
        }

        // Draw each line segment between consecutive points
        for (int i = 0; i < points.size() - 1; i++) {
            Point p1 = points.get(i);
            Point p2 = points.get(i + 1);
            raster.drawLine(p1.x, p1.y, p2.x, p2.y, color, style, thickness);
        }

        // Complete the polygon by connecting the last point to the first
        // (only if we have at least 3 points to form a closed shape)
        if (points.size() >= 3) {
            Point first = points.get(0);
            Point last = points.get(points.size() - 1);
            raster.drawLine(last.x, last.y, first.x, first.y, color, style, thickness);
        }
    }

    @Override
    public boolean contains(Point p) {
        if (points.size() < 3) return false;

        // For filled polygons, check if point is inside
        if (filled) {
            // Implement point-in-polygon test directly here
            boolean inside = false;
            int nPoints = points.size();

            for (int i = 0, j = nPoints - 1; i < nPoints; j = i++) {
                Point pi = points.get(i);
                Point pj = points.get(j);

                if (((pi.y > p.y) != (pj.y > p.y)) &&
                        (p.x < (pj.x - pi.x) * (p.y - pi.y) / (pj.y - pi.y) + pi.x)) {
                    inside = !inside;
                }
            }

            return inside;
        }
        // For unfilled polygons, check if point is near any edge
        for (int i = 0; i < points.size(); i++) {
            Point p1 = points.get(i);
            Point p2 = points.get((i + 1) % points.size());

            // Distance from point to line segment
            double lineLength = Math.sqrt((p2.x - p1.x) * (p2.x - p1.x) + (p2.y - p1.y) * (p2.y - p1.y));
            if (lineLength == 0) continue;

            double t = ((p.x - p1.x) * (p2.x - p1.x) + (p.y - p1.y) * (p2.y - p1.y)) / (lineLength * lineLength);
            t = Math.max(0, Math.min(1, t));

            double nearestX = p1.x + t * (p2.x - p1.x);
            double nearestY = p1.y + t * (p2.y - p1.y);

            double distance = Math.sqrt((p.x - nearestX) * (p.x - nearestX) + (p.y - nearestY) * (p.y - nearestY));
            if (distance <= 5 + thickness) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void move(int dx, int dy) {
        for (Point p : points) {
            p.x += dx;
            p.y += dy;
        }
    }

    @Override
    public void setEndPoint(Point p) {
        // Not applicable for polygons
    }

    @Override
    public boolean canBeFilled() {
        return points.size() >= 3;
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
        Point nearest = null;
        double minDist = 10; // Threshold

        for (Point vertex : points) {
            double dist = Math.sqrt(Math.pow(p.x - vertex.x, 2) + Math.pow(p.y - vertex.y, 2));
            if (dist < minDist) {
                minDist = dist;
                nearest = vertex;
            }
        }

        return nearest;
    }

    public void movePoint(Point vertex, int dx, int dy) {
        // Find the matching vertex in our points list
        for (Point p : points) {
            if (p.x == vertex.x && p.y == vertex.y) {
                // We found the matching point, move it
                p.x += dx;
                p.y += dy;
                return;
            }
        }

        // If we didn't find an exact match, look for the closest point
        double minDistance = Double.MAX_VALUE;
        Point closestPoint = null;

        for (Point p : points) {
            double distance = Math.sqrt(Math.pow(p.x - vertex.x, 2) + Math.pow(p.y - vertex.y, 2));
            if (distance < minDistance) {
                minDistance = distance;
                closestPoint = p;
            }
        }

        // If we found a point and it's reasonably close (within 10 pixels)
        if (closestPoint != null && minDistance < 10) {
            closestPoint.x += dx;
            closestPoint.y += dy;
        }
    }

    @Override
    public void resizeByPoint(Point controlPoint, int dx, int dy) {
        // For polygons, resizeByPoint will just move the individual point
        // which is the same behavior as our new movePoint method
        movePoint(controlPoint, dx, dy);
    }

    @Override
    public void drawControlPoints(CustomRaster raster) {
        for (Point p : points) {
            raster.drawControlPoint(p.x, p.y);
        }
    }
}
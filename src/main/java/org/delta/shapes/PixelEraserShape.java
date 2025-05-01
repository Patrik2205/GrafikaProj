package org.delta.shapes;
import org.delta.raster.CustomRaster;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents pixel eraser operations as a shape.
 * Stores a series of eraser points and sizes to allow persistent pixel erasure.
 */
public class PixelEraserShape implements Shape {
    private List<EraserSegment> segments;
    private int eraserSize;
    private Color backgroundColor;

    /**
     * Creates a new pixel eraser shape with the specified parameters
     * @param eraserSize Size of the eraser in pixels
     * @param backgroundColor Background color for erasing
     */
    public PixelEraserShape(int eraserSize, Color backgroundColor) {
        this.segments = new ArrayList<>();
        this.eraserSize = eraserSize;
        this.backgroundColor = backgroundColor;
    }

    /**
     * Inner class to represent an eraser line segment
     */
    private static class EraserSegment {
        Point start;
        Point end;

        EraserSegment(Point start, Point end) {
            this.start = new Point(start);
            this.end = new Point(end);
        }
    }

    /**
     * Adds a point to the eraser path
     * @param point Point where eraser was applied
     */
    public void addPoint(Point point) {
        Point newPoint = new Point(point);

        // If this is the first point, we can't create a segment yet
        if (segments.isEmpty()) {
            segments.add(new EraserSegment(newPoint, newPoint));
            return;
        }

        // Use the end point of the last segment as the start of the new segment
        Point lastEnd = segments.get(segments.size() - 1).end;
        segments.add(new EraserSegment(lastEnd, newPoint));
    }

    @Override
    public void draw(CustomRaster raster) {
        for (EraserSegment segment : segments) {
            if (segment.start.equals(segment.end)) {
                // For single points (like initial clicks)
                raster.erasePixels(segment.start.x, segment.start.y, eraserSize);
            } else {
                // For line segments (mouse drag)
                raster.erasePixelsLine(segment.start.x, segment.start.y,
                        segment.end.x, segment.end.y, eraserSize);
            }
        }
    }

    @Override
    public boolean contains(Point p) {
        // Pixel eraser shapes can't be selected
        return false;
    }

    @Override
    public void move(int dx, int dy) {
        // Move all segments
        for (EraserSegment segment : segments) {
            segment.start.x += dx;
            segment.start.y += dy;
            segment.end.x += dx;
            segment.end.y += dy;
        }
    }

    @Override
    public void setEndPoint(Point p) {
        // Not applicable for eraser shapes
    }

    @Override
    public boolean canBeFilled() {
        return false;
    }

    @Override
    public void setFilled(boolean filled) {
        // Not applicable for eraser shapes
    }

    @Override
    public void setFillColor(Color color) {
        // Not applicable for eraser shapes
    }

    @Override
    public Point getNearestControlPoint(Point p) {
        // Eraser shapes don't have control points
        return null;
    }

    @Override
    public void resizeByPoint(Point controlPoint, int dx, int dy) {
        // Not applicable for eraser shapes
    }

    @Override
    public void drawControlPoints(CustomRaster raster) {
        // Eraser shapes don't have control points
    }
}
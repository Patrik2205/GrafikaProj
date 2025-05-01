package org.delta.shapes;
import org.delta.raster.CustomRaster;

import java.awt.Color;
import java.awt.Point;

/**
 * Common interface for all shapes in the drawing application.
 * Defines methods for drawing, selecting, moving, and manipulating shapes.
 */
public interface Shape {
    /**
     * Draws the shape on the specified raster
     * @param raster The raster to draw on
     */
    void draw(CustomRaster raster);

    /**
     * Checks if the shape contains the specified point
     * @param p Point to check
     * @return true if the point is inside or on the shape
     */
    boolean contains(Point p);

    /**
     * Moves the shape by the specified offsets
     * @param dx Horizontal offset
     * @param dy Vertical offset
     */
    void move(int dx, int dy);

    /**
     * Sets the end point during shape creation or resizing
     * @param p New end point
     */
    void setEndPoint(Point p);

    /**
     * Checks if the shape can be filled
     * @return true if the shape supports filling
     */
    boolean canBeFilled();

    /**
     * Sets whether the shape should be filled
     * @param filled true to fill the shape, false for outline only
     */
    void setFilled(boolean filled);

    /**
     * Sets the fill color for the shape
     * @param color Fill color
     */
    void setFillColor(Color color);

    /**
     * Gets the control point nearest to the specified point
     * @param p Point to check
     * @return The nearest control point, or null if none is near
     */
    Point getNearestControlPoint(Point p);

    /**
     * Resizes the shape by moving the specified control point
     * @param controlPoint Control point to move
     * @param dx Horizontal offset
     * @param dy Vertical offset
     */
    void resizeByPoint(Point controlPoint, int dx, int dy);

    /**
     * Draws control points for shape manipulation
     * @param raster The raster to draw on
     */
    void drawControlPoints(CustomRaster raster);
}

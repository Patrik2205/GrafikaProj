package org.delta.shapes;

import org.delta.raster.CustomRaster;
import org.delta.shapes.Shape;

import java.awt.Point;
import java.awt.Color;

public class SquareShape implements Shape {
    private Point p1; // The starting point (anchor point)
    private Point p2; // The current/ending point
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

    /**
     * Calculate the square dimensions based on the dragging direction
     * @return int array with [left, top, right, bottom]
     */
    private int[] calculateSquareDimensions() {
        int size = Math.max(Math.abs(p2.x - p1.x), Math.abs(p2.y - p1.y));
        int left, top, right, bottom;

        // Determine the actual corners based on drag direction
        if (p2.x >= p1.x && p2.y >= p1.y) {
            // Dragging bottom-right
            left = p1.x;
            top = p1.y;
            right = p1.x + size;
            bottom = p1.y + size;
        } else if (p2.x < p1.x && p2.y >= p1.y) {
            // Dragging bottom-left
            left = p1.x - size;
            top = p1.y;
            right = p1.x;
            bottom = p1.y + size;
        } else if (p2.x >= p1.x && p2.y < p1.y) {
            // Dragging top-right
            left = p1.x;
            top = p1.y - size;
            right = p1.x + size;
            bottom = p1.y;
        } else {
            // Dragging top-left
            left = p1.x - size;
            top = p1.y - size;
            right = p1.x;
            bottom = p1.y;
        }

        return new int[] {left, top, right, bottom};
    }

    @Override
    public void draw(CustomRaster raster) {
        int[] dims = calculateSquareDimensions();
        int left = dims[0];
        int top = dims[1];
        int right = dims[2];
        int bottom = dims[3];

        // Draw the square
        raster.drawRectangle(left, top, right, bottom, color, style, thickness, filled);

        // Fill if needed
        if (filled) {
            raster.fillRectangle(left, top, right, bottom, fillColor);
        }
    }

    @Override
    public boolean contains(Point p) {
        int[] dims = calculateSquareDimensions();
        int left = dims[0];
        int top = dims[1];
        int right = dims[2];
        int bottom = dims[3];

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
        // Simply store the new end point - drawing logic will handle the square sizing
        p2 = p;
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
        int[] dims = calculateSquareDimensions();
        int left = dims[0];
        int top = dims[1];
        int right = dims[2];
        int bottom = dims[3];

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
        int[] dims = calculateSquareDimensions();
        int left = dims[0];
        int top = dims[1];
        int right = dims[2];
        int bottom = dims[3];

        // Determine which corner is the anchor (which should stay fixed)
        boolean isLeftFixed = (p2.x >= p1.x);
        boolean isTopFixed = (p2.y >= p1.y);
        boolean isRightFixed = (p2.x < p1.x);
        boolean isBottomFixed = (p2.y < p1.y);

        // Top-left corner
        if (controlPoint.x == left && controlPoint.y == top) {
            // Calculate max shift to maintain square aspect ratio
            int maxShift = Math.max(Math.abs(dx), Math.abs(dy));
            int signX = dx < 0 ? -1 : 1;
            int signY = dy < 0 ? -1 : 1;

            // Apply maximum shift in both directions to keep square
            if (isRightFixed && isBottomFixed) {
                // Dragging from bottom-right anchor
                p2.x = p1.x - (maxShift * signX);
                p2.y = p1.y - (maxShift * signY);
            } else {
                // Other anchors
                if (isRightFixed) {
                    // Left edge is being moved
                    p1.x = right - (maxShift * signX);
                } else {
                    // Left edge is fixed
                    p2.x = p1.x + (maxShift * signX);
                }

                if (isBottomFixed) {
                    // Top edge is being moved
                    p1.y = bottom - (maxShift * signY);
                } else {
                    // Top edge is fixed
                    p2.y = p1.y + (maxShift * signY);
                }
            }
        }
        // Top-right corner
        else if (controlPoint.x == right && controlPoint.y == top) {
            int maxShift = Math.max(Math.abs(dx), Math.abs(dy));
            int signX = dx < 0 ? -1 : 1;
            int signY = dy < 0 ? -1 : 1;

            if (isLeftFixed && isBottomFixed) {
                // Dragging from bottom-left anchor
                p2.x = p1.x + (maxShift * signX);
                p2.y = p1.y - (maxShift * signY);
            } else {
                // Other anchors
                if (isLeftFixed) {
                    // Right edge is being moved
                    p2.x = p1.x + (maxShift * signX);
                } else {
                    // Right edge is fixed
                    p1.x = right - (maxShift * signX);
                }

                if (isBottomFixed) {
                    // Top edge is being moved
                    p1.y = bottom - (maxShift * signY);
                } else {
                    // Top edge is fixed
                    p2.y = p1.y + (maxShift * signY);
                }
            }
        }
        // Bottom-left corner
        else if (controlPoint.x == left && controlPoint.y == bottom) {
            int maxShift = Math.max(Math.abs(dx), Math.abs(dy));
            int signX = dx < 0 ? -1 : 1;
            int signY = dy < 0 ? -1 : 1;

            if (isRightFixed && isTopFixed) {
                // Dragging from top-right anchor
                p2.x = p1.x - (maxShift * signX);
                p2.y = p1.y + (maxShift * signY);
            } else {
                // Other anchors
                if (isRightFixed) {
                    // Left edge is being moved
                    p1.x = right - (maxShift * signX);
                } else {
                    // Left edge is fixed
                    p2.x = p1.x + (maxShift * signX);
                }

                if (isTopFixed) {
                    // Bottom edge is being moved
                    p2.y = p1.y + (maxShift * signY);
                } else {
                    // Bottom edge is fixed
                    p1.y = top + (maxShift * signY);
                }
            }
        }
        // Bottom-right corner
        else if (controlPoint.x == right && controlPoint.y == bottom) {
            int maxShift = Math.max(Math.abs(dx), Math.abs(dy));
            int signX = dx < 0 ? -1 : 1;
            int signY = dy < 0 ? -1 : 1;

            if (isLeftFixed && isTopFixed) {
                // Dragging from top-left anchor
                p2.x = p1.x + (maxShift * signX);
                p2.y = p1.y + (maxShift * signY);
            } else {
                // Other anchors
                if (isLeftFixed) {
                    // Right edge is being moved
                    p2.x = p1.x + (maxShift * signX);
                } else {
                    // Right edge is fixed
                    p1.x = right - (maxShift * signX);
                }

                if (isTopFixed) {
                    // Bottom edge is being moved
                    p2.y = p1.y + (maxShift * signY);
                } else {
                    // Bottom edge is fixed
                    p1.y = top + (maxShift * signY);
                }
            }
        }
        // Middle edges
        else if (controlPoint.x == (left + right) / 2) {
            // Top or bottom middle
            int newY = controlPoint.y + dy;
            int size = right - left;

            if (controlPoint.y == top) {
                // Top middle
                if (isBottomFixed) {
                    p1.y = bottom - size;
                } else {
                    p2.y = p1.y + size;
                }
            } else if (controlPoint.y == bottom) {
                // Bottom middle
                if (isTopFixed) {
                    p2.y = p1.y + size;
                } else {
                    p1.y = top + size;
                }
            }
        }
        else if (controlPoint.y == (top + bottom) / 2) {
            // Left or right middle
            int newX = controlPoint.x + dx;
            int size = bottom - top;

            if (controlPoint.x == left) {
                // Left middle
                if (isRightFixed) {
                    p1.x = right - size;
                } else {
                    p2.x = p1.x + size;
                }
            } else if (controlPoint.x == right) {
                // Right middle
                if (isLeftFixed) {
                    p2.x = p1.x + size;
                } else {
                    p1.x = left + size;
                }
            }
        }
    }

    @Override
    public void drawControlPoints(CustomRaster raster) {
        int[] dims = calculateSquareDimensions();
        int left = dims[0];
        int top = dims[1];
        int right = dims[2];
        int bottom = dims[3];

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

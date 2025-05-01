package org.delta.raster;
import org.delta.util.LineStyle;

import java.awt.Point;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Custom raster implementation that provides low-level drawing functionality.
 * Handles pixel manipulation and implements various drawing algorithms
 * for lines, circles, and filling operations.
 */
public class CustomRaster {
    private BufferedImage img;
    private int clearColor = 0xFFFFFF; // White background

    /**
     * Creates a new CustomRaster with specified dimensions
     * @param width Width of the raster in pixels
     * @param height Height of the raster in pixels
     */
    public CustomRaster(int width, int height) {
        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        clear(); // Initialize with background color
    }

    /**
     * Clears the raster with the current clear color
     */
    public void clear() {
        Graphics g = img.getGraphics();
        g.setColor(new Color(clearColor));
        g.fillRect(0, 0, img.getWidth(), img.getHeight());
        g.dispose();
    }

    /**
     * Sets the color used for clearing the raster
     * @param color RGB color value
     */
    public void setClearColor(int color) {
        this.clearColor = color;
    }

    /**
     * Gets the width of the raster
     * @return Width in pixels
     */
    public int getWidth() {
        return img.getWidth();
    }

    /**
     * Gets the height of the raster
     * @return Height in pixels
     */
    public int getHeight() {
        return img.getHeight();
    }

    /**
     * Gets the underlying BufferedImage
     * @return The BufferedImage
     */
    public BufferedImage getImg() {
        return img;
    }

    /**
     * Gets a Graphics object for this raster
     * @return Graphics object
     */
    public Graphics getGraphics() {
        return img.getGraphics();
    }

    /**
     * Gets the color of a specific pixel
     * @param x X-coordinate
     * @param y Y-coordinate
     * @return RGB color value or 0 if out of bounds
     */
    public int getPixel(int x, int y) {
        if (x >= 0 && x < getWidth() && y >= 0 && y < getHeight()) {
            return img.getRGB(x, y);
        }
        return 0;
    }

    /**
     * Sets the color of a specific pixel
     * @param x X-coordinate
     * @param y Y-coordinate
     * @param color RGB color value
     */
    public void setPixel(int x, int y, int color) {
        if (x >= 0 && x < getWidth() && y >= 0 && y < getHeight()) {
            img.setRGB(x, y, color);
        }
    }

    /**
     * Sets the color of a specific pixel
     * @param x X-coordinate
     * @param y Y-coordinate
     * @param color Color object
     */
    public void setPixel(int x, int y, Color color) {
        setPixel(x, y, color.getRGB());
    }

    /**
     * Draws a solid line with thickness 1
     * @param x1 Start x-coordinate
     * @param y1 Start y-coordinate
     * @param x2 End x-coordinate
     * @param y2 End y-coordinate
     * @param color Line color
     */
    public void drawLine(int x1, int y1, int x2, int y2, Color color) {
        drawLine(x1, y1, x2, y2, color, LineStyle.SOLID, 1);
    }

    /**
     * Draws a line with specified style and thickness
     * @param x1 Start x-coordinate
     * @param y1 Start y-coordinate
     * @param x2 End x-coordinate
     * @param y2 End y-coordinate
     * @param color Line color
     * @param style Line style (solid, dashed, dotted)
     * @param thickness Line thickness in pixels
     */
    public void drawLine(int x1, int y1, int x2, int y2, Color color, int style, int thickness) {
        if (style == LineStyle.DOTTED) {
            drawDottedLine(x1, y1, x2, y2, color, thickness);
        } else if (style == LineStyle.DASHED) {
            drawDashedLine(x1, y1, x2, y2, color, thickness);
        } else {
            drawSolidLine(x1, y1, x2, y2, color, thickness);
        }
    }

    /**
     * Draws a solid line with specified thickness
     * Uses modified Bresenham's line algorithm with thickness support
     *
     * @param x1 Start x-coordinate
     * @param y1 Start y-coordinate
     * @param x2 End x-coordinate
     * @param y2 End y-coordinate
     * @param color Line color
     * @param thickness Line thickness in pixels
     */
    private void drawSolidLine(int x1, int y1, int x2, int y2, Color color, int thickness) {
        // Special case: Handle vertical lines for better performance and quality
        if (x1 == x2) {
            int startY = Math.min(y1, y2);
            int endY = Math.max(y1, y2);
            for (int y = startY; y <= endY; y++) {
                // Draw thickness pixels on either side of the central line
                for (int t = 0; t < thickness; t++) {
                    for (int s = -t; s <= t; s++) {
                        setPixel(x1 + s, y, color);
                    }
                }
            }
            return;
        }

        // Special case: Handle horizontal lines for better performance and quality
        if (y1 == y2) {
            int startX = Math.min(x1, x2);
            int endX = Math.max(x1, x2);
            for (int x = startX; x <= endX; x++) {
                // Draw thickness pixels on either side of the central line
                for (int t = 0; t < thickness; t++) {
                    for (int s = -t; s <= t; s++) {
                        setPixel(x, y1 + s, color);
                    }
                }
            }
            return;
        }

        // General case: Bresenham's algorithm with modifications for line thickness
        // Determine if the line is more horizontal (low slope) or more vertical (high slope)
        if (Math.abs(y2 - y1) < Math.abs(x2 - x1)) {
            // Line is more horizontal than vertical
            if (x1 > x2) {
                // Ensure we always draw from left to right for consistency
                drawSolidLowLine(x2, y2, x1, y1, color, thickness);
            } else {
                drawSolidLowLine(x1, y1, x2, y2, color, thickness);
            }
        } else {
            // Line is more vertical than horizontal
            if (y1 > y2) {
                // Ensure we always draw from top to bottom for consistency
                drawSolidHighLine(x2, y2, x1, y1, color, thickness);
            } else {
                drawSolidHighLine(x1, y1, x2, y2, color, thickness);
            }
        }
    }

    /**
     * Helper method for drawing a line with a low slope (more horizontal)
     * Part of the Bresenham's line algorithm implementation
     *
     * @param x1 Start x-coordinate
     * @param y1 Start y-coordinate
     * @param x2 End x-coordinate
     * @param y2 End y-coordinate
     * @param color Line color
     * @param thickness Line thickness in pixels
     */
    private void drawSolidLowLine(int x1, int y1, int x2, int y2, Color color, int thickness) {
        int dx = x2 - x1;
        int dy = y2 - y1;
        int yi = 1;

        // If y-delta is negative, we need to step in negative direction
        if (dy < 0) {
            yi = -1;
            dy = -dy;
        }

        // Initial decision parameter for determining which pixels to draw
        int D = 2 * dy - dx;
        int y = y1;

        // Iterate over each x position
        for (int x = x1; x <= x2; x++) {
            // Draw a thickness-pixel wide segment centered on the main line
            for (int t = 0; t < thickness; t++) {
                for (int s = -t; s <= t; s++) {
                    setPixel(x, y + s, color);
                }
            }

            // Determine whether to move vertically based on the decision parameter
            if (D > 0) {
                y += yi;
                D += 2 * (dy - dx);
            } else {
                D += 2 * dy;
            }
        }
    }

    /**
     * Helper method for drawing a line with a high slope (more vertical)
     * Part of the Bresenham's line algorithm implementation
     *
     * @param x1 Start x-coordinate
     * @param y1 Start y-coordinate
     * @param x2 End x-coordinate
     * @param y2 End y-coordinate
     * @param color Line color
     * @param thickness Line thickness in pixels
     */
    private void drawSolidHighLine(int x1, int y1, int x2, int y2, Color color, int thickness) {
        int dx = x2 - x1;
        int dy = y2 - y1;
        int xi = 1;

        // If x-delta is negative, we need to step in negative direction
        if (dx < 0) {
            xi = -1;
            dx = -dx;
        }

        // Initial decision parameter for determining which pixels to draw
        int D = 2 * dx - dy;
        int x = x1;

        // Iterate over each y position
        for (int y = y1; y <= y2; y++) {
            // Draw a thickness-pixel wide segment centered on the main line
            for (int t = 0; t < thickness; t++) {
                for (int s = -t; s <= t; s++) {
                    setPixel(x + s, y, color);
                }
            }

            // Determine whether to move horizontally based on the decision parameter
            if (D > 0) {
                x += xi;
                D += 2 * (dx - dy);
            } else {
                D += 2 * dx;
            }
        }
    }

    /**
     * Draws a dotted line with specified thickness
     * Uses parameterized algorithm to distribute dots evenly along the line
     *
     * @param x1 Start x-coordinate
     * @param y1 Start y-coordinate
     * @param x2 End x-coordinate
     * @param y2 End y-coordinate
     * @param color Line color
     * @param thickness Dot thickness in pixels
     */
    private void drawDottedLine(int x1, int y1, int x2, int y2, Color color, int thickness) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double length = Math.sqrt(dx * dx + dy * dy);

        // Define fixed dot spacing and calculate number of dots based on line length
        int dotSpacing = 5;
        int numDots = (int) (length / dotSpacing);

        // Place dots at regular intervals along the line
        for (int i = 0; i <= numDots; i++) {
            // Calculate position of each dot using parametric equation
            double t = (double) i / numDots;  // Parameter between 0 and 1
            int x = (int) Math.round(x1 + t * dx);
            int y = (int) Math.round(y1 + t * dy);

            // Draw each dot with specified thickness
            for (int th = 0; th < thickness; th++) {
                for (int s = -th; s <= th; s++) {
                    for (int r = -th; r <= th; r++) {
                        setPixel(x + s, y + r, color);
                    }
                }
            }
        }
    }

    /**
     * Draws a dashed line with specified thickness
     * Renders alternating dashes and gaps along the line path
     *
     * @param x1 Start x-coordinate
     * @param y1 Start y-coordinate
     * @param x2 End x-coordinate
     * @param y2 End y-coordinate
     * @param color Line color
     * @param thickness Line thickness in pixels
     */
    private void drawDashedLine(int x1, int y1, int x2, int y2, Color color, int thickness) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double length = Math.sqrt(dx * dx + dy * dy);

        // Define dash and gap lengths
        int dashLength = 10;
        int totalPatternLength = dashLength * 2; // dash + gap
        int numSegments = (int) (length / totalPatternLength) + 1;

        // Draw each dash segment
        for (int i = 0; i < numSegments; i++) {
            // Calculate start and end points of each dash using parametric equation
            double startT = (double) (i * totalPatternLength) / length;
            double endT = (double) (i * totalPatternLength + dashLength) / length;

            // Stop if we've reached the end of the line
            if (startT > 1.0) break;
            endT = Math.min(endT, 1.0);

            // Convert parametric coordinates to pixel coordinates
            int startX = (int) Math.round(x1 + startT * dx);
            int startY = (int) Math.round(y1 + startT * dy);
            int endX = (int) Math.round(x1 + endT * dx);
            int endY = (int) Math.round(y1 + endT * dy);

            // Draw each dash as a solid line segment
            drawSolidLine(startX, startY, endX, endY, color, thickness);
        }
    }

    /**
     * Draws a circle with specified parameters
     *
     * @param centerX X-coordinate of circle center
     * @param centerY Y-coordinate of circle center
     * @param radius Circle radius in pixels
     * @param color Circle outline color
     * @param style Line style (solid, dashed, dotted)
     * @param thickness Line thickness in pixels
     * @param filled Whether to fill the circle
     */
    public void drawCircle(int centerX, int centerY, int radius, Color color, int style, int thickness, boolean filled) {
        if (filled) {
            fillCircle(centerX, centerY, radius, color);
        }

        if (style == LineStyle.DOTTED) {
            drawDottedCircle(centerX, centerY, radius, color, thickness);
        } else if (style == LineStyle.DASHED) {
            drawDashedCircle(centerX, centerY, radius, color, thickness);
        } else {
            drawSolidCircle(centerX, centerY, radius, color, thickness);
        }
    }

    /**
     * Draws a solid circle outline
     * Uses Midpoint Circle Algorithm (Bresenham's circle algorithm)
     *
     * @param centerX X-coordinate of circle center
     * @param centerY Y-coordinate of circle center
     * @param radius Circle radius in pixels
     * @param color Circle outline color
     * @param thickness Line thickness in pixels
     */
    private void drawSolidCircle(int centerX, int centerY, int radius, Color color, int thickness) {
        int x = 0;
        int y = radius;

        // Initial decision parameter for Midpoint Circle Algorithm
        int d = 3 - 2 * radius;

        // Draw initial points in all octants
        drawCirclePoints(centerX, centerY, x, y, color, thickness);

        // Iterate using Midpoint Circle Algorithm
        while (y >= x) {
            x++;

            // Update decision parameter and y-coordinate based on algorithm
            if (d > 0) {
                y--;
                d = d + 4 * (x - y) + 10;
            } else {
                d = d + 4 * x + 6;
            }

            // Draw points in all octants for each step
            drawCirclePoints(centerX, centerY, x, y, color, thickness);
        }
    }

    /**
     * Helper method to draw points in all octants of a circle
     * Part of the Midpoint Circle Algorithm implementation
     *
     * @param centerX X-coordinate of circle center
     * @param centerY Y-coordinate of circle center
     * @param x X-offset from center
     * @param y Y-offset from center
     * @param color Point color
     * @param thickness Point thickness
     */
    private void drawCirclePoints(int centerX, int centerY, int x, int y, Color color, int thickness) {
        // Draw points in all eight octants of the circle with thickness
        for (int t = 0; t < thickness; t++) {
            for (int s = -t; s <= t; s++) {
                // Draw the eight symmetric points
                setPixel(centerX + x + s, centerY + y, color);
                setPixel(centerX - x + s, centerY + y, color);
                setPixel(centerX + x + s, centerY - y, color);
                setPixel(centerX - x + s, centerY - y, color);
                setPixel(centerX + y + s, centerY + x, color);
                setPixel(centerX - y + s, centerY + x, color);
                setPixel(centerX + y + s, centerY - x, color);
                setPixel(centerX - y + s, centerY - x, color);
            }
        }
    }

    /**
     * Draws a dotted circle outline
     * Places dots at regular angular intervals around the circle
     *
     * @param centerX X-coordinate of circle center
     * @param centerY Y-coordinate of circle center
     * @param radius Circle radius in pixels
     * @param color Circle outline color
     * @param thickness Dot thickness in pixels
     */
    private void drawDottedCircle(int centerX, int centerY, int radius, Color color, int thickness) {
        // Calculate number of dots based on circle circumference
        int numDots = radius * 6;  // More dots for larger radius

        // Place dots at regular angular intervals
        for (int i = 0; i < numDots; i++) {
            double angle = 2 * Math.PI * i / numDots;
            int x = (int) Math.round(centerX + radius * Math.cos(angle));
            int y = (int) Math.round(centerY + radius * Math.sin(angle));

            // Draw each dot with specified thickness
            for (int t = 0; t < thickness; t++) {
                for (int s = -t; s <= t; s++) {
                    for (int r = -t; r <= t; r++) {
                        setPixel(x + s, y + r, color);
                    }
                }
            }
        }
    }

    /**
     * Draws a dashed circle outline
     * Creates a dashed pattern by drawing alternating arcs around the circle
     *
     * @param centerX X-coordinate of circle center
     * @param centerY Y-coordinate of circle center
     * @param radius Circle radius in pixels
     * @param color Circle outline color
     * @param thickness Line thickness in pixels
     */
    private void drawDashedCircle(int centerX, int centerY, int radius, Color color, int thickness) {
        // Divide the circle into segments (alternating dash and gap)
        int numSegments = 16;  // Number of segments to draw

        // Draw every other segment to create the dashed pattern
        for (int i = 0; i < numSegments; i += 2) {  // Skip every other segment
            double startAngle = 2 * Math.PI * i / numSegments;
            double endAngle = 2 * Math.PI * (i + 1) / numSegments;

            drawCircleArc(centerX, centerY, radius, startAngle, endAngle, color, thickness);
        }
    }

    /**
     * Helper method to draw an arc of a circle
     *
     * @param centerX X-coordinate of circle center
     * @param centerY Y-coordinate of circle center
     * @param radius Circle radius in pixels
     * @param startAngle Starting angle in radians
     * @param endAngle Ending angle in radians
     * @param color Arc color
     * @param thickness Line thickness in pixels
     */
    private void drawCircleArc(int centerX, int centerY, int radius, double startAngle, double endAngle, Color color, int thickness) {
        // Calculate number of steps based on the arc length
        int steps = (int) (radius * (endAngle - startAngle));
        steps = Math.max(steps, 10);  // Ensure at least 10 steps for small arcs

        // Draw points along the arc at regular angular intervals
        for (int i = 0; i <= steps; i++) {
            double angle = startAngle + (endAngle - startAngle) * i / steps;
            int x = (int) Math.round(centerX + radius * Math.cos(angle));
            int y = (int) Math.round(centerY + radius * Math.sin(angle));

            // Draw each point with specified thickness
            for (int t = 0; t < thickness; t++) {
                for (int s = -t; s <= t; s++) {
                    for (int r = -t; r <= t; r++) {
                        setPixel(x + s, y + r, color);
                    }
                }
            }
        }
    }

    /**
     * Fills a circle with the specified color
     * Uses the distance formula to determine which pixels are inside the circle
     *
     * @param centerX X-coordinate of circle center
     * @param centerY Y-coordinate of circle center
     * @param radius Circle radius in pixels
     * @param color Fill color
     */
    public void fillCircle(int centerX, int centerY, int radius, Color color) {
        // Scan through a square bounding box around the circle
        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {
                // Use the circle equation (x²+y²=r²) to check if the pixel is inside
                if (x*x + y*y <= radius*radius) {
                    setPixel(centerX + x, centerY + y, color);
                }
            }
        }
    }

    /**
     * Draws a rectangle with specified parameters
     *
     * @param x1 X-coordinate of first corner
     * @param y1 Y-coordinate of first corner
     * @param x2 X-coordinate of opposite corner
     * @param y2 Y-coordinate of opposite corner
     * @param color Rectangle outline color
     * @param style Line style (solid, dashed, dotted)
     * @param thickness Line thickness in pixels
     * @param filled Whether to fill the rectangle
     */
    public void drawRectangle(int x1, int y1, int x2, int y2, Color color, int style, int thickness, boolean filled) {
        int left = Math.min(x1, x2);
        int top = Math.min(y1, y2);
        int right = Math.max(x1, x2);
        int bottom = Math.max(y1, y2);

        if (filled) {
            fillRectangle(left, top, right, bottom, color);
        }

        // Draw the four edges of the rectangle
        drawLine(left, top, right, top, color, style, thickness);      // Top
        drawLine(left, bottom, right, bottom, color, style, thickness); // Bottom
        drawLine(left, top, left, bottom, color, style, thickness);     // Left
        drawLine(right, top, right, bottom, color, style, thickness);   // Right
    }

    /**
     * Fills a rectangle with the specified color
     *
     * @param left X-coordinate of left edge
     * @param top Y-coordinate of top edge
     * @param right X-coordinate of right edge
     * @param bottom Y-coordinate of bottom edge
     * @param color Fill color
     */
    public void fillRectangle(int left, int top, int right, int bottom, Color color) {
        // Fill every pixel within the rectangle bounds
        for (int y = top; y <= bottom; y++) {
            for (int x = left; x <= right; x++) {
                setPixel(x, y, color);
            }
        }
    }

    /**
     * Fills a polygon with the specified color
     * Uses scan-line algorithm to determine which pixels are inside the polygon
     *
     * @param points List of polygon vertices
     * @param color Fill color
     */
    public void fillPolygon(List<Point> points, Color color) {
        if (points.size() < 3) return;  // Need at least 3 points to form a polygon

        // Find bounding box of the polygon for optimization
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (Point p : points) {
            minX = Math.min(minX, p.x);
            maxX = Math.max(maxX, p.x);
            minY = Math.min(minY, p.y);
            maxY = Math.max(maxY, p.y);
        }

        // Check each pixel in the bounding box
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                if (isPointInPolygon(x, y, points)) {
                    setPixel(x, y, color);
                }
            }
        }
    }

    /**
     * Determines if a point is inside a polygon
     * Uses the ray casting algorithm (even-odd rule)
     *
     * @param x X-coordinate of point
     * @param y Y-coordinate of point
     * @param points List of polygon vertices
     * @return true if the point is inside, false otherwise
     */
    public boolean isPointInPolygon(int x, int y, List<Point> points) {
        boolean inside = false;
        int nPoints = points.size();

        // Ray casting algorithm: Count intersections of a ray from (x,y) to infinity
        for (int i = 0, j = nPoints - 1; i < nPoints; j = i++) {
            Point pi = points.get(i);
            Point pj = points.get(j);

            // Check if the ray intersects this edge
            if (((pi.y > y) != (pj.y > y)) &&
                    (x < (pj.x - pi.x) * (y - pi.y) / (pj.y - pi.y) + pi.x)) {
                // If there's an intersection, toggle the inside flag
                inside = !inside;
            }
        }

        return inside;
    }

    /**
     * Draws a control point (handle) for shape manipulation
     *
     * @param x X-coordinate of control point
     * @param y Y-coordinate of control point
     */
    public void drawControlPoint(int x, int y) {
        int size = 4;
        for (int i = -size; i <= size; i++) {
            for (int j = -size; j <= size; j++) {
                if (Math.abs(i) == size || Math.abs(j) == size) {
                    // Draw the outline in blue
                    setPixel(x + i, y + j, Color.BLUE);
                } else if (Math.abs(i) <= size-2 && Math.abs(j) <= size-2) {
                    // Draw the inside in white
                    setPixel(x + i, y + j, Color.WHITE);
                }
            }
        }
    }

    /**
     * Erases pixels in a circular area
     *
     * @param x X-coordinate of eraser center
     * @param y Y-coordinate of eraser center
     * @param radius Eraser radius in pixels
     */
    public void erasePixels(int x, int y, int radius) {
        // Use the background color for erasing
        int backgroundColor = clearColor;

        // Erase in a circular pattern
        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                // Check if point is within the circular radius
                if (dx*dx + dy*dy <= radius*radius) {
                    setPixel(x + dx, y + dy, backgroundColor);
                }
            }
        }
    }

    /**
     * Erases pixels along a line path
     * Used for smooth erasing during mouse drag
     *
     * @param x1 Start x-coordinate
     * @param y1 Start y-coordinate
     * @param x2 End x-coordinate
     * @param y2 End y-coordinate
     * @param radius Eraser radius in pixels
     */
    public void erasePixelsLine(int x1, int y1, int x2, int y2, int radius) {
        // Calculate distance between points
        double distance = Math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1));

        // If points are very close, just erase at the end point
        if (distance < 1) {
            erasePixels(x2, y2, radius);
            return;
        }

        // Determine number of steps to ensure smooth erasing
        int steps = (int)Math.ceil(distance);

        // Erase along the path using parametric equation
        for (int i = 0; i <= steps; i++) {
            double t = (double)i / steps;
            int x = (int)Math.round(x1 + t * (x2 - x1));
            int y = (int)Math.round(y1 + t * (y2 - y1));
            erasePixels(x, y, radius);
        }
    }

    /**
     * Performs a flood fill starting from the specified point
     * Replaces all adjacent pixels of the same color with the new color
     *
     * @param x X-coordinate of start point
     * @param y Y-coordinate of start point
     * @param newColor Fill color
     */
    public void floodFill(int x, int y, Color newColor) {
        // Ignore if out of bounds
        if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight()) {
            return;
        }

        // Get the color we're replacing
        int targetColor = getPixel(x, y);
        int replacementColor = newColor.getRGB();

        // Don't do anything if the colors are the same
        if (targetColor == replacementColor) {
            return;
        }

        // Perform the flood fill using a queue-based approach (avoids stack overflow)
        floodFillIterative(x, y, targetColor, replacementColor);
    }

    /**
     * Iterative implementation of the flood fill algorithm
     * Uses a breadth-first search approach with a queue
     *
     * @param startX X-coordinate of start point
     * @param startY Y-coordinate of start point
     * @param targetColor Color to replace
     * @param replacementColor New color
     */
    private void floodFillIterative(int startX, int startY, int targetColor, int replacementColor) {
        Queue<Point> pixelsToCheck = new LinkedList<>();
        pixelsToCheck.add(new Point(startX, startY));

        // Define the four directions to check (up, right, down, left)
        int[][] directions = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};

        while (!pixelsToCheck.isEmpty()) {
            Point current = pixelsToCheck.remove();
            int x = current.x;
            int y = current.y;

            // Skip if out of bounds
            if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight()) {
                continue;
            }

            // Skip if this pixel doesn't match the target color
            if (getPixel(x, y) != targetColor) {
                continue;
            }

            // Fill this pixel
            setPixel(x, y, replacementColor);

            // Check neighbors in all four directions
            for (int[] dir : directions) {
                pixelsToCheck.add(new Point(x + dir[0], y + dir[1]));
            }
        }
    }

    /**
     * Draws the raster contents to a Graphics object
     *
     * @param g Graphics context to draw to
     */
    public void repaint(Graphics g) {
        g.drawImage(img, 0, 0, null);
    }
}
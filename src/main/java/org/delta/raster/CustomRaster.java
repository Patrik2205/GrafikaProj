package org.delta.raster;
import org.delta.util.LineStyle;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class CustomRaster {
    private BufferedImage img;
    private int clearColor = 0xFFFFFF; // White background

    public CustomRaster(int width, int height) {
        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        clear(); // Initialize with background color
    }

    public void clear() {
        Graphics g = img.getGraphics();
        g.setColor(new Color(clearColor));
        g.fillRect(0, 0, img.getWidth(), img.getHeight());
        g.dispose();
    }

    public void setClearColor(int color) {
        this.clearColor = color;
    }

    public int getWidth() {
        return img.getWidth();
    }

    public int getHeight() {
        return img.getHeight();
    }

    public int getPixel(int x, int y) {
        if (x >= 0 && x < getWidth() && y >= 0 && y < getHeight()) {
            return img.getRGB(x, y);
        }
        return 0;
    }

    public void setPixel(int x, int y, int color) {
        if (x >= 0 && x < getWidth() && y >= 0 && y < getHeight()) {
            img.setRGB(x, y, color);
        }
    }

    public void setPixel(int x, int y, Color color) {
        setPixel(x, y, color.getRGB());
    }

    public void drawLine(int x1, int y1, int x2, int y2, Color color) {
        drawLine(x1, y1, x2, y2, color, LineStyle.SOLID, 1);
    }

    public void drawLine(int x1, int y1, int x2, int y2, Color color, int style, int thickness) {
        if (style == LineStyle.DOTTED) {
            drawDottedLine(x1, y1, x2, y2, color, thickness);
        } else if (style == LineStyle.DASHED) {
            drawDashedLine(x1, y1, x2, y2, color, thickness);
        } else {
            drawSolidLine(x1, y1, x2, y2, color, thickness);
        }
    }

    private void drawSolidLine(int x1, int y1, int x2, int y2, Color color, int thickness) {
        // Handle vertical lines
        if (x1 == x2) {
            int startY = Math.min(y1, y2);
            int endY = Math.max(y1, y2);
            for (int y = startY; y <= endY; y++) {
                for (int t = 0; t < thickness; t++) {
                    for (int s = -t; s <= t; s++) {
                        setPixel(x1 + s, y, color);
                    }
                }
            }
            return;
        }

        // Handle horizontal lines
        if (y1 == y2) {
            int startX = Math.min(x1, x2);
            int endX = Math.max(x1, x2);
            for (int x = startX; x <= endX; x++) {
                for (int t = 0; t < thickness; t++) {
                    for (int s = -t; s <= t; s++) {
                        setPixel(x, y1 + s, color);
                    }
                }
            }
            return;
        }

        // General lines
        if (Math.abs(y2 - y1) < Math.abs(x2 - x1)) {
            // Line is more horizontal than vertical
            if (x1 > x2) {
                drawSolidLowLine(x2, y2, x1, y1, color, thickness);
            } else {
                drawSolidLowLine(x1, y1, x2, y2, color, thickness);
            }
        } else {
            // Line is more vertical than horizontal
            if (y1 > y2) {
                drawSolidHighLine(x2, y2, x1, y1, color, thickness);
            } else {
                drawSolidHighLine(x1, y1, x2, y2, color, thickness);
            }
        }
    }

    private void drawSolidLowLine(int x1, int y1, int x2, int y2, Color color, int thickness) {
        int dx = x2 - x1;
        int dy = y2 - y1;
        int yi = 1;

        if (dy < 0) {
            yi = -1;
            dy = -dy;
        }

        int D = 2 * dy - dx;
        int y = y1;

        for (int x = x1; x <= x2; x++) {
            for (int t = 0; t < thickness; t++) {
                for (int s = -t; s <= t; s++) {
                    setPixel(x, y + s, color);
                }
            }

            if (D > 0) {
                y += yi;
                D += 2 * (dy - dx);
            } else {
                D += 2 * dy;
            }
        }
    }

    private void drawSolidHighLine(int x1, int y1, int x2, int y2, Color color, int thickness) {
        int dx = x2 - x1;
        int dy = y2 - y1;
        int xi = 1;

        if (dx < 0) {
            xi = -1;
            dx = -dx;
        }

        int D = 2 * dx - dy;
        int x = x1;

        for (int y = y1; y <= y2; y++) {
            for (int t = 0; t < thickness; t++) {
                for (int s = -t; s <= t; s++) {
                    setPixel(x + s, y, color);
                }
            }

            if (D > 0) {
                x += xi;
                D += 2 * (dx - dy);
            } else {
                D += 2 * dx;
            }
        }
    }

    private void drawDottedLine(int x1, int y1, int x2, int y2, Color color, int thickness) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double length = Math.sqrt(dx * dx + dy * dy);

        int dotSpacing = 5;
        int numDots = (int) (length / dotSpacing);

        for (int i = 0; i <= numDots; i++) {
            double t = (double) i / numDots;
            int x = (int) Math.round(x1 + t * dx);
            int y = (int) Math.round(y1 + t * dy);

            for (int th = 0; th < thickness; th++) {
                for (int s = -th; s <= th; s++) {
                    for (int r = -th; r <= th; r++) {
                        setPixel(x + s, y + r, color);
                    }
                }
            }
        }
    }

    private void drawDashedLine(int x1, int y1, int x2, int y2, Color color, int thickness) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double length = Math.sqrt(dx * dx + dy * dy);

        int dashLength = 10;
        int totalPatternLength = dashLength * 2; // dash + gap
        int numSegments = (int) (length / totalPatternLength) + 1;

        for (int i = 0; i < numSegments; i++) {
            double startT = (double) (i * totalPatternLength) / length;
            double endT = (double) (i * totalPatternLength + dashLength) / length;

            if (startT > 1.0) break;
            endT = Math.min(endT, 1.0);

            int startX = (int) Math.round(x1 + startT * dx);
            int startY = (int) Math.round(y1 + startT * dy);
            int endX = (int) Math.round(x1 + endT * dx);
            int endY = (int) Math.round(y1 + endT * dy);

            drawSolidLine(startX, startY, endX, endY, color, thickness);
        }
    }

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

    private void drawSolidCircle(int centerX, int centerY, int radius, Color color, int thickness) {
        int x = 0;
        int y = radius;
        int d = 3 - 2 * radius;

        drawCirclePoints(centerX, centerY, x, y, color, thickness);

        while (y >= x) {
            x++;

            if (d > 0) {
                y--;
                d = d + 4 * (x - y) + 10;
            } else {
                d = d + 4 * x + 6;
            }

            drawCirclePoints(centerX, centerY, x, y, color, thickness);
        }
    }

    private void drawCirclePoints(int centerX, int centerY, int x, int y, Color color, int thickness) {
        for (int t = 0; t < thickness; t++) {
            for (int s = -t; s <= t; s++) {
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

    private void drawDottedCircle(int centerX, int centerY, int radius, Color color, int thickness) {
        int numDots = radius * 6;  // More dots for larger radius

        for (int i = 0; i < numDots; i++) {
            double angle = 2 * Math.PI * i / numDots;
            int x = (int) Math.round(centerX + radius * Math.cos(angle));
            int y = (int) Math.round(centerY + radius * Math.sin(angle));

            for (int t = 0; t < thickness; t++) {
                for (int s = -t; s <= t; s++) {
                    for (int r = -t; r <= t; r++) {
                        setPixel(x + s, y + r, color);
                    }
                }
            }
        }
    }

    private void drawDashedCircle(int centerX, int centerY, int radius, Color color, int thickness) {
        int numSegments = 16;  // Number of segments to draw

        for (int i = 0; i < numSegments; i += 2) {  // Skip every other segment
            double startAngle = 2 * Math.PI * i / numSegments;
            double endAngle = 2 * Math.PI * (i + 1) / numSegments;

            drawCircleArc(centerX, centerY, radius, startAngle, endAngle, color, thickness);
        }
    }

    private void drawCircleArc(int centerX, int centerY, int radius, double startAngle, double endAngle, Color color, int thickness) {
        int steps = (int) (radius * (endAngle - startAngle));
        steps = Math.max(steps, 10);  // Ensure at least 10 steps

        for (int i = 0; i <= steps; i++) {
            double angle = startAngle + (endAngle - startAngle) * i / steps;
            int x = (int) Math.round(centerX + radius * Math.cos(angle));
            int y = (int) Math.round(centerY + radius * Math.sin(angle));

            for (int t = 0; t < thickness; t++) {
                for (int s = -t; s <= t; s++) {
                    for (int r = -t; r <= t; r++) {
                        setPixel(x + s, y + r, color);
                    }
                }
            }
        }
    }

    private void fillCircle(int centerX, int centerY, int radius, Color color) {
        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {
                if (x*x + y*y <= radius*radius) {
                    setPixel(centerX + x, centerY + y, color);
                }
            }
        }
    }

    public void drawRectangle(int x1, int y1, int x2, int y2, Color color, int style, int thickness, boolean filled) {
        int left = Math.min(x1, x2);
        int top = Math.min(y1, y2);
        int right = Math.max(x1, x2);
        int bottom = Math.max(y1, y2);

        if (filled) {
            fillRectangle(left, top, right, bottom, color);
        }

        drawLine(left, top, right, top, color, style, thickness);      // Top
        drawLine(left, bottom, right, bottom, color, style, thickness); // Bottom
        drawLine(left, top, left, bottom, color, style, thickness);     // Left
        drawLine(right, top, right, bottom, color, style, thickness);   // Right
    }

    public void fillRectangle(int left, int top, int right, int bottom, Color color) {
        for (int y = top; y <= bottom; y++) {
            for (int x = left; x <= right; x++) {
                setPixel(x, y, color);
            }
        }
    }

    public void fillPolygon(List<Point> points, Color color) {
        if (points.size() < 3) return;

        // Find bounding box
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

    public boolean isPointInPolygon(int x, int y, List<Point> points) {
        boolean inside = false;
        int nPoints = points.size();

        for (int i = 0, j = nPoints - 1; i < nPoints; j = i++) {
            Point pi = points.get(i);
            Point pj = points.get(j);

            if (((pi.y > y) != (pj.y > y)) &&
                    (x < (pj.x - pi.x) * (y - pi.y) / (pj.y - pi.y) + pi.x)) {
                inside = !inside;
            }
        }

        return inside;
    }

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

    public void repaint(Graphics g) {
        g.drawImage(img, 0, 0, null);
    }
}
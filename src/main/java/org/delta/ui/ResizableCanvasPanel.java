package org.delta.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A panel that contains the drawing canvas and provides resizing functionality.
 * This class manages the dynamic resizing of the drawing area by providing resize
 * handles at the edges and corners of the canvas.
 */
public class ResizableCanvasPanel extends JPanel {
    // The canvas that will be resized
    private DrawingCanvas canvas;

    // Size constraints
    private int initialWidth = 800;
    private int initialHeight = 600;
    private int minWidth = 200;       // Minimum allowed width
    private int minHeight = 200;      // Minimum allowed height
    private int maxWidth = 3000;      // Maximum allowed width
    private int maxHeight = 3000;     // Maximum allowed height

    // Resize operation state
    private boolean resizing = false;
    private Point resizeStartPoint;
    private Dimension originalSize;

    // Direction of resize - uses a numeric code for efficiency:
    // 0=none, 1=N, 2=E, 3=S, 4=W, 5=NE, 6=SE, 7=SW, 8=NW
    private int resizeDirection = 0;

    // Size of the resize handles in pixels
    private int handleSize = 10;

    // Status label to show current dimensions
    private JLabel statusLabel;

    /**
     * Creates a new resizable panel containing the drawing canvas
     *
     * @param canvas The drawing canvas to contain and resize
     * @param statusLabel Label to display current canvas dimensions
     */
    public ResizableCanvasPanel(DrawingCanvas canvas, JLabel statusLabel) {
        this.canvas = canvas;
        this.statusLabel = statusLabel;
        setLayout(null); // Use absolute positioning for more control

        // Set initial canvas size and position
        canvas.setSize(initialWidth, initialHeight);
        canvas.setLocation(0, 0);
        add(canvas);

        // Set panel size to be slightly larger than canvas to show border
        setPreferredSize(new Dimension(initialWidth + 2, initialHeight + 2));

        // Add mouse listeners for resize operations
        addMouseListener(new ResizeMouseAdapter());
        addMouseMotionListener(new ResizeMouseAdapter());

        // Initialize status display
        updateStatusLabel();
    }

    /**
     * Custom painting to draw the canvas border and resize handles
     * This method renders visual cues for the resize functionality
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Draw a border around the canvas for visual separation
        g2d.setColor(Color.DARK_GRAY);
        g2d.drawRect(0, 0, canvas.getWidth() + 1, canvas.getHeight() + 1);

        // Draw resize handles in black
        g2d.setColor(Color.BLACK);

        // Draw corner handles for diagonal resizing
        g2d.fillRect(0, 0, handleSize, handleSize); // NW
        g2d.fillRect(canvas.getWidth() - handleSize + 1, 0, handleSize, handleSize); // NE
        g2d.fillRect(0, canvas.getHeight() - handleSize + 1, handleSize, handleSize); // SW
        g2d.fillRect(canvas.getWidth() - handleSize + 1, canvas.getHeight() - handleSize + 1, handleSize, handleSize); // SE

        // Draw edge handles for single-direction resizing
        g2d.fillRect((canvas.getWidth() - handleSize) / 2, 0, handleSize, handleSize); // N
        g2d.fillRect(canvas.getWidth() - handleSize + 1, (canvas.getHeight() - handleSize) / 2, handleSize, handleSize); // E
        g2d.fillRect((canvas.getWidth() - handleSize) / 2, canvas.getHeight() - handleSize + 1, handleSize, handleSize); // S
        g2d.fillRect(0, (canvas.getHeight() - handleSize) / 2, handleSize, handleSize); // W
    }

    /**
     * Determines which resize direction a point corresponds to
     * This method checks if the point is on one of the resize handles
     *
     * @param p Point to check
     * @return Direction code (0=none, 1=N, 2=E, 3=S, 4=W, 5=NE, 6=SE, 7=SW, 8=NW)
     */
    private int getResizeDirection(Point p) {
        int x = p.x;
        int y = p.y;
        int w = canvas.getWidth();
        int h = canvas.getHeight();

        // Check for each possible resize region
        boolean north = y < handleSize;
        boolean south = y > h - handleSize;
        boolean east = x > w - handleSize;
        boolean west = x < handleSize;

        // Return appropriate direction code based on region
        if (north && west) return 8; // NW
        if (north && east) return 5; // NE
        if (south && west) return 7; // SW
        if (south && east) return 6; // SE
        if (north) return 1; // N
        if (east) return 2; // E
        if (south) return 3; // S
        if (west) return 4; // W

        return 0; // No direction (not on a handle)
    }

    /**
     * Updates the canvas size, keeping it within defined limits
     * This is the core method that actually performs the resize operation
     *
     * @param width New width in pixels
     * @param height New height in pixels
     */
    private void updateCanvasSize(int width, int height) {
        // Ensure dimensions are within limits
        width = Math.max(minWidth, Math.min(maxWidth, width));
        height = Math.max(minHeight, Math.min(maxHeight, height));

        // Update canvas size
        canvas.setSize(width, height);

        // Update panel size (slightly larger to show border)
        setPreferredSize(new Dimension(width + 2, height + 2));
        revalidate(); // Tell the layout manager to recalculate layout
        repaint();    // Request a repaint to show the new size

        // Update the status display with new dimensions
        updateStatusLabel();
    }

    /**
     * Updates the status label with current canvas dimensions
     * This provides user feedback about the current canvas size
     */
    private void updateStatusLabel() {
        if (statusLabel != null) {
            statusLabel.setText("Canvas size: " + canvas.getWidth() + " x " + canvas.getHeight() + " px");
        }
    }

    /**
     * Mouse adapter that handles resize operations
     * This inner class encapsulates all mouse interaction for resizing
     */
    private class ResizeMouseAdapter extends MouseAdapter {
        /**
         * Handles mouse press events - starts a resize operation if on a handle
         */
        @Override
        public void mousePressed(MouseEvent e) {
            int direction = getResizeDirection(e.getPoint());
            if (direction != 0) {
                // Start resize operation
                resizing = true;
                resizeDirection = direction;
                resizeStartPoint = e.getPoint();
                originalSize = canvas.getSize();
                setCursor(getResizeCursor(direction));
            }
        }

        /**
         * Handles mouse release events - completes a resize operation
         */
        @Override
        public void mouseReleased(MouseEvent e) {
            if (resizing) {
                // End resize operation
                resizing = false;

                // Reset cursor based on current position
                int direction = getResizeDirection(e.getPoint());
                setCursor(getResizeCursor(direction));
            }
        }

        /**
         * Handles mouse drag events - updates canvas size during resize
         */
        @Override
        public void mouseDragged(MouseEvent e) {
            if (resizing) {
                // Calculate the drag distance
                int dx = e.getX() - resizeStartPoint.x;
                int dy = e.getY() - resizeStartPoint.y;

                // Start with original size
                int newWidth = originalSize.width;
                int newHeight = originalSize.height;

                // Apply changes based on resize direction
                switch (resizeDirection) {
                    case 1: // N - Top edge
                        newHeight = originalSize.height - dy;
                        break;
                    case 2: // E - Right edge
                        newWidth = originalSize.width + dx;
                        break;
                    case 3: // S - Bottom edge
                        newHeight = originalSize.height + dy;
                        break;
                    case 4: // W - Left edge
                        newWidth = originalSize.width - dx;
                        break;
                    case 5: // NE - Top-right corner
                        newWidth = originalSize.width + dx;
                        newHeight = originalSize.height - dy;
                        break;
                    case 6: // SE - Bottom-right corner
                        newWidth = originalSize.width + dx;
                        newHeight = originalSize.height + dy;
                        break;
                    case 7: // SW - Bottom-left corner
                        newWidth = originalSize.width - dx;
                        newHeight = originalSize.height + dy;
                        break;
                    case 8: // NW - Top-left corner
                        newWidth = originalSize.width - dx;
                        newHeight = originalSize.height - dy;
                        break;
                }

                // Update the canvas with the new size
                updateCanvasSize(newWidth, newHeight);
            }
        }

        /**
         * Handles mouse movement - updates cursor based on position over handles
         */
        @Override
        public void mouseMoved(MouseEvent e) {
            int direction = getResizeDirection(e.getPoint());
            setCursor(getResizeCursor(direction));
        }

        /**
         * Handles mouse exiting the panel - resets cursor
         */
        @Override
        public void mouseExited(MouseEvent e) {
            // When mouse leaves the panel, reset cursor to default
            if (!resizing) {
                setCursor(Cursor.getDefaultCursor());
            }
        }

        /**
         * Handles mouse entering the panel - sets appropriate cursor
         */
        @Override
        public void mouseEntered(MouseEvent e) {
            // Set cursor based on position when mouse enters
            int direction = getResizeDirection(e.getPoint());
            setCursor(getResizeCursor(direction));
        }

        /**
         * Returns the appropriate cursor for a resize direction
         * Maps direction codes to the corresponding resize cursor
         *
         * @param direction Direction code (0-8)
         * @return Appropriate resize cursor
         */
        private Cursor getResizeCursor(int direction) {
            switch (direction) {
                case 1: return Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
                case 2: return Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
                case 3: return Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
                case 4: return Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
                case 5: return Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
                case 6: return Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
                case 7: return Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR);
                case 8: return Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
                default: return Cursor.getDefaultCursor();
            }
        }
    }

    /**
     * Gets the current canvas size
     *
     * @return Canvas dimensions as a Dimension object
     */
    public Dimension getCanvasSize() {
        return canvas.getSize();
    }

    /**
     * Sets the canvas size programmatically
     * This method can be called from outside to change the canvas size
     *
     * @param width Width in pixels
     * @param height Height in pixels
     */
    public void setCanvasSize(int width, int height) {
        updateCanvasSize(width, height);
    }
}
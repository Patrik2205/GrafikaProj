package org.delta.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A panel that contains the drawing canvas and provides resizing functionality
 */
public class ResizableCanvasPanel extends JPanel {
    private DrawingCanvas canvas;
    private int initialWidth = 800;
    private int initialHeight = 600;
    private int minWidth = 200;
    private int minHeight = 200;
    private int maxWidth = 3000;
    private int maxHeight = 3000;

    // For resize handling
    private boolean resizing = false;
    private Point resizeStartPoint;
    private Dimension originalSize;
    private int resizeDirection = 0; // 0=none, 1=N, 2=E, 3=S, 4=W, 5=NE, 6=SE, 7=SW, 8=NW

    // Resize handle size
    private int handleSize = 10;

    // Status label to show dimensions
    private JLabel statusLabel;

    public ResizableCanvasPanel(DrawingCanvas canvas, JLabel statusLabel) {
        this.canvas = canvas;
        this.statusLabel = statusLabel;
        setLayout(null); // Use absolute positioning

        // Set canvas size and position
        canvas.setSize(initialWidth, initialHeight);
        canvas.setLocation(0, 0);
        add(canvas);

        // Set panel size to be slightly larger than canvas to show border
        setPreferredSize(new Dimension(initialWidth + 2, initialHeight + 2));

        addMouseListener(new ResizeMouseAdapter());
        addMouseMotionListener(new ResizeMouseAdapter());

        updateStatusLabel();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Draw border around canvas
        g2d.setColor(Color.DARK_GRAY);
        g2d.drawRect(0, 0, canvas.getWidth() + 1, canvas.getHeight() + 1);

        // Draw resize handles
        g2d.setColor(Color.BLACK);

        // Corners
        g2d.fillRect(0, 0, handleSize, handleSize); // NW
        g2d.fillRect(canvas.getWidth() - handleSize + 1, 0, handleSize, handleSize); // NE
        g2d.fillRect(0, canvas.getHeight() - handleSize + 1, handleSize, handleSize); // SW
        g2d.fillRect(canvas.getWidth() - handleSize + 1, canvas.getHeight() - handleSize + 1, handleSize, handleSize); // SE

        // Edges
        g2d.fillRect((canvas.getWidth() - handleSize) / 2, 0, handleSize, handleSize); // N
        g2d.fillRect(canvas.getWidth() - handleSize + 1, (canvas.getHeight() - handleSize) / 2, handleSize, handleSize); // E
        g2d.fillRect((canvas.getWidth() - handleSize) / 2, canvas.getHeight() - handleSize + 1, handleSize, handleSize); // S
        g2d.fillRect(0, (canvas.getHeight() - handleSize) / 2, handleSize, handleSize); // W
    }

    private int getResizeDirection(Point p) {
        int x = p.x;
        int y = p.y;
        int w = canvas.getWidth();
        int h = canvas.getHeight();

        boolean north = y < handleSize;
        boolean south = y > h - handleSize;
        boolean east = x > w - handleSize;
        boolean west = x < handleSize;

        if (north && west) return 8; // NW
        if (north && east) return 5; // NE
        if (south && west) return 7; // SW
        if (south && east) return 6; // SE
        if (north) return 1; // N
        if (east) return 2; // E
        if (south) return 3; // S
        if (west) return 4; // W

        return 0; // No direction
    }

    private void updateCanvasSize(int width, int height) {
        // Ensure dimensions are within limits
        width = Math.max(minWidth, Math.min(maxWidth, width));
        height = Math.max(minHeight, Math.min(maxHeight, height));

        // Update canvas size
        canvas.setSize(width, height);

        // Update panel size (slightly larger to show border)
        setPreferredSize(new Dimension(width + 2, height + 2));
        revalidate();
        repaint();

        updateStatusLabel();
    }

    private void updateStatusLabel() {
        if (statusLabel != null) {
            statusLabel.setText("Canvas size: " + canvas.getWidth() + " x " + canvas.getHeight() + " px");
        }
    }

    private class ResizeMouseAdapter extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            int direction = getResizeDirection(e.getPoint());
            if (direction != 0) {
                resizing = true;
                resizeDirection = direction;
                resizeStartPoint = e.getPoint();
                originalSize = canvas.getSize();
                setCursor(getResizeCursor(direction));
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (resizing) {
                resizing = false;

                // Reset cursor based on current position to ensure proper cursor
                int direction = getResizeDirection(e.getPoint());
                setCursor(getResizeCursor(direction));
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (resizing) {
                int dx = e.getX() - resizeStartPoint.x;
                int dy = e.getY() - resizeStartPoint.y;

                int newWidth = originalSize.width;
                int newHeight = originalSize.height;

                // Apply changes based on resize direction
                switch (resizeDirection) {
                    case 1: // N
                        newHeight = originalSize.height - dy;
                        break;
                    case 2: // E
                        newWidth = originalSize.width + dx;
                        break;
                    case 3: // S
                        newHeight = originalSize.height + dy;
                        break;
                    case 4: // W
                        newWidth = originalSize.width - dx;
                        break;
                    case 5: // NE
                        newWidth = originalSize.width + dx;
                        newHeight = originalSize.height - dy;
                        break;
                    case 6: // SE
                        newWidth = originalSize.width + dx;
                        newHeight = originalSize.height + dy;
                        break;
                    case 7: // SW
                        newWidth = originalSize.width - dx;
                        newHeight = originalSize.height + dy;
                        break;
                    case 8: // NW
                        newWidth = originalSize.width - dx;
                        newHeight = originalSize.height - dy;
                        break;
                }

                updateCanvasSize(newWidth, newHeight);
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            int direction = getResizeDirection(e.getPoint());
            setCursor(getResizeCursor(direction));
        }

        @Override
        public void mouseExited(MouseEvent e) {
            // When mouse leaves the panel, reset cursor to default
            if (!resizing) {
                setCursor(Cursor.getDefaultCursor());
            }
        }

        // Also add this method to handle mouse entering
        @Override
        public void mouseEntered(MouseEvent e) {
            // Set cursor based on position when mouse enters
            int direction = getResizeDirection(e.getPoint());
            setCursor(getResizeCursor(direction));
        }

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

    public Dimension getCanvasSize() {
        return canvas.getSize();
    }

    public void setCanvasSize(int width, int height) {
        updateCanvasSize(width, height);
    }
}
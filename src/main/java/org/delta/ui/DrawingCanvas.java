package org.delta.ui;
import org.delta.raster.CustomRaster;
import org.delta.shapes.*;
import org.delta.shapes.Shape;
import org.delta.util.EraserMode;
import org.delta.util.LineStyle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class DrawingCanvas extends JPanel {
    private EraserMode eraserMode = EraserMode.OBJECT;
    private int eraserSize = 10;
    private Point lastEraserPoint = null;

    private CustomRaster raster;
    private List<Shape> shapes = new ArrayList<>();
    private Shape currentShape = null;
    private Shape selectedShape = null;
    private Point dragStart = null;
    private Point lastPoint = null;
    private List<Point> polygonPoints = new ArrayList<>();

    private String currentTool = "Line";
    private Color currentColor = Color.BLACK;
    private int currentThickness = 2;
    private int currentLineStyle = LineStyle.SOLID;

    private Point selectedControlPoint = null;
    private boolean movingShape = false;
    private boolean movingPoint = false;
    private boolean isRightClick = false;

    private PaintFrame parent;

    // Add these methods
    public void setEraserMode(EraserMode mode) {
        this.eraserMode = mode;
    }

    public void setEraserSize(int size) {
        this.eraserSize = size;
    }

    public DrawingCanvas(PaintFrame parent) {
        this.parent = parent;
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(800, 600));

        // Initialize raster with initial size
        raster = new CustomRaster(800, 600);

        addMouseListeners();
    }

    private void addMouseListeners() {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastPoint = new Point(e.getX(), e.getY());
                dragStart = lastPoint;

                if (currentTool.equals("Select")) {
                    // First check if we already have a selected shape
                    if (selectedShape != null) {
                        // We already have a shape selected, check if user clicked on a control point
                        selectedControlPoint = selectedShape.getNearestControlPoint(lastPoint);

                        if (selectedControlPoint != null) {
                            // Clicked on a control point - prepare for dragging/resizing
                            redrawCanvas();
                            return;
                        } else if (selectedShape.contains(lastPoint)) {
                            // Clicked on the shape but not on a control point
                            // Prepare to move the whole shape
                            redrawCanvas();
                            return;
                        } else {
                            // Clicked outside - deselect
                            selectedShape = null;
                            selectedControlPoint = null;
                            redrawCanvas();
                            return;
                        }
                    }

                    // No shape selected yet - try to select one
                    selectedShape = findShapeAt(lastPoint);
                    if (selectedShape != null) {
                        // Found a shape to select
                        redrawCanvas();
                    }
                    return;
                }

                // Rest of the code for other tools
                if (currentTool.equals("Polygon")) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        // Left-click adds a point
                        polygonPoints.add(new Point(lastPoint)); // Make sure to create a new Point

                        // Update currentShape or create a new one if needed
                        if (currentShape == null || !(currentShape instanceof PolygonShape)) {
                            currentShape = new PolygonShape(new ArrayList<>(polygonPoints),
                                    currentColor, currentThickness, currentLineStyle);
                        } else {
                            ((PolygonShape)currentShape).setPoints(new ArrayList<>(polygonPoints));
                        }

                        // Always redraw to show the updated polygon
                        redrawCanvas();
                        return;
                    } else if (e.getButton() == MouseEvent.BUTTON3 && polygonPoints.size() >= 3) {
                        // Right-click finishes the polygon if we have at least 3 points
                        if (currentShape instanceof PolygonShape) {
                            // Add the complete polygon to our shapes list
                            shapes.add(currentShape);

                            // Reset state for next polygon
                            polygonPoints.clear();
                            currentShape = null;
                            redrawCanvas();
                        }
                        return;
                    }
                }

                if (currentTool.equals("Eraser")) {
                    if (eraserMode == EraserMode.OBJECT) {
                        // Existing object eraser functionality
                        Shape shapeToRemove = findShapeAt(lastPoint);
                        if (shapeToRemove != null) {
                            shapes.remove(shapeToRemove);
                            redrawCanvas();
                        }
                    } else if (eraserMode == EraserMode.PIXEL) {
                        // Pixel eraser functionality
                        lastEraserPoint = lastPoint;
                        raster.erasePixels(lastPoint.x, lastPoint.y, eraserSize);
                        repaint();
                    }
                    return;
                }

                selectedShape = null;

                switch (currentTool) {
                    case "Line":
                        currentShape = new LineShape(lastPoint, lastPoint, currentColor, currentThickness, currentLineStyle);
                        break;
                    case "Rectangle":
                        currentShape = new RectangleShape(lastPoint, lastPoint, currentColor, currentThickness, currentLineStyle);
                        break;
                    case "Square":
                        currentShape = new SquareShape(lastPoint, lastPoint, currentColor, currentThickness, currentLineStyle);
                        break;
                    case "Circle":
                        currentShape = new CircleShape(lastPoint, lastPoint, currentColor, currentThickness, currentLineStyle);
                        break;
                }

                redrawCanvas();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (currentTool.equals("Eraser") && eraserMode == EraserMode.PIXEL) {
                    lastEraserPoint = null;
                    return;
                }

                if (currentShape != null && !currentTool.equals("Polygon")) {
                    shapes.add(currentShape);
                    currentShape = null;
                    redrawCanvas();
                } else if (currentTool.equals("Select")) {
                    movingShape = false;
                    movingPoint = false;
                    selectedControlPoint = null;
                    isRightClick = false;
                    redrawCanvas();
                    return;
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                Point currentPoint = new Point(e.getX(), e.getY());

                if (currentTool.equals("Select") && selectedShape != null) {
                    int dx = currentPoint.x - lastPoint.x;
                    int dy = currentPoint.y - lastPoint.y;

                    // Check if we're dragging with right mouse button
                    boolean rightDrag = (e.getModifiersEx() & MouseEvent.BUTTON3_DOWN_MASK) != 0;

                    if (selectedControlPoint != null) {
                        // We're manipulating a control point
                        if (rightDrag && (selectedShape instanceof PolygonShape ||
                                selectedShape instanceof RectangleShape ||
                                selectedShape instanceof SquareShape)) {
                            // Right-drag on control point - direct movement of the point
                            if (selectedShape instanceof PolygonShape) {
                                ((PolygonShape) selectedShape).movePoint(selectedControlPoint, dx, dy);
                            } else if (selectedShape instanceof RectangleShape) {
                                ((RectangleShape) selectedShape).moveCorner(selectedControlPoint, dx, dy);
                            } else if (selectedShape instanceof SquareShape) {
                                ((SquareShape) selectedShape).moveCorner(selectedControlPoint, dx, dy);
                            }
                        } else {
                            // Left-drag on control point - normal resize
                            selectedShape.resizeByPoint(selectedControlPoint, dx, dy);
                        }
                    } else if (selectedShape.contains(lastPoint)) {
                        // We're moving the entire shape
                        selectedShape.move(dx, dy);
                    }

                    lastPoint = currentPoint;
                    redrawCanvas();
                    return;
                }

                // Other mouse dragging handlers (same as before)
                if (currentTool.equals("Eraser") && eraserMode == EraserMode.PIXEL) {
                    // Perform pixel erasing along the drag path
                    if (lastEraserPoint != null) {
                        raster.erasePixelsLine(lastEraserPoint.x, lastEraserPoint.y,
                                currentPoint.x, currentPoint.y, eraserSize);
                        lastEraserPoint = currentPoint;
                        repaint();
                    }
                    return;
                }

                if (currentShape != null && !currentTool.equals("Polygon")) {
                    switch (currentTool) {
                        case "Line":
                            ((LineShape)currentShape).setEndPoint(currentPoint);
                            break;
                        case "Rectangle":
                        case "Square":
                        case "Circle":
                            currentShape.setEndPoint(currentPoint);
                            break;
                    }
                    redrawCanvas();
                }

                lastPoint = currentPoint;
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
    }

    private Shape findShapeAt(Point p) {
        // Search in reverse order (top to bottom in z-order)
        for (int i = shapes.size() - 1; i >= 0; i--) {
            if (shapes.get(i).contains(p)) {
                return shapes.get(i);
            }
        }
        return null;
    }

    private void redrawCanvas() {
        raster.clear();

        // Draw all shapes
        for (Shape shape : shapes) {
            shape.draw(raster);
        }

        // Draw current shape (preview)
        if (currentShape != null) {
            currentShape.draw(raster);
        }

        // Draw selected shape's control points
        if (selectedShape != null) {
            selectedShape.drawControlPoints(raster);
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        raster.repaint(g);
    }

    public void setSize(int width, int height) {
        super.setSize(width, height);
        setPreferredSize(new Dimension(width, height));

        // Create a new raster with the new size
        CustomRaster newRaster = new CustomRaster(width, height);

        // Copy the contents of the old raster to the new one
        if (raster != null) {
            Graphics g = newRaster.getGraphics();
            g.drawImage(raster.getImg(), 0, 0, null);
            g.dispose();
        }

        // Replace the old raster
        raster = newRaster;

        // Redraw everything
        redrawCanvas();
    }

    public void clearCanvas() {
        shapes.clear();
        currentShape = null;
        selectedShape = null;
        polygonPoints.clear();
        redrawCanvas();
    }

    public void setCurrentTool(String tool) {
        currentTool = tool;
        if (!tool.equals("Polygon")) {
            polygonPoints.clear();
            currentShape = null;
        }
        if (!tool.equals("Select")) {
            selectedShape = null;
        }
        redrawCanvas();
    }

    public void setCurrentColor(Color color) {
        currentColor = color;
    }

    public void setCurrentThickness(int thickness) {
        currentThickness = thickness;
    }

    public void setCurrentLineStyle(int style) {
        currentLineStyle = style;
    }
}
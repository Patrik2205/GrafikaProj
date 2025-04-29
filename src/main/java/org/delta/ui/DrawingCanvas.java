package org.delta.ui;
import org.delta.raster.CustomRaster;
import org.delta.shapes.*;
import org.delta.shapes.Shape;
import org.delta.util.EraserMode;
import org.delta.util.LineStyle;
import org.delta.util.FillMode;

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
    private int selectedEdge = -1;
    private boolean movingShape = false;
    private boolean movingPoint = false;
    private boolean isRightClick = false;

    private PaintFrame parent;

    private FillMode fillMode = FillMode.OBJECT;

    // Add this method to set fill mode
    public void setFillMode(FillMode mode) {
        this.fillMode = mode;
    }

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
                isRightClick = (e.getButton() == MouseEvent.BUTTON3);

                if (currentTool.equals("Select")) {
                    // First check if we already have a selected shape
                    if (selectedShape != null) {
                        // We already have a shape selected, check if user clicked on a control point
                        selectedControlPoint = selectedShape.getNearestControlPoint(lastPoint);

                        if (selectedControlPoint != null) {
                            // Clicked on a control point - prepare for dragging/resizing
                            movingPoint = true;
                            selectedEdge = -1;
                            redrawCanvas();
                            return;
                        }

                        // Check if we're clicking on an edge (for rectangle/square)
                        selectedEdge = -1;
                        if (selectedShape instanceof RectangleShape) {
                            selectedEdge = ((RectangleShape) selectedShape).getNearestEdge(lastPoint);
                        } else if (selectedShape instanceof SquareShape) {
                            selectedEdge = ((SquareShape) selectedShape).getNearestEdge(lastPoint);
                        }

                        if (selectedEdge >= 0) {
                            // Clicked on an edge
                            redrawCanvas();
                            return;
                        } else if (selectedShape.contains(lastPoint)) {
                            // Clicked on the shape but not on a control point or edge
                            movingShape = true;
                            redrawCanvas();
                            return;
                        } else {
                            // Clicked outside - deselect
                            selectedShape = null;
                            selectedControlPoint = null;
                            selectedEdge = -1;
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

                if (currentTool.equals("Fill")) {
                    if (fillMode == FillMode.OBJECT) {
                        // Object fill mode - find and fill a shape
                        Shape shapeToFill = findShapeAt(lastPoint);
                        if (shapeToFill != null && shapeToFill.canBeFilled()) {
                            shapeToFill.setFilled(true);
                            shapeToFill.setFillColor(currentColor);
                            redrawCanvas();
                        }
                    } else if (fillMode == FillMode.FLOOD) {
                        // Flood fill mode - fill connected pixels
                        raster.floodFill(lastPoint.x, lastPoint.y, currentColor);
                        repaint();
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
                    // Don't reset selectedEdge here so it stays highlighted
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

                    // Get the current drag type
                    boolean rightDrag = isRightClick || (e.getModifiersEx() & MouseEvent.BUTTON3_DOWN_MASK) != 0;

                    // CASE 1: Manipulating a control point
                    if (selectedControlPoint != null && movingPoint) {
                        if (rightDrag) {
                            // Right-drag on control point behavior
                            if (selectedShape instanceof CircleShape) {
                                // For circles, right-drag always moves the entire circle
                                selectedShape.move(dx, dy);
                            } else if (selectedShape instanceof PolygonShape) {
                                ((PolygonShape) selectedShape).movePoint(selectedControlPoint, dx, dy);
                            } else if (selectedShape instanceof RectangleShape) {
                                ((RectangleShape) selectedShape).moveCorner(selectedControlPoint, dx, dy);
                            } else if (selectedShape instanceof SquareShape) {
                                ((SquareShape) selectedShape).moveCorner(selectedControlPoint, dx, dy);
                            } else {
                                // Direct point movement for other shapes
                                selectedShape.resizeByPoint(selectedControlPoint, dx, dy);
                            }
                        } else {
                            // Left-drag on control point - constrained resize
                            selectedShape.resizeByPoint(selectedControlPoint, dx, dy);
                        }
                    }
                    // CASE 2: Manipulating an edge
                    else if (selectedEdge >= 0) {
                        if (rightDrag) {
                            // Right-drag on edge - move the entire shape
                            selectedShape.move(dx, dy);
                        } else {
                            // Left-drag on edge - resize in specific direction
                            if (selectedShape instanceof RectangleShape) {
                                ((RectangleShape) selectedShape).resizeByEdge(selectedEdge, dx, dy);
                            } else if (selectedShape instanceof SquareShape) {
                                ((SquareShape) selectedShape).resizeByEdge(selectedEdge, dx, dy);
                            }
                        }
                    }
                    // CASE 3: Moving the entire shape
                    else if (movingShape || selectedShape.contains(lastPoint)) {
                        // We're moving the entire shape
                        movingShape = true;
                        selectedShape.move(dx, dy);
                    }

                    lastPoint = currentPoint;
                    redrawCanvas();
                    return;
                }

                // Other mouse dragging handlers
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

            // Highlight selected edge if applicable
            if (selectedEdge >= 0) {
                highlightSelectedEdge(raster, selectedShape, selectedEdge);
            }
        }

        repaint();
    }

    // Helper method to highlight the selected edge
    private void highlightSelectedEdge(CustomRaster raster, Shape shape, int edgeIndex) {
        if (shape instanceof RectangleShape || shape instanceof SquareShape) {
            List<Point> points;

            if (shape instanceof RectangleShape) {
                points = ((RectangleShape)shape).getCornerPoints();
            } else {
                points = ((SquareShape)shape).getCornerPoints();
            }

            if (points != null && points.size() == 4) {
                Point p1 = points.get(edgeIndex);
                Point p2 = points.get((edgeIndex + 1) % 4);

                // Draw a highlighted line over the selected edge
                Color highlightColor = new Color(0, 200, 255); // Light blue
                raster.drawLine(p1.x, p1.y, p2.x, p2.y, highlightColor, LineStyle.DASHED, 1);
            }
        }
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
        selectedEdge = -1;
        polygonPoints.clear();
        redrawCanvas();
    }

    public void setCurrentTool(String tool) {
        // If we're switching away from the Polygon tool and have a valid polygon in progress
        if (currentTool.equals("Polygon") && !tool.equals("Polygon") &&
                polygonPoints.size() >= 3 && currentShape instanceof PolygonShape) {
            // Save the polygon to the shapes list before switching tools
            shapes.add(currentShape);
        }

        currentTool = tool;

        if (!tool.equals("Polygon")) {
            polygonPoints.clear();
            currentShape = null;
        }
        if (!tool.equals("Select")) {
            selectedShape = null;
            selectedEdge = -1;
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
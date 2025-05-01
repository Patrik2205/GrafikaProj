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

/**
 * Main drawing area that handles shape creation, editing, and user interaction.
 * Manages all drawn shapes and implements the core drawing functionality.
 */
public class DrawingCanvas extends JPanel {
    // Eraser-related properties
    private EraserMode eraserMode = EraserMode.OBJECT;
    private int eraserSize = 10;
    private Point lastEraserPoint = null;

    // Drawing data structures
    private CustomRaster raster;
    private List<Shape> shapes = new ArrayList<>();
    private Shape currentShape = null;
    private Shape selectedShape = null;
    private Point dragStart = null;
    private Point lastPoint = null;
    private List<Point> polygonPoints = new ArrayList<>();

    // Current drawing settings
    private String currentTool = "Line";
    private Color currentColor = Color.BLACK;
    private int currentThickness = 2;
    private int currentLineStyle = LineStyle.SOLID;

    // Selection and manipulation state
    private Point selectedControlPoint = null;
    private int selectedEdge = -1;
    private boolean movingShape = false;
    private boolean movingPoint = false;
    private boolean isRightClick = false;

    private PixelEraserShape currentEraserShape = null;

    // Parent reference for communication
    private PaintFrame parent;

    // Fill mode setting
    private FillMode fillMode = FillMode.OBJECT;

    /**
     * Sets the fill mode to use (object or flood fill)
     * @param mode The fill mode to use
     */
    public void setFillMode(FillMode mode) {
        this.fillMode = mode;
    }

    /**
     * Sets the eraser mode (object or pixel)
     * @param mode The eraser mode to use
     */
    public void setEraserMode(EraserMode mode) {
        this.eraserMode = mode;
    }

    /**
     * Sets the eraser size for pixel erasing
     * @param size Eraser size in pixels
     */
    public void setEraserSize(int size) {
        this.eraserSize = size;
    }

    /**
     * Creates a new drawing canvas
     * @param parent Parent frame for communication
     */
    public DrawingCanvas(PaintFrame parent) {
        this.parent = parent;
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(800, 600));

        // Initialize raster with initial size
        raster = new CustomRaster(800, 600);

        addMouseListeners();
    }

    /**
     * Adds mouse listeners to handle drawing and shape manipulation
     * This is the core of the user interaction logic
     */
    private void addMouseListeners() {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastPoint = new Point(e.getX(), e.getY());
                dragStart = lastPoint;
                isRightClick = (e.getButton() == MouseEvent.BUTTON3);

                // Handle Selection Tool
                if (currentTool.equals("Select")) {
                    handleSelectionToolPress();
                    return;
                }

                // Handle Fill Tool
                if (currentTool.equals("Fill")) {
                    handleFillToolPress();
                    return;
                }

                // Handle Polygon Tool
                if (currentTool.equals("Polygon")) {
                    handlePolygonToolPress(e);
                    return;
                }

                // Handle Eraser Tool
                if (currentTool.equals("Eraser")) {
                    handleEraserToolPress();
                    return;
                }

                // Deselect any selected shape when using other tools
                selectedShape = null;

                // Create appropriate shape based on selected tool
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

            /**
             * Handles mouse press for the Selection tool
             */
            private void handleSelectionToolPress() {
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
            }

            /**
             * Handles mouse press for the Fill tool
             */
            private void handleFillToolPress() {
                if (fillMode == FillMode.OBJECT) {
                    // Object fill mode - find and fill a shape
                    Shape shapeToFill = findShapeAt(lastPoint);
                    if (shapeToFill != null && shapeToFill.canBeFilled()) {
                        shapeToFill.setFilled(true);
                        shapeToFill.setFillColor(currentColor);
                        redrawCanvas();
                    }
                } else if (fillMode == FillMode.FLOOD) {
                    // Create a FloodFillShape and add it to the shapes list
                    FloodFillShape floodFill = new FloodFillShape(new Point(lastPoint), currentColor);
                    shapes.add(floodFill);

                    // Also perform the fill visually for immediate feedback
                    raster.floodFill(lastPoint.x, lastPoint.y, currentColor);
                    redrawCanvas();
                }
            }

            /**
             * Handles mouse press for the Polygon tool
             */
            private void handlePolygonToolPress(MouseEvent e) {
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
                }
            }

            /**
             * Handles mouse press for the Eraser tool
             */
            private void handleEraserToolPress() {
                if (eraserMode == EraserMode.OBJECT) {
                    // Object eraser - remove the shape under the cursor
                    Shape shapeToRemove = findShapeAt(lastPoint);
                    if (shapeToRemove != null) {
                        shapes.remove(shapeToRemove);
                        redrawCanvas();
                    }
                } else if (eraserMode == EraserMode.PIXEL) {
                    // Create a new PixelEraserShape
                    Color backgroundColor = Color.WHITE; // Or get from your clear color setting
                    currentEraserShape = new PixelEraserShape(eraserSize, backgroundColor);
                    currentEraserShape.addPoint(lastPoint);
                    lastEraserPoint = lastPoint;

                    // Apply the erasure visually while drawing
                    raster.erasePixels(lastPoint.x, lastPoint.y, eraserSize);
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (currentTool.equals("Eraser") && eraserMode == EraserMode.PIXEL) {
                    // Finalize the eraser shape and add it to the shapes list
                    if (currentEraserShape != null) {
                        shapes.add(currentEraserShape);
                        currentEraserShape = null;
                    }
                    lastEraserPoint = null;
                    redrawCanvas(); // Make sure to redraw to finalize changes
                    return;
                }

                if (currentShape != null && !currentTool.equals("Polygon")) {
                    // Finalize the current shape by adding it to the shapes list
                    shapes.add(currentShape);
                    currentShape = null;
                    redrawCanvas();
                } else if (currentTool.equals("Select")) {
                    // Reset shape manipulation flags
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

                // Handle Selection Tool Dragging - Shape Manipulation
                if (currentTool.equals("Select") && selectedShape != null) {
                    handleSelectionToolDrag(e, currentPoint);
                    return;
                }

                // Handle Eraser Tool Dragging - Pixel Erasing
                if (currentTool.equals("Eraser") && eraserMode == EraserMode.PIXEL) {
                    // Add this point to the current eraser shape
                    if (currentEraserShape != null) {
                        currentEraserShape.addPoint(currentPoint);
                    }

                    // Perform pixel erasing along the drag path for visual feedback
                    if (lastEraserPoint != null) {
                        raster.erasePixelsLine(lastEraserPoint.x, lastEraserPoint.y,
                                currentPoint.x, currentPoint.y, eraserSize);
                        lastEraserPoint = currentPoint;
                        repaint();
                    }
                    return;
                }

                // Handle dragging for shape creation
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

            /**
             * Handles mouse drag for the Selection tool
             */
            private void handleSelectionToolDrag(MouseEvent e, Point currentPoint) {
                int dx = currentPoint.x - lastPoint.x;
                int dy = currentPoint.y - lastPoint.y;

                // Get the current drag type
                boolean rightDrag = isRightClick || (e.getModifiersEx() & MouseEvent.BUTTON3_DOWN_MASK) != 0;

                // CASE 1: Manipulating a control point
                if (selectedControlPoint != null && movingPoint) {
                    if (rightDrag) {
                        // Right-drag on control point behavior - free movement
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
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
    }

    /**
     * Finds the topmost shape that contains the given point
     * @param p Point to check
     * @return The shape at that point, or null if none is found
     */
    private Shape findShapeAt(Point p) {
        // Search in reverse order (top to bottom in z-order)
        for (int i = shapes.size() - 1; i >= 0; i--) {
            if (shapes.get(i).contains(p)) {
                return shapes.get(i);
            }
        }
        return null;
    }

    /**
     * Redraws the entire canvas with all shapes and selection indicators
     */
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

    /**
     * Highlights the selected edge of a shape for visual feedback
     * @param raster The raster to draw on
     * @param shape The shape containing the edge
     * @param edgeIndex Index of the edge to highlight
     */
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

    /**
     * Resizes the canvas
     * @param width New width
     * @param height New height
     */
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

    /**
     * Clears the canvas of all shapes and drawings
     */
    public void clearCanvas() {
        shapes.clear();
        currentShape = null;
        selectedShape = null;
        selectedEdge = -1;
        polygonPoints.clear();
        redrawCanvas();
    }

    /**
     * Sets the current drawing tool
     * @param tool Name of the tool to use
     */
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

    /**
     * Sets the current drawing color
     * @param color Color to use
     */
    public void setCurrentColor(Color color) {
        currentColor = color;
    }

    /**
     * Sets the current line thickness
     * @param thickness Thickness in pixels
     */
    public void setCurrentThickness(int thickness) {
        currentThickness = thickness;
    }

    /**
     * Sets the current line style
     * @param style Line style (solid, dashed, dotted)
     */
    public void setCurrentLineStyle(int style) {
        currentLineStyle = style;
    }
}
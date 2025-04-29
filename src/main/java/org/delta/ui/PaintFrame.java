package org.delta.ui;
import org.delta.util.EraserMode;
import org.delta.util.FillMode;
import org.delta.util.LineStyle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class PaintFrame extends JFrame {
    private DrawingCanvas canvas;
    private ResizableCanvasPanel canvasPanel;

    private JPanel toolPanel;
    private JPanel colorPanel;
    private JPanel lineStylePanel;
    private JPanel eraserPanel;

    private JComboBox<String> eraserModeCombo;
    private EraserMode currentEraserMode = EraserMode.OBJECT;

    private String currentTool = "Line";
    private Color currentColor = Color.BLACK;
    private int currentThickness = 2;
    private int currentLineStyle = LineStyle.SOLID;

    private JLabel statusLabel;

    private JPanel fillPanel;
    private JComboBox<String> fillModeCombo;
    private FillMode currentFillMode = FillMode.OBJECT;

    // Add this method to set fill mode
    public void setFillMode(FillMode mode) {
        currentFillMode = mode;
        canvas.setFillMode(mode);
    }

    public PaintFrame() {
        setTitle("Paint Application");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create status bar
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusLabel = new JLabel("Canvas size: 800 x 600 px");
        statusBar.add(statusLabel);

        // Create canvas
        canvas = new DrawingCanvas(this);

        // Create resizable canvas panel
        canvasPanel = new ResizableCanvasPanel(canvas, statusLabel);

        // Create scroll pane for canvas
        JScrollPane scrollPane = new JScrollPane(canvasPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Add menu for canvas size presets
        JMenuBar menuBar = new JMenuBar();
        JMenu canvasMenu = new JMenu("Canvas");

        // Add canvas size presets
        JMenuItem size640x480 = new JMenuItem("640 x 480");
        size640x480.addActionListener(e -> canvasPanel.setCanvasSize(640, 480));

        JMenuItem size800x600 = new JMenuItem("800 x 600");
        size800x600.addActionListener(e -> canvasPanel.setCanvasSize(800, 600));

        JMenuItem size1024x768 = new JMenuItem("1024 x 768");
        size1024x768.addActionListener(e -> canvasPanel.setCanvasSize(1024, 768));

        JMenuItem size1280x720 = new JMenuItem("1280 x 720 (HD)");
        size1280x720.addActionListener(e -> canvasPanel.setCanvasSize(1280, 720));

        JMenuItem sizeCustom = new JMenuItem("Custom Size...");
        sizeCustom.addActionListener(e -> {
            JTextField widthField = new JTextField(5);
            JTextField heightField = new JTextField(5);

            JPanel panel = new JPanel();
            panel.add(new JLabel("Width:"));
            panel.add(widthField);
            panel.add(Box.createHorizontalStrut(15));
            panel.add(new JLabel("Height:"));
            panel.add(heightField);

            int result = JOptionPane.showConfirmDialog(this, panel,
                    "Enter Custom Canvas Size", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                try {
                    int width = Integer.parseInt(widthField.getText());
                    int height = Integer.parseInt(heightField.getText());
                    canvasPanel.setCanvasSize(width, height);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Please enter valid numbers for width and height.",
                            "Invalid Input", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        canvasMenu.add(size640x480);
        canvasMenu.add(size800x600);
        canvasMenu.add(size1024x768);
        canvasMenu.add(size1280x720);
        canvasMenu.addSeparator();
        canvasMenu.add(sizeCustom);

        menuBar.add(canvasMenu);
        setJMenuBar(menuBar);

        // Create control panels
        createControlPanels();

        // Add components to frame
        add(scrollPane, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);

        // Set initial tool
        setTool("Line");
    }

    private void createControlPanels() {
        // Main control panel (top)
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        // Tool selection panel
        toolPanel = createToolPanel();

        // Color selection panel
        colorPanel = createColorPanel();

        // Line style panel
        lineStylePanel = createLineStylePanel();

        // Create eraser panel
        eraserPanel = createEraserPanel();
        eraserPanel.setVisible(false);  // Initially hidden

        // Create fill panel
        fillPanel = createFillPanel();
        fillPanel.setVisible(false);  // Initially hidden

        // Add all panels to control panel
        controlPanel.add(toolPanel);
        controlPanel.add(new JSeparator(JSeparator.VERTICAL));
        controlPanel.add(colorPanel);
        controlPanel.add(new JSeparator(JSeparator.VERTICAL));
        controlPanel.add(lineStylePanel);
        controlPanel.add(eraserPanel);
        controlPanel.add(fillPanel);

        // Add clear button
        JButton clearButton = new JButton("Clear Canvas");
        clearButton.addActionListener(e -> {
            canvas.clearCanvas();
        });
        controlPanel.add(clearButton);

        // Add control panel to frame
        add(controlPanel, BorderLayout.NORTH);
    }

    private JPanel createToolPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 4, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Tools"));

        String[] tools = {"Line", "Rectangle", "Square", "Circle", "Polygon", "Select", "Eraser", "Fill"};

        for (String tool : tools) {
            JButton button = new JButton(tool);
            button.addActionListener(e -> setTool(tool));
            panel.add(button);
        }

        return panel;
    }

    private JPanel createColorPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 5, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Colors"));

        Color[] colors = {
                Color.BLACK, Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW,
                Color.ORANGE, Color.MAGENTA, Color.CYAN, Color.PINK, Color.GRAY
        };

        for (Color color : colors) {
            JButton colorButton = new JButton();
            colorButton.setBackground(color);
            colorButton.setPreferredSize(new Dimension(30, 30));
            colorButton.addActionListener(e -> setColor(color));
            panel.add(colorButton);
        }

        return panel;
    }

    private JPanel createLineStylePanel() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Line Style"));

        // Line thickness slider
        JSlider thicknessSlider = new JSlider(JSlider.HORIZONTAL, 1, 10, 2);
        thicknessSlider.setMajorTickSpacing(1);
        thicknessSlider.setPaintTicks(true);
        thicknessSlider.setPaintLabels(true);
        thicknessSlider.addChangeListener(e -> {
            if (!thicknessSlider.getValueIsAdjusting()) {
                setThickness(thicknessSlider.getValue());
            }
        });
        panel.add(new JLabel("Thickness:"));
        panel.add(thicknessSlider);

        // Line style combo box
        JComboBox<String> styleCombo = new JComboBox<>(new String[]{"Solid", "Dashed", "Dotted"});
        styleCombo.addActionListener(e -> {
            String selected = (String) styleCombo.getSelectedItem();
            switch (selected) {
                case "Solid":
                    setLineStyle(LineStyle.SOLID);
                    break;
                case "Dashed":
                    setLineStyle(LineStyle.DASHED);
                    break;
                case "Dotted":
                    setLineStyle(LineStyle.DOTTED);
                    break;
            }
        });
        panel.add(styleCombo);

        return panel;
    }

    // Add this method to create eraser controls
    private JPanel createEraserPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Eraser Options"));

        JLabel modeLabel = new JLabel("Eraser Mode:");
        eraserModeCombo = new JComboBox<>(new String[]{"Object", "Pixel"});
        eraserModeCombo.addActionListener(e -> {
            String selected = (String) eraserModeCombo.getSelectedItem();
            switch (selected) {
                case "Object":
                    setEraserMode(EraserMode.OBJECT);
                    break;
                case "Pixel":
                    setEraserMode(EraserMode.PIXEL);
                    break;
            }
        });

        // Add slider for pixel eraser size
        JLabel sizeLabel = new JLabel("Eraser Size:");
        JSlider sizeSlider = new JSlider(JSlider.HORIZONTAL, 5, 50, 10);
        sizeSlider.setMajorTickSpacing(10);
        sizeSlider.setPaintTicks(true);
        sizeSlider.addChangeListener(e -> {
            if (!sizeSlider.getValueIsAdjusting()) {
                canvas.setEraserSize(sizeSlider.getValue());
            }
        });

        panel.add(modeLabel);
        panel.add(eraserModeCombo);
        panel.add(sizeLabel);
        panel.add(sizeSlider);

        return panel;
    }

    private JPanel createFillPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Fill Options"));

        JLabel modeLabel = new JLabel("Fill Mode:");
        fillModeCombo = new JComboBox<>(new String[]{"Object", "Flood Fill"});
        fillModeCombo.addActionListener(e -> {
            String selected = (String) fillModeCombo.getSelectedItem();
            switch (selected) {
                case "Object":
                    setFillMode(FillMode.OBJECT);
                    break;
                case "Flood Fill":
                    setFillMode(FillMode.FLOOD);
                    break;
            }
        });

        panel.add(modeLabel);
        panel.add(fillModeCombo);

        return panel;
    }

    // Add this method to set eraser mode
    public void setEraserMode(EraserMode mode) {
        currentEraserMode = mode;
        canvas.setEraserMode(mode);
    }

    // Modify the setTool method to update eraser panel visibility
    public void setTool(String tool) {
        currentTool = tool;
        canvas.setCurrentTool(tool);

        // Show/hide appropriate panels based on tool selection
        if (eraserPanel != null) {
            eraserPanel.setVisible(tool.equals("Eraser"));
        }

        if (fillPanel != null) {
            fillPanel.setVisible(tool.equals("Fill"));
        }

        // Update UI to reflect selected tool
        for (Component c : toolPanel.getComponents()) {
            if (c instanceof JButton) {
                JButton button = (JButton) c;
                button.setBackground(button.getText().equals(tool) ?
                        new Color(200, 200, 255) : UIManager.getColor("Button.background"));
            }
        }
    }

    public void setColor(Color color) {
        currentColor = color;
        canvas.setCurrentColor(color);

        // Update UI to reflect selected color
        for (Component c : colorPanel.getComponents()) {
            if (c instanceof JButton) {
                JButton button = (JButton) c;
                button.setBorder(button.getBackground().equals(color) ?
                        BorderFactory.createLineBorder(Color.WHITE, 3) : null);
            }
        }
    }

    public void setThickness(int thickness) {
        currentThickness = thickness;
        canvas.setCurrentThickness(thickness);
    }

    public void setLineStyle(int style) {
        currentLineStyle = style;
        canvas.setCurrentLineStyle(style);
    }
}
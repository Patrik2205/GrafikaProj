package org.delta.ui;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class PaintFrame extends JFrame {
    private DrawingCanvas canvas;
    private JPanel toolPanel;
    private JPanel colorPanel;
    private JPanel lineStylePanel;

    private String currentTool = "Line";
    private Color currentColor = Color.BLACK;
    private int currentThickness = 2;
    private int currentLineStyle = LineStyle.SOLID;

    public PaintFrame() {
        setTitle("Paint Application");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create canvas
        canvas = new DrawingCanvas(this);
        add(canvas, BorderLayout.CENTER);

        // Create control panels
        createControlPanels();

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

        // Add all panels to control panel
        controlPanel.add(toolPanel);
        controlPanel.add(new JSeparator(JSeparator.VERTICAL));
        controlPanel.add(colorPanel);
        controlPanel.add(new JSeparator(JSeparator.VERTICAL));
        controlPanel.add(lineStylePanel);

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

    public void setTool(String tool) {
        currentTool = tool;
        canvas.setCurrentTool(tool);

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
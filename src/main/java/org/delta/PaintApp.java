package org.delta;
import org.delta.ui.PaintFrame;

import javax.swing.*;

public class PaintApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PaintFrame frame = new PaintFrame();
            frame.setVisible(true);
        });
    }
}
package com.willwinder.universalgcodesender.uielements.toolbar;

import javax.swing.Icon;
import javax.swing.UIManager;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;

/**
 * Vectorized version of {@code toolbar_arrow_horizontal.png} and
 * {@code toolbar_arrow_vertical.png}.
 */
final class ToolbarArrowIcon extends VectorIcon {
    public static final Icon INSTANCE_HORIZONTAL = new ToolbarArrowIcon(true);
    public static final Icon INSTANCE_VERTICAL = new ToolbarArrowIcon(false);
    private final boolean horizontal;

    private ToolbarArrowIcon(boolean horizontal) {
        super(11, 11);
        this.horizontal = horizontal;
    }

    @Override
    protected void paintIcon(Component c, Graphics2D g, int width, int height, double scaling) {
        if (horizontal) {
            // Rotate 90 degrees counterclockwise.
            g.rotate(-Math.PI / 2.0, width / 2.0, height / 2.0);
        }
        // Draw two chevrons pointing downwards. Make strokes a little thicker at low scalings.
        double strokeWidth = 0.8 * scaling + 0.3;
        g.setStroke(new BasicStroke((float) strokeWidth));
        final Color color;
        if (UIManager.getBoolean("nb.dark.theme")) {
            // Foreground brightness level taken from the combobox dropdown on Darcula.
            color = new Color(187, 187, 187, 255);
        } else {
            color = new Color(50, 50, 50, 255);
        }
        g.setColor(color);
        for (int i = 0; i < 2; i++) {
            final int y = round((1.4 + 4.1 * i) * scaling);
            final double arrowWidth = round(5.0 * scaling);
            final double arrowHeight = round(3.0 * scaling);
            final double marginX = (width - arrowWidth) / 2.0;
            final double arrowMidX = marginX + arrowWidth / 2.0;
            // Clip the top of the chevrons.
            g.clipRect(0, y, width, height);
            Path2D.Double arrowPath = new Path2D.Double();
            arrowPath.moveTo(arrowMidX - arrowWidth / 2.0, y);
            arrowPath.lineTo(arrowMidX, y + arrowHeight);
            arrowPath.lineTo(arrowMidX + arrowWidth / 2.0, y);
            g.draw(arrowPath);
        }
    }
}

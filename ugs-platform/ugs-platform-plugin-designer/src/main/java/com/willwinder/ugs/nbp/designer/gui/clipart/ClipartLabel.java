package com.willwinder.ugs.nbp.designer.gui.clipart;

import javax.swing.JLabel;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

public class ClipartLabel extends JLabel {
    public ClipartLabel(String text) {
        super(text);
        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform, true, true);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Rectangle2D stringBounds = getFont().getStringBounds(getText(), frc);
                int currentSize = (int) Math.round(Math.max(stringBounds.getWidth(), stringBounds.getHeight()));
                float sizeChange = (float) Math.min(getWidth(), getHeight()) / (float) currentSize;
                if (sizeChange < 1f || sizeChange > 1.1f) {
                    int newFontSize = Math.min(Math.round((getFont().getSize() * sizeChange)), getHeight());
                    setFont(getFont().deriveFont((float) newFontSize));
                }
            }
        });
    }
}

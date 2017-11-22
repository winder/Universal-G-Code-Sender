package com.willwinder.universalgcodesender.uielements.components;

import javax.swing.*;
import java.awt.*;

public class RoundedPanel extends JPanel {

    private boolean gradientEnabled = false;

    public RoundedPanel() {
        super();
        setOpaque(false);
    }

    public boolean isGradientEnabled() {
        return gradientEnabled;
    }

    public void setGradientEnabled(boolean gradientEnabled) {
        this.gradientEnabled = gradientEnabled;
    }

    @Override
    protected void paintComponent(Graphics gfx) {
        super.paintComponent(gfx);
        Dimension arcs = new Dimension(15,15);
        Graphics2D gfx2d = (Graphics2D) gfx;
        gfx2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gfx2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        prepareBackground(gfx2d);

        // background
        gfx2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arcs.width, arcs.height);

        // border
        gfx2d.setColor(getBackground().darker());
        gfx2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arcs.width, arcs.height);
    }

    private void prepareBackground(Graphics2D gfx2d) {
        if (isGradientEnabled()) {
            prepareGradientBackground(gfx2d);
        } else {
            prepareFlatBackground(gfx2d);
        }
    }

    private void prepareFlatBackground(Graphics2D gfx2d) {
        gfx2d.setColor(getBackground());
    }

    private void prepareGradientBackground(Graphics2D gfx2d) {
        Color topColor = getBackground().brighter();
        Color bottomColor = getBackground();
        GradientPaint gp = new GradientPaint(0, 0, topColor, 0, getHeight(), bottomColor);
        gfx2d.setPaint(gp);
    }


}

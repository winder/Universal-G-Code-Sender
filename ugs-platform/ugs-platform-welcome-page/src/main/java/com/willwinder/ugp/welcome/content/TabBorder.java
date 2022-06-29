package com.willwinder.ugp.welcome.content;

import com.willwinder.universalgcodesender.uielements.helpers.ThemeColors;

import javax.swing.border.Border;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

public final class TabBorder implements Border {
    private final boolean isLastButton;

    public TabBorder(boolean isLastButton) {
        this.isLastButton = isLastButton;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        g.setColor(ThemeColors.GREY);
        g.drawLine(x, y, 0, height);
        if (isLastButton) {
            g.drawLine(width - 1, y, width - 1, height);
        }
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(10, 10, 10, 10);
    }

    @Override
    public boolean isBorderOpaque() {
        return false;
    }

}

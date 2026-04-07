package com.willwinder.ugs.designer.gui;

import com.willwinder.ugs.designer.entities.entities.cuttable.Direction;
import com.willwinder.universalgcodesender.utils.SvgIconLoader;

import javax.swing.ImageIcon;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;

public class DirectionIcon extends ImageIcon {
    private final ImageIcon icon;

    public enum Size {
        SMALL(16),
        MEDIUM(24),
        LARGE(32);

        public final int value;

        Size(int size) {
            this.value = size;
        }
    }


    public DirectionIcon(Direction direction, Size size) {
        switch (direction) {
            case CLIMB:
                icon = SvgIconLoader.loadImageIcon("img/direction-climb.svg", size.value).orElse(null);
                setDescription(direction.getLabel());
                break;
            case CONVENTIONAL:
                icon = SvgIconLoader.loadImageIcon("img/direction-conventional.svg", size.value).orElse(null);
                setDescription(direction.getLabel());
                break;
            case BOTH:
            default:
                icon = SvgIconLoader.loadImageIcon("img/direction-both.svg", size.value).orElse(null);
                setDescription(direction.getLabel());
                break;

        }
    }

    @Override
    public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
        icon.paintIcon(c, g, x, y);
    }

    @Override
    public int getIconWidth() {
        return icon.getIconWidth();
    }

    @Override
    public int getIconHeight() {
        return icon.getIconHeight();
    }

    @Override
    public Image getImage() {
        return icon.getImage();
    }
}
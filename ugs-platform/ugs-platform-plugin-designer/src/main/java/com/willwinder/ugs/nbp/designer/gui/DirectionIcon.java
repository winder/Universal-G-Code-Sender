package com.willwinder.ugs.nbp.designer.gui;

import com.willwinder.ugs.nbp.designer.entities.cuttable.Direction;
import org.openide.util.ImageUtilities;

import javax.swing.ImageIcon;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;

public class DirectionIcon extends ImageIcon {
    private final ImageIcon icon;

    public enum Size {
        SMALL(""),
        MEDIUM("24"),
        LARGE("32");

        public final String value;

        Size(String size) {
            this.value = size;
        }
    }


    public DirectionIcon(Direction direction, Size size) {
        switch (direction) {
            case CLIMB:
                icon = ImageUtilities.loadImageIcon("img/direction-climb" + size.value + ".svg", false);
                setDescription(direction.getLabel());
                break;
            case CONVENTIONAL:
            default:
                icon = ImageUtilities.loadImageIcon("img/direction-conventional" + size.value + ".svg", false);
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
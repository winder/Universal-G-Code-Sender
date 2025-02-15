package com.willwinder.ugs.nbp.designer.gui;

import com.willwinder.ugs.nbp.designer.entities.cuttable.CutType;
import org.openide.util.ImageUtilities;

import javax.swing.*;
import java.awt.*;

public class CutTypeIcon extends ImageIcon {
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


    public CutTypeIcon(CutType cutType, Size size) {
        switch (cutType) {
            case NONE:
                icon = ImageUtilities.loadImageIcon("img/cutnone" + size.value + ".svg", false);
                setDescription(cutType.getName());
                break;
            case POCKET:
                icon = ImageUtilities.loadImageIcon("img/cutpocket" + size.value + ".svg", false);
                setDescription(cutType.getName());
                break;
            case OUTSIDE_PATH:
                icon = ImageUtilities.loadImageIcon("img/cutoutside" + size.value + ".svg", false);
                setDescription(cutType.getName());
                break;
            case INSIDE_PATH:
                icon = ImageUtilities.loadImageIcon("img/cutinside" + size.value + ".svg", false);
                setDescription(cutType.getName());
                break;
            case ON_PATH:
            case LASER_ON_PATH:
                icon = ImageUtilities.loadImageIcon("img/cutonpath" + size.value + ".svg", false);
                setDescription(cutType.getName());
                break;
            case CENTER_DRILL:
                icon = ImageUtilities.loadImageIcon("img/centerdrill" + size.value + ".svg", false);
                setDescription(cutType.getName());
                break;
            case SURFACE:
            case LASER_FILL:
                icon = ImageUtilities.loadImageIcon("img/cutfill" + size.value + ".svg", false);
                setDescription(cutType.getName());
                break;
            default:
                icon = ImageUtilities.loadImageIcon("img/cutnone" + size.value + ".svg", false);
                setDescription(cutType.getName());
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

package com.willwinder.ugs.nbp.designer.gui;

import com.willwinder.ugs.nbp.designer.entities.cuttable.CutType;
import org.openide.util.ImageUtilities;

import javax.swing.*;
import java.awt.*;

public class CutTypeIcon extends ImageIcon {
    private ImageIcon icon;

    public CutTypeIcon(CutType cutType) {
        this(cutType, Size.MEDIUM);
    }

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
                setDescription("No cut");
                break;
            case POCKET:
                icon = ImageUtilities.loadImageIcon("img/cutpocket" + size.value + ".svg", false);
                setDescription("Pocket");
                break;
            case OUTSIDE_PATH:
                icon = ImageUtilities.loadImageIcon("img/cutoutside" + size.value + ".svg", false);
                setDescription("Outside");
                break;
            case INSIDE_PATH:
                icon = ImageUtilities.loadImageIcon("img/cutinside" + size.value + ".svg", false);
                setDescription("Inside");
                break;
            case ON_PATH:
                icon = ImageUtilities.loadImageIcon("img/cutonpath" + size.value + ".svg", false);
                setDescription("On path");
                break;
        }
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
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

package com.willwinder.ugs.designer.gui;

import com.willwinder.ugs.designer.entities.entities.cuttable.CutType;
import com.willwinder.universalgcodesender.utils.SvgIconLoader;

import javax.swing.ImageIcon;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;

public class CutTypeIcon extends ImageIcon {
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

    public CutTypeIcon(CutType cutType, Size size) {
        switch (cutType) {
            case NONE:
                icon = SvgIconLoader.loadImageIcon("img/cutnone.svg", size.value).orElse(null);
                setDescription(cutType.getName());
                break;
            case POCKET:
                icon = SvgIconLoader.loadImageIcon("img/cutpocket.svg", size.value).orElse(null);
                setDescription(cutType.getName());
                break;
            case OUTSIDE_PATH:
                icon = SvgIconLoader.loadImageIcon("img/cutoutside.svg", size.value).orElse(null);
                setDescription(cutType.getName());
                break;
            case INSIDE_PATH:
                icon = SvgIconLoader.loadImageIcon("img/cutinside.svg", size.value).orElse(null);
                setDescription(cutType.getName());
                break;
            case ON_PATH:
            case LASER_ON_PATH:
                icon = SvgIconLoader.loadImageIcon("img/cutonpath.svg", size.value).orElse(null);
                setDescription(cutType.getName());
                break;
            case CENTER_DRILL:
                icon = SvgIconLoader.loadImageIcon("img/centerdrill.svg", size.value).orElse(null);
                setDescription(cutType.getName());
                break;
            case SURFACE:
            case LASER_FILL:
            case LASER_RASTER:
                icon = SvgIconLoader.loadImageIcon("img/cutfill.svg", size.value).orElse(null);
                setDescription(cutType.getName());
                break;
            default:
                icon = SvgIconLoader.loadImageIcon("img/cutnone.svg", size.value).orElse(null);
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

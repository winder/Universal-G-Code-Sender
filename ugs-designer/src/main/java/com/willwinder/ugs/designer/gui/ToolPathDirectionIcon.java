/*
    Copyright 2026 Joacim Breiler

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.ugs.designer.gui;

import com.willwinder.ugs.designer.entities.cuttable.ToolPathDirection;
import com.willwinder.universalgcodesender.utils.SvgIconLoader;

import javax.swing.ImageIcon;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;

public class ToolPathDirectionIcon extends ImageIcon {
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

    public ToolPathDirectionIcon(ToolPathDirection direction, Size size) {
        icon = SvgIconLoader.loadImageIcon(resource(direction), size.value).orElse(null);
        setDescription(direction.getLabel());
    }

    private static String resource(ToolPathDirection direction) {
        return switch (direction) {
            case LEFT_RIGHT -> "img/arrow-right.svg";
            case RIGHT_LEFT -> "img/arrow-left.svg";
            case TOP_BOTTOM -> "img/arrow-down.svg";
            case BOTTOM_TOP -> "img/arrow-up.svg";
        };
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
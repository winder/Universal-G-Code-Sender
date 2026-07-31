/*
    Copyright 2026 Damian Nikodem

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
package com.willwinder.ugs.designer.gui.toollibrary;

import com.willwinder.ugs.designer.model.toollibrary.EndmillShape;
import com.willwinder.universalgcodesender.utils.SvgIconLoader;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.awt.Component;
import java.awt.Graphics;
import java.util.Optional;

/**
 * Renders the silhouette of an endmill for the given shape using the {@code img/endmill-*.svg}
 * icons.
 */
public class ToolShapeIcon implements Icon {
    private final int size;
    private final transient ImageIcon icon;

    public ToolShapeIcon(EndmillShape shape, int size) {
        this.size = size;
        this.icon = SvgIconLoader.loadImageIcon(getIconPath(shape), size).orElse(null);
    }

    public static String getIconPath(EndmillShape shape) {
        EndmillShape resolved = Optional.ofNullable(shape).orElse(EndmillShape.CUSTOM);
        return switch (resolved) {
            case UPCUT -> "img/endmill-upcut.svg";
            case DOWNCUT -> "img/endmill-downcut.svg";
            case V_BIT -> "img/endmill-vbit.svg";
            case BALL -> "img/endmill-ball.svg";
            case COMPRESSION -> "img/endmill-compression.svg";
            case STRAIGHT -> "img/endmill-straight.svg";
            case CUSTOM -> "img/endmill-custom.svg";
        };
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        if (icon != null) {
            icon.paintIcon(c, g, x, y);
        }
    }

    @Override
    public int getIconWidth() {
        return size;
    }

    @Override
    public int getIconHeight() {
        return size;
    }
}

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

import com.willwinder.ugs.designer.entities.entities.cuttable.ToolPathDirection;
import com.willwinder.universalgcodesender.utils.SvgIconLoader;

import javax.swing.ImageIcon;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.util.Objects;

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
        if (Objects.requireNonNull(direction) == ToolPathDirection.VERTICAL) {
            icon = SvgIconLoader.loadImageIcon("img/vertical.svg", size.value).orElse(null);
            setDescription(direction.getLabel());
        } else {
            icon = SvgIconLoader.loadImageIcon("img/horizontal.svg", size.value).orElse(null);
            setDescription(direction.getLabel());
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
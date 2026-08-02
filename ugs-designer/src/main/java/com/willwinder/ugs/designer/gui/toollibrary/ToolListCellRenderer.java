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
package com.willwinder.ugs.designer.gui.toollibrary;

import com.willwinder.ugs.designer.model.toollibrary.EndmillShape;
import com.willwinder.ugs.designer.model.toollibrary.ToolDefinition;
import net.miginfocom.swing.MigLayout;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.EnumMap;
import java.util.Map;

public class ToolListCellRenderer extends JPanel implements ListCellRenderer<ToolDefinition> {
    private static final int ICON_SIZE = 16;
    private static final Border CELL_BORDER = new EmptyBorder(1, 1, 1, 1);

    private final transient Map<EndmillShape, Icon> iconCache = new EnumMap<>(EndmillShape.class);
    private final JLabel toolNumberLabel = new JLabel();
    private final JLabel iconLabel = new JLabel();
    private final JLabel nameLabel = new JLabel();

    public ToolListCellRenderer() {
        setLayout(new MigLayout("insets 2 6 2 6, gapx 8, fillx",
                "[28!, right][" + ICON_SIZE + "!][grow, fill]", "[]"));
        setOpaque(true);
        setBorder(CELL_BORDER);

        add(toolNumberLabel, "aligny center");
        add(iconLabel, "aligny center");
        add(nameLabel, "growx, aligny center");
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends ToolDefinition> list, ToolDefinition tool,
                                                  int index, boolean isSelected, boolean cellHasFocus) {
        applyFonts(list.getFont());
        applyColors(list, isSelected);

        if (tool == null) {
            toolNumberLabel.setText("");
            iconLabel.setIcon(null);
            nameLabel.setText("");
            return this;
        }

        toolNumberLabel.setText(tool.hasToolNumber() ? "T" + tool.getToolNumber() : "");
        iconLabel.setIcon(icon(tool.getShape()));
        nameLabel.setText(tool.getName() == null ? tool.getId() : tool.getName());
        return this;
    }

    private void applyFonts(Font base) {
        Font font = base == null ? nameLabel.getFont() : base;
        toolNumberLabel.setFont(font.deriveFont(Font.BOLD).deriveFont(font.getSize() * 0.6f));
    }

    private void applyColors(JList<?> list, boolean isSelected) {
        Color background = isSelected ? list.getSelectionBackground() : list.getBackground();
        Color foreground = isSelected ? list.getSelectionForeground() : list.getForeground();
        setBackground(background);
        nameLabel.setForeground(foreground);
        toolNumberLabel.setForeground(isSelected ? foreground : mutedColor(foreground));
    }

    private static Color mutedColor(Color foreground) {
        Color disabled = UIManager.getColor("Label.disabledForeground");
        return disabled == null ? blend(foreground, UIManager.getColor("List.background")) : disabled;
    }

    private static Color blend(Color foreground, Color background) {
        if (background == null) {
            return foreground;
        }
        return new Color(
                (foreground.getRed() + background.getRed()) / 2,
                (foreground.getGreen() + background.getGreen()) / 2,
                (foreground.getBlue() + background.getBlue()) / 2);
    }

    private Icon icon(EndmillShape shape) {
        EndmillShape resolved = shape == null ? EndmillShape.CUSTOM : shape;
        return iconCache.computeIfAbsent(resolved, s -> new ToolShapeIcon(s, ICON_SIZE));
    }
}

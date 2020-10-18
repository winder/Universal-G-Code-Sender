/*
    Copyright 2020 Will Winder

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
package com.willwinder.ugs.nbp.toolbox.settings;

import com.willwinder.ugs.nbp.lib.services.ActionReference;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.ImageUtilities;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.Component;
import java.awt.Font;
import java.awt.image.BufferedImage;

/**
 * Renders an action reference in a list
 *
 * @author Joacim Breiler
 */
public class ActionReferenceRenderer implements ListCellRenderer<ActionReference> {

    private final static EmptyBorder EMPTY_BORDER = new EmptyBorder(4, 16, 4, 4);
    private final static DefaultListCellRenderer DEFAULT_RENDERER = new DefaultListCellRenderer();

    @Override
    public Component getListCellRendererComponent(JList<? extends ActionReference> list, ActionReference value, int index, boolean isSelected, boolean cellHasFocus) {
        if (value instanceof CategoryActionReference) {
            JLabel category = new JLabel(((CategoryActionReference) value).getCategoryName());
            category.setBorder(new EmptyBorder(8, 4, 4, 4));

            Font f = category.getFont();
            category.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
            return category;
        } else {
            String name = RegExUtils.replaceAll(value.getName(), "&", "");
            JLabel label = getLabelFromDefaultRenderer(list, index, isSelected, cellHasFocus, name);
            String iconBase = (String) value.getAction().getValue("iconBase");
            if (StringUtils.isNotEmpty(iconBase)) {
                ImageIcon imageIcon = ImageUtilities.loadImageIcon(iconBase, false);
                label.setIcon(imageIcon);
            } else {
                label.setIcon(getEmptyIcon());
            }

            label.setBorder(EMPTY_BORDER);
            return label;
        }
    }

    /**
     * Renders a jlabel component using default renderer
     *
     * @param list         the list with action references
     * @param index        the index of the cell
     * @param isSelected   if the element is selected
     * @param cellHasFocus if the cell has focus
     * @param name         the name to render
     * @return a JLabel
     */
    private JLabel getLabelFromDefaultRenderer(JList<? extends ActionReference> list, int index, boolean isSelected, boolean cellHasFocus, String name) {
        JLabel listCellRendererComponent = (JLabel) DEFAULT_RENDERER.getListCellRendererComponent(list, name, index, isSelected, cellHasFocus);
        listCellRendererComponent.setHorizontalAlignment(SwingConstants.LEADING);
        return listCellRendererComponent;
    }

    /**
     * Creates an empty icon
     *
     * @return an empty icon
     */
    private Icon getEmptyIcon() {
        BufferedImage bufferedImage = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        return new ImageIcon(bufferedImage);
    }
}

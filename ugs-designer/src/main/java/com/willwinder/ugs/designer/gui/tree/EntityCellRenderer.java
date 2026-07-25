/*
    Copyright 2021-2026 Joacim Breiler

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
package com.willwinder.ugs.designer.gui.tree;

import com.willwinder.ugs.designer.Utils;
import com.willwinder.ugs.designer.entities.EntityGroup;
import com.willwinder.ugs.designer.entities.cuttable.CutType;
import com.willwinder.ugs.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.designer.entities.cuttable.Path;
import com.willwinder.ugs.designer.gui.CutTypeIcon;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.services.LookupService;
import com.willwinder.universalgcodesender.utils.SvgIconLoader;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;

public class EntityCellRenderer extends DefaultTreeCellRenderer {

    private static final int OPEN_PATH_ICON_GAP = 6;
    private static final Icon ICON_HIDDEN = SvgIconLoader.loadImageIcon("img/eyeoff.svg", SvgIconLoader.SIZE_MEDIUM).orElse(null);
    private static final Icon ICON_GROUP_OPEN = SvgIconLoader.loadImageIcon("img/open.svg", SvgIconLoader.SIZE_MEDIUM).orElse(null);
    private static final Icon ICON_GROUP_CLOSED = SvgIconLoader.loadImageIcon("img/folder.svg", SvgIconLoader.SIZE_MEDIUM).orElse(null);
    private static final Icon ICON_OPEN_PATH = SvgIconLoader.loadImageIcon("img/open-path.svg", SvgIconLoader.SIZE_SMALL).orElse(null);

    private transient Icon trailingIcon;

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(
                tree, value, sel,
                expanded, leaf, row,
                hasFocus);

        setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        trailingIcon = null;

        BackendAPI backendAPI = LookupService.lookup(BackendAPI.class);
        UnitUtils.Units preferredUnits = backendAPI.getSettings().getPreferredUnits();

        Object treeObject = getUserObject(value);
        if (treeObject instanceof EntityGroup group) {
            renderGroup(group, expanded);
        } else if (treeObject instanceof Cuttable cuttable) {
            renderCuttable(cuttable, preferredUnits);
        } else {
            setToolTipText(null); //no tool tip
        }

        return this;
    }

    private void renderGroup(EntityGroup group, boolean expanded) {
        setIcon(expanded ? ICON_GROUP_OPEN : ICON_GROUP_CLOSED);
        setText(group.getName());
        setToolTipText(null);
    }

    private void renderCuttable(Cuttable cuttable, UnitUtils.Units preferredUnits) {
        CutType cutType = cuttable.getCutType();
        double cutStart = UnitUtils.scaleUnits(UnitUtils.Units.MM, preferredUnits) * cuttable.getStartDepth();
        double cutDepth = UnitUtils.scaleUnits(UnitUtils.Units.MM, preferredUnits) * cuttable.getTargetDepth();
        if (cuttable.isHidden()) {
            setIcon(ICON_HIDDEN);
        } else {
            setIcon(new CutTypeIcon(cutType, CutTypeIcon.Size.MEDIUM));
        }

        if (isOpenPath(cuttable)) {
            trailingIcon = ICON_OPEN_PATH;
            setToolTipText(Localization.getString("platform.plugin.designer.tree.open-path.tooltip"));
        } else {
            setToolTipText(null);
        }

        if (cutType == CutType.NONE) {
            setText(cuttable.getName());
            return;
        }

        setText("<html>" + cuttable.getName() +
                "<br/><small>" +
                Utils.toString(cutStart) + " - " + Utils.toString(cutDepth) +
                " " + preferredUnits.abbreviation +
                ", " + cuttable.getFeedRate() + " mm/min" +
                ", " + cuttable.getSpindleSpeed() + "% <small>" +
                "</html>");
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        if (trailingIcon != null) {
            size.width += OPEN_PATH_ICON_GAP + trailingIcon.getIconWidth();
        }
        return size;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (trailingIcon != null) {
            int x = getWidth() - trailingIcon.getIconWidth();
            int y = (getHeight() - trailingIcon.getIconHeight()) / 2;
            trailingIcon.paintIcon(this, g, x, y);
        }
    }

    Icon getTrailingIcon() {
        return trailingIcon;
    }

    private boolean isOpenPath(Cuttable cuttable) {
        return cuttable instanceof Path path && !path.isClosed();
    }

    private Object getUserObject(Object value) {
        if (value instanceof DefaultMutableTreeNode) {
            return ((DefaultMutableTreeNode) value).getUserObject();
        }
        return value;
    }
}

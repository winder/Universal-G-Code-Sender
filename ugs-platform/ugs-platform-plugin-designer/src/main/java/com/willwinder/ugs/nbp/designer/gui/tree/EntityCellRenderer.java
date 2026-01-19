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
package com.willwinder.ugs.nbp.designer.gui.tree;

import com.willwinder.ugs.nbp.designer.Utils;
import com.willwinder.ugs.nbp.designer.entities.EntityGroup;
import com.willwinder.ugs.nbp.designer.entities.cuttable.CutType;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.nbp.designer.gui.CutTypeIcon;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UnitUtils;
import org.openide.util.ImageUtilities;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.Component;

public class EntityCellRenderer extends DefaultTreeCellRenderer {

    private static final Icon ICON_HIDDEN = ImageUtilities.loadImageIcon("img/eyeoff24.svg", false);
    private static final Icon ICON_GROUP_OPEN = ImageUtilities.loadImageIcon("img/open24.svg", false);
    private static final Icon ICON_GROUP_CLOSED = ImageUtilities.loadImageIcon("img/folder24.svg", false);

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(
                tree, value, sel,
                expanded, leaf, row,
                hasFocus);

        setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        BackendAPI backendAPI = CentralLookup.getDefault().lookup(BackendAPI.class);
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

        if (cutType == CutType.NONE) {
            setText(cuttable.getName());
        } else {
            setText("<html>" + cuttable.getName() + "<br/><small>" + Utils.toString(cutStart) + " - "  + Utils.toString(cutDepth) + " " + preferredUnits.abbreviation + ", " + cuttable.getFeedRate() + " mm/min, " + cuttable.getSpindleSpeed() + "% <small></html>");
        }
    }

    private Object getUserObject(Object value) {
        if (value instanceof DefaultMutableTreeNode) {
            return ((DefaultMutableTreeNode) value).getUserObject();
        }
        return value;
    }
}

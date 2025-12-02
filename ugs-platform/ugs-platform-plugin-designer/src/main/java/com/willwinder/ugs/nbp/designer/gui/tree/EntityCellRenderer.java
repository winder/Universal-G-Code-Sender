/*
    Copyright 2021-2025 Will Winder

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
import com.willwinder.ugs.nbp.designer.entities.cuttable.CutType;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.nbp.designer.gui.CutTypeIcon;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UnitUtils;
import org.openide.util.ImageUtilities;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

public class EntityCellRenderer implements TreeCellRenderer {

    private static final Icon ICON_HIDDEN = ImageUtilities.loadImageIcon("img/eyeoff.svg", false);
    private final DefaultTreeCellRenderer delegate = new DefaultTreeCellRenderer();
    private final JPanel wrapper = new JPanel(new BorderLayout());

    public EntityCellRenderer() {
        wrapper.setBorder(BorderFactory.createEmptyBorder());
        wrapper.setOpaque(false);
        wrapper.add(delegate, BorderLayout.CENTER);
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        Component c = delegate.getTreeCellRendererComponent(
                tree, value, sel, expanded, leaf, row, hasFocus);

        BackendAPI backendAPI = CentralLookup.getDefault().lookup(BackendAPI.class);
        UnitUtils.Units preferredUnits = backendAPI.getSettings().getPreferredUnits();

        Object treeObject = getUserObject(value);
        if (leaf && treeObject instanceof Cuttable cuttable) {
            CutType cutType = cuttable.getCutType();
            double cutDepth = UnitUtils.scaleUnits(UnitUtils.Units.MM, preferredUnits) * cuttable.getTargetDepth();
            if (cuttable.isHidden()) {
                delegate.setIcon(ICON_HIDDEN);
            } else {
                delegate.setIcon(new CutTypeIcon(cutType, CutTypeIcon.Size.SMALL));
            }

            if (cutType == CutType.NONE) {
                delegate.setText(cuttable.getName());
            } else {
                delegate.setText((Utils.toString(cutDepth)) + " " + preferredUnits.abbreviation + " - " + cuttable.getName());
            }
        } else {
            delegate.setToolTipText(null); //no tool tip
        }

        wrapper.setPreferredSize(new Dimension(tree.getWidth(), c.getPreferredSize().height));
        return wrapper;
    }

    private Object getUserObject(Object value) {
        if (value instanceof DefaultMutableTreeNode node) {
            return node.getUserObject();
        }
        return value;
    }
}

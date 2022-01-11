package com.willwinder.ugs.nbp.designer.gui.tree;

import com.willwinder.ugs.nbp.designer.Utils;
import com.willwinder.ugs.nbp.designer.entities.cuttable.CutType;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.nbp.designer.gui.CutTypeIcon;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UnitUtils;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

public class EntityCellRenderer extends DefaultTreeCellRenderer {
    private final JTree tree;

    public EntityCellRenderer(JTree tree) {
        this.tree = tree;
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(
                tree, value, sel,
                expanded, leaf, row,
                hasFocus);

        BackendAPI backendAPI = CentralLookup.getDefault().lookup(BackendAPI.class);
        UnitUtils.Units preferredUnits = backendAPI.getSettings().getPreferredUnits();

        Object treeObject = getUserObject(value);
        if (leaf && treeObject instanceof Cuttable) {
            Cuttable cuttable = (Cuttable) treeObject;
            CutType cutType = cuttable.getCutType();
            double cutDepth = UnitUtils.scaleUnits(UnitUtils.Units.MM, preferredUnits) * cuttable.getTargetDepth();
            setIcon(new CutTypeIcon(cutType, CutTypeIcon.Size.SMALL));

            if (cutType == CutType.NONE) {
                setText(cuttable.getName());
            } else {
                setText((Utils.toString(cutDepth)) + " " + preferredUnits.abbreviation + " - " + cuttable.getName());
            }
        } else {
            setToolTipText(null); //no tool tip
        }

        return this;
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension preferredSize = super.getPreferredSize();
        preferredSize.width = tree.getWidth();
        return preferredSize;
    }

    private Object getUserObject(Object value) {
        if (value instanceof DefaultMutableTreeNode) {
            return ((DefaultMutableTreeNode) value).getUserObject();
        }
        return value;
    }
}

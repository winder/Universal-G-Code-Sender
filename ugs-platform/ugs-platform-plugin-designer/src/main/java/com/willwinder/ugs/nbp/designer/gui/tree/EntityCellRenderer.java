package com.willwinder.ugs.nbp.designer.gui.tree;

import com.willwinder.ugs.nbp.designer.Utils;
import com.willwinder.ugs.nbp.designer.entities.cuttable.CutType;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.nbp.designer.gui.CutTypeIcon;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UnitUtils;
import org.openide.util.ImageUtilities;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.Component;

public class EntityCellRenderer extends DefaultTreeCellRenderer {

    private static final Icon ICON_HIDDEN = ImageUtilities.loadImageIcon("img/eyeoff.svg", false);

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
            if (cuttable.isHidden()) {
                setIcon(ICON_HIDDEN);
            } else {
                setIcon(new CutTypeIcon(cutType, CutTypeIcon.Size.SMALL));
            }

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

    private Object getUserObject(Object value) {
        if (value instanceof DefaultMutableTreeNode) {
            return ((DefaultMutableTreeNode) value).getUserObject();
        }
        return value;
    }
}

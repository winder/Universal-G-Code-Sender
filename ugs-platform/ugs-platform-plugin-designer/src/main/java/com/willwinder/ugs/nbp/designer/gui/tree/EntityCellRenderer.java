/*
    Copyright 2021-2024 Will Winder

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
import com.willwinder.ugs.nbp.designer.entities.Entity;
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
import java.awt.Dimension;


public class EntityCellRenderer extends DefaultTreeCellRenderer {

    private static final Icon ICON_HIDDEN = ImageUtilities.loadImageIcon("img/eyeoff.svg", false);

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {        
        String textToUse="";
        Icon iconToUse = null;

        BackendAPI backendAPI = CentralLookup.getDefault().lookup(BackendAPI.class);
        UnitUtils.Units preferredUnits = backendAPI.getSettings().getPreferredUnits();
                
        Object treeObject = getUserObject(value);
        if (leaf && treeObject instanceof Cuttable cuttable) {
            CutType cutType = cuttable.getCutType();
            double cutDepth = UnitUtils.scaleUnits(UnitUtils.Units.MM, preferredUnits) * cuttable.getTargetDepth();
            if (cuttable.isHidden()) {                
                iconToUse = ICON_HIDDEN;
            } else {                
                iconToUse = new CutTypeIcon(cutType, CutTypeIcon.Size.SMALL);
            }

            if (cutType == CutType.NONE) {
                textToUse =cuttable.getName(); 
            } else {
                
                textToUse = (Utils.toString(cutDepth)) + " " + preferredUnits.abbreviation + " - " + cuttable.getName();
            }
            setText( textToUse );
        } else {
            if (treeObject instanceof Entity entity) {
                textToUse = entity.getName();
            }
            setToolTipText(null); //no tool tip
        }
        
        EntityCellRenderer result = (EntityCellRenderer) super.getTreeCellRendererComponent(
                tree, textToUse, sel,
                expanded, leaf, row,
                hasFocus);
        Dimension trueSize = new Dimension(tree.getWidth(), result.getPreferredSize().height);
        result.setPreferredSize(trueSize);
        result.setMinimumSize(trueSize);
        result.setSize(trueSize);
        if (iconToUse != null) {
            result.setIcon(iconToUse);
        }
       
//        result.setText(textToUse);               
        return result;
    }

    private Object getUserObject(Object value) {
        if (value instanceof DefaultMutableTreeNode defaultMutableTreeNode) {
            return defaultMutableTreeNode.getUserObject();
        }
        return value;
    }
}

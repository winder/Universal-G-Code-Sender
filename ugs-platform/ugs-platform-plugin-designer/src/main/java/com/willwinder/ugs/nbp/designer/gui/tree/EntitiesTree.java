/*
    Copyright 2021 Will Winder

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

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntityGroup;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionEvent;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionListener;
import com.willwinder.ugs.nbp.designer.logic.Controller;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Joacim Breiler
 */
public class EntitiesTree extends JPanel implements TreeSelectionListener, SelectionListener {

    private transient Controller controller;
    private final JTree tree;

    public EntitiesTree(Controller controller) {
        setLayout(new BorderLayout());
        tree = new JTree();
        tree.setEditable(true);
        tree.setRootVisible(false);
        tree.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        tree.setCellRenderer(new EntityCellRenderer(tree));
        tree.setModel(new EntityTreeModel(controller));
        tree.addTreeSelectionListener(this);
        tree.expandRow(0);

        setBackground(tree.getBackground());
        add(tree, BorderLayout.CENTER);
        updateController(controller);
    }

    private void updateController(Controller controller) {
        this.controller = controller;
        controller.getSelectionManager().addSelectionListener(this);
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        Arrays.asList(e.getPaths()).forEach(p -> {
            Entity entity = (Entity) p.getLastPathComponent();
            if (e.isAddedPath(p) && !controller.getSelectionManager().isSelected(entity)) {
                controller.getSelectionManager().addSelection(entity);
            } else if (!e.isAddedPath(p) && controller.getSelectionManager().isSelected(entity)) {
                controller.getSelectionManager().removeSelection(entity);
            }
        });
    }

    @Override
    public void onSelectionEvent(SelectionEvent selectionEvent) {
        EntityGroup rootEntity = (EntityGroup) controller.getDrawing().getRootEntity();
        List<TreePath> treePathList = EntityTreeUtils.getSelectedPaths(controller, rootEntity, Collections.emptyList());
        TreePath[] treePaths = treePathList.toArray(new TreePath[0]);
        tree.setSelectionPaths(treePaths);
    }
}

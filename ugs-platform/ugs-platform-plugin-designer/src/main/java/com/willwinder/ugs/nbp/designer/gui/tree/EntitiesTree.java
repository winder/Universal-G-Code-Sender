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
import com.willwinder.ugs.nbp.designer.gui.DrawingEvent;
import com.willwinder.ugs.nbp.designer.gui.DrawingListener;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.ControllerEventType;
import com.willwinder.ugs.nbp.designer.logic.ControllerListener;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Joacim Breiler
 */
public class EntitiesTree extends JPanel implements DrawingListener, ControllerListener, SelectionListener {

    private final DefaultMutableTreeNode topNode = new DefaultMutableTreeNode("Drawing");
    private transient Controller controller;
    private final JTree tree;

    public EntitiesTree(Controller controller) {
        this();
        updateController(controller);
    }

    public void updateController(Controller controller) {
        if (this.controller != null && this.controller != controller) {
            this.controller.removeListener(this);
            this.controller.getDrawing().removeListener(this);
            this.controller.getSelectionManager().removeSelectionListener(this);
        }

        this.controller = controller;
        this.controller.addListener(this);
        this.controller.getDrawing().addListener(this);
        this.controller.getSelectionManager().addSelectionListener(this);
    }

    public EntitiesTree() {

        setLayout(new BorderLayout());
        tree = new JTree(topNode);
        tree.setEditable(true);
        tree.setRootVisible(false);
        tree.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        tree.setCellRenderer(new EntityCellRenderer(tree));
        setBackground(tree.getBackground());

        tree.addTreeSelectionListener(e -> {
            TreePath[] selectionPaths = tree.getSelectionPaths();
            if (selectionPaths != null && controller != null) {
                List<Entity> entities = Arrays.stream(selectionPaths)
                        .filter(path -> ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject() instanceof Entity)
                        .map(path -> (Entity) ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject())
                        .collect(Collectors.toList());
                controller.getSelectionManager().setSelection(entities);
            }
        });

        tree.expandRow(0);

        add(tree, BorderLayout.CENTER);
    }

    @Override
    public void onDrawingEvent(DrawingEvent event) {
        reloadTree();
    }

    private void reloadTree() {
        topNode.removeAllChildren();
        ((EntityGroup) controller.getDrawing().getRootEntity()).getChildren().forEach(child -> addNode(topNode, child));
        tree.setModel(new DefaultTreeModel(topNode));
        tree.expandRow(0);
    }

    private void addNode(DefaultMutableTreeNode parent, Entity entity) {
        EntityTreeNode node = new EntityTreeNode(entity);
        if (entity instanceof EntityGroup) {
            ((EntityGroup) entity).getChildren().forEach(childEntity -> addNode(node, childEntity));
        }

        parent.add(node);
    }

    @Override
    public void onControllerEvent(ControllerEventType event) {
        if (event == ControllerEventType.NEW_DRAWING) {
            controller.getDrawing().addListener(this);
            reloadTree();
        }
    }

    @Override
    public void onSelectionEvent(SelectionEvent selectionEvent) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getModel().getRoot();
        List<DefaultMutableTreeNode> selectedNodes = getSelectedNodes(node);
        TreePath[] treePathList = selectedNodes.stream().map(this::getPath).toArray(TreePath[]::new);
        tree.setSelectionPaths(treePathList);
    }

    public TreePath getPath(TreeNode treeNode) {
        List<Object> nodes = new ArrayList<>();
        if (treeNode != null) {
            nodes.add(treeNode);
            treeNode = treeNode.getParent();
            while (treeNode != null) {
                nodes.add(0, treeNode);
                treeNode = treeNode.getParent();
            }
        }

        return nodes.isEmpty() ? null : new TreePath(nodes.toArray());
    }

    private List<DefaultMutableTreeNode> getSelectedNodes(DefaultMutableTreeNode root) {

        List<DefaultMutableTreeNode> treeNodes = new ArrayList<>();
        for (int i = 0; i < root.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) root.getChildAt(i);
            Entity userObject = (Entity) child.getUserObject();
            if (controller != null && controller.getSelectionManager().isSelected(userObject)) {
                treeNodes.add(child);
            }
            treeNodes.addAll(getSelectedNodes(child));
        }

        return treeNodes;
    }
}

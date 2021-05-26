package com.willwinder.ugs.nbp.designer.gui;

import com.willwinder.ugs.nbp.designer.gui.entities.Entity;
import com.willwinder.ugs.nbp.designer.gui.entities.Group;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.ControllerEventType;
import com.willwinder.ugs.nbp.designer.logic.ControllerListener;
import com.willwinder.ugs.nbp.designer.logic.selection.SelectionEvent;
import com.willwinder.ugs.nbp.designer.logic.selection.SelectionListener;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;

public class EntitiesTree extends JPanel implements DrawingListener, ControllerListener, SelectionListener {

    private final DefaultMutableTreeNode topNode = new DefaultMutableTreeNode("Drawing");
    private final Controller controller;
    private final JTree tree;

    public EntitiesTree(Controller controller) {
        this.controller = controller;
        this.controller.addListener(this);
        controller.getDrawing().addListener(this);
        controller.getSelectionManager().addSelectionListener(this);
        tree = new JTree(topNode);
        tree.setCellRenderer(new TreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                if (value instanceof DefaultMutableTreeNode && ((DefaultMutableTreeNode) value).getUserObject() instanceof Entity) {
                    Entity entity = (Entity) ((DefaultMutableTreeNode) value).getUserObject();
                    return new JLabel(entity.getClass().getSimpleName() + entity.hashCode() + ", " +  entity.getRotation() + ", " + entity.getPosition()+ ", " + entity.getSize());
                } else {
                    return new JLabel(value.toString());
                }
            }
        });
        tree.expandRow(0);
        add(tree);
    }

    @Override
    public void onDrawingEvent(DrawingEvent event) {
        reloadTree();
    }

    private void reloadTree() {
        topNode.removeAllChildren();
        addNode(topNode, controller.getDrawing().getRootEntity());
        tree.setModel(new DefaultTreeModel(topNode));
        tree.expandRow(0);
    }

    private void addNode(DefaultMutableTreeNode parent, Entity entity) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(entity);

        if (entity instanceof Group) {
            ((Group) entity).getChildren().forEach(childEntity -> {
                addNode(node, childEntity);
            });
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
        reloadTree();
    }
}

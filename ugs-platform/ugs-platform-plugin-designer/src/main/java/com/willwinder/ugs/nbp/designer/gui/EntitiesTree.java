package com.willwinder.ugs.nbp.designer.gui;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntityGroup;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.ControllerEventType;
import com.willwinder.ugs.nbp.designer.logic.ControllerListener;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionEvent;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EntitiesTree extends JPanel implements DrawingListener, ControllerListener, SelectionListener {

    private final DefaultMutableTreeNode topNode = new DefaultMutableTreeNode("Drawing");
    private Controller controller;
    private final JTree tree;

    public EntitiesTree(Controller controller) {
        this();
        updateController(controller);
    }

    public void updateController(Controller controller) {
        if(this.controller != null) {
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

        setLayout(new MigLayout("fill, insets 5"));
        tree = new JTree(topNode);
        setBackground(tree.getBackground());

        tree.addTreeSelectionListener(e -> {
            TreePath[] selectionPaths = tree.getSelectionPaths();
            if (selectionPaths != null && controller != null) {
                List<Entity> entities = Arrays.stream(selectionPaths)
                        .filter(path -> ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject() instanceof  Entity)
                        .map(path -> (Entity) ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject())
                        .collect(Collectors.toList());
                controller.getSelectionManager().setSelection(entities);
            }
        });

        tree.expandRow(0);

        add(tree, "grow");
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

package com.willwinder.universalgcodesender.fx.component.visualizer.designer;

import com.willwinder.ugs.designer.entities.entities.Entity;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.logic.ControllerFactory;
import com.willwinder.universalgcodesender.fx.component.visualizer.PickHandler;
import javafx.scene.Group;
import javafx.scene.Node;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class EntityShapesNode extends Group {

    private final Map<Node, Entity> nodeToEntity = new IdentityHashMap<>();

    public void refreshFromController() {
        getChildren().clear();
        nodeToEntity.clear();

        Controller controller = ControllerFactory.getController();
        for (Entity entity : controller.getDrawing().getEntities()) {
            Node node = EntityShapeFactory.create(entity);
            if (node != null) {
                PickHandler handler = multiSelect -> {
                    if (multiSelect) {
                        controller.getSelectionManager().toggleSelection(entity);
                    } else {
                        controller.getSelectionManager().setSelection(List.of(entity));
                    }
                };

                node.setUserData(handler);

                if (node instanceof Group group) {
                    for (Node child : group.getChildren()) {
                        child.setUserData(handler);
                    }
                }

                nodeToEntity.put(node, entity);
                getChildren().add(node);
            }
        }
    }

    public void onZoomChange(double zoomFactor) {
    }
}

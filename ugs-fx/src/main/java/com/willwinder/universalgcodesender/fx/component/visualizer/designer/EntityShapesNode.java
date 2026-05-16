package com.willwinder.universalgcodesender.fx.component.visualizer.designer;

import com.willwinder.ugs.designer.entities.entities.Entity;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.logic.ControllerFactory;
import javafx.scene.Group;
import javafx.scene.Node;

public class EntityShapesNode extends Group {

    private Runnable onEntityMoved = () -> {};

    public void setOnEntityMoved(Runnable onEntityMoved) {
        this.onEntityMoved = onEntityMoved != null ? onEntityMoved : () -> {};
    }

    public void refreshFromController() {
        getChildren().clear();

        Controller controller = ControllerFactory.getController();
        Runnable onMoved = () -> {
            refreshFromController();
            onEntityMoved.run();
        };

        for (Entity entity : controller.getDrawing().getEntities()) {
            Node node = EntityShapeFactory.create(entity);
            if (node != null) {
                EntityClickHandler handler = new EntityClickHandler(controller, entity, onMoved);

                node.setUserData(handler);

                if (node instanceof Group group) {
                    for (Node child : group.getChildren()) {
                        child.setUserData(handler);
                    }
                }

                getChildren().add(node);
            }
        }
    }

    public void onZoomChange(double zoomFactor) {
    }
}

package com.willwinder.universalgcodesender.fx.component.visualizer.designer;

import com.willwinder.ugs.designer.entities.entities.Entity;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.logic.ControllerFactory;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.shape.MeshView;

public class EntityShapesNode extends Group {

    // Fills go through normal depth testing so they Z-fight predictably when overlapping.
    // Borders are drawn afterwards with depth testing disabled, so every entity's outline
    // remains visible through any geometry in front of it.
    private final Group fillsGroup = new Group();
    private final Group bordersGroup = new Group();
    private Runnable onEntityMoved = () -> {};

    public EntityShapesNode() {
        bordersGroup.setDepthTest(DepthTest.DISABLE);
        getChildren().addAll(fillsGroup, bordersGroup);
    }

    public void setOnEntityMoved(Runnable onEntityMoved) {
        this.onEntityMoved = onEntityMoved != null ? onEntityMoved : () -> {};
    }

    public void refreshFromController() {
        fillsGroup.getChildren().clear();
        bordersGroup.getChildren().clear();

        Controller controller = ControllerFactory.getController();
        Runnable onMoved = () -> {
            refreshFromController();
            onEntityMoved.run();
        };

        for (Entity entity : controller.getDrawing().getEntities()) {
            EntityShapeFactory.EntityNodes nodes = EntityShapeFactory.create(entity);
            if (nodes == null) {
                continue;
            }

            EntityClickHandler handler = new EntityClickHandler(controller, entity, onMoved);
            MeshView fill = nodes.fill();
            if (fill != null) {
                fill.setUserData(handler);
                fillsGroup.getChildren().add(fill);
            }
            MeshView border = nodes.border();
            if (border != null) {
                border.setUserData(handler);
                bordersGroup.getChildren().add(border);
            }
        }
    }

    public void onZoomChange(double zoomFactor) {
    }
}

package com.willwinder.universalgcodesender.fx.component.visualizer;

import com.willwinder.ugs.designer.logic.ControllerFactory;
import com.willwinder.universalgcodesender.fx.component.visualizer.designer.ControlsNode;
import com.willwinder.universalgcodesender.fx.component.visualizer.designer.EntityShapesNode;
import com.willwinder.universalgcodesender.fx.component.visualizer.models.GcodeModel;
import com.willwinder.universalgcodesender.fx.component.visualizer.models.Model;
import com.willwinder.universalgcodesender.fx.model.UgsdWorkspaceContext;
import com.willwinder.universalgcodesender.fx.model.WorkspaceContext;
import com.willwinder.universalgcodesender.fx.service.WorkspaceManager;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.services.LookupService;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

public class WorkspaceScene extends Model {
    private final EntityShapesNode entityShapesNode = new EntityShapesNode();
    private ControlsNode controlsNode;

    public WorkspaceScene() {
        WorkspaceManager.getInstance().addListener(new WorkspaceManager.WorkspaceListener() {
            @Override
            public void onWorkspaceOpened(WorkspaceContext workspace) {
                Platform.runLater(() -> setWorkspace(workspace));
            }

            @Override
            public void onWorkspaceClosed() {
                Platform.runLater(() -> setWorkspace(null));
            }

            @Override
            public void onWorkspaceDirtyStateChanged(WorkspaceContext workspace, boolean dirty) {
                // no-op for now
            }
        });

        // Forward clicks to any PickHandler stored in the picked node's userData chain.
        // Must be on worldGroup (inside SubScene) so getPickResult() returns real 3D intersection data.
        entityShapesNode.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            Node hit = event.getPickResult().getIntersectedNode();
            while (hit != null) {
                if (hit.getUserData() instanceof PickHandler handler) {
                    handler.onPicked(event.isShiftDown());
                    event.consume();
                    return;
                }
                hit = hit.getParent();
            }
        });

    }

    public void setWorkspace(WorkspaceContext workspace) {
        getChildren().clear();

        if (controlsNode != null) {
            controlsNode.dispose();
            controlsNode = null;
        }

        if (workspace == null) {
            return;
        }

        if (workspace instanceof UgsdWorkspaceContext) {
            entityShapesNode.refreshFromController();
            getChildren().add(entityShapesNode);

            var controller = ControllerFactory.getController();
            controlsNode = new ControlsNode(controller.getDrawing(), controller.getSelectionManager(),
                    entityShapesNode::refreshFromController);
            getChildren().add(controlsNode);
        }

        getChildren().add( new GcodeModel(LookupService.lookup(BackendAPI.class).getGcodeFile()));

    }

    @Override
    public void onZoomChange(double zoomFactor) {
        entityShapesNode.onZoomChange(zoomFactor);
    }

    @Override
    public boolean useLighting() {
        return false;
    }
}

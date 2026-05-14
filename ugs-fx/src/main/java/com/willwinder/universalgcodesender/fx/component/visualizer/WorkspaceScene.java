package com.willwinder.universalgcodesender.fx.component.visualizer;

import com.willwinder.ugs.designer.entities.entities.EntityListener;
import com.willwinder.ugs.designer.entities.entities.EventType;
import com.willwinder.ugs.designer.logic.Controller;
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

import java.io.File;
import java.util.EnumSet;
import java.util.Set;

public class WorkspaceScene extends Model {
    private static final Set<EventType> DESIGN_CHANGE_EVENTS = EnumSet.of(
            EventType.MOVED,
            EventType.RESIZED,
            EventType.ROTATED,
            EventType.PATH_CHANGED,
            EventType.CHILD_ADDED,
            EventType.CHILD_REMOVED,
            EventType.SETTINGS_CHANGED
    );

    private final EntityShapesNode entityShapesNode = new EntityShapesNode();
    private ControlsNode controlsNode;
    private GcodeModel gcodeModel;
    private Controller designController;
    private EntityListener designChangeListener;

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
                Platform.runLater(() -> onDirtyStateChanged(dirty));
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
        gcodeModel = null;

        if (controlsNode != null) {
            controlsNode.dispose();
            controlsNode = null;
        }

        detachDesignChangeListener();

        if (workspace == null) {
            return;
        }

        if (workspace instanceof UgsdWorkspaceContext) {
            entityShapesNode.refreshFromController();
            getChildren().add(entityShapesNode);

            designController = ControllerFactory.getController();
            controlsNode = new ControlsNode(designController.getDrawing(), designController.getSelectionManager(),
                    entityShapesNode::refreshFromController);
            getChildren().add(controlsNode);

            attachDesignChangeListener(designController);
        }

        addGcodeModel();
    }

    private void onDirtyStateChanged(boolean dirty) {
        if (dirty) {
            removeGcodeModel();
        } else {
            removeGcodeModel();
            addGcodeModel();
        }
    }

    private void addGcodeModel() {
        File file = LookupService.lookup(BackendAPI.class).getGcodeFile();
        if (file == null) {
            return;
        }
        gcodeModel = new GcodeModel(file);
        gcodeModel.visibleProperty().bind(VisualizerSettings.getInstance().showGcodeModelProperty());
        getChildren().add(gcodeModel);
    }

    private void removeGcodeModel() {
        if (gcodeModel != null) {
            getChildren().remove(gcodeModel);
            gcodeModel = null;
        }
    }

    private void attachDesignChangeListener(Controller controller) {
        designChangeListener = event -> {
            if (DESIGN_CHANGE_EVENTS.contains(event.getType())) {
                WorkspaceManager.getInstance().markActiveWorkspaceDirty(true);
            }
        };
        controller.getDrawing().getRootEntity().addListener(designChangeListener);
    }

    private void detachDesignChangeListener() {
        if (designController != null && designChangeListener != null) {
            designController.getDrawing().getRootEntity().removeListener(designChangeListener);
        }
        designController = null;
        designChangeListener = null;
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

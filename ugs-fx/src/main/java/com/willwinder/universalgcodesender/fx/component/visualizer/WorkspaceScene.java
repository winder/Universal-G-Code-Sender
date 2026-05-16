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
import com.willwinder.universalgcodesender.fx.service.VisualizerService;
import com.willwinder.universalgcodesender.fx.service.WorkspaceManager;
import com.willwinder.universalgcodesender.fx.settings.VisualizerSettings;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.services.LookupService;
import javafx.application.Platform;
import javafx.scene.input.MouseEvent;

import java.io.File;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Consumer;

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
    private final Consumer<MouseEvent> backgroundClickHandler = this::onBackgroundClick;
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
    }

    private void onBackgroundClick(MouseEvent event) {
        if (designController == null) {
            return;
        }
        if (event.isShiftDown()) {
            return;
        }
        designController.getSelectionManager().clearSelection();
    }

    public void setWorkspace(WorkspaceContext workspace) {
        getChildren().clear();
        gcodeModel = null;

        if (controlsNode != null) {
            controlsNode.dispose();
            controlsNode = null;
        }

        detachDesignChangeListener();
        VisualizerService.getInstance().removeBackgroundClickHandler(backgroundClickHandler);

        if (workspace == null) {
            return;
        }

        if (workspace instanceof UgsdWorkspaceContext) {
            designController = ControllerFactory.getController();
            controlsNode = new ControlsNode(designController.getDrawing(), designController.getSelectionManager(),
                    entityShapesNode::refreshFromController);
            entityShapesNode.setOnEntityMoved(controlsNode::refresh);
            entityShapesNode.refreshFromController();
            getChildren().add(entityShapesNode);
            getChildren().add(controlsNode);

            attachDesignChangeListener(designController);
            VisualizerService.getInstance().addBackgroundClickHandler(backgroundClickHandler);
        }

        addGcodeModel();
    }

    private void onDirtyStateChanged(boolean dirty) {
        removeGcodeModel();
        if (!dirty) {
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

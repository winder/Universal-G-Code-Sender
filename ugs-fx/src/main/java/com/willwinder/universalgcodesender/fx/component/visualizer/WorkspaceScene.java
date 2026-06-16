package com.willwinder.universalgcodesender.fx.component.visualizer;

import com.willwinder.ugs.designer.entities.entities.EntityListener;
import com.willwinder.ugs.designer.entities.entities.EventType;
import com.willwinder.ugs.designer.entities.entities.controls.Control;
import com.willwinder.ugs.designer.gui.MouseEntityEvent;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.logic.ControllerFactory;
import com.willwinder.ugs.designer.logic.Tool;
import com.willwinder.universalgcodesender.fx.component.visualizer.designer.ControlsNode;
import com.willwinder.universalgcodesender.fx.component.visualizer.designer.EntityShapeFactory;
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
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.MeshView;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
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
    private final Group drawPreview = new Group();
    private final Consumer<MouseEvent> backgroundClickHandler = this::onBackgroundClick;
    private ControlsNode controlsNode;
    private GcodeModel gcodeModel;
    private Controller designController;
    private EntityListener designChangeListener;
    private double zoomFactor = 1.0;

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
        VisualizerService.getInstance().setDrawGestureProvider(null);
        drawPreview.getChildren().clear();

        if (workspace == null) {
            return;
        }

        if (workspace instanceof UgsdWorkspaceContext) {
            designController = ControllerFactory.getController();
            controlsNode = new ControlsNode(designController.getDrawing(), designController.getSelectionManager(),
                    entityShapesNode::refreshFromController);
            controlsNode.onZoomChange(zoomFactor);
            entityShapesNode.setOnEntityMoved(controlsNode::refresh);
            entityShapesNode.refreshFromController();
            getChildren().add(entityShapesNode);
            getChildren().add(controlsNode);
            getChildren().add(drawPreview);

            attachDesignChangeListener(designController);
            VisualizerService.getInstance().addBackgroundClickHandler(backgroundClickHandler);
            VisualizerService.getInstance().setDrawGestureProvider(this::beginDrawGesture);
        }

        addGcodeModel();
    }

    /**
     * Begins a draw gesture when a designer creation tool (rectangle, ellipse, point, text,
     * line) is active. Returns a {@link DragHandler} that forwards the press/drag/release to
     * the matching legacy create-control as designer-space {@link MouseEntityEvent}s — the
     * control itself creates the entity, switches back to the select tool and selects it.
     * Returns null otherwise so normal selection/move handling applies.
     */
    private DragHandler beginDrawGesture(double designerX, double designerY) {
        if (designController == null || !isCreationTool(designController.getTool())) {
            return null;
        }

        Tool tool = designController.getTool();
        Point2D point = new Point2D.Double(designerX, designerY);
        Control createControl = designController.getDrawing().getControls().stream()
                .filter(control -> control.isWithin(point))
                .findFirst()
                .orElse(null);
        if (createControl == null) {
            return null;
        }

        return new DragHandler() {
            @Override
            public void onDragStart(double x, double y) {
                // Point/text create on press, so refresh the scene; drag tools only set state.
                boolean created = dispatch(createControl, EventType.MOUSE_PRESSED, x, y, x, y);
                if (created) {
                    refreshScene();
                }
            }

            @Override
            public void onDrag(double sx, double sy, double cx, double cy) {
                // No scene refresh mid-drag — nothing is created yet, the preview shows progress.
                dispatch(createControl, EventType.MOUSE_DRAGGED, sx, sy, cx, cy);
                showPreview(tool, sx, sy, cx, cy);
            }

            @Override
            public void onDragEnd(double sx, double sy, double cx, double cy) {
                dispatch(createControl, EventType.MOUSE_RELEASED, sx, sy, cx, cy);
                drawPreview.getChildren().clear();
                refreshScene();
            }
        };
    }

    private boolean dispatch(Control control, EventType type, double sx, double sy, double cx, double cy) {
        control.onEvent(new MouseEntityEvent(control, type,
                new Point2D.Double(sx, sy), new Point2D.Double(cx, cy)));
        // The control creates its entity and switches away from the creation tool when done.
        return !isCreationTool(designController.getTool());
    }

    private void refreshScene() {
        entityShapesNode.refreshFromController();
        if (controlsNode != null) {
            controlsNode.refresh();
        }
    }

    private void showPreview(Tool tool, double sx, double sy, double cx, double cy) {
        double minX = Math.min(sx, cx);
        double minY = Math.min(sy, cy);
        double width = Math.abs(cx - sx);
        double height = Math.abs(cy - sy);
        Shape shape = switch (tool) {
            case RECTANGLE -> new Rectangle2D.Double(minX, minY, width, height);
            case CIRCLE -> new Ellipse2D.Double(minX, minY, width, height);
            case LINE -> {
                Path2D.Double path = new Path2D.Double();
                path.moveTo(sx, sy);
                path.lineTo(cx, cy);
                yield path;
            }
            default -> null;
        };

        MeshView preview = EntityShapeFactory.createPreviewBorder(shape);
        if (preview == null) {
            drawPreview.getChildren().clear();
        } else {
            drawPreview.getChildren().setAll(preview);
        }
    }

    private static boolean isCreationTool(Tool tool) {
        return tool == Tool.POINT || tool == Tool.CIRCLE || tool == Tool.RECTANGLE
                || tool == Tool.LINE || tool == Tool.TEXT;
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
        this.zoomFactor = zoomFactor;
        entityShapesNode.onZoomChange(zoomFactor);
        if (controlsNode != null) {
            controlsNode.onZoomChange(zoomFactor);
        }
    }

    @Override
    public boolean useLighting() {
        return false;
    }
}

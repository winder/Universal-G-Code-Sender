package com.willwinder.universalgcodesender.fx.component.visualizer.designer;

import com.willwinder.ugs.designer.entities.entities.EventType;
import com.willwinder.ugs.designer.entities.entities.controls.Control;
import com.willwinder.ugs.designer.entities.entities.controls.MoveControl;
import com.willwinder.ugs.designer.entities.entities.controls.ResizeControl;
import com.willwinder.ugs.designer.entities.entities.controls.RotationControl;
import com.willwinder.ugs.designer.entities.entities.selection.SelectionListener;
import com.willwinder.ugs.designer.entities.entities.selection.SelectionManager;
import com.willwinder.ugs.designer.gui.Drawing;
import com.willwinder.ugs.designer.gui.MouseEntityEvent;
import com.willwinder.universalgcodesender.fx.component.visualizer.DragHandler;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.transform.Scale;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Objects;

public class ControlsNode extends Group {

    /**
     * The handles are drawn at a fixed size that is a bit large. This constant scales them down.
     */
    private static final double HANDLE_SCALE = 0.7;

    private final Drawing drawing;
    private final SelectionManager selectionManager;
    private final SelectionListener selectionListener;
    private final Runnable onEntityChanged;

    /**
     * The current workspace zoom factor. Handles are scaled by its inverse so they keep a
     * constant on-screen size regardless of how far the workspace is zoomed in or out.
     */
    private double zoomFactor = 1.0;

    public ControlsNode(Drawing drawing, SelectionManager selectionManager, Runnable onEntityChanged) {
        this.drawing = drawing;
        this.selectionManager = selectionManager;
        this.onEntityChanged = onEntityChanged;
        this.selectionListener = event -> Platform.runLater(this::refresh);
        selectionManager.addSelectionListener(selectionListener);
    }

    public void dispose() {
        selectionManager.removeSelectionListener(selectionListener);
    }

    public void refresh() {
        List<Node> nodes = drawing.getControls().stream()
                .map(this::createControlNode)
                .filter(Objects::nonNull)
                .toList();
        getChildren().setAll(nodes);
    }

    /**
     * Keeps the handles a constant size on screen by counter-scaling them against the workspace
     * zoom. Only the fixed-size handles carry a {@link Scale} (added in {@link #createControlNode}),
     * so the full-bounds controls (selection, highlight) are left alone and keep their hit area.
     */
    public void onZoomChange(double zoomFactor) {
        this.zoomFactor = zoomFactor;
        double scaleFactor = HANDLE_SCALE / zoomFactor;
        for (Node node : getChildren()) {
            node.getTransforms().stream()
                    .filter(Scale.class::isInstance)
                    .map(Scale.class::cast)
                    .findFirst()
                    .ifPresent(scale -> {
                        scale.setX(scaleFactor);
                        scale.setY(scaleFactor);
                    });
        }
    }

    /**
     * Adds a {@link Scale} pivoted on the handle's own centre so it keeps a constant on-screen
     * size while staying anchored to the point it controls.
     */
    private void applyConstantScreenSize(Node node) {
        double scaleFactor = HANDLE_SCALE / zoomFactor;
        Bounds bounds = node.getBoundsInLocal();
        node.getTransforms().add(new Scale(scaleFactor, scaleFactor, bounds.getCenterX(), bounds.getCenterY()));
    }

    /**
     * The small drag, resize and rotate handles are drawn at a fixed size and should stay that
     * size on screen. The full-bounds controls (selection rectangle, model highlight, creation
     * and vertex overlays) must keep tracking the entity geometry, so they are not counter-scaled.
     */
    private static boolean isFixedSizeHandle(Control control) {
        return control instanceof ResizeControl
                || control instanceof RotationControl
                || control instanceof MoveControl;
    }

    private Node createControlNode(Control control) {
        DragHandler dragHandler = new DragHandler() {
            @Override
            public void onDragStart(double x, double y) {
                control.onEvent(new MouseEntityEvent(control, EventType.MOUSE_PRESSED,
                        new Point2D.Double(x, y), new Point2D.Double(x, y)));
            }

            @Override
            public void onDrag(double sx, double sy, double cx, double cy) {
                control.onEvent(new MouseEntityEvent(control, EventType.MOUSE_DRAGGED,
                        new Point2D.Double(sx, sy), new Point2D.Double(cx, cy)));
                onEntityChanged.run();
                refresh();
            }

            @Override
            public void onDragEnd(double sx, double sy, double cx, double cy) {
                control.onEvent(new MouseEntityEvent(control, EventType.MOUSE_RELEASED,
                        new Point2D.Double(sx, sy), new Point2D.Double(cx, cy)));
                onEntityChanged.run();
                refresh();
            }
        };
        Node node = EntityShapeFactory.createControlNode(control, dragHandler);
        if (node != null && isFixedSizeHandle(control)) {
            applyConstantScreenSize(node);
        }
        return node;
    }
}

package com.willwinder.universalgcodesender.fx.component.designer;

import com.willwinder.ugs.designer.entities.EventType;
import com.willwinder.ugs.designer.entities.controls.Control;
import com.willwinder.ugs.designer.entities.controls.MoveControl;
import com.willwinder.ugs.designer.entities.controls.ResizeControl;
import com.willwinder.ugs.designer.entities.controls.RotationControl;
import com.willwinder.ugs.designer.gui.MouseEntityEvent;
import com.willwinder.universalgcodesender.fx.component.visualizer.DragHandler;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.transform.Scale;

import java.awt.geom.Point2D;
import java.util.Optional;

/**
 * Wraps a designer {@link Control} together with the JavaFX {@link Node} that renders its
 * on-screen handle. Encapsulates translating pointer drags into designer
 * {@link MouseEntityEvent}s that drive the control's move, resize and rotate operations, and
 * keeps the small move/resize/rotate handles at a constant on-screen size regardless of the
 * workspace zoom.
 */
public class ControlHandle {

    /**
     * The handles are drawn at a fixed size that is a bit large. This constant scales them down.
     */
    private static final double HANDLE_SCALE = 0.7;

    private final Control control;
    private final Runnable onEntityChanged;
    private final Runnable onRefresh;
    private Node node;

    /**
     * The counter-scale keeping a fixed-size handle a constant on-screen size, or null for
     * full-bounds controls (selection, highlight) that must keep tracking the entity geometry.
     */
    private Scale scale;

    private ControlHandle(Control control, Runnable onEntityChanged, Runnable onRefresh) {
        this.control = control;
        this.onEntityChanged = onEntityChanged;
        this.onRefresh = onRefresh;
    }

    /**
     * Builds a handle for the given control, or an empty {@link Optional} when the control did not
     * draw anything — many controls short-circuit their render based on tool/selection state but
     * still expose a stale shape, and without this the handles would linger after a selection is
     * cleared.
     *
     * @param control         the designer control to wrap
     * @param zoomFactor      the current workspace zoom, used to size fixed handles
     * @param onEntityChanged invoked after a drag mutates the entity, so the scene can rebuild
     * @param onRefresh       invoked after a drag so the handles can follow the entity
     */
    public static Optional<ControlHandle> create(Control control, double zoomFactor, Runnable onEntityChanged, Runnable onRefresh) {
        ControlHandle handle = new ControlHandle(control, onEntityChanged, onRefresh);
        Node node = EntityShapeFactory.createControlNode(control, handle.createDragHandler());
        if (node == null) {
            return Optional.empty();
        }

        handle.node = node;
        control.getHoverCursor()
                .map(ControlHandle::toFxCursor)
                .ifPresent(node::setCursor);
        if (isFixedSizeHandle(control)) {
            handle.applyConstantScreenSize(zoomFactor);
        }
        return Optional.of(handle);
    }

    public Node getNode() {
        return node;
    }

    /**
     * Keeps a fixed-size handle a constant size on screen by counter-scaling it against the
     * workspace zoom. Full-bounds controls carry no {@link Scale} and are left untouched so they
     * keep tracking the entity geometry and hit area.
     */
    public void onZoomChange(double zoomFactor) {
        if (scale == null) {
            return;
        }
        double scaleFactor = HANDLE_SCALE / zoomFactor;
        scale.setX(scaleFactor);
        scale.setY(scaleFactor);
    }

    private DragHandler createDragHandler() {
        return new DragHandler() {
            @Override
            public void onDragStart(double x, double y) {
                dispatch(EventType.MOUSE_PRESSED, x, y, x, y);
            }

            @Override
            public void onDrag(double startX, double startY, double currentX, double currentY) {
                dispatch(EventType.MOUSE_DRAGGED, startX, startY, currentX, currentY);
                onEntityChanged.run();
                onRefresh.run();
            }

            @Override
            public void onDragEnd(double startX, double startY, double currentX, double currentY) {
                dispatch(EventType.MOUSE_RELEASED, startX, startY, currentX, currentY);
                onEntityChanged.run();
                onRefresh.run();
            }
        };
    }

    private void dispatch(EventType type, double startX, double startY, double currentX, double currentY) {
        control.onEvent(new MouseEntityEvent(control, type,
                new Point2D.Double(startX, startY), new Point2D.Double(currentX, currentY)));
    }

    /**
     * Maps the AWT hover {@link java.awt.Cursor} a control exposes onto its JavaFX equivalent.
     * Custom cursors (e.g. the rotation control's SVG icon) have no direct JavaFX counterpart and
     * fall back to a crosshair so the handle still signals it is interactive.
     */
    private static Cursor toFxCursor(java.awt.Cursor cursor) {
        return switch (cursor.getType()) {
            case java.awt.Cursor.HAND_CURSOR -> Cursor.HAND;
            case java.awt.Cursor.MOVE_CURSOR -> Cursor.MOVE;
            case java.awt.Cursor.CROSSHAIR_CURSOR -> Cursor.CROSSHAIR;
            case java.awt.Cursor.TEXT_CURSOR -> Cursor.TEXT;
            case java.awt.Cursor.WAIT_CURSOR -> Cursor.WAIT;
            case java.awt.Cursor.N_RESIZE_CURSOR -> Cursor.N_RESIZE;
            case java.awt.Cursor.S_RESIZE_CURSOR -> Cursor.S_RESIZE;
            case java.awt.Cursor.E_RESIZE_CURSOR -> Cursor.E_RESIZE;
            case java.awt.Cursor.W_RESIZE_CURSOR -> Cursor.W_RESIZE;
            case java.awt.Cursor.NE_RESIZE_CURSOR -> Cursor.NE_RESIZE;
            case java.awt.Cursor.NW_RESIZE_CURSOR -> Cursor.NW_RESIZE;
            case java.awt.Cursor.SE_RESIZE_CURSOR -> Cursor.SE_RESIZE;
            case java.awt.Cursor.SW_RESIZE_CURSOR -> Cursor.SW_RESIZE;
            default -> Cursor.CROSSHAIR;
        };
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

    /**
     * Adds a {@link Scale} pivoted on the handle's own centre so it keeps a constant on-screen
     * size while staying anchored to the point it controls.
     */
    private void applyConstantScreenSize(double zoomFactor) {
        double scaleFactor = HANDLE_SCALE / zoomFactor;
        Bounds bounds = node.getBoundsInLocal();
        scale = new Scale(scaleFactor, scaleFactor, bounds.getCenterX(), bounds.getCenterY());
        node.getTransforms().add(scale);
    }
}

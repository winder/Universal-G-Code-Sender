package com.willwinder.universalgcodesender.fx.component.visualizer.designer;

import com.willwinder.ugs.designer.entities.entities.EventType;
import com.willwinder.ugs.designer.entities.entities.controls.Control;
import com.willwinder.ugs.designer.entities.entities.selection.SelectionListener;
import com.willwinder.ugs.designer.entities.entities.selection.SelectionManager;
import com.willwinder.ugs.designer.gui.Drawing;
import com.willwinder.ugs.designer.gui.MouseEntityEvent;
import com.willwinder.universalgcodesender.fx.component.visualizer.DragHandler;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Node;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Objects;

public class ControlsNode extends Group {

    private final Drawing drawing;
    private final SelectionManager selectionManager;
    private final SelectionListener selectionListener;
    private final Runnable onEntityChanged;

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

    private void refresh() {
        List<Node> nodes = drawing.getControls().stream()
                .map(this::createControlNode)
                .filter(Objects::nonNull)
                .toList();
        getChildren().setAll(nodes);
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
        return EntityShapeFactory.createControlNode(control, dragHandler);
    }
}

package com.willwinder.universalgcodesender.fx.component.designer;

import com.willwinder.ugs.designer.entities.controls.Control;
import com.willwinder.ugs.designer.entities.selection.SelectionListener;
import com.willwinder.ugs.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.designer.gui.Drawing;
import javafx.application.Platform;
import javafx.scene.Group;

import java.util.ArrayList;
import java.util.List;

public class ControlsNode extends Group {

    private final Drawing drawing;
    private final SelectionManager selectionManager;
    private final SelectionListener selectionListener;
    private final Runnable onEntityChanged;
    private final List<ControlHandle> handles = new ArrayList<>();

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
        handles.clear();
        for (Control control : drawing.getControls()) {
            ControlHandle.create(control, zoomFactor, onEntityChanged, this::refresh)
                    .ifPresent(handles::add);
        }
        getChildren().setAll(handles.stream().map(ControlHandle::getNode).toList());
    }

    public void onZoomChange(double zoomFactor) {
        this.zoomFactor = zoomFactor;
        handles.forEach(handle -> handle.onZoomChange(zoomFactor));
    }
}

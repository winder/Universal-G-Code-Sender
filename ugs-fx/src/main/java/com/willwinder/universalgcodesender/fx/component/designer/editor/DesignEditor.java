/*
    Copyright 2026 Joacim Breiler

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.universalgcodesender.fx.component.designer.editor;

import com.willwinder.ugs.designer.actions.MoveAction;
import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.entities.cuttable.Text;
import com.willwinder.ugs.designer.entities.selection.SelectionListener;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.logic.ControllerEventType;
import com.willwinder.ugs.designer.logic.ControllerFactory;
import com.willwinder.ugs.designer.logic.ControllerListener;
import com.willwinder.ugs.designer.logic.Tool;
import com.willwinder.universalgcodesender.fx.component.visualizer.input.InputHandler;
import com.willwinder.universalgcodesender.fx.component.visualizer.input.MouseMapping;
import com.willwinder.universalgcodesender.fx.component.visualizer.input.PointerEvent;
import com.willwinder.universalgcodesender.fx.component.visualizer.overlay.OverlayPainter;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Camera;
import com.willwinder.universalgcodesender.fx.model.UgsdWorkspaceContext;
import com.willwinder.universalgcodesender.fx.model.WorkspaceContext;
import com.willwinder.universalgcodesender.fx.service.WorkspaceManager;
import com.willwinder.universalgcodesender.fx.settings.VisualizerSettings;
import com.willwinder.universalgcodesender.fx.settings.VisualizerSettings.ModifierKey;
import javafx.application.Platform;
import javafx.geometry.Point3D;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Edits the design of the active {@code .ugsd} workspace with the mouse. Sits in the input
 * stack before the camera navigation and claims the primary button's presses over the work
 * plane, handing them to the tool the designer {@link Controller} says is current. Modifiers
 * such as Shift and Alt are passed on to the tools, unless the combination is what pans or
 * rotates the view. Anything else falls through so the view still pans and rotates.
 */
public final class DesignEditor implements InputHandler, OverlayPainter {
    private static final Font READOUT_FONT = Font.font(11);
    private static final Color READOUT_COLOR = Color.web("#303030");
    private static final Color READOUT_BACKGROUND = Color.rgb(255, 255, 255, 0.85);

    private final Camera camera;
    private final Runnable requestRender;
    private final Consumer<Text> textEditor;
    private final BiConsumer<Double, Double> contextMenu;
    private final Predicate<MouseEvent> claimsPress;
    private final EditorState state = new EditorState();
    private final Map<Tool, EditorTool> tools = new EnumMap<>(Tool.class);
    private final ControllerListener controllerListener = event -> {
        if (event == ControllerEventType.TOOL_SELECTED || event == ControllerEventType.NEW_DRAWING) {
            Platform.runLater(this::syncTool);
        }
    };
    private final SelectionListener selectionListener = event -> Platform.runLater(this::onSelectionChanged);
    private final WorkspaceManager.WorkspaceListener workspaceListener = new WorkspaceManager.WorkspaceListener() {
        @Override
        public void onWorkspaceOpened(WorkspaceContext workspace) {
            Platform.runLater(() -> bind(workspace));
        }

        @Override
        public void onWorkspaceClosed() {
            Platform.runLater(() -> bind(null));
        }

        @Override
        public void onWorkspaceDirtyStateChanged(WorkspaceContext workspace, boolean dirty) {
        }
    };

    private Controller controller;
    private ToolContext toolContext;
    private EditorTool currentTool;
    private Tool currentToolType;
    private Point2D lastDesignPoint;

    /**
     * @param camera        the visualizer camera, for pixel to millimeter conversions
     * @param requestRender asks the visualizer for a frame
     * @param textEditor    opens the inline editor for a text entity
     * @param contextMenu   shows the design context menu at the given screen position
     */
    public DesignEditor(Camera camera, Runnable requestRender, Consumer<Text> textEditor,
                        BiConsumer<Double, Double> contextMenu) {
        this(camera, requestRender, textEditor, contextMenu, DesignEditor::isEditingPress);
    }

    /**
     * @param claimsPress decides whether a press belongs to the editor rather than the view
     */
    DesignEditor(Camera camera, Runnable requestRender, Consumer<Text> textEditor,
                 BiConsumer<Double, Double> contextMenu, Predicate<MouseEvent> claimsPress) {
        this.camera = camera;
        this.requestRender = requestRender;
        this.textEditor = textEditor;
        this.contextMenu = contextMenu;
        this.claimsPress = claimsPress;
        WorkspaceManager.getInstance().addListener(workspaceListener);
        bind(WorkspaceManager.getInstance().getActiveWorkspace().orElse(null));
    }

    public void dispose() {
        WorkspaceManager.getInstance().removeListener(workspaceListener);
        bind(null);
    }

    /**
     * Whether a design workspace is open and shown, so the editor takes input.
     */
    public boolean isActive() {
        return controller != null && VisualizerSettings.getInstance().showDesignProperty().get();
    }

    public EditorState state() {
        return state;
    }

    public Optional<Controller> controller() {
        return Optional.ofNullable(controller);
    }

    public Tool currentToolType() {
        return currentToolType;
    }

    /**
     * The handles around the selection at the current zoom, empty unless the select tool is
     * active.
     */
    public List<HandleSet.Handle> handles() {
        if (!isActive() || currentToolType != Tool.SELECT) {
            return List.of();
        }
        return HandleSet.handles(controller.getSelectionManager(),
                point -> camera.worldUnitsPerPixelAt(new Point3D(point.getX(), point.getY(), 0)));
    }

    private static boolean isEditingPress(MouseEvent event) {
        VisualizerSettings settings = VisualizerSettings.getInstance();
        MouseMapping primary = MouseMapping.parse(settings.primaryMouseButtonProperty().get(),
                settings.primaryModifierKeyProperty().get(), MouseButton.PRIMARY, ModifierKey.NONE);
        MouseMapping pan = MouseMapping.parse(settings.panMouseButtonProperty().get(),
                settings.panModifierKeyProperty().get(), MouseButton.SECONDARY, ModifierKey.NONE);
        MouseMapping rotate = MouseMapping.parse(settings.rotateMouseButtonProperty().get(),
                settings.rotateModifierKeyProperty().get(), MouseButton.SECONDARY, ModifierKey.SHIFT);
        return primary.matchesPressWithExtraModifiers(event) && !pan.matchesPress(event) && !rotate.matchesPress(event);
    }

    @Override
    public boolean onPressed(PointerEvent event) {
        if (!isActive() || currentTool == null || !claimsPress.test(event.mouse())) {
            return false;
        }
        Optional<Point2D> design = toDesign(event);
        if (design.isEmpty()) {
            return false;
        }
        lastDesignPoint = design.get();
        currentTool.onPressed(event, design.get());
        requestRender.run();
        return true;
    }

    @Override
    public void onDragged(PointerEvent event, PointerEvent pressed) {
        if (currentTool == null) {
            return;
        }
        Point2D design = toDesign(event).orElse(lastDesignPoint);
        if (design != null) {
            lastDesignPoint = design;
            currentTool.onDragged(event, design);
        }
    }

    @Override
    public void onReleased(PointerEvent event, PointerEvent pressed) {
        if (currentTool == null) {
            return;
        }
        Point2D design = toDesign(event).orElse(lastDesignPoint);
        if (design != null) {
            currentTool.onReleased(event, design);
        }
        requestRender.run();
    }

    @Override
    public boolean onMoved(PointerEvent event) {
        if (isActive() && currentTool != null) {
            toDesign(event).ifPresent(design -> currentTool.onMoved(event, design));
        }
        return false;
    }

    @Override
    public Optional<Cursor> cursorAt(PointerEvent event) {
        if (!isActive() || currentTool == null) {
            return Optional.empty();
        }
        return toDesign(event).flatMap(design -> currentTool.cursorAt(event, design));
    }

    @Override
    public boolean onKeyPressed(KeyEvent event) {
        if (!isActive()) {
            return false;
        }
        if (event.getCode() == KeyCode.ESCAPE) {
            controller.setTool(Tool.SELECT);
            return true;
        }
        if (event.getCode().isArrowKey()) {
            return nudgeSelection(event);
        }
        return currentTool != null && currentTool.onKeyPressed(event);
    }

    @Override
    public boolean onContextMenu(PointerEvent event) {
        if (!isActive()) {
            return false;
        }
        contextMenu.accept(event.mouse().getScreenX(), event.mouse().getScreenY());
        return true;
    }

    @Override
    public void paint(GraphicsContext graphics, Camera camera, double width, double height) {
        String readout = state.readout();
        Point2D position = state.readoutPosition();
        if (!isActive() || readout == null || position == null) {
            return;
        }
        camera.project(new Point3D(position.getX(), position.getY(), 0)).ifPresent(pixel -> {
            graphics.setFont(READOUT_FONT);
            graphics.setTextAlign(TextAlignment.LEFT);
            graphics.setTextBaseline(VPos.BOTTOM);
            double textWidth = readout.length() * 6.5 + 8;
            graphics.setFill(READOUT_BACKGROUND);
            graphics.fillRoundRect(pixel.getX() + 12, pixel.getY() - 26, textWidth, 18, 4, 4);
            graphics.setFill(READOUT_COLOR);
            graphics.fillText(readout, pixel.getX() + 16, pixel.getY() - 11);
        });
    }

    /**
     * Arrow keys move the selection by the snap grid, or a millimeter, ten times that with
     * Shift, as one undoable move each.
     */
    private boolean nudgeSelection(KeyEvent event) {
        List<Entity> selected = new ArrayList<>(controller.getSelectionManager().getSelection());
        if (selected.isEmpty()) {
            return false;
        }
        double step = toolContext.snapper().nudgeStep() * (event.isShiftDown() ? 10 : 1);
        Point2D delta = switch (event.getCode()) {
            case LEFT -> new Point2D.Double(-step, 0);
            case RIGHT -> new Point2D.Double(step, 0);
            case UP -> new Point2D.Double(0, step);
            case DOWN -> new Point2D.Double(0, -step);
            default -> null;
        };
        if (delta == null) {
            return false;
        }
        MoveAction action = new MoveAction(selected, delta);
        action.execute();
        controller.getUndoManager().addAction(action);
        requestRender.run();
        return true;
    }

    private void onSelectionChanged() {
        if (currentTool != null) {
            currentTool.onSelectionChanged();
        }
        requestRender.run();
    }

    private Optional<Point2D> toDesign(PointerEvent event) {
        return event.workPlanePoint().map(point -> new Point2D.Double(point.getX(), point.getY()));
    }

    private void bind(WorkspaceContext workspace) {
        unbind();
        if (!(workspace instanceof UgsdWorkspaceContext)) {
            requestRender.run();
            return;
        }
        controller = ControllerFactory.getController();
        toolContext = new ToolContext(controller, camera, new HitTester(controller),
                new Snapper(() -> controller.getModel().getSnapToGrid()), state, requestRender, textEditor);
        tools.put(Tool.SELECT, new SelectTool(toolContext));
        tools.put(Tool.RECTANGLE, CreateShapeTool.rectangle(toolContext));
        tools.put(Tool.CIRCLE, CreateShapeTool.ellipse(toolContext));
        tools.put(Tool.LINE, new LineTool(toolContext));
        tools.put(Tool.POINT, new PointTool(toolContext));
        tools.put(Tool.TEXT, new TextTool(toolContext));
        tools.put(Tool.VERTEX, new VertexTool(toolContext));
        controller.addListener(controllerListener);
        controller.getSelectionManager().addSelectionListener(selectionListener);
        syncTool();
    }

    private void unbind() {
        if (controller != null) {
            controller.removeListener(controllerListener);
            controller.getSelectionManager().removeSelectionListener(selectionListener);
        }
        if (currentTool != null) {
            currentTool.deactivate();
        }
        controller = null;
        toolContext = null;
        currentTool = null;
        currentToolType = null;
        tools.clear();
        state.clearTransient();
        state.setHoveredHandle(null);
        state.setHoveredEntity(null);
        state.setVertices(List.of(), -1);
    }

    /**
     * Follows the controller's tool, which the toolbar and the creation tools change.
     */
    private void syncTool() {
        if (controller == null) {
            return;
        }
        Tool tool = controller.getTool();
        if (tool == currentToolType && currentTool != null) {
            return;
        }
        if (currentTool != null) {
            currentTool.deactivate();
        }
        currentToolType = tool;
        currentTool = tools.get(tool);
        if (currentTool != null) {
            currentTool.activate();
        }
        requestRender.run();
    }
}

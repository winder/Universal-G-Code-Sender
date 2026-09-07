package com.willwinder.universalgcodesender.fx.component.designer.editor;

import com.willwinder.ugs.designer.entities.Anchor;
import com.willwinder.ugs.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Camera;
import com.willwinder.universalgcodesender.fx.component.visualizer.input.PointerEvent;
import javafx.event.EventType;
import javafx.scene.input.MouseEvent;
import org.junit.Before;
import org.junit.Test;

import java.awt.geom.Point2D;
import java.util.List;

import static com.willwinder.universalgcodesender.fx.component.designer.editor.EditorTestSupport.design;
import static com.willwinder.universalgcodesender.fx.component.designer.editor.EditorTestSupport.drag;
import static com.willwinder.universalgcodesender.fx.component.designer.editor.EditorTestSupport.pointer;
import static com.willwinder.universalgcodesender.fx.component.designer.editor.EditorTestSupport.press;
import static com.willwinder.universalgcodesender.fx.component.designer.editor.EditorTestSupport.release;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class SelectToolTest {
    private Controller controller;
    private Camera camera;
    private EditorState state;
    private SelectTool tool;
    private Rectangle rectangle;

    @Before
    public void setUp() {
        controller = EditorTestSupport.freshController();
        camera = EditorTestSupport.topDownCamera();
        state = new EditorState();
        tool = new SelectTool(EditorTestSupport.toolContext(controller, camera, state));
        rectangle = new Rectangle(10, 10, 20, 10);
        controller.getModel().insertEntity(rectangle);
        controller.getUndoManager().clear();
    }

    @Test
    public void click_shouldSelectTheEntityUnderTheCursor() {
        tool.onPressed(press(15, 15), design(15, 15));
        tool.onReleased(release(15, 15), design(15, 15));

        assertThat(controller.getSelectionManager().getSelection()).containsExactly(rectangle);
    }

    @Test
    public void click_onTheSelectedEntityShouldCycleThroughTheEntitiesUnderTheCursor() {
        Rectangle inner = new Rectangle(12, 12, 3, 3);
        controller.getModel().insertEntity(inner);

        tool.onPressed(press(13, 13), design(13, 13));
        tool.onReleased(release(13, 13), design(13, 13));
        assertThat(controller.getSelectionManager().getSelection()).containsExactly(inner);

        tool.onPressed(press(13, 13), design(13, 13));
        tool.onReleased(release(13, 13), design(13, 13));
        assertThat(controller.getSelectionManager().getSelection()).containsExactly(rectangle);

        tool.onPressed(press(13, 13), design(13, 13));
        tool.onReleased(release(13, 13), design(13, 13));
        assertThat(controller.getSelectionManager().getSelection()).containsExactly(inner);
        assertThat(controller.getUndoManager().canUndo()).isFalse();
    }

    @Test
    public void click_onEmptySpaceShouldClearTheSelection() {
        controller.getSelectionManager().setSelection(List.of(rectangle));

        tool.onPressed(press(80, 80), design(80, 80));
        tool.onReleased(release(80, 80), design(80, 80));

        assertThat(controller.getSelectionManager().isEmpty()).isTrue();
    }

    @Test
    public void shiftClick_shouldAddTheEntityToTheSelection() {
        Rectangle other = new Rectangle(60, 60, 5, 5);
        controller.getModel().insertEntity(other);
        controller.getSelectionManager().setSelection(List.of(other));

        tool.onPressed(shiftPointer(MouseEvent.MOUSE_PRESSED, 15, 15), design(15, 15));
        tool.onReleased(shiftPointer(MouseEvent.MOUSE_RELEASED, 15, 15), design(15, 15));

        assertThat(controller.getSelectionManager().getSelection()).containsExactlyInAnyOrder(rectangle, other);
    }

    @Test
    public void shiftClick_onASelectedEntityShouldRemoveItFromTheSelection() {
        Rectangle other = new Rectangle(60, 60, 5, 5);
        controller.getModel().insertEntity(other);
        controller.getSelectionManager().setSelection(List.of(rectangle, other));
        Point2D before = rectangle.getPosition();

        tool.onPressed(shiftPointer(MouseEvent.MOUSE_PRESSED, 15, 15), design(15, 15));
        tool.onReleased(shiftPointer(MouseEvent.MOUSE_RELEASED, 15, 15), design(15, 15));

        assertThat(controller.getSelectionManager().getSelection()).containsExactly(other);
        assertThat(rectangle.getPosition()).isEqualTo(before);
    }

    @Test
    public void shiftDrag_shouldToggleWhatTheRubberBandTouches() {
        Rectangle other = new Rectangle(60, 60, 5, 5);
        controller.getModel().insertEntity(other);
        controller.getSelectionManager().setSelection(List.of(rectangle, other));

        tool.onPressed(shiftPointer(MouseEvent.MOUSE_PRESSED, 0, 0), design(0, 0));
        tool.onDragged(shiftPointer(MouseEvent.MOUSE_DRAGGED, 35, 25), design(35, 25));
        tool.onReleased(shiftPointer(MouseEvent.MOUSE_RELEASED, 35, 25), design(35, 25));

        assertThat(controller.getSelectionManager().getSelection()).containsExactly(other);
    }

    @Test
    public void drag_onEmptySpaceShouldSelectWhatTheRubberBandTouches() {
        Rectangle other = new Rectangle(60, 60, 5, 5);
        controller.getModel().insertEntity(other);

        tool.onPressed(press(0, 0), design(0, 0));
        tool.onDragged(drag(35, 25), design(35, 25));
        assertThat(state.rubberBand()).isNotNull();
        tool.onReleased(release(35, 25), design(35, 25));

        assertThat(controller.getSelectionManager().getSelection()).containsExactly(rectangle);
        assertThat(state.rubberBand()).isNull();
    }

    @Test
    public void drag_insideTheSelectionShouldMoveItAndRecordOneUndo() {
        controller.getSelectionManager().setSelection(List.of(rectangle));
        Point2D before = rectangle.getPosition();

        tool.onPressed(press(15, 15), design(15, 15));
        tool.onDragged(drag(25, 18), design(25, 18));
        tool.onReleased(release(25, 18), design(25, 18));

        Point2D after = rectangle.getPosition();
        assertThat(after.getX()).isCloseTo(before.getX() + 10, within(1e-6));
        assertThat(after.getY()).isCloseTo(before.getY() + 3, within(1e-6));
        assertThat(controller.getUndoManager().canUndo()).isTrue();

        controller.getUndoManager().undo();
        assertThat(rectangle.getPosition().getX()).isCloseTo(before.getX(), within(1e-6));
        assertThat(rectangle.getPosition().getY()).isCloseTo(before.getY(), within(1e-6));
    }

    @Test
    public void drag_withShiftShouldConstrainTheMoveToOneAxis() {
        controller.getSelectionManager().setSelection(List.of(rectangle));
        Point2D before = rectangle.getPosition();

        tool.onPressed(press(15, 15), design(15, 15));
        tool.onDragged(pointer(MouseEvent.MOUSE_DRAGGED, 25, 18, true, false, 1), design(25, 18));
        tool.onReleased(pointer(MouseEvent.MOUSE_RELEASED, 25, 18, true, false, 1), design(25, 18));

        assertThat(rectangle.getPosition().getX()).isCloseTo(before.getX() + 10, within(1e-6));
        assertThat(rectangle.getPosition().getY()).isCloseTo(before.getY(), within(1e-6));
    }

    @Test
    public void drag_onAResizeHandleShouldResizeAndRecordOneUndo() {
        controller.getSelectionManager().setSelection(List.of(rectangle));
        HandleSet.Handle handle = tool.handles().stream()
                .filter(h -> h.anchor() == Anchor.LEFT_CENTER)
                .findFirst().orElseThrow();
        Point2D start = handle.center();

        tool.onPressed(press(start.getX(), start.getY()), start);
        Point2D end = design(start.getX() + 10, start.getY());
        tool.onDragged(drag(end.getX(), end.getY()), end);
        tool.onReleased(release(end.getX(), end.getY()), end);

        assertThat(rectangle.getSize().getWidth()).isCloseTo(30, within(1e-6));
        assertThat(rectangle.getSize().getHeight()).isCloseTo(10, within(1e-6));
        assertThat(rectangle.getPosition().getX()).isCloseTo(10, within(1e-6));
        assertThat(controller.getUndoManager().canUndo()).isTrue();

        controller.getUndoManager().undo();
        assertThat(rectangle.getSize().getWidth()).isCloseTo(20, within(1e-6));
    }

    @Test
    public void drag_onTheRotateHandleShouldRotateAroundTheCenterAndRecordOneUndo() {
        controller.getSelectionManager().setSelection(List.of(rectangle));
        HandleSet.Handle handle = tool.handles().stream()
                .filter(h -> h.kind() == HandleSet.Kind.ROTATE)
                .findFirst().orElseThrow();
        Point2D center = rectangle.getCenter();
        Point2D start = handle.center();
        // A quarter turn around the centre.
        Point2D end = design(center.getX() + (start.getY() - center.getY()), center.getY());

        tool.onPressed(press(start.getX(), start.getY()), start);
        tool.onDragged(drag(end.getX(), end.getY()), end);
        tool.onReleased(release(end.getX(), end.getY()), end);

        assertThat(Math.abs(rectangle.getRotation())).isCloseTo(90, within(1e-6));
        assertThat(rectangle.getCenter().getX()).isCloseTo(center.getX(), within(1e-6));
        assertThat(rectangle.getCenter().getY()).isCloseTo(center.getY(), within(1e-6));
        assertThat(controller.getUndoManager().canUndo()).isTrue();
    }

    @Test
    public void handles_shouldBeEmptyWithoutSelection() {
        assertThat(tool.handles()).isEmpty();
    }

    @Test
    public void onMoved_shouldTrackTheHoveredHandle() {
        controller.getSelectionManager().setSelection(List.of(rectangle));
        HandleSet.Handle handle = tool.handles().getFirst();

        tool.onMoved(press(handle.center().getX(), handle.center().getY()), handle.center());

        assertThat(state.hoveredHandle()).isEqualTo(handle);
    }

    private static PointerEvent shiftPointer(EventType<MouseEvent> type, double x, double y) {
        return pointer(type, x, y, true, false, 1);
    }
}

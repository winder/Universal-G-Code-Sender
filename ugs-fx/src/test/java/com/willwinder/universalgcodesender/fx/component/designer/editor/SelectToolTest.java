package com.willwinder.universalgcodesender.fx.component.designer.editor;

import com.willwinder.ugs.designer.constraint.Axis;
import com.willwinder.ugs.designer.constraint.DistanceConstraint;
import com.willwinder.ugs.designer.constraint.ShapePoint;
import com.willwinder.ugs.designer.constraint.ShapeSnapping;
import com.willwinder.ugs.designer.entities.Anchor;
import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.entities.cuttable.Path;
import com.willwinder.ugs.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.logic.Tool;
import com.willwinder.universalgcodesender.fx.component.visualizer.input.PointerEvent;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Camera;
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
    private EditorState state;
    private SelectTool tool;
    private Rectangle rectangle;

    @Before
    public void setUp() {
        controller = EditorTestSupport.freshController();
        Camera camera = EditorTestSupport.topDownCamera();
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
    public void doubleClick_onAPathShouldSwitchToTheVertexToolWithThePathSelected() {
        Path path = new Path();
        path.moveTo(50, 50);
        path.lineTo(80, 50);
        controller.getModel().insertEntity(path);

        tool.onPressed(press(65, 50), design(65, 50));
        tool.onReleased(release(65, 50), design(65, 50));
        tool.onPressed(pointer(MouseEvent.MOUSE_PRESSED, 65, 50, false, false, 2), design(65, 50));
        tool.onReleased(pointer(MouseEvent.MOUSE_RELEASED, 65, 50, false, false, 2), design(65, 50));

        assertThat(controller.getTool()).isEqualTo(Tool.VERTEX);
        assertThat(controller.getSelectionManager().getSelection()).containsExactly(path);
        assertThat(controller.getUndoManager().canUndo()).isFalse();
    }

    @Test
    public void doubleClick_onAShapeThatIsNotAPathShouldStayInTheSelectTool() {
        tool.onPressed(press(15, 15), design(15, 15));
        tool.onReleased(release(15, 15), design(15, 15));
        tool.onPressed(pointer(MouseEvent.MOUSE_PRESSED, 15, 15, false, false, 2), design(15, 15));
        tool.onReleased(pointer(MouseEvent.MOUSE_RELEASED, 15, 15, false, false, 2), design(15, 15));

        assertThat(controller.getTool()).isEqualTo(Tool.SELECT);
        assertThat(controller.getSelectionManager().getSelection()).containsExactly(rectangle);
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
    public void drag_onAResizeHandleShouldSnapTheDraggedEdgeToTheGridLines() {
        controller.getModel().setSnapToGrid(0.5);
        Rectangle offGrid = new Rectangle(50, 50, 10.1, 10);
        controller.getModel().insertEntity(offGrid);
        controller.getSelectionManager().setSelection(List.of(offGrid));
        HandleSet.Handle handle = tool.handles().stream()
                .filter(h -> h.anchor() == Anchor.LEFT_CENTER)
                .findFirst().orElseThrow();
        Point2D start = handle.center();

        // The right edge starts at 60.1; a short drag brings it to the grid line at 60, not 60.6.
        tool.onPressed(press(start.getX(), start.getY()), start);
        Point2D end = design(start.getX() + 0.1, start.getY());
        tool.onDragged(drag(end.getX(), end.getY()), end);
        tool.onReleased(release(end.getX(), end.getY()), end);

        assertThat(offGrid.getPosition().getX()).isCloseTo(50, within(1e-6));
        assertThat(offGrid.getSize().getWidth()).isCloseTo(10, within(1e-6));
    }

    @Test
    public void drag_onAResizeHandleOfARotatedStraightLineShouldLengthenIt() {
        Path line = new Path();
        line.moveTo(50, 50);
        line.lineTo(70, 50);
        controller.getModel().insertEntity(line);
        line.rotate(30);
        controller.getSelectionManager().setSelection(List.of(line));
        Point2D center = line.getCenter();
        HandleSet.Handle handle = tool.handles().stream()
                .filter(h -> h.anchor() == Anchor.LEFT_CENTER)
                .findFirst().orElseThrow();
        Point2D start = handle.center();
        double length = start.distance(center);
        Point2D end = design(start.getX() + (start.getX() - center.getX()) / length * 10,
                start.getY() + (start.getY() - center.getY()) / length * 10);

        tool.onPressed(press(start.getX(), start.getY()), start);
        tool.onDragged(drag(end.getX(), end.getY()), end);
        tool.onReleased(release(end.getX(), end.getY()), end);

        java.awt.geom.Rectangle2D bounds = line.getShape().getBounds2D();
        assertThat(Math.hypot(bounds.getWidth(), bounds.getHeight())).isCloseTo(30, within(1e-6));
    }

    @Test
    public void doubleClick_onAResizeHandleOverAPathShouldResizeInsteadOfEditingNodes() {
        Path path = new Path();
        path.moveTo(50, 50);
        path.lineTo(80, 50);
        path.lineTo(80, 80);
        controller.getModel().insertEntity(path);
        controller.getSelectionManager().setSelection(List.of(path));
        HandleSet.Handle handle = tool.handles().stream()
                .filter(h -> h.anchor() == Anchor.TOP_RIGHT)
                .findFirst().orElseThrow();
        // On the path's corner at (80, 80)
        Point2D corner = handle.center();

        tool.onPressed(pointer(MouseEvent.MOUSE_PRESSED, corner.getX(), corner.getY(), false, false, 2), corner);
        tool.onReleased(pointer(MouseEvent.MOUSE_RELEASED, corner.getX(), corner.getY(), false, false, 2), corner);

        assertThat(controller.getTool()).isEqualTo(Tool.SELECT);
    }

    @Test
    public void drag_onAResizeHandleOfARotatedItemShouldStretchAlongItsOwnAxesWithoutSkewing() {
        rectangle.rotate(30);
        controller.getSelectionManager().setSelection(List.of(rectangle));
        Point2D center = rectangle.getCenter();
        List<Point2D> cornersBefore = corners(rectangle);
        HandleSet.Handle handle = tool.handles().stream()
                .filter(h -> h.anchor() == Anchor.LEFT_CENTER)
                .findFirst().orElseThrow();
        Point2D start = handle.center();
        // Pull the handle 10 further out along the rectangle's own width axis.
        double length = start.distance(center);
        Point2D end = design(start.getX() + (start.getX() - center.getX()) / length * 10,
                start.getY() + (start.getY() - center.getY()) / length * 10);

        tool.onPressed(press(start.getX(), start.getY()), start);
        tool.onDragged(drag(end.getX(), end.getY()), end);
        tool.onReleased(release(end.getX(), end.getY()), end);

        List<Point2D> corners = corners(rectangle);
        assertThat(corners).hasSize(4);
        double sideA = corners.get(0).distance(corners.get(1));
        double sideB = corners.get(1).distance(corners.get(2));
        double diagonal = corners.get(0).distance(corners.get(2));
        assertThat(Math.max(sideA, sideB)).isCloseTo(30, within(1e-6));
        assertThat(Math.min(sideA, sideB)).isCloseTo(10, within(1e-6));
        // Still a rectangle: the sides meet at a right angle.
        assertThat(diagonal).isCloseTo(Math.hypot(sideA, sideB), within(1e-6));
        assertThat(rectangle.getRotation()).isCloseTo(30, within(1e-6));
        // The anchored left edge stays where it was, so two of the corners have not moved.
        long unmoved = corners.stream()
                .filter(corner -> cornersBefore.stream().anyMatch(before -> before.distance(corner) < 1e-6))
                .count();
        assertThat(unmoved).isEqualTo(2);

        controller.getUndoManager().undo();
        List<Point2D> undone = corners(rectangle);
        for (int i = 0; i < 4; i++) {
            assertThat(undone.get(i).distance(cornersBefore.get(i))).isLessThan(1e-6);
        }
    }

    /**
     * The distinct corners of the rectangle's outline in drawing order; the path may repeat points.
     */
    private static List<Point2D> corners(Rectangle rectangle) {
        List<Point2D> corners = new java.util.ArrayList<>();
        double[] coords = new double[6];
        for (java.awt.geom.PathIterator it = rectangle.getShape().getPathIterator(null); !it.isDone(); it.next()) {
            if (it.currentSegment(coords) != java.awt.geom.PathIterator.SEG_CLOSE) {
                Point2D point = new Point2D.Double(coords[0], coords[1]);
                if (corners.stream().noneMatch(corner -> corner.distance(point) < 1e-9)) {
                    corners.add(point);
                }
            }
        }
        return corners;
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

    @Test
    public void click_onADimensionShouldSelectItInPlaceOfTheSelectedEntities() {
        DistanceConstraint constraint = addDimensionAbove();
        controller.getSelectionManager().setSelection(List.of(rectangle));

        tool.onPressed(press(40, 30), design(40, 30));
        tool.onReleased(release(40, 30), design(40, 30));

        assertThat(controller.getModel().getConstraintManager().getSelectedConstraint()).contains(constraint);
        assertThat(controller.getSelectionManager().getSelection()).isEmpty();
    }

    @Test
    public void click_besideADimensionShouldDeselectIt() {
        addDimensionAbove();
        tool.onPressed(press(40, 30), design(40, 30));
        tool.onReleased(release(40, 30), design(40, 30));

        tool.onPressed(press(15, 15), design(15, 15));
        tool.onReleased(release(15, 15), design(15, 15));

        assertThat(controller.getModel().getConstraintManager().getSelectedConstraint()).isEmpty();
        assertThat(controller.getSelectionManager().getSelection()).containsExactly(rectangle);
    }

    @Test
    public void drag_onADimensionShouldMoveItAcrossItsAxis() {
        DistanceConstraint constraint = addDimensionAbove();

        tool.onPressed(press(40, 30), design(40, 30));
        tool.onDragged(drag(45, 38), design(45, 38));
        tool.onReleased(release(45, 38), design(45, 38));

        assertThat(constraint.getOffset()).isCloseTo(23, within(1e-6));
        assertThat(rectangle.getPosition().getX()).isCloseTo(10, within(1e-6));
    }

    @Test
    public void drag_onADimensionShouldBeUndoable() {
        DistanceConstraint constraint = addDimensionAbove();
        tool.onPressed(press(40, 30), design(40, 30));
        tool.onDragged(drag(40, 38), design(40, 38));
        tool.onReleased(release(40, 38), design(40, 38));

        controller.getUndoManager().undo();

        assertThat(constraint.getOffset()).isCloseTo(15, within(1e-6));
    }

    @Test
    public void doubleClick_onADimensionShouldEditItsDistance() {
        DistanceConstraint constraint = addDimensionAbove();
        SelectTool editingTool = new SelectTool(EditorTestSupport.toolContext(controller,
                EditorTestSupport.topDownCamera(), state, (_, onDistance) -> onDistance.accept(5)));

        editingTool.onPressed(pointer(MouseEvent.MOUSE_PRESSED, 40, 30, false, false, 2), design(40, 30));
        editingTool.onReleased(pointer(MouseEvent.MOUSE_RELEASED, 40, 30, false, false, 2), design(40, 30));

        assertThat(constraint.getDistance()).isCloseTo(5, within(1e-6));
    }

    @Test
    public void onDragged_shouldMoveTheReferenceAlongWhenTheTargetIsMoved() {
        DistanceConstraint constraint = addDimensionAbove();
        Entity target = constraint.getTarget();
        controller.getSelectionManager().setSelection(List.of(target));

        tool.onPressed(press(55, 15), design(55, 15));
        tool.onDragged(drag(60, 15), design(60, 15));
        tool.onReleased(release(60, 15), design(60, 15));

        assertThat(target.getPosition().getX()).isCloseTo(55, within(1e-6));
        assertThat(rectangle.getPosition().getX()).isCloseTo(15, within(1e-6));
    }

    @Test
    public void onDragged_shouldMoveBothConstrainedEntitiesWhenBothAreSelected() {
        DistanceConstraint constraint = addDimensionAbove();
        Entity target = constraint.getTarget();
        controller.getSelectionManager().setSelection(List.of(rectangle, target));

        tool.onPressed(press(55, 15), design(55, 15));
        tool.onDragged(drag(60, 15), design(60, 15));
        tool.onReleased(release(60, 15), design(60, 15));

        assertThat(target.getPosition().getX()).isCloseTo(55, within(1e-6));
        assertThat(rectangle.getPosition().getX()).isCloseTo(15, within(1e-6));
    }

    // A horizontal dimension from the rectangle's right edge to a second rectangle, drawn along y = 30
    private DistanceConstraint addDimensionAbove() {
        Rectangle other = new Rectangle(50, 10, 10, 10);
        controller.getModel().insertEntity(other);
        DistanceConstraint constraint = DistanceConstraint.measured(rectangle, at(rectangle, Anchor.BOTTOM_RIGHT), other,
                at(other, Anchor.BOTTOM_LEFT), Axis.HORIZONTAL, 15);
        controller.getModel().getConstraintManager().add(constraint);
        controller.getUndoManager().clear();
        return constraint;
    }

    private static ShapePoint at(Entity entity, Anchor anchor) {
        return ShapeSnapping.pointAt(entity, entity.getPosition(anchor));
    }
}

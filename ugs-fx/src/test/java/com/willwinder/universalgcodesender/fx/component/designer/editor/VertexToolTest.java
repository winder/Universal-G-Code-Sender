package com.willwinder.universalgcodesender.fx.component.designer.editor;

import com.willwinder.ugs.designer.entities.cuttable.Path;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.logic.Tool;
import com.willwinder.universalgcodesender.fx.component.designer.editor.EditorState.VertexHandle;
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

public class VertexToolTest {
    private Controller controller;
    private EditorState state;
    private VertexTool tool;
    private Path path;

    @Before
    public void setUp() {
        controller = EditorTestSupport.freshController();
        state = new EditorState();
        tool = new VertexTool(EditorTestSupport.toolContext(controller, EditorTestSupport.topDownCamera(), state));
        // A curve from (0, 0) to (60, 0) shaped by (10, 40) and (50, 40), then a line to (120, 0).
        path = new Path();
        path.moveTo(0, 0);
        path.curveTo(10, 40, 50, 40, 60, 0);
        path.lineTo(120, 0);
        controller.getModel().insertEntity(path);
        controller.getSelectionManager().setSelection(List.of(path));
        controller.getUndoManager().clear();
        tool.activate();
    }

    @Test
    public void activate_shouldShowOnlyTheNodesWithNoneSelected() {
        assertThat(nodes()).extracting(VertexHandle::position)
                .containsExactly(design(0, 0), design(60, 0), design(120, 0));
        assertThat(nodes()).noneMatch(VertexHandle::selected);
        assertThat(curveHandles()).isEmpty();
    }

    @Test
    public void click_onANodeShouldSelectItAndShowItsCurveHandles() {
        click(60, 0);

        assertThat(nodes()).filteredOn(VertexHandle::selected).extracting(VertexHandle::position)
                .containsExactly(design(60, 0));
        // Only the handle shaping the curve into this node; the line out of it has none.
        assertThat(curveHandles()).singleElement().satisfies(handle -> {
            assertThat(handle.position()).isEqualTo(design(50, 40));
            assertThat(handle.node()).isEqualTo(design(60, 0));
        });

        click(0, 0);

        assertThat(nodes()).filteredOn(VertexHandle::selected).extracting(VertexHandle::position)
                .containsExactly(design(0, 0));
        assertThat(curveHandles()).extracting(VertexHandle::position).containsExactly(design(10, 40));
    }

    @Test
    public void click_onANodeShouldNotRecordAnUndo() {
        click(60, 0);

        assertThat(controller.getUndoManager().canUndo()).isFalse();
    }

    @Test
    public void doubleClick_onANodeShouldSwitchBackToTheSelectToolWithThePathSelected() {
        controller.setTool(Tool.VERTEX);

        doubleClick(60, 0);

        assertThat(controller.getTool()).isEqualTo(Tool.SELECT);
        assertThat(controller.getSelectionManager().getSelection()).containsExactly(path);
        assertThat(controller.getUndoManager().canUndo()).isFalse();
    }

    @Test
    public void doubleClick_onThePathOutlineShouldAddASelectedNodeThereAndBeUndoable() {
        controller.setTool(Tool.VERTEX);

        doubleClick(90, 0);

        assertThat(controller.getTool()).isEqualTo(Tool.VERTEX);
        assertThat(nodes()).extracting(VertexHandle::position)
                .containsExactly(design(0, 0), design(60, 0), design(90, 0), design(120, 0));
        assertThat(selectedPositions()).containsExactly(design(90, 0));

        controller.getUndoManager().undo();
        tool.onSelectionChanged();
        assertThat(nodes()).extracting(VertexHandle::position)
                .containsExactly(design(0, 0), design(60, 0), design(120, 0));
    }

    @Test
    public void doubleClick_onACurveShouldAddANodeOnItKeepingItsShape() {
        // The top of the curve, halfway along it
        doubleClick(30, 30);

        assertThat(nodes()).extracting(VertexHandle::position).hasSize(4);
        Point2D added = selectedPositions().getFirst();
        assertThat(added.getX()).isCloseTo(30, within(1e-3));
        assertThat(added.getY()).isCloseTo(30, within(1e-3));
        // The curve still reaches the same height on either side
        assertThat(path.getShape().getBounds2D().getMaxY()).isCloseTo(30, within(1e-3));
    }

    @Test
    public void addNodes_shouldAddANodeBetweenTwoSelectedNeighbours() {
        click(60, 0);
        shiftClick(120, 0);
        assertThat(VertexEditing.canAddNodesProperty().get()).isTrue();

        assertThat(VertexEditing.addNodes()).isTrue();

        assertThat(nodes()).extracting(VertexHandle::position)
                .containsExactly(design(0, 0), design(60, 0), design(90, 0), design(120, 0));
        assertThat(selectedPositions()).containsExactlyInAnyOrder(design(60, 0), design(90, 0), design(120, 0));
    }

    @Test
    public void addNodes_shouldNotBePossibleWithoutTwoNeighboursSelected() {
        click(0, 0);
        shiftClick(120, 0);

        assertThat(VertexEditing.canAddNodesProperty().get()).isFalse();
    }

    @Test
    public void makeCurve_shouldGiveACornerNodeHandlesAndMakeTheLineACurve() {
        click(120, 0);
        assertThat(VertexEditing.canMakeCurveProperty().get()).isTrue();
        assertThat(VertexEditing.canMakeLineProperty().get()).isFalse();

        assertThat(VertexEditing.makeCurve()).isTrue();

        assertThat(selectedPositions()).containsExactly(design(120, 0));
        assertThat(curveHandles()).extracting(VertexHandle::position).contains(design(100, 0));
        assertThat(VertexEditing.canMakeCurveProperty().get()).isFalse();
        assertThat(VertexEditing.canMakeLineProperty().get()).isTrue();
        assertThat(controller.getUndoManager().canUndo()).isTrue();
    }

    @Test
    public void makeLine_shouldStraightenTheCurvesOfANode() {
        click(60, 0);
        assertThat(VertexEditing.canMakeLineProperty().get()).isTrue();

        assertThat(VertexEditing.makeLine()).isTrue();

        assertThat(selectedPositions()).containsExactly(design(60, 0));
        assertThat(curveHandles()).isEmpty();
        assertThat(path.getShape().getBounds2D().getMaxY()).isCloseTo(0, within(1e-9));
    }

    @Test
    public void delete_shouldRemoveOnlyTheSelectedNodeAndBeUndoable() {
        click(60, 0);

        assertThat(tool.onDelete()).isTrue();

        assertThat(controller.getModel().getEntities()).contains(path);
        assertThat(nodes()).extracting(VertexHandle::position)
                .containsExactly(design(0, 0), design(120, 0));
        assertThat(nodes()).noneMatch(VertexHandle::selected);

        controller.getUndoManager().undo();
        tool.onSelectionChanged();
        assertThat(nodes()).extracting(VertexHandle::position)
                .containsExactly(design(0, 0), design(60, 0), design(120, 0));
    }

    @Test
    public void delete_withoutASelectedNodeShouldBeLeftToTheDefault() {
        assertThat(tool.onDelete()).isFalse();
        assertThat(nodes()).hasSize(3);
    }

    @Test
    public void delete_shouldBeLeftToTheDefaultWhenNothingWouldBeLeftOfThePath() {
        Path line = new Path();
        line.moveTo(200, 0);
        line.lineTo(260, 0);
        controller.getModel().insertEntity(line);
        controller.getSelectionManager().setSelection(List.of(line));
        tool.onSelectionChanged();
        click(260, 0);

        assertThat(tool.onDelete()).isFalse();
        assertThat(nodes()).hasSize(2);
    }

    @Test
    public void breakAtSelectedNode_shouldSplitThePathAndKeepTheNodeSelected() {
        click(60, 0);
        assertThat(VertexEditing.nodeSelectedProperty().get()).isTrue();
        assertThat(VertexEditing.canBreakProperty().get()).isTrue();

        assertThat(VertexEditing.breakAtSelectedNode()).isTrue();

        assertThat(controller.getModel().getEntities()).contains(path);
        assertThat(nodes()).extracting(VertexHandle::position)
                .containsExactly(design(0, 0), design(60, 0), design(60, 0), design(120, 0));
        assertThat(nodes()).filteredOn(VertexHandle::selected).extracting(VertexHandle::position)
                .containsExactly(design(60, 0));

        controller.getUndoManager().undo();
        tool.onSelectionChanged();
        assertThat(nodes()).extracting(VertexHandle::position)
                .containsExactly(design(0, 0), design(60, 0), design(120, 0));
    }

    @Test
    public void nodes_shouldMarkTheEndsOfAnOpenPath() {
        assertThat(nodes()).extracting(VertexHandle::endpoint).containsExactly(true, false, true);
    }

    @Test
    public void breakAtSelectedNode_shouldLeaveTwoLooseEndsThatCanBeDraggedApart() {
        click(60, 0);
        VertexEditing.breakAtSelectedNode();

        assertThat(nodes()).extracting(VertexHandle::endpoint).containsExactly(true, true, true, true);

        // Drags the end of the first part away; the start of the second part stays behind.
        tool.onPressed(press(60, 0), design(60, 0));
        tool.onDragged(drag(60, 30), design(60, 30));
        tool.onReleased(release(60, 30), design(60, 30));

        assertThat(nodes()).extracting(VertexHandle::position)
                .containsExactly(design(0, 0), design(60, 30), design(60, 0), design(120, 0));
        // Nothing joins the two ends any more.
        java.awt.geom.PathIterator it = path.getShape().getPathIterator(null);
        int moves = 0;
        double[] coords = new double[6];
        for (; !it.isDone(); it.next()) {
            if (it.currentSegment(coords) == java.awt.geom.PathIterator.SEG_MOVETO) {
                moves++;
            }
        }
        assertThat(moves).isEqualTo(2);
    }

    @Test
    public void joinSelectedNodes_shouldJoinTheEndsLeftByABreak() {
        click(60, 0);
        VertexEditing.breakAtSelectedNode();
        // Both ends are on the same spot; Shift adds the one not yet selected.
        shiftClick(60, 0);
        assertThat(selectedPositions()).containsExactly(design(60, 0), design(60, 0));
        assertThat(VertexEditing.canJoinProperty().get()).isTrue();

        assertThat(VertexEditing.joinPath()).isTrue();

        assertThat(nodes()).extracting(VertexHandle::position)
                .containsExactly(design(0, 0), design(60, 0), design(120, 0));
        assertThat(nodes()).extracting(VertexHandle::endpoint).containsExactly(true, false, true);
        assertThat(selectedPositions()).containsExactly(design(60, 0));
        assertThat(VertexEditing.canJoinProperty().get()).isFalse();

        controller.getUndoManager().undo();
        tool.onSelectionChanged();
        assertThat(nodes()).hasSize(4);
    }

    @Test
    public void canJoin_shouldBeFalseUnlessTwoLooseEndsAreSelected() {
        click(0, 0);
        assertThat(VertexEditing.canJoinProperty().get()).isFalse();

        shiftClick(60, 0);
        assertThat(VertexEditing.canJoinProperty().get()).isFalse();
        assertThat(VertexEditing.joinPath()).isFalse();
    }

    @Test
    public void vertexEditing_shouldFollowTheSelectedNodeAndTheTool() {
        assertThat(VertexEditing.nodeSelectedProperty().get()).isFalse();

        click(0, 0);
        // The start of an open path is selected but there is nothing to break.
        assertThat(VertexEditing.nodeSelectedProperty().get()).isTrue();
        assertThat(VertexEditing.canBreakProperty().get()).isFalse();
        assertThat(VertexEditing.breakAtSelectedNode()).isFalse();

        tool.deactivate();
        assertThat(VertexEditing.nodeSelectedProperty().get()).isFalse();
    }

    @Test
    public void shiftClick_shouldAddAndRemoveNodesFromTheSelection() {
        click(0, 0);
        shiftClick(60, 0);

        assertThat(selectedPositions()).containsExactlyInAnyOrder(design(0, 0), design(60, 0));
        // The curve handles of every selected node are shown.
        assertThat(curveHandles()).extracting(VertexHandle::position)
                .containsExactlyInAnyOrder(design(10, 40), design(50, 40));
        assertThat(VertexEditing.nodeSelectedProperty().get()).isTrue();
        assertThat(VertexEditing.canBreakProperty().get()).isFalse();

        shiftClick(0, 0);

        assertThat(selectedPositions()).containsExactly(design(60, 0));
    }

    @Test
    public void shiftClick_onEmptySpaceShouldKeepTheSelectedNodes() {
        click(0, 0);
        shiftClick(60, 0);

        shiftClick(300, 300);

        assertThat(controller.getSelectionManager().getSelection()).containsExactly(path);
        assertThat(selectedPositions()).containsExactlyInAnyOrder(design(0, 0), design(60, 0));
    }

    @Test
    public void click_onOneOfSeveralSelectedNodesShouldNarrowTheSelectionToIt() {
        click(0, 0);
        shiftClick(60, 0);

        click(60, 0);

        assertThat(selectedPositions()).containsExactly(design(60, 0));
    }

    @Test
    public void drag_onOneOfSeveralSelectedNodesShouldMoveThemAll() {
        click(0, 0);
        shiftClick(120, 0);

        tool.onPressed(press(120, 0), design(120, 0));
        tool.onDragged(drag(130, 10), design(130, 10));
        tool.onReleased(release(130, 10), design(130, 10));

        assertThat(nodes()).extracting(VertexHandle::position)
                .containsExactly(design(10, 10), design(60, 0), design(130, 10));
        assertThat(selectedPositions()).containsExactlyInAnyOrder(design(10, 10), design(130, 10));
        // The handle out of the first node moved with it.
        assertThat(curveHandles()).extracting(VertexHandle::position).containsExactly(design(20, 50));

        controller.getUndoManager().undo();
        tool.onSelectionChanged();
        assertThat(nodes()).extracting(VertexHandle::position)
                .containsExactly(design(0, 0), design(60, 0), design(120, 0));
    }

    @Test
    public void delete_shouldRemoveAllSelectedNodes() {
        Path line = new Path();
        line.moveTo(200, 0);
        line.lineTo(220, 0);
        line.lineTo(240, 0);
        line.lineTo(260, 0);
        controller.getModel().insertEntity(line);
        controller.getSelectionManager().setSelection(List.of(line));
        tool.onSelectionChanged();
        click(220, 0);
        shiftClick(240, 0);

        assertThat(tool.onDelete()).isTrue();

        assertThat(nodes()).extracting(VertexHandle::position).containsExactly(design(200, 0), design(260, 0));
        assertThat(state.vertices()).noneMatch(VertexHandle::selected);

        controller.getUndoManager().undo();
        tool.onSelectionChanged();
        assertThat(nodes()).hasSize(4);
    }

    @Test
    public void marquee_shouldSelectTheNodesInsideTheRubberBand() {
        click(0, 0);

        tool.onPressed(press(50, -10), design(50, -10));
        tool.onDragged(drag(130, 10), design(130, 10));
        assertThat(state.rubberBand()).isNotNull();
        tool.onReleased(release(130, 10), design(130, 10));

        assertThat(state.rubberBand()).isNull();
        assertThat(selectedPositions()).containsExactly(design(60, 0), design(120, 0));
        assertThat(controller.getSelectionManager().getSelection()).containsExactly(path);
        assertThat(controller.getUndoManager().canUndo()).isFalse();
    }

    @Test
    public void marquee_withShiftShouldAddToTheSelectedNodes() {
        click(0, 0);

        tool.onPressed(pointer(MouseEvent.MOUSE_PRESSED, 110, -10, true, false, 1), design(110, -10));
        tool.onDragged(pointer(MouseEvent.MOUSE_DRAGGED, 130, 10, true, false, 1), design(130, 10));
        tool.onReleased(pointer(MouseEvent.MOUSE_RELEASED, 130, 10, true, false, 1), design(130, 10));

        assertThat(selectedPositions()).containsExactly(design(0, 0), design(120, 0));
    }

    @Test
    public void marquee_aroundTheStartOfAClosedShapeShouldSelectItAsOneNode() {
        Path closed = new Path();
        closed.moveTo(200, 0);
        closed.lineTo(240, 0);
        closed.lineTo(240, 40);
        closed.lineTo(200, 0);
        closed.close();
        controller.getModel().insertEntity(closed);
        controller.getSelectionManager().setSelection(List.of(closed));
        tool.onSelectionChanged();

        tool.onPressed(press(190, -10), design(190, -10));
        tool.onDragged(drag(210, 10), design(210, 10));
        tool.onReleased(release(210, 10), design(210, 10));

        assertThat(selectedPositions()).containsExactly(design(200, 0));
        assertThat(VertexEditing.canBreakProperty().get()).isTrue();
    }

    @Test
    public void undo_ofAnAddedNodeShouldKeepTheSameNodesSelected() {
        // A node on the curve, before the one at (60, 0)
        doubleClick(30, 30);
        click(60, 0);

        controller.getUndoManager().undo();
        tool.onSelectionChanged();

        assertThat(nodes()).hasSize(3);
        assertThat(selectedPositions()).containsExactly(design(60, 0));
    }

    @Test
    public void click_onEmptySpaceShouldDeselectTheNode() {
        click(60, 0);

        click(300, 300);

        assertThat(state.vertices()).noneMatch(VertexHandle::selected);
        assertThat(curveHandles()).isEmpty();
    }

    @Test
    public void drag_onANodeShouldMoveItsCurveHandlesAlongAndKeepItSelected() {
        tool.onPressed(press(60, 0), design(60, 0));
        tool.onDragged(drag(70, 10), design(70, 10));
        tool.onReleased(release(70, 10), design(70, 10));

        assertThat(nodes()).filteredOn(VertexHandle::selected).extracting(VertexHandle::position)
                .containsExactly(design(70, 10));
        assertThat(curveHandles()).extracting(VertexHandle::position).containsExactly(design(60, 50));
        assertThat(controller.getUndoManager().canUndo()).isTrue();

        controller.getUndoManager().undo();
        tool.onSelectionChanged();
        assertThat(nodes()).extracting(VertexHandle::position)
                .containsExactly(design(0, 0), design(60, 0), design(120, 0));
    }

    @Test
    public void drag_onACurveHandleShouldReshapeTheCurveWithoutMovingTheNode() {
        click(60, 0);

        tool.onPressed(press(50, 40), design(50, 40));
        tool.onDragged(drag(50, 50), design(50, 50));
        tool.onReleased(release(50, 50), design(50, 50));

        assertThat(nodes()).extracting(VertexHandle::position)
                .containsExactly(design(0, 0), design(60, 0), design(120, 0));
        assertThat(curveHandles()).extracting(VertexHandle::position).containsExactly(design(50, 50));
        assertThat(path.getShape().getBounds2D().getMaxY()).isGreaterThan(30);
        assertThat(nodes()).filteredOn(VertexHandle::selected).hasSize(1);
    }

    @Test
    public void closedShape_shouldTreatTheStartAndEndNodeAsOne() {
        Path closed = new Path();
        closed.moveTo(200, 0);
        closed.curveTo(200, 40, 260, 40, 260, 0);
        closed.curveTo(260, -40, 200, -40, 200, 0);
        closed.close();
        controller.getModel().insertEntity(closed);
        controller.getSelectionManager().setSelection(List.of(closed));
        tool.onSelectionChanged();

        click(200, 0);

        // The handles of both curves meeting at the start are shown.
        assertThat(curveHandles()).extracting(VertexHandle::position)
                .containsExactlyInAnyOrder(design(200, 40), design(200, -40));

        tool.onPressed(press(200, 0), design(200, 0));
        tool.onDragged(drag(190, 0), design(190, 0));
        tool.onReleased(release(190, 0), design(190, 0));

        // Both ends moved together, so the shape stays closed, and none of its nodes is a loose end.
        assertThat(nodes()).noneMatch(VertexHandle::endpoint);
        assertThat(nodes()).extracting(VertexHandle::position)
                .containsExactly(design(190, 0), design(260, 0), design(190, 0));
        assertThat(curveHandles()).extracting(VertexHandle::position)
                .containsExactlyInAnyOrder(design(190, 40), design(190, -40));
    }

    private void shiftClick(double x, double y) {
        Point2D point = design(x, y);
        tool.onPressed(pointer(MouseEvent.MOUSE_PRESSED, x, y, true, false, 1), point);
        tool.onReleased(pointer(MouseEvent.MOUSE_RELEASED, x, y, true, false, 1), point);
    }

    private List<Point2D> selectedPositions() {
        return nodes().stream().filter(VertexHandle::selected).map(VertexHandle::position).toList();
    }

    private void doubleClick(double x, double y) {
        click(x, y);
        Point2D point = design(x, y);
        tool.onPressed(pointer(MouseEvent.MOUSE_PRESSED, x, y, false, false, 2), point);
        tool.onReleased(pointer(MouseEvent.MOUSE_RELEASED, x, y, false, false, 2), point);
    }

    private void click(double x, double y) {
        Point2D point = design(x, y);
        tool.onPressed(press(x, y), point);
        tool.onReleased(release(x, y), point);
    }

    private List<VertexHandle> nodes() {
        return state.vertices().stream().filter(handle -> !handle.isCurveHandle()).toList();
    }

    private List<VertexHandle> curveHandles() {
        return state.vertices().stream().filter(VertexHandle::isCurveHandle).toList();
    }
}

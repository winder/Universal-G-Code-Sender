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

import com.willwinder.ugs.designer.constraint.AlignmentConstraint;
import com.willwinder.ugs.designer.constraint.Axis;
import com.willwinder.ugs.designer.constraint.CoincidentConstraint;
import com.willwinder.ugs.designer.constraint.Constraint;
import com.willwinder.ugs.designer.constraint.ConstraintManager;
import com.willwinder.ugs.designer.constraint.ConstraintPlacement;
import com.willwinder.ugs.designer.constraint.DimensionLine;
import com.willwinder.ugs.designer.constraint.DistanceConstraint;
import com.willwinder.ugs.designer.constraint.EqualSizeConstraint;
import com.willwinder.ugs.designer.constraint.ShapePoint;
import com.willwinder.ugs.designer.constraint.ShapeSnapping;
import com.willwinder.ugs.designer.entities.Anchor;
import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.logic.Tool;
import com.willwinder.ugs.designer.model.Size;
import javafx.scene.input.MouseEvent;
import org.junit.Before;
import org.junit.Test;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleConsumer;

import static com.willwinder.universalgcodesender.fx.component.designer.editor.EditorTestSupport.design;
import static com.willwinder.universalgcodesender.fx.component.designer.editor.EditorTestSupport.drag;
import static com.willwinder.universalgcodesender.fx.component.designer.editor.EditorTestSupport.pointer;
import static com.willwinder.universalgcodesender.fx.component.designer.editor.EditorTestSupport.press;
import static com.willwinder.universalgcodesender.fx.component.designer.editor.EditorTestSupport.release;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class ConstraintToolTest {
    private Controller controller;
    private EditorState state;
    private ToolContext context;
    private ConstraintTool tool;
    private Rectangle first;
    private Rectangle second;
    private final List<Constraint> editedConstraints = new ArrayList<>();
    private Double enteredDistance;

    @Before
    public void setUp() {
        controller = EditorTestSupport.freshController();
        controller.setTool(Tool.CONSTRAINT);
        state = new EditorState();
        context = EditorTestSupport.toolContext(controller, EditorTestSupport.topDownCamera(), state, this::editDistance);
        tool = new ConstraintTool(context, Constraint.Kind.DISTANCE);
        first = new Rectangle(0, 0, 10, 10);
        second = new Rectangle(30, 0, 10, 10);
        controller.getModel().insertEntities(List.of(first, second));
    }

    @Test
    public void onPressed_shouldAddHorizontalConstraintWhenPlacedAboveTheAnchors() {
        click(10, 10);
        click(30, 10);

        click(20, 20);

        List<Constraint> constraints = constraintManager().getConstraints();
        assertThat(constraints).hasSize(1);
        DistanceConstraint constraint = (DistanceConstraint) constraints.getFirst();
        assertThat(constraint.getReference()).isSameAs(first);
        assertThat(constraint.getReferencePoint()).isEqualTo(new Point2D.Double(10, 10));
        assertThat(constraint.getTarget()).isSameAs(second);
        assertThat(constraint.getTargetPoint()).isEqualTo(new Point2D.Double(30, 10));
        assertThat(constraint.getAxis()).isEqualTo(Axis.HORIZONTAL);
        assertThat(constraint.getDistance()).isCloseTo(20, within(1e-6));
        assertThat(constraint.getOffset()).isCloseTo(10, within(1e-6));
        assertThat(constraintManager().getSelectedConstraint()).contains(constraint);
        assertThat(controller.getUndoManager().canUndo()).isTrue();
    }

    @Test
    public void onPressed_shouldAddVerticalConstraintWhenPlacedBesideTheAnchors() {
        second.setPosition(new Point2D.Double(0, 30));
        click(0, 10);
        click(0, 30);

        click(-20, 20);

        DistanceConstraint constraint = (DistanceConstraint) constraintManager().getConstraints().getFirst();
        assertThat(constraint.getAxis()).isEqualTo(Axis.VERTICAL);
        assertThat(constraint.getDistance()).isCloseTo(20, within(1e-6));
    }

    @Test
    public void onReleased_shouldAskForTheDistanceOfAPlacedConstraint() {
        enteredDistance = 5d;
        click(10, 10);
        click(30, 10);

        click(20, 20);

        assertThat(editedConstraints).hasSize(1);
        assertThat(second.getPosition().getX()).isCloseTo(15, within(1e-6));
    }

    @Test
    public void onReleased_shouldMakeChangedDistanceUndoable() {
        enteredDistance = 5d;
        click(10, 10);
        click(30, 10);
        click(20, 20);

        controller.getUndoManager().undo();

        assertThat(constraintManager().getConstraints()).hasSize(1);
        assertThat(second.getPosition().getX()).isCloseTo(30, within(1e-6));
    }

    @Test
    public void onPressed_shouldConstrainTheSizeWhenBothPointsAreOnTheSameEntity() {
        click(0, 10);
        click(10, 10);

        click(5, 20);

        DistanceConstraint constraint = (DistanceConstraint) constraintManager().getConstraints().getFirst();
        assertThat(constraint.isSize()).isTrue();
        assertThat(constraint.getAxis()).isEqualTo(Axis.HORIZONTAL);
        assertThat(constraint.getDistance()).isCloseTo(10, within(1e-6));
    }

    @Test
    public void onReleased_shouldResizeTheEntityToTheEnteredSize() {
        enteredDistance = 25d;
        click(0, 10);
        click(10, 10);

        click(5, 20);

        assertThat(first.getSize().getWidth()).isCloseTo(25, within(1e-6));
        assertThat(first.getPosition().getX()).isCloseTo(0, within(1e-6));
    }

    @Test
    public void onReleased_shouldIgnoreAZeroSize() {
        enteredDistance = 0d;
        click(0, 10);
        click(10, 10);

        click(5, 20);

        assertThat(first.getSize().getWidth()).isCloseTo(10, within(1e-6));
    }

    @Test
    public void onPressed_shouldNotPickTheReferencePointAsTarget() {
        click(10, 10);
        click(10, 10);

        click(20, 20);

        assertThat(constraintManager().getConstraints()).isEmpty();
    }

    @Test
    public void onMoved_shouldPreviewTheDimensionAlongTheAxisItWouldConstrain() {
        click(10, 10);
        click(30, 10);

        tool.onMoved(pointer(MouseEvent.MOUSE_MOVED, 20, 20, false, false, 0), design(20, 20));

        assertThat(state.previewDimension()).isInstanceOfSatisfying(DimensionLine.Linear.class, line -> {
            assertThat(line.frame().isAcrossVertical()).isTrue();
            assertThat(line.lineStart().getY()).isCloseTo(20, within(1e-6));
        });
    }

    @Test
    public void onPressed_shouldClearThePreviewDimensionWhenTheConstraintIsPlaced() {
        click(10, 10);
        click(30, 10);

        click(20, 20);

        assertThat(state.previewDimension()).isNull();
    }

    @Test
    public void onPressed_shouldLineUpTheTargetWhenItIsPickedWithTheAlignmentTool() {
        useAlignmentTool();
        second.setPosition(new Point2D.Double(3, 30));
        click(0, 10);

        click(3, 40);

        AlignmentConstraint constraint = (AlignmentConstraint) constraintManager().getConstraints().getFirst();
        assertThat(constraint.getAxis()).isEqualTo(Axis.HORIZONTAL);
        assertThat(second.getPosition()).isEqualTo(new Point2D.Double(0, 30));
        assertThat(editedConstraints).isEmpty();
    }

    @Test
    public void onMoved_shouldPreviewTheAlignmentToTheHoveredPoint() {
        useAlignmentTool();
        second.setPosition(new Point2D.Double(30, 3));
        click(10, 10);

        tool.onMoved(pointer(MouseEvent.MOUSE_MOVED, 30, 13, false, false, 0), design(30, 13));

        assertThat(state.readout()).isEqualTo("Align Y");
        assertThat(state.previewDimension()).isInstanceOfSatisfying(DimensionLine.Alignment.class,
                alignment -> assertThat(alignment.to()).isEqualTo(new Point2D.Double(30, 10)));
    }

    @Test
    public void onPressed_shouldNotAlignTwoPointsOfTheSameEntity() {
        useAlignmentTool();
        click(0, 5);
        click(10, 5);

        click(10, 20);

        assertThat(constraintManager().getConstraints()).isEmpty();
    }

    @Test
    public void onPressed_shouldGlueTheTargetWhenItIsPickedWithTheCoincidentTool() {
        useTool(Tool.COINCIDENT, Constraint.Kind.COINCIDENT);
        click(5, 5);

        click(35, 5);

        Constraint constraint = constraintManager().getConstraints().getFirst();
        assertThat(constraint).isInstanceOf(CoincidentConstraint.class);
        assertThat(second.getCenter()).isEqualTo(new Point2D.Double(5, 5));
        assertThat(editedConstraints).isEmpty();
    }

    @Test
    public void onMoved_shouldPreviewTheGlueToTheHoveredPoint() {
        useTool(Tool.COINCIDENT, Constraint.Kind.COINCIDENT);
        click(5, 5);

        tool.onMoved(pointer(MouseEvent.MOUSE_MOVED, 35, 5, false, false, 0), design(35, 5));

        assertThat(state.readout()).isEqualTo("Glue");
        assertThat(state.previewDimension()).isInstanceOf(DimensionLine.Coincidence.class);
    }

    @Test
    public void onPressed_shouldGiveTheTargetTheSizeOfTheReferenceWithTheEqualSizeTool() {
        useTool(Tool.EQUAL_SIZE, Constraint.Kind.EQUAL_SIZE);
        second.setSize(new Size(20, 4));
        click(5, 10);

        click(35, 4);

        Constraint constraint = constraintManager().getConstraints().getFirst();
        assertThat(constraint).isInstanceOf(EqualSizeConstraint.class);
        assertThat(second.getSize().getWidth()).isCloseTo(10, within(1e-6));
        assertThat(editedConstraints).isEmpty();
    }

    @Test
    public void onMoved_shouldPreviewTheSizeTheHoveredPointPicks() {
        useTool(Tool.EQUAL_SIZE, Constraint.Kind.EQUAL_SIZE);

        tool.onMoved(pointer(MouseEvent.MOUSE_MOVED, 10, 5, false, false, 0), design(10, 5));

        assertThat(state.readout()).isEqualTo("Height");
        assertThat(state.previewDimension().label()).isEqualTo("10");
    }

    @Test
    public void onMoved_shouldPreviewTheEqualSizeWithTheHoveredPoint() {
        useTool(Tool.EQUAL_SIZE, Constraint.Kind.EQUAL_SIZE);
        click(10, 5);

        tool.onMoved(pointer(MouseEvent.MOUSE_MOVED, 35, 10, false, false, 0), design(35, 10));

        assertThat(state.readout()).isEqualTo("Height = Width");
        assertThat(state.previewDimension()).isInstanceOf(DimensionLine.EqualSize.class);
    }

    @Test
    public void onMoved_shouldExplainWhyASizeWithoutDistanceCanNotBePlaced() {
        click(0, 0);
        click(0, 10);

        tool.onMoved(pointer(MouseEvent.MOUSE_MOVED, 5, 30, false, false, 0), design(5, 30));

        assertThat(state.readout()).isEqualTo("The points have no width between them");
    }

    @Test
    public void onPressed_shouldConstrainATargetThatIsAlreadyDriven() {
        Rectangle third = new Rectangle(60, 0, 10, 10);
        controller.getModel().insertEntity(third);
        DistanceConstraint existing = DistanceConstraint.measured(first, at(first, Anchor.CENTER), second, at(second, Anchor.CENTER), Axis.HORIZONTAL, 0);
        constraintManager().add(existing);
        click(60, 10);
        click(30, 10);

        click(45, 20);

        assertThat(constraintManager().getConstraints()).hasSize(2);
        assertThat(existing.getReference()).isSameAs(second);
    }

    @Test
    public void onPressed_shouldRejectConstraintBetweenShapesThatAreAlreadyLinked() {
        constraintManager().add(DistanceConstraint.measured(first, at(first, Anchor.CENTER), second, at(second, Anchor.CENTER), Axis.HORIZONTAL, 0));
        click(30, 10);
        click(10, 10);

        click(20, 20);

        assertThat(constraintManager().getConstraints()).hasSize(1);
        assertThat(state.readout()).isEqualTo("The shapes are already linked horizontally");
    }

    @Test
    public void onPressed_shouldEditTheDistanceOfADoubleClickedDimension() {
        DistanceConstraint constraint = DistanceConstraint.measured(first, at(first, Anchor.BOTTOM_RIGHT), second, at(second, Anchor.BOTTOM_LEFT), Axis.HORIZONTAL, 15);
        constraintManager().add(constraint);
        enteredDistance = 12d;

        tool.onPressed(pointer(MouseEvent.MOUSE_PRESSED, 20, 20, false, false, 2), design(20, 20));
        tool.onReleased(release(20, 20), design(20, 20));

        assertThat(editedConstraints).containsExactly(constraint);
        assertThat(constraint.getDistance()).isCloseTo(12, within(1e-6));
    }

    @Test
    public void onPressed_shouldSelectAnExistingDimension() {
        DistanceConstraint constraint = DistanceConstraint.measured(first, at(first, Anchor.BOTTOM_RIGHT), second, at(second, Anchor.BOTTOM_LEFT), Axis.HORIZONTAL, 15);
        constraintManager().add(constraint);

        click(20, 20);

        assertThat(constraintManager().getSelectedConstraint()).contains(constraint);
        assertThat(editedConstraints).isEmpty();
    }

    @Test
    public void onDragged_shouldMoveAnExistingDimension() {
        DistanceConstraint constraint = DistanceConstraint.measured(first, at(first, Anchor.BOTTOM_RIGHT), second, at(second, Anchor.BOTTOM_LEFT), Axis.HORIZONTAL, 15);
        constraintManager().add(constraint);

        tool.onPressed(press(20, 20), design(20, 20));
        tool.onDragged(drag(20, 30), design(20, 30));
        tool.onReleased(release(20, 30), design(20, 30));

        assertThat(constraint.getOffset()).isCloseTo(25, within(1e-6));
        assertThat(constraintManager().getConstraints()).hasSize(1);
    }

    @Test
    public void onMoved_shouldShowTheVerticesAndCenterButNotPickAPointAlongAnEdge() {
        tool.onMoved(EditorTestSupport.pointer(MouseEvent.MOUSE_MOVED, 10, 5, false, false, 0), design(10, 5));

        assertThat(state.snapMarkers()).filteredOn(marker -> !marker.highlighted())
                .extracting(ConstraintPlacement.Marker::position)
                .containsExactlyInAnyOrder(new Point2D.Double(0, 0), new Point2D.Double(10, 0),
                        new Point2D.Double(10, 10), new Point2D.Double(0, 10), new Point2D.Double(5, 5));
        assertThat(state.snapMarkers()).noneMatch(ConstraintPlacement.Marker::highlighted);
    }

    @Test
    public void onPressed_shouldMeasureFromAVertexOfARotatedShape() {
        first.rotate(45);
        double corner = 5 + 5 * Math.sqrt(2);
        click(corner, 5);
        click(30, 10);

        click(20, 20);

        DistanceConstraint constraint = (DistanceConstraint) constraintManager().getConstraints().getFirst();
        assertThat(constraint.getReferencePoint().getX()).isCloseTo(corner, within(1e-6));
        assertThat(constraint.getDistance()).isCloseTo(30 - corner, within(1e-6));
    }

    @Test
    public void onPressed_shouldMeasureTheSizeOfARotatedShapeAlongItsOwnAxis() {
        first.rotate(30);
        Point2D left = first.getTransform().transform(new Point2D.Double(0, 1), null);
        Point2D right = first.getTransform().transform(new Point2D.Double(1, 1), null);
        Point2D above = first.getTransform().transform(new Point2D.Double(0.5, 2.5), null);
        click(left.getX(), left.getY());
        click(right.getX(), right.getY());

        click(above.getX(), above.getY());

        DistanceConstraint constraint = (DistanceConstraint) constraintManager().getConstraints().getFirst();
        assertThat(constraint.getAxis()).isEqualTo(Axis.HORIZONTAL);
        assertThat(constraint.getDistance()).isCloseTo(10, within(1e-6));
        assertThat(constraint.getOffset()).isCloseTo(15, within(1e-6));
    }

    private void useAlignmentTool() {
        useTool(Tool.ALIGNMENT, Constraint.Kind.ALIGNMENT);
    }

    private void useTool(Tool selected, Constraint.Kind kind) {
        controller.setTool(selected);
        tool = new ConstraintTool(context, kind);
    }

    private void click(double x, double y) {
        tool.onPressed(press(x, y), design(x, y));
        tool.onReleased(release(x, y), design(x, y));
    }

    private void editDistance(DistanceConstraint constraint, DoubleConsumer onDistance) {
        editedConstraints.add(constraint);
        if (enteredDistance != null) {
            onDistance.accept(enteredDistance);
        }
    }

    private ConstraintManager constraintManager() {
        return controller.getModel().getConstraintManager();
    }
    private static ShapePoint at(Entity entity, Anchor anchor) {
        return ShapeSnapping.pointAt(entity, entity.getPosition(anchor));
    }
}

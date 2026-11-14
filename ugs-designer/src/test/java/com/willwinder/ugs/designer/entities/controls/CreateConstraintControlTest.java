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
package com.willwinder.ugs.designer.entities.controls;

import com.willwinder.ugs.designer.actions.SimpleUndoManager;
import com.willwinder.ugs.designer.constraint.AlignmentConstraint;
import com.willwinder.ugs.designer.constraint.Axis;
import com.willwinder.ugs.designer.constraint.CoincidentConstraint;
import com.willwinder.ugs.designer.constraint.Constraint;
import com.willwinder.ugs.designer.constraint.DistanceConstraint;
import com.willwinder.ugs.designer.constraint.EqualSizeConstraint;
import com.willwinder.ugs.designer.entities.EventType;
import com.willwinder.ugs.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.designer.gui.MouseEntityEvent;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.logic.Tool;
import com.willwinder.ugs.designer.model.Size;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class CreateConstraintControlTest {
    private Controller controller;
    private CreateConstraintControl control;
    private Rectangle first;
    private Rectangle second;

    @Before
    public void setUp() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        controller = new Controller(new SelectionManager(), new SimpleUndoManager());
        controller.getDrawing().setScale(2);
        control = new CreateConstraintControl(controller);
        first = new Rectangle(0, 0, 10, 10);
        second = new Rectangle(30, 0, 10, 10);
        controller.getModel().insertEntities(List.of(first, second));
        controller.setTool(Tool.CONSTRAINT);
    }

    @Test
    public void onEvent_shouldAddAHorizontalConstraintWhenPlacedAboveThePoints() {
        press(10, 10);
        press(30, 10);

        press(20, 20);

        List<Constraint> constraints = controller.getModel().getConstraintManager().getConstraints();
        assertThat(constraints).hasSize(1);
        DistanceConstraint constraint = (DistanceConstraint) constraints.getFirst();
        assertThat(constraint.getReference()).isSameAs(first);
        assertThat(constraint.getTarget()).isSameAs(second);
        assertThat(constraint.getAxis()).isEqualTo(Axis.HORIZONTAL);
        assertThat(constraint.getDistance()).isCloseTo(20, within(1e-6));
        assertThat(controller.getModel().getConstraintManager().getSelectedConstraint()).contains(constraint);
        assertThat(controller.getUndoManager().canUndo()).isTrue();
    }

    @Test
    public void onEvent_shouldConstrainTheSizeWhenBothPointsAreOnTheSameEntity() {
        press(0, 10);
        press(10, 10);

        press(5, 20);

        DistanceConstraint constraint = (DistanceConstraint) controller.getModel().getConstraintManager().getConstraints().getFirst();
        assertThat(constraint.isSize()).isTrue();
        assertThat(constraint.getDistance()).isCloseTo(10, within(1e-6));
    }

    @Test
    public void onEvent_shouldStartOverWhenNothingIsPickedForTheTarget() {
        press(10, 10);
        press(20, 30);

        press(20, 20);

        assertThat(controller.getModel().getConstraintManager().getConstraints()).isEmpty();
    }

    @Test
    public void onEvent_shouldLineUpThePointsWithTheAlignmentTool() {
        controller.setTool(Tool.ALIGNMENT);
        second.setPosition(new Point2D.Double(3, 30));
        press(0, 10);

        press(3, 40);

        Constraint constraint = controller.getModel().getConstraintManager().getConstraints().getFirst();
        assertThat(constraint).isInstanceOf(AlignmentConstraint.class);
        assertThat(second.getPosition()).isEqualTo(new Point2D.Double(0, 30));
    }

    @Test
    public void onEvent_shouldGlueThePointsWithTheCoincidentTool() {
        controller.setTool(Tool.COINCIDENT);
        press(5, 5);

        press(35, 5);

        Constraint constraint = controller.getModel().getConstraintManager().getConstraints().getFirst();
        assertThat(constraint).isInstanceOf(CoincidentConstraint.class);
        assertThat(second.getCenter()).isEqualTo(new Point2D.Double(5, 5));
    }

    @Test
    public void onEvent_shouldNotPickAPointAlongAnEdgeWithTheDistanceTool() {
        press(10, 5);
        press(30, 10);

        press(20, 20);

        assertThat(controller.getModel().getConstraintManager().getConstraints()).isEmpty();
        assertThat(control.isIdleAt(new Point2D.Double(10, 5))).isTrue();
    }

    @Test
    public void isIdleAt_shouldBeFalseOverAPointThatCanBePicked() {
        boolean result = control.isIdleAt(new Point2D.Double(10, 10));

        assertThat(result).isFalse();
    }

    @Test
    public void isIdleAt_shouldBeFalseWhilePickingTheTarget() {
        press(10, 10);

        boolean result = control.isIdleAt(new Point2D.Double(20, 30));

        assertThat(result).isFalse();
    }

    private void press(double x, double y) {
        Point2D point = new Point2D.Double(x, y);
        control.onEvent(new MouseEntityEvent(control, EventType.MOUSE_PRESSED, point, point, false, false, false, 1));
    }

    @Test
    public void onEvent_shouldGiveTheWidthOfAShapeTheHeightOfAnotherWithTheEqualSizeTool() {
        controller.setTool(Tool.EQUAL_SIZE);
        second.setSize(new Size(20, 4));
        press(10, 5);

        press(35, 4);

        EqualSizeConstraint constraint = (EqualSizeConstraint) controller.getModel().getConstraintManager().getConstraints().getFirst();
        assertThat(constraint.getReferenceAxis()).isEqualTo(Axis.VERTICAL);
        assertThat(constraint.getTargetAxis()).isEqualTo(Axis.HORIZONTAL);
        assertThat(second.getSize().getWidth()).isCloseTo(10, within(1e-6));
        assertThat(second.getSize().getHeight()).isCloseTo(4, within(1e-6));
    }

}

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
import com.willwinder.ugs.designer.constraint.Axis;
import com.willwinder.ugs.designer.constraint.ConstraintManager;
import com.willwinder.ugs.designer.constraint.DistanceConstraint;
import com.willwinder.ugs.designer.constraint.ShapeSnapping;
import com.willwinder.ugs.designer.entities.EventType;
import com.willwinder.ugs.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.designer.gui.MouseEntityEvent;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.logic.Tool;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class DimensionControlTest {
    private Controller controller;
    private DimensionControl control;
    private Rectangle first;
    private DistanceConstraint constraint;

    @Before
    public void setUp() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        controller = new Controller(new SelectionManager(), new SimpleUndoManager());
        controller.getDrawing().setScale(2);
        control = new DimensionControl(controller, new CreateConstraintControl(controller));
        first = new Rectangle(0, 0, 10, 10);
        Rectangle second = new Rectangle(30, 0, 10, 10);
        controller.getModel().insertEntities(List.of(first, second));
        constraint = DistanceConstraint.measured(first, ShapeSnapping.pointAt(first, new Point2D.Double(10, 0)),
                second, ShapeSnapping.pointAt(second, new Point2D.Double(30, 0)), Axis.HORIZONTAL, 20);
        constraintManager().add(constraint);
    }

    @Test
    public void isWithin_shouldBeTrueOnTheDimension() {
        boolean result = control.isWithin(new Point2D.Double(20, 20));

        assertThat(result).isTrue();
    }

    @Test
    public void isWithin_shouldBeFalseWithTheVertexTool() {
        controller.setTool(Tool.VERTEX);

        boolean result = control.isWithin(new Point2D.Double(20, 20));

        assertThat(result).isFalse();
    }

    @Test
    public void onEvent_shouldSelectThePressedDimensionInsteadOfTheEntities() {
        controller.getSelectionManager().setSelection(List.of(first));

        send(EventType.MOUSE_PRESSED, 20, 20, 20, 20);

        assertThat(constraintManager().getSelectedConstraint()).contains(constraint);
        assertThat(controller.getSelectionManager().getSelection()).isEmpty();
    }

    @Test
    public void onEvent_shouldMoveTheDraggedDimensionAsOneUndoableChange() {
        send(EventType.MOUSE_PRESSED, 20, 20, 20, 20);
        send(EventType.MOUSE_DRAGGED, 20, 20, 20, 26);
        send(EventType.MOUSE_RELEASED, 20, 20, 20, 26);

        controller.getUndoManager().undo();

        assertThat(constraint.getOffset()).isCloseTo(20, within(1e-6));
        controller.getUndoManager().redo();
        assertThat(constraint.getOffset()).isCloseTo(26, within(1e-6));
    }

    @Test
    public void onSelectionEvent_shouldDeselectTheDimensionWhenEntitiesAreSelected() {
        constraintManager().setSelectedConstraint(constraint);

        controller.getSelectionManager().setSelection(List.of(first));

        assertThat(constraintManager().getSelectedConstraint()).isEmpty();
    }

    private void send(EventType type, double startX, double startY, double x, double y) {
        control.onEvent(new MouseEntityEvent(control, type, new Point2D.Double(startX, startY), new Point2D.Double(x, y), false, false, false, 1));
    }

    private ConstraintManager constraintManager() {
        return controller.getModel().getConstraintManager();
    }
}

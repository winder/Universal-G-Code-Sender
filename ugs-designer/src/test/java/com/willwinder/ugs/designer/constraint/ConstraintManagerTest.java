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
package com.willwinder.ugs.designer.constraint;

import com.willwinder.ugs.designer.entities.Anchor;
import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.designer.logic.DesignModel;
import com.willwinder.ugs.designer.model.Size;
import org.junit.Before;
import org.junit.Test;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

public class ConstraintManagerTest {
    private DesignModel model;
    private ConstraintManager constraintManager;
    private Rectangle first;
    private Rectangle second;
    private Rectangle third;

    @Before
    public void setUp() {
        model = new DesignModel();
        constraintManager = model.getConstraintManager();
        first = new Rectangle(0, 0, 10, 10);
        second = new Rectangle(30, 0, 10, 10);
        third = new Rectangle(60, 0, 10, 10);
        model.insertEntities(List.of(first, second, third));
    }

    @Test
    public void add_shouldMoveTargetToSatisfyDistance() {
        DistanceConstraint constraint = horizontal(first, at(first, Anchor.BOTTOM_RIGHT), second, at(second, Anchor.BOTTOM_LEFT), 5);

        constraintManager.add(constraint);

        assertThat(second.getPosition().getX()).isCloseTo(15, within(1e-6));
    }

    @Test
    public void add_shouldThrowWhenConstraintConflicts() {
        constraintManager.add(horizontal(first, at(first, Anchor.CENTER), second, at(second, Anchor.CENTER), 30));

        assertThatThrownBy(() -> constraintManager.add(horizontal(second, at(second, Anchor.CENTER), first, at(first, Anchor.CENTER), -30)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void solve_shouldMoveTargetWhenReferenceMoves() {
        constraintManager.add(horizontal(first, at(first, Anchor.BOTTOM_RIGHT), second, at(second, Anchor.BOTTOM_LEFT), 20));

        first.move(new Point2D.Double(7, 3));

        assertThat(second.getPosition().getX()).isCloseTo(37, within(1e-6));
        assertThat(second.getPosition().getY()).isCloseTo(0, within(1e-6));
    }

    @Test
    public void solve_shouldMoveTargetWhenReferenceIsResized() {
        constraintManager.add(horizontal(first, at(first, Anchor.BOTTOM_RIGHT), second, at(second, Anchor.BOTTOM_LEFT), 20));

        first.setSize(new Size(15, 10));

        assertThat(second.getPosition().getX()).isCloseTo(35, within(1e-6));
    }

    @Test
    public void solve_shouldMoveTheReferenceWhenTheTargetIsMoved() {
        constraintManager.add(horizontal(first, at(first, Anchor.BOTTOM_RIGHT), second, at(second, Anchor.BOTTOM_LEFT), 20));

        second.move(new Point2D.Double(5, 5));

        assertThat(second.getPosition().getX()).isCloseTo(35, within(1e-6));
        assertThat(second.getPosition().getY()).isCloseTo(5, within(1e-6));
        assertThat(first.getPosition().getX()).isCloseTo(5, within(1e-6));
        assertThat(first.getPosition().getY()).isCloseTo(0, within(1e-6));
    }

    @Test
    public void solve_shouldMoveTheWholeChainWhenTheMiddleIsMoved() {
        constraintManager.add(horizontal(first, at(first, Anchor.CENTER), second, at(second, Anchor.CENTER), 30));
        constraintManager.add(horizontal(second, at(second, Anchor.CENTER), third, at(third, Anchor.CENTER), 30));

        second.move(new Point2D.Double(4, 0));

        assertThat(first.getCenter().getX()).isCloseTo(9, within(1e-6));
        assertThat(second.getCenter().getX()).isCloseTo(39, within(1e-6));
        assertThat(third.getCenter().getX()).isCloseTo(69, within(1e-6));
    }

    @Test
    public void solve_shouldNotMoveHeldEntities() {
        constraintManager.add(horizontal(first, at(first, Anchor.CENTER), second, at(second, Anchor.CENTER), 30));
        constraintManager.setHeldEntities(() -> List.of(first, second));

        first.move(new Point2D.Double(4, 0));
        second.move(new Point2D.Double(4, 0));

        assertThat(first.getCenter().getX()).isCloseTo(9, within(1e-6));
        assertThat(second.getCenter().getX()).isCloseTo(39, within(1e-6));
    }

    @Test
    public void solve_shouldApplyChainedConstraintsFromTheFirstReference() {
        constraintManager.add(horizontal(second, at(second, Anchor.CENTER), third, at(third, Anchor.CENTER), 30));
        constraintManager.add(horizontal(first, at(first, Anchor.CENTER), second, at(second, Anchor.CENTER), 30));

        first.move(new Point2D.Double(10, 0));

        assertThat(second.getCenter().getX()).isCloseTo(45, within(1e-6));
        assertThat(third.getCenter().getX()).isCloseTo(75, within(1e-6));
    }

    @Test
    public void solve_shouldApplyEachAxisIndependently() {
        constraintManager.add(horizontal(first, at(first, Anchor.CENTER), second, at(second, Anchor.CENTER), 0));
        constraintManager.add(vertical(first, at(first, Anchor.TOP_LEFT), second, at(second, Anchor.BOTTOM_LEFT), 2));

        first.move(new Point2D.Double(3, 4));

        assertThat(second.getCenter().getX()).isCloseTo(8, within(1e-6));
        assertThat(second.getPosition().getY()).isCloseTo(16, within(1e-6));
    }

    @Test
    public void add_shouldFlipTheConstraintOfAnAlreadyDrivenTarget() {
        DistanceConstraint existing = horizontal(first, at(first, Anchor.CENTER), second, at(second, Anchor.CENTER), 30);
        constraintManager.add(existing);

        List<Constraint> result = constraintManager.add(horizontal(third, at(third, Anchor.CENTER), second, at(second, Anchor.CENTER), -10));

        assertThat(result).containsExactly(existing);
        assertThat(existing.getReference()).isSameAs(second);
        assertThat(existing.getDistance()).isCloseTo(-30, within(1e-6));
        assertThat(third.getPosition().getX()).isCloseTo(60, within(1e-6));
        assertThat(second.getPosition().getX()).isCloseTo(50, within(1e-6));
        assertThat(first.getPosition().getX()).isCloseTo(20, within(1e-6));
    }

    @Test
    public void add_shouldFlipEveryConstraintFromTheTargetToTheRootOfItsTree() {
        constraintManager.add(horizontal(first, at(first, Anchor.CENTER), second, at(second, Anchor.CENTER), 30));
        constraintManager.add(horizontal(second, at(second, Anchor.CENTER), third, at(third, Anchor.CENTER), 30));
        Rectangle fourth = new Rectangle(100, 0, 10, 10);
        model.insertEntity(fourth);

        List<Constraint> result = constraintManager.add(horizontal(fourth, at(fourth, Anchor.CENTER), third, at(third, Anchor.CENTER), -10));

        assertThat(result).hasSize(2);
        assertThat(third.getPosition().getX()).isCloseTo(90, within(1e-6));
        assertThat(first.getPosition().getX()).isCloseTo(30, within(1e-6));
    }

    @Test
    public void flip_shouldRestoreTheConstraintsFlippedByAnAdd() {
        DistanceConstraint existing = horizontal(first, at(first, Anchor.CENTER), second, at(second, Anchor.CENTER), 30);
        constraintManager.add(existing);
        DistanceConstraint added = horizontal(third, at(third, Anchor.CENTER), second, at(second, Anchor.CENTER), -30);
        List<Constraint> flipped = constraintManager.add(added);
        constraintManager.remove(added);

        constraintManager.flip(flipped);

        assertThat(existing.getReference()).isSameAs(first);
        assertThat(existing.getDistance()).isCloseTo(30, within(1e-6));
    }

    @Test
    public void canAdd_shouldAllowDriversOnDifferentAxes() {
        constraintManager.add(horizontal(first, at(first, Anchor.CENTER), second, at(second, Anchor.CENTER), 30));

        boolean result = constraintManager.canAdd(vertical(third, at(third, Anchor.CENTER), second, at(second, Anchor.CENTER), 0));

        assertThat(result).isTrue();
    }

    @Test
    public void add_shouldGlueTheTargetPointToTheReferencePoint() {
        constraintManager.add(Constraint.coincident(first, at(first, Anchor.CENTER), second, at(second, Anchor.TOP_LEFT)));

        first.move(new Point2D.Double(4, 6));

        assertThat(second.getPosition()).isEqualTo(new Point2D.Double(9, 1));
    }

    @Test
    public void solve_shouldMoveTheReferenceWhenAGluedTargetIsMoved() {
        constraintManager.add(Constraint.coincident(first, at(first, Anchor.CENTER), second, at(second, Anchor.TOP_LEFT)));

        second.move(new Point2D.Double(10, -2));

        assertThat(first.getPosition()).isEqualTo(new Point2D.Double(10, -2));
    }

    @Test
    public void conflictMessage_shouldNameTheAxisAFlippedGlueConflictsOn() {
        constraintManager.add(Constraint.coincident(first, at(first, Anchor.CENTER), second, at(second, Anchor.CENTER)));
        constraintManager.add(vertical(third, at(third, Anchor.CENTER), first, at(first, Anchor.CENTER), 0));

        Optional<String> result = constraintManager.conflictMessage(horizontal(third, at(third, Anchor.CENTER), second, at(second, Anchor.CENTER), -10));

        assertThat(result).contains("Vertical position already constrained");
    }

    @Test
    public void conflictMessage_shouldExplainThatTheShapesAreAlreadyLinked() {
        constraintManager.add(horizontal(first, at(first, Anchor.CENTER), second, at(second, Anchor.CENTER), 30));
        constraintManager.add(horizontal(third, at(third, Anchor.CENTER), second, at(second, Anchor.CENTER), -30));

        Optional<String> result = constraintManager.conflictMessage(horizontal(first, at(first, Anchor.CENTER), third, at(third, Anchor.CENTER), 60));

        assertThat(result).contains("The shapes are already linked horizontally");
    }

    @Test
    public void validate_shouldReportALoop() {
        constraintManager.add(horizontal(first, at(first, Anchor.CENTER), second, at(second, Anchor.CENTER), 30));

        Optional<ConstraintManager.Conflict> result = constraintManager.validate(horizontal(second, at(second, Anchor.CENTER), first, at(first, Anchor.CENTER), -30));

        assertThat(result).contains(ConstraintManager.Conflict.LOOP);
    }

    @Test
    public void validate_shouldReportPointsWithoutSizeBetweenThem() {
        DistanceConstraint constraint = horizontal(first, at(first, Anchor.TOP_LEFT), first, at(first, Anchor.BOTTOM_LEFT), 0);

        Optional<ConstraintManager.Conflict> result = constraintManager.validate(constraint);

        assertThat(result).contains(ConstraintManager.Conflict.NO_SIZE);
    }

    @Test
    public void canAdd_shouldRejectLoops() {
        constraintManager.add(horizontal(first, at(first, Anchor.CENTER), second, at(second, Anchor.CENTER), 30));
        constraintManager.add(horizontal(second, at(second, Anchor.CENTER), third, at(third, Anchor.CENTER), 30));

        boolean result = constraintManager.canAdd(horizontal(third, at(third, Anchor.CENTER), first, at(first, Anchor.CENTER), -60));

        assertThat(result).isFalse();
    }

    @Test
    public void add_shouldResizeTheEntityWhenBothPointsAreOnIt() {
        DistanceConstraint constraint = horizontal(first, at(first, Anchor.BOTTOM_LEFT), first, at(first, Anchor.BOTTOM_RIGHT), 25);

        constraintManager.add(constraint);

        assertThat(first.getPosition().getX()).isCloseTo(0, within(1e-6));
        assertThat(first.getSize().getWidth()).isCloseTo(25, within(1e-6));
        assertThat(first.getSize().getHeight()).isCloseTo(10, within(1e-6));
    }

    @Test
    public void add_shouldKeepTheReferencePointOfASizeInPlace() {
        DistanceConstraint constraint = horizontal(first, at(first, Anchor.BOTTOM_RIGHT), first, at(first, Anchor.BOTTOM_LEFT), -4);

        constraintManager.add(constraint);

        assertThat(first.getPosition().getX()).isCloseTo(6, within(1e-6));
        assertThat(first.getSize().getWidth()).isCloseTo(4, within(1e-6));
    }

    @Test
    public void canAdd_shouldRejectASizeWithoutDistanceBetweenThePoints() {
        boolean result = constraintManager.canAdd(DistanceConstraint.measured(first, at(first, Anchor.TOP_LEFT), first, at(first, Anchor.BOTTOM_LEFT), Axis.HORIZONTAL, 0));

        assertThat(result).isFalse();
    }

    @Test
    public void canAdd_shouldRejectASecondSizeOnTheSameAxis() {
        constraintManager.add(horizontal(first, at(first, Anchor.BOTTOM_LEFT), first, at(first, Anchor.BOTTOM_RIGHT), 10));

        boolean result = constraintManager.canAdd(horizontal(first, at(first, Anchor.BOTTOM_LEFT), first, at(first, Anchor.CENTER), 5));

        assertThat(result).isFalse();
    }

    @Test
    public void canAdd_shouldAllowASizeOfADrivenEntity() {
        constraintManager.add(horizontal(first, at(first, Anchor.BOTTOM_RIGHT), second, at(second, Anchor.BOTTOM_LEFT), 20));

        boolean result = constraintManager.canAdd(horizontal(second, at(second, Anchor.BOTTOM_LEFT), second, at(second, Anchor.BOTTOM_RIGHT), 10));

        assertThat(result).isTrue();
    }

    @Test
    public void solve_shouldResizeBeforeMovingIntoPlace() {
        constraintManager.add(horizontal(first, at(first, Anchor.BOTTOM_RIGHT), second, at(second, Anchor.BOTTOM_RIGHT), 30));
        DistanceConstraint width = horizontal(second, at(second, Anchor.BOTTOM_LEFT), second, at(second, Anchor.BOTTOM_RIGHT), 10);
        constraintManager.add(width);

        constraintManager.setDistance(width, 20);

        assertThat(second.getPosition().getX()).isCloseTo(20, within(1e-6));
        assertThat(second.getSize().getWidth()).isCloseTo(20, within(1e-6));
    }

    @Test
    public void solve_shouldMoveTargetsFollowingAResizedReference() {
        DistanceConstraint width = horizontal(first, at(first, Anchor.BOTTOM_LEFT), first, at(first, Anchor.BOTTOM_RIGHT), 10);
        constraintManager.add(width);
        constraintManager.add(horizontal(first, at(first, Anchor.BOTTOM_RIGHT), second, at(second, Anchor.BOTTOM_LEFT), 20));

        constraintManager.setDistance(width, 15);

        assertThat(second.getPosition().getX()).isCloseTo(35, within(1e-6));
    }

    @Test
    public void add_shouldResizeARotatedEntityAlongItsOwnAxis() {
        first.rotate(30);
        Point2D left = first.getTransform().transform(new Point2D.Double(0, 0), null);
        DistanceConstraint constraint = DistanceConstraint.measured(first, ShapeSnapping.pointAt(first, left), first,
                ShapeSnapping.pointAt(first, first.getTransform().transform(new Point2D.Double(1, 0), null)), Axis.HORIZONTAL, 0);

        constraintManager.add(constraint);

        constraintManager.setDistance(constraint, 25);

        Point2D right = constraint.getTargetPoint();
        assertThat(constraint.getReferencePoint().distance(left)).isCloseTo(0, within(1e-6));
        assertThat(right.distance(left)).isCloseTo(25, within(1e-6));
        assertThat(first.getRotation()).isCloseTo(30, within(1e-6));
    }

    @Test
    public void add_shouldMeasureARotatedSizeAlongTheEntitysAxis() {
        first.rotate(30);

        DistanceConstraint constraint = DistanceConstraint.measured(first, ShapeSnapping.pointAt(first, first.getTransform().transform(new Point2D.Double(0, 0), null)), first,
                ShapeSnapping.pointAt(first, first.getTransform().transform(new Point2D.Double(1, 0), null)), Axis.HORIZONTAL, 0);

        assertThat(constraint.getDistance()).isCloseTo(10, within(1e-6));
    }

    @Test
    public void solve_shouldKeepTheHeightOfARotatedEntityWhenItsWidthChanges() {
        first.rotate(45);
        DistanceConstraint width = DistanceConstraint.measured(first, ShapeSnapping.pointAt(first, first.getTransform().transform(new Point2D.Double(0, 0), null)), first,
                ShapeSnapping.pointAt(first, first.getTransform().transform(new Point2D.Double(1, 0), null)), Axis.HORIZONTAL, 0);
        DistanceConstraint height = DistanceConstraint.measured(first, ShapeSnapping.pointAt(first, first.getTransform().transform(new Point2D.Double(0, 0), null)), first,
                ShapeSnapping.pointAt(first, first.getTransform().transform(new Point2D.Double(0, 1), null)), Axis.VERTICAL, 0);
        constraintManager.add(width);
        constraintManager.add(height);

        constraintManager.setDistance(width, 30);

        assertThat(width.measure()).isCloseTo(30, within(1e-6));
        assertThat(height.measure()).isCloseTo(10, within(1e-6));
    }

    @Test
    public void solve_shouldKeepTheSizeWhenTheEntityIsResized() {
        constraintManager.add(horizontal(first, at(first, Anchor.BOTTOM_LEFT), first, at(first, Anchor.BOTTOM_RIGHT), 10));

        first.setSize(new Size(30, 20));

        assertThat(first.getSize().getWidth()).isCloseTo(10, within(1e-6));
        assertThat(first.getSize().getHeight()).isCloseTo(20, within(1e-6));
    }

    @Test
    public void getActiveConstraints_shouldSkipConstraintsOfRemovedEntities() {
        DistanceConstraint constraint = horizontal(first, at(first, Anchor.CENTER), second, at(second, Anchor.CENTER), 30);
        constraintManager.add(constraint);

        model.removeEntity(first);

        assertThat(constraintManager.getActiveConstraints()).isEmpty();
        assertThat(constraintManager.getConstraints()).containsExactly(constraint);
    }

    @Test
    public void solve_shouldApplyConstraintAgainWhenRemovedEntityIsRestored() {
        constraintManager.add(horizontal(first, at(first, Anchor.CENTER), second, at(second, Anchor.CENTER), 30));
        model.removeEntity(first);
        first.move(new Point2D.Double(10, 0));

        model.insertEntity(first);

        assertThat(second.getCenter().getX()).isCloseTo(45, within(1e-6));
    }

    @Test
    public void setDistance_shouldMoveTarget() {
        DistanceConstraint constraint = horizontal(first, at(first, Anchor.CENTER), second, at(second, Anchor.CENTER), 30);
        constraintManager.add(constraint);

        constraintManager.setDistance(constraint, 12);

        assertThat(second.getCenter().getX()).isCloseTo(17, within(1e-6));
    }

    @Test
    public void addListener_shouldBeNotifiedWhenConstraintsChange() {
        int[] notifications = {0};
        constraintManager.addListener(() -> notifications[0]++);
        DistanceConstraint constraint = horizontal(first, at(first, Anchor.CENTER), second, at(second, Anchor.CENTER), 30);

        constraintManager.add(constraint);
        constraintManager.remove(constraint);

        assertThat(notifications[0]).isEqualTo(2);
    }

    @Test
    public void clear_shouldRemoveConstraintsWhenModelIsCleared() {
        constraintManager.add(horizontal(first, at(first, Anchor.CENTER), second, at(second, Anchor.CENTER), 30));

        model.clear();

        assertThat(constraintManager.getConstraints()).isEmpty();
    }

    @Test
    public void solve_shouldMeasureFromTheOutlineOfARotatedReference() {
        constraintManager.add(horizontal(first, at(first, Anchor.BOTTOM_RIGHT), second, at(second, Anchor.BOTTOM_LEFT), 20));

        first.rotate(new Point2D.Double(0, 0), -90);

        assertThat(second.getPosition().getX()).isCloseTo(20, within(1e-6));
    }

    @Test
    public void solve_shouldMeasureAlongTheAxesOfEntitiesRotatedTogether() {
        DistanceConstraint constraint = horizontal(first, at(first, Anchor.BOTTOM_RIGHT), second, at(second, Anchor.BOTTOM_LEFT), 20);
        constraintManager.add(constraint);
        constraintManager.setHeldEntities(() -> List.of(first, second));
        first.rotate(new Point2D.Double(0, 0), 30);
        second.rotate(new Point2D.Double(0, 0), 30);
        constraintManager.setHeldEntities(List::of);
        double across = constraint.getFrame().crossCoordinate(second.getCenter());

        first.move(new Point2D.Double(10, 0));

        assertThat(constraint.measure()).isCloseTo(20, within(1e-6));
        assertThat(constraint.getFrame().crossCoordinate(second.getCenter())).isCloseTo(across, within(1e-6));
    }

    @Test
    public void solve_shouldSatisfyConstraintsMeasuredAlongDifferentlyRotatedAxes() {
        DistanceConstraint rotated = vertical(first, at(first, Anchor.CENTER), second, at(second, Anchor.CENTER), 10);
        DistanceConstraint straight = horizontal(second, at(second, Anchor.CENTER), third, at(third, Anchor.CENTER), 30);
        constraintManager.setHeldEntities(() -> List.of(first, second, third));
        first.rotate(new Point2D.Double(0, 0), 30);
        second.rotate(new Point2D.Double(0, 0), 30);
        constraintManager.setHeldEntities(List::of);

        constraintManager.setConstraints(List.of(rotated, straight));

        assertThat(rotated.measure()).isCloseTo(10, within(1e-6));
        assertThat(straight.measure()).isCloseTo(30, within(1e-6));
    }

    @Test
    public void solve_shouldKeepTheAxesWhenOnlyOneEntityIsRotated() {
        DistanceConstraint constraint = horizontal(first, at(first, Anchor.BOTTOM_RIGHT), second, at(second, Anchor.BOTTOM_LEFT), 20);
        constraintManager.add(constraint);

        first.rotate(30);

        assertThat(constraint.getFrame()).isEqualTo(AxisFrame.of(Axis.HORIZONTAL));
        assertThat(constraint.measure()).isCloseTo(20, within(1e-6));
    }

    @Test
    public void solve_shouldTurnTheAxesWhenTheEntitiesAreRotatedOneAtATime() {
        DistanceConstraint constraint = horizontal(first, at(first, Anchor.BOTTOM_RIGHT), second, at(second, Anchor.BOTTOM_LEFT), 20);
        constraintManager.add(constraint);

        first.rotate(new Point2D.Double(0, 0), 30);
        second.rotate(new Point2D.Double(0, 0), 30);

        assertThat(Math.toDegrees(constraint.getFrame().angle())).isCloseTo(-30, within(1e-6));
        assertThat(constraint.measure()).isCloseTo(20, within(1e-6));
    }

    @Test
    public void solve_shouldNotTurnTheAxesWhenTheEntitiesAreRotatedDifferently() {
        DistanceConstraint constraint = horizontal(first, at(first, Anchor.BOTTOM_RIGHT), second, at(second, Anchor.BOTTOM_LEFT), 20);
        constraintManager.add(constraint);

        first.rotate(30);
        second.rotate(45);

        assertThat(constraint.getFrame()).isEqualTo(AxisFrame.of(Axis.HORIZONTAL));
    }

    @Test
    public void getActiveConstraints_shouldSkipConstraintsWhosePointIsGone() {
        constraintManager.add(horizontal(first, new VertexPoint(999, false), second, at(second, Anchor.CENTER), 30));

        assertThat(constraintManager.getActiveConstraints()).isEmpty();
    }

    @Test
    public void remove_shouldDeselectTheRemovedConstraint() {
        DistanceConstraint constraint = horizontal(first, at(first, Anchor.CENTER), second, at(second, Anchor.CENTER), 30);
        constraintManager.add(constraint);
        constraintManager.setSelectedConstraint(constraint);

        constraintManager.remove(constraint);

        assertThat(constraintManager.getSelectedConstraint()).isEmpty();
    }

    @Test
    public void setOffset_shouldMoveTheDimensionWithoutMovingEntities() {
        DistanceConstraint constraint = horizontal(first, at(first, Anchor.CENTER), second, at(second, Anchor.CENTER), 30);
        constraintManager.add(constraint);

        constraintManager.setOffset(constraint, 12);

        assertThat(constraint.getOffset()).isCloseTo(12, within(1e-6));
        assertThat(second.getCenter().getX()).isCloseTo(35, within(1e-6));
    }

    @Test
    public void add_shouldGiveTheTargetOfAnEqualSizeTheWidthOfTheReference() {
        second.setSize(new Size(20, 6));

        constraintManager.add(Constraint.equalSize(first, Axis.HORIZONTAL, second, Axis.HORIZONTAL));

        assertThat(second.getSize().getWidth()).isCloseTo(10, within(1e-6));
        assertThat(second.getSize().getHeight()).isCloseTo(6, within(1e-6));
        assertThat(second.getCenter()).isEqualTo(new Point2D.Double(40, 3));
    }

    @Test
    public void add_shouldGiveTheWidthOfTheTargetTheHeightOfTheReference() {
        first.setSize(new Size(10, 16));

        constraintManager.add(Constraint.equalSize(first, Axis.VERTICAL, second, Axis.HORIZONTAL));

        assertThat(second.getSize().getWidth()).isCloseTo(16, within(1e-6));
        assertThat(second.getSize().getHeight()).isCloseTo(10, within(1e-6));
    }

    @Test
    public void add_shouldMakeASquareOfAnEntityWhoseWidthIsItsHeight() {
        first.setSize(new Size(10, 16));

        constraintManager.add(Constraint.equalSize(first, Axis.HORIZONTAL, first, Axis.VERTICAL));

        assertThat(first.getSize().getHeight()).isCloseTo(10, within(1e-6));
    }

    @Test
    public void canAdd_shouldRejectAnEqualSizeOfASizeWithItself() {
        boolean result = constraintManager.canAdd(Constraint.equalSize(first, Axis.HORIZONTAL, first, Axis.HORIZONTAL));

        assertThat(result).isFalse();
    }

    @Test
    public void solve_shouldResizeTheReferenceWhenTheTargetOfAnEqualSizeIsResized() {
        constraintManager.add(Constraint.equalSize(first, Axis.HORIZONTAL, second, Axis.VERTICAL));

        second.setSize(new Size(16, 12));

        assertThat(first.getSize().getWidth()).isCloseTo(12, within(1e-6));
        assertThat(first.getSize().getHeight()).isCloseTo(10, within(1e-6));
    }

    @Test
    public void add_shouldFlipAnEqualSizeWhoseTargetHasItsSizeHeld() {
        constraintManager.add(horizontal(second, at(second, Anchor.BOTTOM_LEFT), second, at(second, Anchor.BOTTOM_RIGHT), 20));
        EqualSizeConstraint equalSize = Constraint.equalSize(first, Axis.VERTICAL, second, Axis.HORIZONTAL);

        constraintManager.add(equalSize);

        assertThat(equalSize.getReference()).isSameAs(second);
        assertThat(equalSize.getReferenceAxis()).isEqualTo(Axis.HORIZONTAL);
        assertThat(first.getSize().getHeight()).isCloseTo(20, within(1e-6));
        assertThat(second.getSize().getWidth()).isCloseTo(20, within(1e-6));
    }

    @Test
    public void add_shouldFlipEqualSizesSoTheSizeOfTheirTargetCanBeHeld() {
        EqualSizeConstraint equalSize = Constraint.equalSize(first, Axis.HORIZONTAL, second, Axis.HORIZONTAL);
        constraintManager.add(equalSize);

        List<Constraint> flipped = constraintManager.add(horizontal(second, at(second, Anchor.BOTTOM_LEFT), second, at(second, Anchor.BOTTOM_RIGHT), 25));

        assertThat(flipped).containsExactly(equalSize);
        assertThat(first.getSize().getWidth()).isCloseTo(25, within(1e-6));
        assertThat(second.getSize().getWidth()).isCloseTo(25, within(1e-6));
    }

    @Test
    public void canAdd_shouldAllowAWidthAndAHeightOfTheSameEntityToFollowDifferentSizes() {
        constraintManager.add(Constraint.equalSize(first, Axis.HORIZONTAL, third, Axis.HORIZONTAL));

        boolean result = constraintManager.canAdd(Constraint.equalSize(second, Axis.HORIZONTAL, third, Axis.VERTICAL));

        assertThat(result).isTrue();
    }

    @Test
    public void conflictMessage_shouldRefuseAnEqualSizeBetweenSizesThatAreBothHeld() {
        constraintManager.add(horizontal(first, at(first, Anchor.BOTTOM_LEFT), first, at(first, Anchor.BOTTOM_RIGHT), 10));
        constraintManager.add(horizontal(second, at(second, Anchor.BOTTOM_LEFT), second, at(second, Anchor.BOTTOM_RIGHT), 20));

        Optional<String> result = constraintManager.conflictMessage(Constraint.equalSize(first, Axis.HORIZONTAL, second, Axis.HORIZONTAL));

        assertThat(result).contains("Width already constrained");
    }

    @Test
    public void conflictMessage_shouldExplainThatTheSizesAreAlreadyLinked() {
        constraintManager.add(Constraint.equalSize(first, Axis.HORIZONTAL, second, Axis.VERTICAL));
        constraintManager.add(Constraint.equalSize(second, Axis.VERTICAL, third, Axis.HORIZONTAL));

        Optional<String> result = constraintManager.conflictMessage(Constraint.equalSize(third, Axis.HORIZONTAL, first, Axis.HORIZONTAL));

        assertThat(result).contains("The sizes are already linked");
    }

    @Test
    public void add_shouldNotMoveEntitiesJoinedByAnEqualSize() {
        constraintManager.add(horizontal(first, at(first, Anchor.CENTER), second, at(second, Anchor.CENTER), 30));

        constraintManager.add(Constraint.equalSize(first, Axis.HORIZONTAL, third, Axis.HORIZONTAL));

        assertThat(third.getCenter().getX()).isCloseTo(65, within(1e-6));
        assertThat(constraintManager.canAdd(horizontal(first, at(first, Anchor.CENTER), third, at(third, Anchor.CENTER), 60))).isTrue();
    }

    private static DistanceConstraint horizontal(Rectangle reference, ShapePoint referencePoint, Rectangle target, ShapePoint targetPoint, double distance) {
        return new DistanceConstraint(reference, referencePoint, target, targetPoint, Axis.HORIZONTAL, distance, 0);
    }

    private static DistanceConstraint vertical(Rectangle reference, ShapePoint referencePoint, Rectangle target, ShapePoint targetPoint, double distance) {
        return new DistanceConstraint(reference, referencePoint, target, targetPoint, Axis.VERTICAL, distance, 0);
    }

    private static ShapePoint at(Entity entity, Anchor anchor) {
        return ShapeSnapping.pointAt(entity, entity.getPosition(anchor));
    }
}

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

import java.util.Optional;
import com.willwinder.ugs.designer.entities.cuttable.Rectangle;
import org.junit.Test;

import java.awt.geom.Point2D;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class DimensionLineTest {

    @Test
    public void direction_shouldPointFromTheReferenceTowardsTheTarget() {
        DimensionLine.Linear dimension = DimensionLine.of(new Point2D.Double(20, 0), new Point2D.Double(5, 0), AxisFrame.of(Axis.HORIZONTAL), 10, -15);

        Point2D result = dimension.direction();

        assertThat(result).isEqualTo(new Point2D.Double(-1, 0));
    }

    @Test
    public void direction_shouldFollowTheAxisWhenTheDistanceIsZero() {
        DimensionLine.Linear dimension = DimensionLine.of(new Point2D.Double(5, 10), new Point2D.Double(8, 10), AxisFrame.of(Axis.VERTICAL), 4, 0);

        Point2D result = dimension.direction();

        assertThat(result).isEqualTo(new Point2D.Double(0, 1));
    }

    @Test
    public void alignment_shouldRunFromTheReferenceToWhereTheTargetLinesUp() {
        DimensionLine.Alignment result = DimensionLine.alignment(new Point2D.Double(0, 5), new Point2D.Double(3, 35), AxisFrame.of(Axis.HORIZONTAL));

        assertThat(result.from()).isEqualTo(new Point2D.Double(0, 5));
        assertThat(result.to()).isEqualTo(new Point2D.Double(0, 35));
        assertThat(result.hasLabel()).isFalse();
    }

    @Test
    public void lines_shouldExtendAnAlignmentBeyondItsPoints() {
        DimensionLine.Alignment dimension = DimensionLine.alignment(new Point2D.Double(0, 5), new Point2D.Double(0, 35), AxisFrame.of(Axis.HORIZONTAL));

        List<Point2D[]> result = dimension.lines(0.5);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).containsExactly(new Point2D.Double(0, 1), new Point2D.Double(0, 39));
    }

    @Test
    public void distanceTo_shouldMeasureFromTheLineOfAnAlignment() {
        DimensionLine.Alignment dimension = DimensionLine.alignment(new Point2D.Double(0, 5), new Point2D.Double(0, 35), AxisFrame.of(Axis.HORIZONTAL));

        double result = dimension.distanceTo(new Point2D.Double(2, 20), 0.1);

        assertThat(result).isCloseTo(2, within(1e-9));
    }

    @Test
    public void distanceTo_shouldBeZeroInsideTheMarkOfACoincidence() {
        DimensionLine.Coincidence dimension = DimensionLine.coincidence(new Point2D.Double(5, 5), new Point2D.Double(5, 5));

        double result = dimension.distanceTo(new Point2D.Double(5.5, 5), 0.1);

        assertThat(result).isZero();
        assertThat(dimension.hasLabel()).isFalse();
    }

    @Test
    public void lines_shouldJoinThePointsOfACoincidenceUntilTheyMeet() {
        DimensionLine.Coincidence dimension = DimensionLine.coincidence(new Point2D.Double(5, 5), new Point2D.Double(20, 5));

        List<Point2D[]> result = dimension.lines(0.1);

        assertThat(result).anySatisfy(line -> assertThat(line).containsExactly(new Point2D.Double(5, 5), new Point2D.Double(20, 5)));
    }

    @Test
    public void of_shouldPlaceTheDimensionLineAtTheOffsetFromTheMiddleOfTheAnchors() {
        DimensionLine.Linear dimension = DimensionLine.of(new Point2D.Double(0, 10), new Point2D.Double(30, 20), AxisFrame.of(Axis.HORIZONTAL), 5, 30);

        assertThat(dimension.lineStart()).isEqualTo(new Point2D.Double(0, 20));
        assertThat(dimension.lineEnd()).isEqualTo(new Point2D.Double(30, 20));
    }

    @Test
    public void labelPositions_shouldBeInTheMiddleWhenTheArrowsFitInside() {
        DimensionLine.Linear dimension = DimensionLine.of(new Point2D.Double(0, 0), new Point2D.Double(30, 0), AxisFrame.of(Axis.HORIZONTAL), 10, 30);

        List<Point2D> result = dimension.labelPositions(0.1);

        assertThat(result).containsExactly(new Point2D.Double(15, 10));
    }

    @Test
    public void labelPositions_shouldBePastTheEndWhenTheArrowsAreOutside() {
        DimensionLine.Linear dimension = DimensionLine.of(new Point2D.Double(10, 0), new Point2D.Double(10, 0), AxisFrame.of(Axis.HORIZONTAL), 10, 0);

        Point2D result = dimension.labelPositions(0.1).getFirst();

        double expected = 10 + (DimensionLine.ARROW_LENGTH_PX + DimensionLine.OUTSIDE_LEADER_PX + 3 + dimension.labelWidthPx() / 2) * 0.1;
        assertThat(result.getX()).isCloseTo(expected, within(1e-9));
        assertThat(result.getY()).isEqualTo(10);
    }

    @Test
    public void distanceTo_shouldIncludeTheLinesBeyondOutsideArrows() {
        DimensionLine.Linear dimension = DimensionLine.of(new Point2D.Double(10, 0), new Point2D.Double(10, 0), AxisFrame.of(Axis.HORIZONTAL), 10, 0);

        double result = dimension.distanceTo(new Point2D.Double(9, 10), 0.1);

        assertThat(result).isZero();
    }

    @Test
    public void of_shouldRunAlongATurnedFrame() {
        double diagonal = Math.sqrt(0.5);
        AxisFrame frame = new AxisFrame(new Point2D.Double(diagonal, diagonal), new Point2D.Double(-diagonal, diagonal));

        DimensionLine.Linear dimension = DimensionLine.of(new Point2D.Double(0, 0), new Point2D.Double(10, 10), frame, Math.sqrt(2), 0);

        assertThat(dimension.lineStart().getX()).isCloseTo(-1, within(1e-9));
        assertThat(dimension.lineStart().getY()).isCloseTo(1, within(1e-9));
        assertThat(dimension.lineEnd().getX()).isCloseTo(9, within(1e-9));
        assertThat(dimension.lineEnd().getY()).isCloseTo(11, within(1e-9));
    }

    @Test
    public void offsetThrough_shouldPlaceTheDimensionLineThroughThePosition() {
        Point2D from = new Point2D.Double(0, 0);
        Point2D to = new Point2D.Double(20, 10);
        AxisFrame frame = AxisFrame.of(Axis.HORIZONTAL);

        double result = DimensionLine.offsetThrough(from, to, frame, new Point2D.Double(7, 25));

        assertThat(DimensionLine.of(from, to, frame, result, 20).lineStart().getY()).isCloseTo(25, within(1e-9));
    }

    @Test
    public void axisFor_shouldPickTheLongerDistanceBetweenThePoints() {
        Axis result = DimensionLine.axisFor(AxisFrame.of(Axis.HORIZONTAL), new Point2D.Double(0, 0), new Point2D.Double(20, 5), new Point2D.Double(10, 2));

        assertThat(result).isEqualTo(Axis.HORIZONTAL);
    }

    @Test
    public void of_shouldShowOnlyTheEqualsSignsOfAnEqualSizeThatIsNotHighlighted() {
        Rectangle reference = new Rectangle(0, 0, 10, 20);
        Rectangle target = new Rectangle(30, 0, 10, 10);

        DimensionLine result = DimensionLine.of(Constraint.equalSize(reference, Axis.HORIZONTAL, target, Axis.VERTICAL), false);

        assertThat(result.lines(0.1)).isEmpty();
        assertThat(result.arrowheads(0.1)).isEmpty();
        assertThat(result.labelPositions(0.1)).containsExactly(new Point2D.Double(5, 20), new Point2D.Double(40, 5));
        assertThat(result.label()).isEqualTo("=");
    }

    @Test
    public void of_shouldJoinTheEqualsSignsOfAHighlightedEqualSize() {
        Rectangle reference = new Rectangle(0, 0, 10, 20);
        Rectangle target = new Rectangle(30, 0, 10, 10);

        DimensionLine result = DimensionLine.of(Constraint.equalSize(reference, Axis.HORIZONTAL, target, Axis.VERTICAL), true);

        assertThat(result.lines(0.1)).satisfiesExactly(
                line -> assertThat(line).containsExactly(new Point2D.Double(0, 20), new Point2D.Double(10, 20)),
                line -> assertThat(line).containsExactly(new Point2D.Double(40, 0), new Point2D.Double(40, 10)),
                line -> assertThat(line).containsExactly(new Point2D.Double(5, 20), new Point2D.Double(40, 5)));
        assertThat(result.arrowheads(0.1)).hasSize(4);
    }

    @Test
    public void distanceTo_shouldBeZeroOnEitherEqualsSignOfAnEqualSize() {
        Rectangle reference = new Rectangle(0, 0, 10, 20);
        Rectangle target = new Rectangle(30, 0, 10, 10);
        DimensionLine dimension = DimensionLine.of(Constraint.equalSize(reference, Axis.HORIZONTAL, target, Axis.VERTICAL), false);

        double result = dimension.distanceTo(new Point2D.Double(40, 5), 0.1);

        assertThat(result).isZero();
        assertThat(dimension.distanceTo(new Point2D.Double(22.5, 12.5), 0.1)).isGreaterThan(1);
    }

    @Test
    public void size_shouldMeasureTheHeightAlongTheRightEdge() {
        Rectangle rectangle = new Rectangle(0, 0, 10, 20);

        DimensionLine.Linear result = DimensionLine.size(rectangle, Axis.VERTICAL);

        assertThat(result.lineStart()).isEqualTo(new Point2D.Double(10, 0));
        assertThat(result.lineEnd()).isEqualTo(new Point2D.Double(10, 20));
        assertThat(result.label()).isEqualTo("20");
    }

    @Test
    public void allOf_shouldStackTheEqualsSignsOfEqualSizesOnTheSameSize() {
        Rectangle reference = new Rectangle(0, 0, 10, 20);
        EqualSizeConstraint first = Constraint.equalSize(reference, Axis.HORIZONTAL, new Rectangle(30, 0, 10, 10), Axis.VERTICAL);
        EqualSizeConstraint second = Constraint.equalSize(reference, Axis.HORIZONTAL, new Rectangle(60, 0, 10, 10), Axis.HORIZONTAL);

        DimensionLine result = DimensionLine.allOf(List.of(first, second), _ -> false).get(1).dimension();

        assertThat(result.labelPositions(0.1).getFirst().getX()).isCloseTo(6.65, within(1e-9));
        assertThat(result.labelPositions(0.1).getFirst().getY()).isCloseTo(20, within(1e-9));
        assertThat(result.labelPositions(0.1).get(1)).isEqualTo(new Point2D.Double(65, 10));
    }

    @Test
    public void constraintAt_shouldPickTheBarsOfAHighlightedEqualSizeOnly() {
        EqualSizeConstraint constraint = Constraint.equalSize(new Rectangle(0, 0, 10, 20), Axis.HORIZONTAL, new Rectangle(30, 0, 10, 10), Axis.VERTICAL);
        Point2D onTheTargetsBar = new Point2D.Double(40, 1);

        Optional<Constraint> result = DimensionLine.constraintAt(List.of(constraint), _ -> true, onTheTargetsBar, 0.1, 0.1);

        assertThat(result).containsSame(constraint);
        assertThat(DimensionLine.constraintAt(List.of(constraint), _ -> false, onTheTargetsBar, 0.1, 0.1)).isEmpty();
    }

    @Test
    public void constraintAt_shouldPickEachOfTheStackedEqualsSigns() {
        Rectangle reference = new Rectangle(0, 0, 10, 20);
        EqualSizeConstraint first = Constraint.equalSize(reference, Axis.HORIZONTAL, new Rectangle(30, 0, 10, 10), Axis.VERTICAL);
        EqualSizeConstraint second = Constraint.equalSize(reference, Axis.HORIZONTAL, new Rectangle(60, 0, 10, 10), Axis.HORIZONTAL);

        Optional<Constraint> result = DimensionLine.constraintAt(List.of(first, second), _ -> false, new Point2D.Double(6.65, 20), 0.1, 0.1);

        assertThat(result).containsSame(second);
        assertThat(DimensionLine.constraintAt(List.of(first, second), _ -> false, new Point2D.Double(5, 20), 0.1, 0.1)).containsSame(first);
    }
}

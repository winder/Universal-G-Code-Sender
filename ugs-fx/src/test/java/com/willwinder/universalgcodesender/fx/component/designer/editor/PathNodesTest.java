package com.willwinder.universalgcodesender.fx.component.designer.editor;

import com.willwinder.ugs.designer.model.path.Segment;
import com.willwinder.ugs.designer.model.path.SegmentType;
import com.willwinder.ugs.designer.utils.PathUtils;
import org.junit.Test;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class PathNodesTest {

    @Test
    public void deleteNode_shouldJoinTheNeighboursOfAMiddleNode() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.lineTo(10, 0);
        path.lineTo(20, 0);

        List<Segment> result = segments(delete(path, 1, 10, 0));

        assertThat(types(result)).containsExactly(SegmentType.MOVE_TO, SegmentType.LINE_TO);
        assertThat(result.get(1).getLastPoint()).isEqualTo(new Point2D.Double(20, 0));
    }

    @Test
    public void deleteNode_shouldKeepTheOuterCurveHandlesWhenJoiningCurves() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.curveTo(0, 10, 10, 20, 20, 20);
        path.curveTo(30, 20, 40, 10, 40, 0);

        List<Segment> result = segments(delete(path, 1, 20, 20));

        assertThat(types(result)).containsExactly(SegmentType.MOVE_TO, SegmentType.CUBIC_TO);
        assertThat(result.get(1).getPoints()).containsExactly(
                new Point2D.Double(0, 10), new Point2D.Double(40, 10), new Point2D.Double(40, 0));
    }

    @Test
    public void deleteNode_shouldMakeTheNextNodeTheStartWhenDeletingTheFirst() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.lineTo(10, 0);
        path.lineTo(20, 0);

        List<Segment> result = segments(delete(path, 0, 0, 0));

        assertThat(types(result)).containsExactly(SegmentType.MOVE_TO, SegmentType.LINE_TO);
        assertThat(result.getFirst().getLastPoint()).isEqualTo(new Point2D.Double(10, 0));
    }

    @Test
    public void deleteNode_shouldTreatTheStartAndEndOfAClosedShapeAsOneNode() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.lineTo(10, 0);
        path.lineTo(10, 10);
        path.lineTo(0, 10);
        path.lineTo(0, 0);
        path.closePath();

        List<Segment> result = segments(delete(path, 0, 0, 0));

        // A closed triangle of the three remaining corners.
        assertThat(types(result)).containsExactly(SegmentType.MOVE_TO, SegmentType.LINE_TO,
                SegmentType.LINE_TO, SegmentType.LINE_TO, SegmentType.CLOSE);
        assertThat(result.stream().filter(s -> s.getType() != SegmentType.CLOSE).map(Segment::getLastPoint))
                .containsExactly(new Point2D.Double(10, 0), new Point2D.Double(10, 10),
                        new Point2D.Double(0, 10), new Point2D.Double(10, 0));
    }

    @Test
    public void deleteNode_shouldKeepOtherSubpathsUntouched() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.lineTo(10, 0);
        path.lineTo(20, 0);
        path.moveTo(0, 50);
        path.quadTo(10, 60, 20, 50);

        List<Segment> result = segments(delete(path, 1, 10, 0));

        assertThat(types(result)).containsExactly(SegmentType.MOVE_TO, SegmentType.LINE_TO,
                SegmentType.MOVE_TO, SegmentType.QUAD_TO);
    }

    @Test
    public void deleteNode_shouldGiveNothingWhenOnlyOneNodeWouldBeLeft() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.lineTo(10, 0);

        assertThat(PathNodes.deleteNode(PathUtils.getSegments(path), 1, new Point2D.Double(10, 0))).isEmpty();
    }

    @Test
    public void deleteNode_shouldRemoveTheGivenOneOfTwoEndsOnTheSameSpot() {
        Path2D square = new Path2D.Double();
        square.moveTo(0, 0);
        square.lineTo(10, 0);
        square.lineTo(10, 10);
        square.lineTo(0, 10);
        square.closePath();
        List<Segment> broken = PathUtils.getSegments(
                PathNodes.breakAtNode(PathUtils.getSegments(square), 0, new Point2D.Double(0, 0)).orElseThrow());

        // The end of the broken square, not its start which is on the same spot
        List<Segment> result = segments(PathNodes.deleteNode(broken, broken.size() - 1, new Point2D.Double(0, 0))
                .orElseThrow());

        assertThat(result.stream().map(Segment::getLastPoint)).containsExactly(
                new Point2D.Double(0, 0), new Point2D.Double(10, 0), new Point2D.Double(10, 10),
                new Point2D.Double(0, 10));
    }

    @Test
    public void deleteNodes_shouldRemoveEveryGivenNode() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.lineTo(10, 0);
        path.lineTo(10, 10);
        path.lineTo(0, 10);
        path.lineTo(0, 20);

        List<Segment> result = segments(PathNodes.deleteNodes(PathUtils.getSegments(path), List.of(
                new PathNodes.NodeAt(1, new Point2D.Double(10, 0)),
                new PathNodes.NodeAt(3, new Point2D.Double(0, 10)))).orElseThrow());

        assertThat(result.stream().map(Segment::getLastPoint)).containsExactly(
                new Point2D.Double(0, 0), new Point2D.Double(10, 10), new Point2D.Double(0, 20));
    }

    @Test
    public void joinNodes_shouldJoinTheEndsOfTwoPartsHalfwayKeepingTheirCurves() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.curveTo(0, 10, 10, 20, 20, 20);
        path.moveTo(30, 20);
        path.curveTo(40, 20, 50, 10, 50, 0);

        List<Segment> result = segments(PathNodes.joinNodes(PathUtils.getSegments(path),
                new PathNodes.NodeAt(1, new Point2D.Double(20, 20)),
                new PathNodes.NodeAt(2, new Point2D.Double(30, 20))).orElseThrow());

        assertThat(types(result)).containsExactly(SegmentType.MOVE_TO, SegmentType.CUBIC_TO, SegmentType.CUBIC_TO);
        // The handles into and out of the joined node moved along with their nodes.
        assertThat(result.get(1).getPoints()).containsExactly(
                new Point2D.Double(0, 10), new Point2D.Double(15, 20), new Point2D.Double(25, 20));
        assertThat(result.get(2).getPoints()).containsExactly(
                new Point2D.Double(35, 20), new Point2D.Double(50, 10), new Point2D.Double(50, 0));
    }

    @Test
    public void joinNodes_shouldTurnAPartAroundWhenJoiningTwoStarts() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.lineTo(10, 0);
        path.moveTo(20, 10);
        path.lineTo(30, 10);

        List<Segment> result = segments(PathNodes.joinNodes(PathUtils.getSegments(path),
                new PathNodes.NodeAt(0, new Point2D.Double(0, 0)),
                new PathNodes.NodeAt(2, new Point2D.Double(20, 10))).orElseThrow());

        assertThat(result.stream().map(Segment::getLastPoint)).containsExactly(
                new Point2D.Double(10, 0), new Point2D.Double(10, 5), new Point2D.Double(30, 10));
    }

    @Test
    public void joinNodes_shouldCloseAPartWhenJoiningItsTwoEnds() {
        Path2D square = new Path2D.Double();
        square.moveTo(0, 0);
        square.lineTo(10, 0);
        square.lineTo(10, 10);
        square.lineTo(0, 10);
        square.closePath();
        List<Segment> broken = PathUtils.getSegments(
                PathNodes.breakAtNode(PathUtils.getSegments(square), 0, new Point2D.Double(0, 0)).orElseThrow());

        // Both ends of the broken square are at (0, 0): the first and the last segment.
        List<Segment> result = segments(PathNodes.joinNodes(broken,
                new PathNodes.NodeAt(0, new Point2D.Double(0, 0)),
                new PathNodes.NodeAt(broken.size() - 1, new Point2D.Double(0, 0))).orElseThrow());

        assertThat(types(result)).endsWith(SegmentType.CLOSE);
        assertThat(result.stream().filter(segment -> segment.getType() != SegmentType.CLOSE).map(Segment::getLastPoint))
                .containsExactly(new Point2D.Double(0, 0), new Point2D.Double(10, 0), new Point2D.Double(10, 10),
                        new Point2D.Double(0, 10), new Point2D.Double(0, 0));
    }

    @Test
    public void joinNodes_shouldGiveNothingForANodeThatIsNotALooseEnd() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.lineTo(10, 0);
        path.lineTo(20, 0);
        path.moveTo(40, 0);
        path.lineTo(50, 0);

        assertThat(PathNodes.joinNodes(PathUtils.getSegments(path),
                new PathNodes.NodeAt(1, new Point2D.Double(10, 0)),
                new PathNodes.NodeAt(3, new Point2D.Double(40, 0)))).isEmpty();
    }

    @Test
    public void breakAtNode_shouldSplitAnOpenPathInTwoAtTheNode() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.curveTo(0, 10, 10, 20, 20, 20);
        path.curveTo(30, 20, 40, 10, 40, 0);

        List<Segment> result = segments(PathNodes.breakAtNode(PathUtils.getSegments(path), 1, new Point2D.Double(20, 20)).orElseThrow());

        assertThat(types(result)).containsExactly(SegmentType.MOVE_TO, SegmentType.CUBIC_TO,
                SegmentType.MOVE_TO, SegmentType.CUBIC_TO);
        // Both curves keep their handles, the second part starts where the first one ends.
        assertThat(result.get(1).getPoints()).containsExactly(
                new Point2D.Double(0, 10), new Point2D.Double(10, 20), new Point2D.Double(20, 20));
        assertThat(result.get(2).getLastPoint()).isEqualTo(new Point2D.Double(20, 20));
        assertThat(result.get(3).getPoints()).containsExactly(
                new Point2D.Double(30, 20), new Point2D.Double(40, 10), new Point2D.Double(40, 0));
    }

    @Test
    public void breakAtNode_shouldOpenAClosedShapeAtTheNode() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.lineTo(10, 0);
        path.lineTo(10, 10);
        path.lineTo(0, 10);
        path.closePath();

        List<Segment> result = segments(PathNodes.breakAtNode(PathUtils.getSegments(path), 2, new Point2D.Double(10, 10)).orElseThrow());

        // Open, starting and ending at the node it was broken at, going all the way round.
        assertThat(types(result)).doesNotContain(SegmentType.CLOSE);
        assertThat(result.stream().map(Segment::getLastPoint)).containsExactly(
                new Point2D.Double(10, 10), new Point2D.Double(0, 10), new Point2D.Double(0, 0),
                new Point2D.Double(10, 0), new Point2D.Double(10, 10));
    }

    @Test
    public void breakAtNode_shouldGiveNothingAtTheEndsOfAnOpenPath() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.lineTo(10, 0);
        path.lineTo(20, 0);
        List<Segment> segments = PathUtils.getSegments(path);

        assertThat(PathNodes.breakAtNode(segments, 0, new Point2D.Double(0, 0))).isEmpty();
        assertThat(PathNodes.breakAtNode(segments, 2, new Point2D.Double(20, 0))).isEmpty();
    }

    @Test
    public void nearestCurvePoint_shouldFindThePointOnTheClosingLine() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.lineTo(10, 0);
        path.lineTo(10, 10);
        path.closePath();

        PathNodes.CurvePoint point = PathNodes.nearestCurvePoint(PathUtils.getSegments(path), new Point2D.Double(4, 5))
                .orElseThrow();

        assertThat(point.segmentIndex()).isEqualTo(3);
        assertThat(point.position().getX()).isCloseTo(4.5, within(1e-6));
        assertThat(point.position().getY()).isCloseTo(4.5, within(1e-6));
    }

    @Test
    public void insertNode_shouldSplitTheClosingLineOfAClosedShape() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.lineTo(10, 0);
        path.lineTo(10, 10);
        path.closePath();

        List<Segment> result = segments(PathNodes.insertNode(PathUtils.getSegments(path), 3, 0.5).orElseThrow());

        assertThat(types(result)).endsWith(SegmentType.CLOSE);
        assertThat(result.stream().filter(s -> s.getType() != SegmentType.CLOSE).map(Segment::getLastPoint))
                .containsExactly(new Point2D.Double(0, 0), new Point2D.Double(10, 0), new Point2D.Double(10, 10),
                        new Point2D.Double(5, 5), new Point2D.Double(0, 0));
    }

    @Test
    public void insertNode_shouldSplitACurveIntoTwoThatKeepItsShape() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.curveTo(0, 40, 40, 40, 40, 0);

        List<Segment> result = segments(PathNodes.insertNode(PathUtils.getSegments(path), 1, 0.5).orElseThrow());

        assertThat(types(result)).containsExactly(SegmentType.MOVE_TO, SegmentType.CUBIC_TO, SegmentType.CUBIC_TO);
        assertThat(result.get(1).getPoints()).containsExactly(
                new Point2D.Double(0, 20), new Point2D.Double(10, 30), new Point2D.Double(20, 30));
        assertThat(result.get(2).getPoints()).containsExactly(
                new Point2D.Double(30, 30), new Point2D.Double(40, 20), new Point2D.Double(40, 0));
    }

    @Test
    public void makeCurveNodes_shouldGiveACornerHandlesAlongTheLineBetweenItsNeighbours() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.lineTo(30, 30);
        path.lineTo(60, 0);

        List<Segment> result = segments(PathNodes.makeCurveNodes(PathUtils.getSegments(path),
                List.of(new PathNodes.NodeAt(1, new Point2D.Double(30, 30)))).orElseThrow());

        assertThat(types(result)).containsExactly(SegmentType.MOVE_TO, SegmentType.CUBIC_TO, SegmentType.CUBIC_TO);
        assertThat(result.get(1).getPoints()[1].getY()).isCloseTo(30, within(1e-9));
        assertThat(result.get(2).getPoints()[0].getY()).isCloseTo(30, within(1e-9));
        assertThat(result.get(1).getPoints()[1].getX()).isLessThan(30);
        assertThat(result.get(2).getPoints()[0].getX()).isGreaterThan(30);
    }

    @Test
    public void makeCurveNodes_shouldGiveNothingWhenTheNodeAlreadyIsACurve() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.curveTo(0, 10, 20, 30, 30, 30);
        path.curveTo(40, 30, 60, 10, 60, 0);

        assertThat(PathNodes.makeCurveNodes(PathUtils.getSegments(path),
                List.of(new PathNodes.NodeAt(1, new Point2D.Double(30, 30))))).isEmpty();
    }

    @Test
    public void makeLineNodes_shouldStraightenBothSidesOfANode() {
        Path2D path = new Path2D.Double();
        path.moveTo(0, 0);
        path.curveTo(0, 10, 20, 30, 30, 30);
        path.curveTo(40, 30, 60, 10, 60, 0);

        List<Segment> result = segments(PathNodes.makeLineNodes(PathUtils.getSegments(path),
                List.of(new PathNodes.NodeAt(1, new Point2D.Double(30, 30)))).orElseThrow());

        assertThat(types(result)).containsExactly(SegmentType.MOVE_TO, SegmentType.LINE_TO, SegmentType.LINE_TO);
    }

    private static Path2D delete(Path2D path, int segmentIndex, double x, double y) {
        Optional<Path2D> result = PathNodes.deleteNode(PathUtils.getSegments(path), segmentIndex, new Point2D.Double(x, y));
        assertThat(result).isPresent();
        return result.get();
    }

    private static List<Segment> segments(Path2D path) {
        return PathUtils.getSegments(path);
    }

    private static List<SegmentType> types(List<Segment> segments) {
        return segments.stream().map(Segment::getType).toList();
    }
}

package com.willwinder.universalgcodesender.fx.component.designer.editor;

import com.willwinder.ugs.designer.entities.Anchor;
import com.willwinder.ugs.designer.entities.cuttable.Point;
import com.willwinder.ugs.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.designer.entities.selection.SelectionManager;
import org.junit.Test;

import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class HandleSetTest {

    @Test
    public void handles_shouldBeEmptyForNoSelectionOrASinglePoint() {
        SelectionManager selection = new SelectionManager();
        assertThat(HandleSet.handles(selection, 1)).isEmpty();

        selection.setSelection(List.of(new Point(5, 5)));
        assertThat(HandleSet.handles(selection, 1)).isEmpty();
    }

    @Test
    public void handles_shouldSurroundTheSelectionWithEightResizeAndOneRotateHandle() {
        SelectionManager selection = new SelectionManager();
        selection.setSelection(List.of(new Rectangle(10, 20, 30, 10)));
        double worldPerPixel = 0.25;

        List<HandleSet.Handle> handles = HandleSet.handles(selection, worldPerPixel);

        assertThat(handles).hasSize(9);
        assertThat(handles.stream().filter(h -> h.kind() == HandleSet.Kind.ROTATE)).hasSize(1);
        HandleSet.Handle topLeftAnchor = handles.stream().filter(h -> h.anchor() == Anchor.TOP_LEFT).findFirst().orElseThrow();
        // The handle sits on the frame opposite its anchor: centred on the bottom right corner.
        assertThat(topLeftAnchor.center().getX()).isCloseTo(40, within(1e-6));
        assertThat(topLeftAnchor.center().getY()).isCloseTo(20, within(1e-6));
        HandleSet.Handle leftAnchor = handles.stream().filter(h -> h.anchor() == Anchor.LEFT_CENTER).findFirst().orElseThrow();
        // Edge handles are centred on the middle of their edge.
        assertThat(leftAnchor.center().getX()).isCloseTo(40, within(1e-6));
        assertThat(leftAnchor.center().getY()).isCloseTo(25, within(1e-6));
        assertThat(topLeftAnchor.size()).isCloseTo(HandleSet.HANDLE_SIZE_PX * worldPerPixel, within(1e-9));
        HandleSet.Handle rotate = handles.stream().filter(h -> h.kind() == HandleSet.Kind.ROTATE).findFirst().orElseThrow();
        assertThat(rotate.center().getX()).isCloseTo(25, within(1e-6));
        assertThat(rotate.center().getY()).isCloseTo(30 + HandleSet.ROTATE_MARGIN_PX * worldPerPixel, within(1e-6));
    }

    @Test
    public void handles_shouldMoveOutsideTheFrameWhenTheSelectionIsSmallOnScreen() {
        SelectionManager selection = new SelectionManager();
        selection.setSelection(List.of(new Rectangle(10, 20, 3, 3)));
        double worldPerPixel = 1;

        List<HandleSet.Handle> handles = HandleSet.handles(selection, worldPerPixel);

        Point2D inside = new Point2D.Double(11.5, 21.5);
        assertThat(HandleSet.handleAt(handles, inside)).isEmpty();
        HandleSet.Handle topLeftAnchor = handles.stream().filter(h -> h.anchor() == Anchor.TOP_LEFT).findFirst().orElseThrow();
        assertThat(topLeftAnchor.center().getX()).isGreaterThan(13);
        assertThat(topLeftAnchor.center().getY()).isLessThan(20);
    }

    @Test
    public void handles_shouldSizeEachHandleFromTheScaleAtItsOwnPosition() {
        SelectionManager selection = new SelectionManager();
        selection.setSelection(List.of(new Rectangle(0, 0, 100, 100)));

        // Pretend the far side of the selection is twice as far from the camera.
        List<HandleSet.Handle> handles = HandleSet.handles(selection, point -> point.getY() > 50 ? 2.0 : 1.0);

        HandleSet.Handle near = handles.stream().filter(h -> h.anchor() == Anchor.TOP_CENTER).findFirst().orElseThrow();
        HandleSet.Handle far = handles.stream().filter(h -> h.anchor() == Anchor.BOTTOM_CENTER).findFirst().orElseThrow();
        assertThat(near.size()).isCloseTo(HandleSet.HANDLE_SIZE_PX, within(1e-9));
        assertThat(far.size()).isCloseTo(HandleSet.HANDLE_SIZE_PX * 2, within(1e-9));
    }

    @Test
    public void handleAt_shouldHitWithinAGenerousReach() {
        SelectionManager selection = new SelectionManager();
        selection.setSelection(List.of(new Rectangle(0, 0, 10, 10)));
        List<HandleSet.Handle> handles = HandleSet.handles(selection, 1);
        HandleSet.Handle handle = handles.getFirst();
        Point2D near = new Point2D.Double(handle.center().getX() + handle.size() * 0.6, handle.center().getY());
        Point2D far = new Point2D.Double(handle.center().getX() + handle.size() * 2, handle.center().getY());

        assertThat(HandleSet.handleAt(handles, near)).contains(handle);
        assertThat(HandleSet.handleAt(handles, far)).isNotEqualTo(java.util.Optional.of(handle));
    }

    @Test
    public void isSameHandle_shouldMatchTheSameControlAcrossRecomputationsAndZoom() {
        SelectionManager selection = new SelectionManager();
        selection.setSelection(List.of(new Rectangle(0, 0, 10, 10)));
        List<HandleSet.Handle> before = HandleSet.handles(selection, 1);
        List<HandleSet.Handle> after = HandleSet.handles(selection, 2);

        for (int i = 0; i < before.size(); i++) {
            assertThat(before.get(i).isSameHandle(after.get(i))).isTrue();
        }
        assertThat(before.getFirst().isSameHandle(after.get(1))).isFalse();
    }

    @Test
    public void frame_shouldReturnTheFourCornersOfTheSelection() {
        SelectionManager selection = new SelectionManager();
        selection.setSelection(List.of(new Rectangle(10, 20, 30, 10)));

        List<Point2D> frame = HandleSet.frame(selection);

        assertThat(frame).hasSize(4);
        assertThat(frame.get(0).getX()).isCloseTo(10, within(1e-6));
        assertThat(frame.get(2).getX()).isCloseTo(40, within(1e-6));
        assertThat(frame.get(2).getY()).isCloseTo(30, within(1e-6));
    }

    @Test
    public void frame_shouldFollowTheRotationOfASingleSelectedItem() {
        Rectangle rectangle = new Rectangle(0, 0, 40, 10);
        rectangle.rotate(30);
        SelectionManager selection = new SelectionManager();
        selection.setSelection(List.of(rectangle));

        List<Point2D> frame = HandleSet.frame(selection);

        // The frame hugs the rotated rectangle: its corners are the rectangle's corners.
        List<Point2D> corners = corners(rectangle.getShape());
        assertThat(frame).hasSize(4);
        for (Point2D corner : frame) {
            assertThat(corners).anySatisfy(expected -> assertThat(corner.distance(expected)).isLessThan(1e-6));
        }
        assertThat(HandleSet.frameContains(selection, rectangle.getCenter())).isTrue();
        Rectangle2D bounds = rectangle.getBounds();
        assertThat(HandleSet.frameContains(selection, new Point2D.Double(bounds.getMinX() + 0.1, bounds.getMinY() + 0.1))).isFalse();
    }

    @Test
    public void handles_shouldBeRotatedWithASingleSelectedItem() {
        Rectangle rectangle = new Rectangle(0, 0, 40, 10);
        rectangle.rotate(90);
        SelectionManager selection = new SelectionManager();
        selection.setSelection(List.of(rectangle));
        Point2D center = rectangle.getCenter();

        List<HandleSet.Handle> handles = HandleSet.handles(selection, 1);

        // Rotated a quarter turn, the rotate handle lies beside the rectangle instead of above it.
        HandleSet.Handle rotate = handles.stream().filter(h -> h.kind() == HandleSet.Kind.ROTATE).findFirst().orElseThrow();
        assertThat(rotate.center().getY()).isCloseTo(center.getY(), within(1e-6));
        assertThat(Math.abs(rotate.center().getX() - center.getX()))
                .isCloseTo(5 + HandleSet.ROTATE_MARGIN_PX, within(1e-6));
    }

    @Test
    public void frame_shouldStayUnrotatedForSeveralSelectedItems() {
        Rectangle first = new Rectangle(0, 0, 10, 10);
        first.rotate(45);
        Rectangle second = new Rectangle(20, 0, 10, 10);
        second.rotate(45);
        SelectionManager selection = new SelectionManager();
        selection.setSelection(List.of(first, second));

        List<Point2D> frame = HandleSet.frame(selection);

        Rectangle2D bounds = selection.getShape().getBounds2D();
        assertThat(frame.get(0).getX()).isCloseTo(bounds.getMinX(), within(1e-6));
        assertThat(frame.get(0).getY()).isCloseTo(bounds.getMinY(), within(1e-6));
        assertThat(frame.get(2).getX()).isCloseTo(bounds.getMaxX(), within(1e-6));
        assertThat(frame.get(2).getY()).isCloseTo(bounds.getMaxY(), within(1e-6));
    }

    private static List<Point2D> corners(Shape shape) {
        List<Point2D> corners = new ArrayList<>();
        double[] coords = new double[6];
        for (PathIterator it = shape.getPathIterator(null); !it.isDone(); it.next()) {
            if (it.currentSegment(coords) != PathIterator.SEG_CLOSE) {
                corners.add(new Point2D.Double(coords[0], coords[1]));
            }
        }
        return corners;
    }
}

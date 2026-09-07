package com.willwinder.universalgcodesender.fx.component.designer.editor;

import com.willwinder.ugs.designer.entities.Anchor;
import com.willwinder.ugs.designer.entities.cuttable.Point;
import com.willwinder.ugs.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.designer.entities.selection.SelectionManager;
import org.junit.Test;

import java.awt.geom.Point2D;
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
        double worldPerPixel = 0.5;

        List<HandleSet.Handle> handles = HandleSet.handles(selection, worldPerPixel);

        assertThat(handles).hasSize(9);
        assertThat(handles.stream().filter(h -> h.kind() == HandleSet.Kind.ROTATE)).hasSize(1);
        double margin = HandleSet.RESIZE_MARGIN_PX * worldPerPixel;
        HandleSet.Handle topLeftAnchor = handles.stream().filter(h -> h.anchor() == Anchor.TOP_LEFT).findFirst().orElseThrow();
        // The handle sits opposite its anchor: at the right edge, below the bottom edge.
        assertThat(topLeftAnchor.center().getX()).isCloseTo(40 + margin, within(1e-6));
        assertThat(topLeftAnchor.center().getY()).isCloseTo(20 - margin, within(1e-6));
        assertThat(topLeftAnchor.size()).isCloseTo(HandleSet.HANDLE_SIZE_PX * worldPerPixel, within(1e-9));
        HandleSet.Handle rotate = handles.stream().filter(h -> h.kind() == HandleSet.Kind.ROTATE).findFirst().orElseThrow();
        assertThat(rotate.center().getX()).isCloseTo(25, within(1e-6));
        assertThat(rotate.center().getY()).isCloseTo(30 + HandleSet.ROTATE_MARGIN_PX * worldPerPixel, within(1e-6));
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
        HandleSet.Handle handle = handles.get(0);
        Point2D near = new Point2D.Double(handle.center().getX() + handle.size() * 0.6, handle.center().getY());
        Point2D far = new Point2D.Double(handle.center().getX() + handle.size() * 2, handle.center().getY());

        assertThat(HandleSet.handleAt(handles, near)).contains(handle);
        assertThat(HandleSet.handleAt(handles, far)).isNotEqualTo(java.util.Optional.of(handle));
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
}

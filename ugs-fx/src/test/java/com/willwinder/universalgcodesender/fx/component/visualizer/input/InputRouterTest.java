package com.willwinder.universalgcodesender.fx.component.visualizer.input;

import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Ray;
import javafx.geometry.Point3D;
import javafx.scene.Cursor;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class InputRouterTest {
    private final List<Cursor> cursors = new ArrayList<>();
    private InputRouter router;

    @Before
    public void setUp() {
        router = new InputRouter(cursors::add);
    }

    @Test
    public void pressed_shouldGiveTheGestureToTheFirstConsumingHandler() {
        RecordingHandler ignoring = new RecordingHandler(false);
        RecordingHandler first = new RecordingHandler(true);
        RecordingHandler second = new RecordingHandler(true);
        router.addHandler(ignoring);
        router.addHandler(first);
        router.addHandler(second);

        router.pressed(pointer(MouseEvent.MOUSE_PRESSED, 10, 10));
        router.dragged(pointer(MouseEvent.MOUSE_DRAGGED, 20, 10));
        router.released(pointer(MouseEvent.MOUSE_RELEASED, 30, 10));

        assertThat(ignoring.events).containsExactly("pressed");
        assertThat(first.events).containsExactly("pressed", "dragged", "released");
        assertThat(second.events).isEmpty();
        assertThat(router.isDragging()).isFalse();
    }

    @Test
    public void dragged_shouldPassTheStartingEventToTheOwner() {
        RecordingHandler handler = new RecordingHandler(true);
        router.addHandler(handler);
        PointerEvent start = pointer(MouseEvent.MOUSE_PRESSED, 10, 10);

        router.pressed(start);
        router.dragged(pointer(MouseEvent.MOUSE_DRAGGED, 20, 10));

        assertThat(handler.lastPressed).isSameAs(start);
        assertThat(router.isDragging()).isTrue();
    }

    @Test
    public void dragged_shouldDoNothingWhenNobodyOwnsTheGesture() {
        RecordingHandler handler = new RecordingHandler(false);
        router.addHandler(handler);

        router.pressed(pointer(MouseEvent.MOUSE_PRESSED, 10, 10));
        router.dragged(pointer(MouseEvent.MOUSE_DRAGGED, 20, 10));
        router.released(pointer(MouseEvent.MOUSE_RELEASED, 30, 10));

        assertThat(handler.events).containsExactly("pressed");
    }

    @Test
    public void moved_shouldStopAtTheFirstConsumerAndApplyTheFirstCursor() {
        RecordingHandler noCursor = new RecordingHandler(false);
        RecordingHandler hand = new RecordingHandler(false);
        hand.cursor = Cursor.HAND;
        hand.consumeMove = true;
        RecordingHandler crosshair = new RecordingHandler(false);
        crosshair.cursor = Cursor.CROSSHAIR;
        router.addHandler(noCursor);
        router.addHandler(hand);
        router.addHandler(crosshair);

        router.moved(pointer(MouseEvent.MOUSE_MOVED, 5, 5));

        assertThat(noCursor.events).containsExactly("moved");
        assertThat(hand.events).containsExactly("moved");
        assertThat(crosshair.events).isEmpty();
        assertThat(cursors).containsExactly(Cursor.HAND);
    }

    @Test
    public void moved_shouldFallBackToTheDefaultCursor() {
        router.addHandler(new RecordingHandler(false));

        router.moved(pointer(MouseEvent.MOUSE_MOVED, 5, 5));

        assertThat(cursors).containsExactly(Cursor.DEFAULT);
    }

    @Test
    public void scrolled_shouldReturnWhetherAnyHandlerConsumed() {
        RecordingHandler handler = new RecordingHandler(false);
        router.addHandler(handler);
        ScrollInput scroll = new ScrollInput(1, 2, 0, 40, false, false, false, false);

        assertThat(router.scrolled(scroll)).isFalse();
        handler.consumeScroll = true;
        assertThat(router.scrolled(scroll)).isTrue();
    }

    @Test
    public void removeHandler_shouldEndItsGesture() {
        RecordingHandler handler = new RecordingHandler(true);
        router.addHandler(handler);
        router.pressed(pointer(MouseEvent.MOUSE_PRESSED, 10, 10));

        router.removeHandler(handler);
        router.dragged(pointer(MouseEvent.MOUSE_DRAGGED, 20, 10));

        assertThat(router.isDragging()).isFalse();
        assertThat(handler.events).containsExactly("pressed");
    }

    @Test
    public void addHandlerFirst_shouldBeAskedBeforeTheOthers() {
        RecordingHandler later = new RecordingHandler(true);
        RecordingHandler first = new RecordingHandler(true);
        router.addHandler(later);
        router.addHandlerFirst(first);

        router.pressed(pointer(MouseEvent.MOUSE_PRESSED, 10, 10));

        assertThat(first.events).containsExactly("pressed");
        assertThat(later.events).isEmpty();
    }

    static PointerEvent pointer(javafx.event.EventType<MouseEvent> type, double x, double y) {
        MouseEvent mouse = new MouseEvent(type, x, y, x, y, MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false, false, null);
        Ray ray = new Ray(new Point3D(x, y, 100), new Point3D(0, 0, -1));
        return new PointerEvent(mouse, ray, Optional.of(new Point3D(x, y, 0)));
    }

    private static final class RecordingHandler implements InputHandler {
        private final List<String> events = new ArrayList<>();
        private final boolean consumePress;
        private boolean consumeMove;
        private boolean consumeScroll;
        private Cursor cursor;
        private PointerEvent lastPressed;

        private RecordingHandler(boolean consumePress) {
            this.consumePress = consumePress;
        }

        @Override
        public boolean onPressed(PointerEvent event) {
            events.add("pressed");
            return consumePress;
        }

        @Override
        public void onDragged(PointerEvent event, PointerEvent pressed) {
            events.add("dragged");
            lastPressed = pressed;
        }

        @Override
        public void onReleased(PointerEvent event, PointerEvent pressed) {
            events.add("released");
        }

        @Override
        public boolean onMoved(PointerEvent event) {
            events.add("moved");
            return consumeMove;
        }

        @Override
        public boolean onScroll(ScrollInput scroll) {
            return consumeScroll;
        }

        @Override
        public Optional<Cursor> cursorAt(PointerEvent event) {
            return Optional.ofNullable(cursor);
        }
    }
}

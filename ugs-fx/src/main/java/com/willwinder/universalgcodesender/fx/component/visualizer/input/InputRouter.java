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
package com.willwinder.universalgcodesender.fx.component.visualizer.input;

import javafx.scene.Cursor;
import javafx.scene.input.KeyEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Dispatches the visualizer's input to an ordered stack of {@link InputHandler}s. The first
 * handler to consume an event wins; a consumed press makes its handler the owner of the gesture
 * until the release.
 */
public final class InputRouter {
    private final List<InputHandler> handlers = new ArrayList<>();
    private final Consumer<Cursor> cursorConsumer;
    private InputHandler gestureOwner;
    private PointerEvent pressed;

    /**
     * @param cursorConsumer receives the cursor to show whenever the pointer moves or a gesture ends
     */
    public InputRouter(Consumer<Cursor> cursorConsumer) {
        this.cursorConsumer = cursorConsumer;
    }

    /**
     * Adds a handler after the existing ones, so it is asked last.
     */
    public void addHandler(InputHandler handler) {
        handlers.add(handler);
    }

    /**
     * Adds a handler before the existing ones, so it is asked first.
     */
    public void addHandlerFirst(InputHandler handler) {
        handlers.add(0, handler);
    }

    public void removeHandler(InputHandler handler) {
        handlers.remove(handler);
        if (gestureOwner == handler) {
            gestureOwner = null;
            pressed = null;
        }
    }

    public List<InputHandler> handlers() {
        return Collections.unmodifiableList(handlers);
    }

    public boolean isDragging() {
        return gestureOwner != null;
    }

    public void pressed(PointerEvent event) {
        gestureOwner = null;
        pressed = event;
        for (InputHandler handler : handlers) {
            if (handler.onPressed(event)) {
                gestureOwner = handler;
                return;
            }
        }
    }

    public void dragged(PointerEvent event) {
        if (gestureOwner != null) {
            gestureOwner.onDragged(event, pressed);
        }
    }

    public void released(PointerEvent event) {
        InputHandler owner = gestureOwner;
        PointerEvent start = pressed;
        gestureOwner = null;
        pressed = null;
        if (owner != null) {
            owner.onReleased(event, start);
        }
        updateCursor(event);
    }

    public void moved(PointerEvent event) {
        for (InputHandler handler : handlers) {
            if (handler.onMoved(event)) {
                break;
            }
        }
        updateCursor(event);
    }

    public boolean scrolled(ScrollInput scroll) {
        for (InputHandler handler : handlers) {
            if (handler.onScroll(scroll)) {
                return true;
            }
        }
        return false;
    }

    public boolean contextMenu(PointerEvent event) {
        for (InputHandler handler : handlers) {
            if (handler.onContextMenu(event)) {
                return true;
            }
        }
        return false;
    }

    public boolean keyPressed(KeyEvent event) {
        for (InputHandler handler : handlers) {
            if (handler.onKeyPressed(event)) {
                return true;
            }
        }
        return false;
    }

    public boolean keyReleased(KeyEvent event) {
        for (InputHandler handler : handlers) {
            if (handler.onKeyReleased(event)) {
                return true;
            }
        }
        return false;
    }

    private void updateCursor(PointerEvent event) {
        Cursor cursor = handlers.stream()
                .map(handler -> handler.cursorAt(event))
                .flatMap(Optional::stream)
                .findFirst()
                .orElse(Cursor.DEFAULT);
        cursorConsumer.accept(cursor);
    }
}

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
package com.willwinder.universalgcodesender.fx.actions;

import com.willwinder.ugs.designer.actions.AbstractDesignAction;
import com.willwinder.ugs.designer.entities.entities.selection.SelectionEvent;
import com.willwinder.ugs.designer.entities.entities.selection.SelectionListener;

/**
 * Base class for the design operation actions, both the alignment actions (align left, top, ...)
 * and the geometric operations (union, subtract, group, ...). Each of them reuses the existing
 * Swing based {@link AbstractDesignAction} that already implements the operation against the
 * designer model.
 * <p>
 * The wrapped action mutates the drawing directly without opening a dialog, so — like the other
 * context aware edit actions (see {@link AbstractDesignEditAction}) — it is dispatched on the
 * JavaFX thread that owns the designer model in the FX visualizer.
 * <p>
 * The enabled state is delegated to the wrapped action, which already knows the selection rules
 * for the operation (alignment needs more than one selected entity, a union needs at least two,
 * a break apart needs a single compound path, ...). Each selection change is forwarded to the
 * delegate so its enabled state is recomputed before it is mirrored onto the JavaFX
 * {@link #enabledProperty()}.
 */
public abstract class AbstractDesignOperationAction extends AbstractDesignEditAction implements SelectionListener {

    private final transient AbstractDesignAction delegate;

    protected AbstractDesignOperationAction(AbstractDesignAction delegate, String title, String icon) {
        super(title, icon);
        this.delegate = delegate;
        controller.getSelectionManager().addSelectionListener(this);
        enabledProperty().set(delegate.isEnabled());
    }

    @Override
    protected void performAction() {
        delegate.actionPerformed(null);
    }

    @Override
    public void onSelectionEvent(SelectionEvent selectionEvent) {
        if (delegate instanceof SelectionListener selectionListener) {
            selectionListener.onSelectionEvent(selectionEvent);
        }
        setEnabledLater(delegate.isEnabled());
    }
}

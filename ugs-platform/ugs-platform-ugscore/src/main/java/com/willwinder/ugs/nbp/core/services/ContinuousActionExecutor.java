/*
    Copyright 2023 Will Winder

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
package com.willwinder.ugs.nbp.core.services;

import com.willwinder.ugs.nbp.core.actions.ContinuousAction;
import com.willwinder.ugs.nbp.lib.services.ActionReference;
import com.willwinder.universalgcodesender.listeners.LongPressKeyListener;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Joacim Breiler
 */
public class ContinuousActionExecutor extends LongPressKeyListener {

    private final AtomicReference<ActionReference> actionReference = new AtomicReference<>();
    private boolean isLongPressed = false;

    /**
     * Constructor for creating a long press key listener
     *
     * @param longPressDelay time in milliseconds before a key press will be
     *                       considered a long press
     */
    public ContinuousActionExecutor(long longPressDelay) {
        super(longPressDelay);
    }

    public ActionReference getCurrentAction() {
        return this.actionReference.get();
    }

    public void setCurrentAction(ActionReference actionReference) {
        // Release any previous key press
        release();

        this.actionReference.set(actionReference);
    }

    @Override
    protected void onKeyPressed(KeyEvent keyEvent) {
    }

    @Override
    protected void onKeyRelease(KeyEvent keyEvent) {
        if (actionReference.get() == null) {
            return;
        }

        actionReference.get().getAction().actionPerformed(new ActionEvent(keyEvent.getSource(), keyEvent.getID(), null));
        actionReference.set(null);
        isLongPressed = false;
    }

    @Override
    protected void onKeyLongPressed(KeyEvent keyEvent) {
        if (actionReference.get() == null) {
            return;
        }

        isLongPressed = true;
        ((ContinuousAction) actionReference.get().getAction()).actionActivate();
    }

    @Override
    protected void onKeyLongRelease(KeyEvent e) {
        release();
    }


    public void release() {
        if (actionReference.get() == null || !isLongPressed) {
            return;
        }

        ((ContinuousAction) actionReference.get().getAction()).actionDeactivated();
        actionReference.set(null);
    }
}

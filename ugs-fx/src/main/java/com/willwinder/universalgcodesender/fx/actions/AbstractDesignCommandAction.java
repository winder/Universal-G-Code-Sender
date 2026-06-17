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

import com.willwinder.universalgcodesender.i18n.Localization;
import javafx.event.ActionEvent;

import javax.swing.SwingUtilities;

/**
 * Base class for design toolbar commands that reuse an existing Swing based design action that
 * opens a modal Swing dialog (such as importing a file or inserting clipart). Unlike the other
 * designer actions, which mutate the model on the JavaFX thread (see {@link AbstractDesignEditAction}
 * and {@link AbstractDesignToolAction}), the wrapped action here must be dispatched on the Swing
 * event dispatch thread because that is where its dialog has to run. The model mutation that follows
 * the dialog is serialized behind the modal dialog, so it does not overlap the FX-thread gestures.
 */
public abstract class AbstractDesignCommandAction extends BaseAction {

    private final transient javax.swing.Action delegate;

    protected AbstractDesignCommandAction(javax.swing.Action delegate, String title, String icon) {
        super(title, title, Localization.getString("actions.category.designer"), icon);
        this.delegate = delegate;
    }

    @Override
    public void handleAction(ActionEvent event) {
        SwingUtilities.invokeLater(() -> delegate.actionPerformed(null));
    }
}

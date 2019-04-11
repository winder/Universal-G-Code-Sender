/*
    Copyright 2016-2019 Will Winder

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

import com.willwinder.universalgcodesender.model.Overrides;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Defines an action for an override command
 *
 * @author wwinder
 */
public class OverrideAction extends AbstractAction {
    private final OverrideActionService overrideActionService;
    private final Overrides action;

    /**
     * Constructor
     *
     * @param overrideActionService the service for running the action
     * @param action                the action to execute
     */
    public OverrideAction(OverrideActionService overrideActionService, Overrides action) {
        this.overrideActionService = overrideActionService;
        this.action = action;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        overrideActionService.runAction(action);
    }

    @Override
    public boolean isEnabled() {
        return overrideActionService.canRunAction();
    }
}

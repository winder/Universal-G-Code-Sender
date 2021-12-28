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
import org.openide.util.Lookup;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.io.Serializable;

/**
 * Defines an action for an override command
 *
 * @author wwinder
 */
public class OverrideAction extends AbstractAction implements Serializable {
    private OverrideActionService overrideActionService;
    private Overrides action;

    /**
     * Empty constructor to be used for serialization
     */
    public OverrideAction() {
    }

    /**
     * Constructor
     *
     * @param action the action to execute
     */
    public OverrideAction(Overrides action) {
        this.action = action;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        getOverrideActionService().runAction(action);
    }

    private OverrideActionService getOverrideActionService() {
        if (overrideActionService == null) {
            overrideActionService = Lookup.getDefault().lookup(OverrideActionService.class);
        }
        return overrideActionService;
    }

    @Override
    public boolean isEnabled() {
        return getOverrideActionService() != null && getOverrideActionService().canRunAction();
    }
}

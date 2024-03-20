/*
    Copyright 2024 Will Winder

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
package com.willwinder.ugs.nbp.joystick.action;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.OverrideType;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.firmware.IOverrideManager;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Makes it possible to do an analog override
 *
 * @author Joacim Breiler
 */
public abstract class AnalogOverrideAction extends AbstractAction implements AnalogAction, UGSEventListener {
    private final transient BackendAPI backend;
    private final OverrideType overrideType;
    private float value;

    protected AnalogOverrideAction(OverrideType overrideType) {
        this.overrideType = overrideType;
        setEnabled(false);

        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        backend.addUGSEventListener(this);
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt instanceof ControllerStateEvent controllerStateEvent) {
            ControllerState state = controllerStateEvent.getState();
            setEnabled(state == ControllerState.IDLE || state == ControllerState.RUN);
        }
    }

    @Override
    public void setValue(float value) {
        this.value = value;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isEnabled() || backend.getController() == null || backend.getController().getOverrideManager() == null) {
            return;
        }

        IOverrideManager overrideManager = backend.getController().getOverrideManager();
        overrideManager.setSliderTarget(overrideType, Math.round(value * overrideManager.getSliderMax(overrideType) + overrideManager.getSliderDefault(overrideType)));
    }
}

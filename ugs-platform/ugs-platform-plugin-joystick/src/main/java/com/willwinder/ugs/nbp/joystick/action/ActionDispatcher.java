/*
    Copyright 2020 Will Winder

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

import com.willwinder.ugs.nbp.joystick.model.JoystickControl;
import com.willwinder.ugs.nbp.joystick.model.JoystickState;
import com.willwinder.ugs.nbp.joystick.service.JoystickServiceListener;
import com.willwinder.ugs.nbp.lib.services.ActionReference;
import com.willwinder.universalgcodesender.utils.ContinuousJogWorker;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;

/**
 * A jog service that binds to a joystick handling continuous jogging and it's jogging speed.
 */
public class ActionDispatcher implements JoystickServiceListener {
    private final ContinuousJogWorker continuousJogWorker;
    private final ActionManager actionManager;

    public ActionDispatcher(ActionManager actionManager, ContinuousJogWorker continuousJogWorker) {
        this.continuousJogWorker = continuousJogWorker;
        this.actionManager = actionManager;
    }

    @Override
    public void onUpdate(JoystickState state) {
        Arrays.stream(JoystickControl.values()).forEach(joystickControl ->
                actionManager.getMappedAction(joystickControl).ifPresent(actionReference ->
                        updateControlState(state, joystickControl, actionReference)));

        continuousJogWorker.update();
    }

    private void updateControlState(JoystickState state, JoystickControl joystickControl, ActionReference actionReference) {
        Action action = actionReference.getAction();
        if (action.isEnabled()) {
            if (action instanceof AnalogAction) {
                AnalogAction analogAction = ((AnalogAction) action);
                analogAction.setValue(state.getAxis(joystickControl));
                action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, joystickControl.name()));
            } else if (state.getButton(joystickControl)) {
                action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, joystickControl.name()));
            }
        }
    }
}

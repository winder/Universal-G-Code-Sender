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
package com.willwinder.ugs.nbp.joystick.service;

import com.willwinder.ugs.nbp.joystick.model.JoystickAxis;
import com.willwinder.ugs.nbp.joystick.model.JoystickButton;
import com.willwinder.ugs.nbp.joystick.model.JoystickState;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.services.JogService;
import com.willwinder.universalgcodesender.utils.ContinuousJogWorker;

/**
 * A jog service that binds to a joystick handling continuous jogging and it's jogging speed.
 */
public class JoystickJogService implements JoystickServiceListener {
    private final JogService jogService;
    private final BackendAPI backendAPI;
    private final ContinuousJogWorker continousJogWorker;

    public JoystickJogService() {
        jogService = CentralLookup.getDefault().lookup(JogService.class);
        backendAPI = CentralLookup.getDefault().lookup(BackendAPI.class);

        continousJogWorker = new ContinuousJogWorker(backendAPI, jogService);
    }

    @Override
    public void onUpdate(JoystickState state) {
        if (state.getButton(JoystickButton.START)) {
            sendFile();
        } else if (state.getButton(JoystickButton.BACK)) {
            cancelSend();
        } else if (state.getButton(JoystickButton.LEFT_BUMPER)) {
            jogService.divideFeedRate();
        } else if (state.getButton(JoystickButton.RIGHT_BUMPER)) {
            jogService.multiplyFeedRate();
        } else if (jogService.canJog() && backendAPI.getController().getCapabilities().hasContinuousJogging()) {
            float x = readValue(state, JoystickAxis.LEFT_X, JoystickButton.DPAD_LEFT, JoystickButton.DPAD_RIGHT);
            float y = readValue(state, JoystickAxis.LEFT_Y, JoystickButton.DPAD_DOWN, JoystickButton.DPAD_UP);
            float z = readValue(state, JoystickAxis.RIGHT_Y, JoystickButton.A, JoystickButton.Y);
            continousJogWorker.setDirection(x, y, z);

            if (x != 0 || y != 0 || z != 0) {
                continousJogWorker.start();
            } else {
                continousJogWorker.stop();
            }
        }
    }

    private void cancelSend() {
        try {
            if (backendAPI.canCancel()) {
                backendAPI.cancel();
            }
        } catch (Exception e) {
            // Never mind
        }
    }

    private void sendFile() {
        try {
            if (backendAPI.canSend()) {
                backendAPI.send();
            }
        } catch (Exception e) {
            // Never mind
        }
    }

    /**
     * Reads a joystick value
     *
     * @param state
     * @param axis
     * @param negativeButton
     * @param positiveButton
     * @return
     */
    private float readValue(JoystickState state, JoystickAxis axis, JoystickButton negativeButton, JoystickButton positiveButton) {
        // Start reading the analog value
        float result = state.getAxis(axis);

        // Then read the digital value
        if (state.getButton(negativeButton)) {
            result = -1;
        } else if (state.getButton(positiveButton)) {
            result = 1;
        }

        return result;
    }
}

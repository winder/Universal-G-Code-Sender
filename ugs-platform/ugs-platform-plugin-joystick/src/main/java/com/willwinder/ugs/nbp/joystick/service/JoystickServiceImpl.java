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

import com.studiohartman.jamepad.ControllerAxis;
import com.studiohartman.jamepad.ControllerButton;
import com.studiohartman.jamepad.ControllerIndex;
import com.studiohartman.jamepad.ControllerManager;
import com.studiohartman.jamepad.ControllerUnpluggedException;
import com.willwinder.ugs.nbp.joystick.Utils;
import com.willwinder.ugs.nbp.joystick.model.JoystickAxis;
import com.willwinder.ugs.nbp.joystick.model.JoystickButton;
import com.willwinder.ugs.nbp.joystick.model.JoystickState;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JoystickServiceImpl implements JoystickService {
    /**
     * Milliseconds to wait between reading joystick/gamepad values
     */
    private static final int READ_DELAY_MILLISECONDS = 10;
    private static final Logger LOGGER = Logger.getLogger(JoystickServiceImpl.class.getSimpleName());

    private final ControllerManager controllerManager;
    private final JoystickState joystickState;
    private final ExecutorService joystickReadThread;

    private ControllerIndex currentController;
    private Set<JoystickServiceListener> listeners;
    private boolean isRunning;

    public JoystickServiceImpl() {
        joystickReadThread = Executors.newSingleThreadExecutor();
        controllerManager = new ControllerManager();
        joystickState = new JoystickState();
        listeners = new HashSet<>();

        JoystickJogService joystickJogService = new JoystickJogService();
        addListener(joystickJogService);
    }

    @Override
    public void initialize() {
        if (isRunning) {
            return;
        }
        controllerManager.initSDLGamepad();
        joystickReadThread.execute(this::mainLoop);
    }

    @Override
    public void destroy() {
        isRunning = false;
        if (controllerManager != null) {
            controllerManager.quitSDLGamepad();
        }
    }

    @Override
    public void addListener(JoystickServiceListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(JoystickServiceListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void removeAllListeners() {
        listeners.clear();
    }

    private void mainLoop() {
        isRunning = true;
        while (isRunning) {
            controllerManager.update();
            if (controllerManager.getNumControllers() > 0) {
                readDataLoop();
            }

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                // Never mind this
            }
        }
    }

    private void readDataLoop() {
        currentController = controllerManager.getControllerIndex(0);
        try {
            joystickState.setName(currentController.getName());
            while (isRunning && currentController.isConnected()) {
                readData();
                Thread.sleep(READ_DELAY_MILLISECONDS);
            }
        } catch (ControllerUnpluggedException | InterruptedException e) {
            LOGGER.log(Level.WARNING, "Controller unplugged or interrupted", e);
        }
    }

    private void readData() {
        joystickState.setDirty(false);
        Arrays.asList(ControllerButton.values()).forEach(this::updateJoystickButtonState);
        Arrays.asList(ControllerAxis.values()).forEach(this::updateJoystickAxisState);

        if (joystickState.isDirty()) {
            notifyListeners();
        }
    }

    private void notifyListeners() {
        listeners.forEach(listener -> listener.onUpdate(joystickState));
    }

    private void updateJoystickAxisState(ControllerAxis controllerAxis) {
        try {
            // Round values, got issues with the controller having 0.01 as zero-value
            float value = Math.round(currentController.getAxisState(controllerAxis) * 10) / 10f;
            JoystickAxis axis = Utils.getJoystickAxisFromControllerAxis(controllerAxis);
            joystickState.setAxis(axis, value);
        } catch (ControllerUnpluggedException e) {
            throw new JoystickException("Couldn't read value from joystick axis", e);
        }
    }

    private void updateJoystickButtonState(ControllerButton controllerButton) {
        try {
            boolean value = currentController.isButtonPressed(controllerButton);
            JoystickButton button = Utils.getJoystickButtonFromControllerButton(controllerButton);
            joystickState.setButton(button, value);
        } catch (ControllerUnpluggedException e) {
            throw new JoystickException("Couldn't read value from joystick button", e);
        }
    }
}

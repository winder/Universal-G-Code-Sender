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
import com.willwinder.ugs.nbp.joystick.Settings;
import com.willwinder.ugs.nbp.joystick.Utils;
import com.willwinder.ugs.nbp.joystick.action.ActionDispatcher;
import com.willwinder.ugs.nbp.joystick.action.ActionManager;
import com.willwinder.ugs.nbp.joystick.action.AnalogJogAction;
import com.willwinder.ugs.nbp.joystick.model.JoystickControl;
import com.willwinder.ugs.nbp.joystick.model.JoystickState;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.services.JogService;
import com.willwinder.universalgcodesender.utils.ContinuousJogWorker;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.willwinder.ugs.nbp.joystick.Utils.ACTION_DIVIDE_FEED;
import static com.willwinder.ugs.nbp.joystick.Utils.ACTION_JOG_X;
import static com.willwinder.ugs.nbp.joystick.Utils.ACTION_JOG_X_MINUS;
import static com.willwinder.ugs.nbp.joystick.Utils.ACTION_JOG_X_PLUS;
import static com.willwinder.ugs.nbp.joystick.Utils.ACTION_JOG_Y;
import static com.willwinder.ugs.nbp.joystick.Utils.ACTION_JOG_Y_MINUS;
import static com.willwinder.ugs.nbp.joystick.Utils.ACTION_JOG_Y_PLUS;
import static com.willwinder.ugs.nbp.joystick.Utils.ACTION_JOG_Z;
import static com.willwinder.ugs.nbp.joystick.Utils.ACTION_MULTIPLY_FEED;
import static com.willwinder.ugs.nbp.joystick.Utils.ACTION_START;
import static com.willwinder.ugs.nbp.joystick.Utils.ACTION_STOP;
import static com.willwinder.ugs.nbp.joystick.Utils.ACTION_Z_DOWN;
import static com.willwinder.ugs.nbp.joystick.Utils.ACTION_Z_UP;

/**
 * A joystick service ties action managers, game controllers and event threads together.
 *
 * @author Joacim Breiler
 */
public class JoystickServiceImpl implements JoystickService {

    private static final Logger LOGGER = Logger.getLogger(JoystickServiceImpl.class.getSimpleName());

    /**
     * Milliseconds to wait between reading joystick/gamepad values
     */
    private static final int READ_DELAY_MILLISECONDS = 1;

    /**
     * A version number for the settings so that we can handle version changes
     */
    private static final int SETTINGS_VERSION = 1;
    private static final int MAX_NUM_CONTROLLERS = 4;

    private final ControllerManager controllerManager;
    private final JoystickState joystickState;
    private final ExecutorService joystickReadThread;
    private final ActionDispatcher joystickActionDispatcher;
    private final ActionManager actionManager;

    private ControllerIndex currentController;
    private Set<JoystickServiceListener> listeners;
    private boolean isRunning;
    private boolean isActionDispatcherActive = true;

    public JoystickServiceImpl() {
        joystickReadThread = Executors.newSingleThreadExecutor();
        controllerManager = new ControllerManager(MAX_NUM_CONTROLLERS, "/com/willwinder/ugs/nbp/joystick/gamecontrollerdb.txt");
        joystickState = new JoystickState();
        listeners = new HashSet<>();

        JogService jogService = CentralLookup.getDefault().lookup(JogService.class);
        BackendAPI backendAPI = CentralLookup.getDefault().lookup(BackendAPI.class);
        ContinuousJogWorker continuousJogWorker = new ContinuousJogWorker(backendAPI, jogService);

        actionManager = new ActionManager();
        actionManager.registerAction("continuousJogXAction", "Actions/Machine", new AnalogJogAction(continuousJogWorker, Axis.X));
        actionManager.registerAction("continuousJogYAction", "Actions/Machine", new AnalogJogAction(continuousJogWorker, Axis.Y));
        actionManager.registerAction("continuousJogZAction", "Actions/Machine", new AnalogJogAction(continuousJogWorker, Axis.Z));

        joystickActionDispatcher = new ActionDispatcher(actionManager, continuousJogWorker);
        addListener(joystickActionDispatcher);

        if (!hasSettingsBeenInitialized()) {
            initDefaultSettings();
        }
    }

    /**
     * Checks the settings version if they have been initialized
     *
     * @return true if settings has been initialized.
     */
    private boolean hasSettingsBeenInitialized() {
        return Settings.getVersion() > 0;
    }

    private void initDefaultSettings() {
        actionManager.getActionById(ACTION_Z_DOWN).ifPresent(actionReference -> actionManager.setMappedAction(JoystickControl.A, actionReference));
        actionManager.getActionById(ACTION_Z_UP).ifPresent(actionReference -> actionManager.setMappedAction(JoystickControl.Y, actionReference));
        actionManager.getActionById(ACTION_JOG_Z).ifPresent(actionReference -> actionManager.setMappedAction(JoystickControl.RIGHT_Y, actionReference));
        actionManager.getActionById(ACTION_JOG_X).ifPresent(actionReference -> actionManager.setMappedAction(JoystickControl.LEFT_X, actionReference));
        actionManager.getActionById(ACTION_JOG_Y).ifPresent(actionReference -> actionManager.setMappedAction(JoystickControl.LEFT_Y, actionReference));
        actionManager.getActionById(ACTION_DIVIDE_FEED).ifPresent(actionReference -> actionManager.setMappedAction(JoystickControl.L1, actionReference));
        actionManager.getActionById(ACTION_MULTIPLY_FEED).ifPresent(actionReference -> actionManager.setMappedAction(JoystickControl.R1, actionReference));
        actionManager.getActionById(ACTION_START).ifPresent(actionReference -> actionManager.setMappedAction(JoystickControl.START, actionReference));
        actionManager.getActionById(ACTION_JOG_Y_PLUS).ifPresent(actionReference -> actionManager.setMappedAction(JoystickControl.DPAD_UP, actionReference));
        actionManager.getActionById(ACTION_JOG_X_PLUS).ifPresent(actionReference -> actionManager.setMappedAction(JoystickControl.DPAD_RIGHT, actionReference));
        actionManager.getActionById(ACTION_JOG_X_MINUS).ifPresent(actionReference -> actionManager.setMappedAction(JoystickControl.DPAD_LEFT, actionReference));
        actionManager.getActionById(ACTION_JOG_Y_MINUS).ifPresent(actionReference -> actionManager.setMappedAction(JoystickControl.DPAD_DOWN, actionReference));
        actionManager.getActionById(ACTION_STOP).ifPresent(actionReference -> actionManager.setMappedAction(JoystickControl.BACK, actionReference));
        Settings.setVersion(SETTINGS_VERSION);
    }

    @Override
    public void initialize() {
        if (isRunning) {
            return;
        }

        controllerManager.initSDLGamepad();
        int numControllers = controllerManager.getNumControllers();
        if (numControllers > 0) {
            try {
                LOGGER.info(String.format("Found %d gamepad controllers, will use the first one with the name \"%s\"", numControllers, controllerManager.getControllerIndex(0).getName()));
            } catch (ControllerUnpluggedException e) {
                LOGGER.severe("Couldn't get the name of the first gamepad controller");
            }
        } else {
            LOGGER.info("Couldn't find any gamepad controllers");
        }

        joystickReadThread.execute(this::mainLoop);
    }

    @Override
    public void destroy() {
        isRunning = false;
        try {
            if (controllerManager != null && controllerManager.getNumControllers() > 0) {
                controllerManager.quitSDLGamepad();
            }
        } catch (IllegalStateException e) {
            LOGGER.fine("Couldn't release the joystick manager: " + e.getMessage());
        }
    }

    @Override
    public void setActivateActionDispatcher(boolean isActionDispatcherActive) {
        this.isActionDispatcherActive = isActionDispatcherActive;
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

    @Override
    public ActionManager getActionManager() {
        return actionManager;
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
            while (isRunning && currentController.isConnected()) {
                readData();
                Thread.sleep(READ_DELAY_MILLISECONDS);
            }
        } catch (InterruptedException e) {
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
        listeners.forEach(listener -> {
            if (listener == joystickActionDispatcher && !isActionDispatcherActive) {
                return;
            }

            listener.onUpdate(joystickState);
        });
    }

    private void updateJoystickAxisState(ControllerAxis controllerAxis) {
        try {
            // We might have rounding errors from the controller, ignore the low value range
            float value = currentController.getAxisState(controllerAxis);
            float axisThreshold = Settings.getAxisThreshold();
            if (value < axisThreshold && value > -axisThreshold) {
                value = 0;
            }

            JoystickControl axis = Utils.getJoystickAxisFromControllerAxis(controllerAxis);
            boolean reverseAxis = Settings.isReverseAxis(axis);
            joystickState.setAxis(axis, reverseAxis ? -value : value);
        } catch (ControllerUnpluggedException e) {
            throw new JoystickException("Couldn't read value from joystick axis", e);
        }
    }

    private void updateJoystickButtonState(ControllerButton controllerButton) {
        try {
            boolean value = currentController.isButtonPressed(controllerButton);
            JoystickControl button = Utils.getJoystickButtonFromControllerButton(controllerButton);
            joystickState.setButton(button, value);
        } catch (ControllerUnpluggedException e) {
            throw new JoystickException("Couldn't read value from joystick button", e);
        }
    }
}

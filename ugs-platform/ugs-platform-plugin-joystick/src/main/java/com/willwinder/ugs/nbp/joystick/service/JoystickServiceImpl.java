/*
    Copyright 2020-2024 Will Winder

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

import com.willwinder.ugs.nbp.joystick.Settings;
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
import com.willwinder.ugs.nbp.joystick.action.ActionDispatcher;
import com.willwinder.ugs.nbp.joystick.action.ActionManager;
import com.willwinder.ugs.nbp.joystick.action.AnalogFeedOverrideAction;
import com.willwinder.ugs.nbp.joystick.action.AnalogJogAction;
import com.willwinder.ugs.nbp.joystick.action.AnalogSpindleOverrideAction;
import com.willwinder.ugs.nbp.joystick.driver.JamepadJoystickDriver;
import com.willwinder.ugs.nbp.joystick.driver.JoystickDriver;
import com.willwinder.ugs.nbp.joystick.driver.JoystickDriverListener;
import com.willwinder.ugs.nbp.joystick.model.JoystickControl;
import com.willwinder.ugs.nbp.joystick.model.JoystickDevice;
import com.willwinder.ugs.nbp.joystick.model.JoystickState;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.services.JogService;
import com.willwinder.universalgcodesender.utils.ContinuousJogWorker;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A joystick service ties action managers, game controllers and event threads together.
 *
 * @author Joacim Breiler
 */
public class JoystickServiceImpl implements JoystickService, JoystickDriverListener {
    public static final String CONTINUOUS_JOG_ACTION_CATEGORY = "Actions/Machine";
    public static final String CONTINUOUS_OVERRIDE_ACTION_CATEGORY = "Actions/Overrides";

    private static final Logger LOGGER = Logger.getLogger(JoystickServiceImpl.class.getSimpleName());

    /**
     * A version number for the settings so that we can handle version changes
     */
    private static final int SETTINGS_VERSION = 1;
    private final ActionDispatcher joystickActionDispatcher;
    private final ActionManager actionManager;
    private final Set<JoystickServiceListener> listeners;
    private final JoystickDriver driver;
    private boolean isActionDispatcherActive = true;


    public JoystickServiceImpl() {
        driver = new JamepadJoystickDriver();
        driver.addListener(this);
        listeners = new HashSet<>();

        JogService jogService = CentralLookup.getDefault().lookup(JogService.class);
        BackendAPI backendAPI = CentralLookup.getDefault().lookup(BackendAPI.class);
        ContinuousJogWorker continuousJogWorker = new ContinuousJogWorker(backendAPI, jogService);

        actionManager = new ActionManager();
        actionManager.registerAction("continuousJogXAction", CONTINUOUS_JOG_ACTION_CATEGORY, new AnalogJogAction(continuousJogWorker, Axis.X));
        actionManager.registerAction("continuousJogYAction", CONTINUOUS_JOG_ACTION_CATEGORY, new AnalogJogAction(continuousJogWorker, Axis.Y));
        actionManager.registerAction("continuousJogZAction", CONTINUOUS_JOG_ACTION_CATEGORY, new AnalogJogAction(continuousJogWorker, Axis.Z));
        actionManager.registerAction("continuousJogAAction", CONTINUOUS_JOG_ACTION_CATEGORY, new AnalogJogAction(continuousJogWorker, Axis.A));
        actionManager.registerAction("continuousJogBAction", CONTINUOUS_JOG_ACTION_CATEGORY, new AnalogJogAction(continuousJogWorker, Axis.B));
        actionManager.registerAction("continuousJogCAction", CONTINUOUS_JOG_ACTION_CATEGORY, new AnalogJogAction(continuousJogWorker, Axis.C));
        actionManager.registerAction("analogFeedOverrideAction", CONTINUOUS_OVERRIDE_ACTION_CATEGORY, new AnalogFeedOverrideAction());
        actionManager.registerAction("analogSpindleOverrideAction", CONTINUOUS_OVERRIDE_ACTION_CATEGORY, new AnalogSpindleOverrideAction());

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
        try {
            driver.initialize();
            listeners.forEach(JoystickServiceListener::onControllerChanged);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not initialize joystick service", e);
        }
    }

    @Override
    public void destroy() {
        driver.destroy();
        listeners.forEach(JoystickServiceListener::onControllerChanged);
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

    @Override
    public void onJoystickUpdated() {
        JoystickState state = driver.getState();
        listeners.forEach(listener -> {
            if (listener == joystickActionDispatcher && !isActionDispatcherActive) {
                return;
            }

            listener.onUpdate(state);
        });
    }

    @Override
    public void onDeviceChanged() {
        listeners.forEach(JoystickServiceListener::onControllerChanged);
    }

    public Optional<JoystickDevice> getCurrentDevice() {
        return driver.getCurrentDevice();
    }
}

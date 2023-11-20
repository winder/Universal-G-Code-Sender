package com.willwinder.ugs.nbp.joystick.driver;

import com.studiohartman.jamepad.Configuration;
import com.studiohartman.jamepad.ControllerAxis;
import com.studiohartman.jamepad.ControllerButton;
import com.studiohartman.jamepad.ControllerIndex;
import com.studiohartman.jamepad.ControllerManager;
import com.studiohartman.jamepad.ControllerUnpluggedException;
import com.willwinder.ugs.nbp.joystick.Settings;
import com.willwinder.ugs.nbp.joystick.Utils;
import com.willwinder.ugs.nbp.joystick.model.JamepadJoystickDevice;
import com.willwinder.ugs.nbp.joystick.model.JoystickControl;
import com.willwinder.ugs.nbp.joystick.model.JoystickDevice;
import com.willwinder.ugs.nbp.joystick.service.JoystickException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/**
 * A joystick
 */
public class JamepadJoystickDriver extends AbstractJoystickDriver {
    private static final Logger LOGGER = Logger.getLogger(JamepadJoystickDriver.class.getSimpleName());
    private static final int MAX_NUM_CONTROLLERS = 4;

    /**
     * Milliseconds to wait between reading joystick/gamepad values
     */
    private static final int READ_DELAY_MILLISECONDS = 1;
    private static final long CONNECT_DELAY_MILLISECONDS = 5000;
    private final ExecutorService joystickReadThread;
    private ControllerManager controllerManager;
    private JamepadJoystickDevice currentDevice;
    private boolean isRunning;
    private List<ControllerButton> availableButtons;

    public JamepadJoystickDriver() {
        joystickReadThread = Executors.newSingleThreadExecutor();
    }

    private void mainLoop() {
        LOGGER.info("Initializing SDL");
        controllerManager.initSDLGamepad();

        while (isRunning) {
            currentDevice = getDevices().stream()
                    .filter(JamepadJoystickDevice.class::isInstance)
                    .map(c -> (JamepadJoystickDevice) c)
                    .findFirst()
                    .orElse(null);

            if (currentDevice != null) {
                availableButtons = getAvailableControllerButtons();
                LOGGER.info(() -> String.format("Found gamepad controller '%s'", currentDevice.name()));
                notifyDeviceChanged();

                LOGGER.info("Starting readDataLoop...");
                readDataLoop();

                LOGGER.info("readDataLoop returned - possible controller unplug/replug");
                currentDevice = null;
                joystickState.clear();
                notifyJoystickUpdated();
                notifyDeviceChanged();
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // Never mind this
                }
            }
        }

        LOGGER.info("Shutting down SDL");
        try {
            controllerManager.quitSDLGamepad();
        } catch (IllegalStateException e) {
            LOGGER.fine("Couldn't release the joystick manager: " + e.getMessage());
        }
    }

    private List<ControllerButton> getAvailableControllerButtons()  {
        ArrayList<ControllerButton> availableControls = new ArrayList<>();
        try {
            for (ControllerButton controllerButton : ControllerButton.values()) {
                if (currentDevice.controller().isButtonAvailable(controllerButton)){
                    availableControls.add(controllerButton);
                }
            }
        } catch (ControllerUnpluggedException e) {
            LOGGER.warning("Controller unplugged");
        }
        return availableControls;
    }

    private void readDataLoop() {
        try {
            // Wait for the controller to properly initialize to avoid getting junk data
            Thread.sleep(CONNECT_DELAY_MILLISECONDS);
            while (isRunning && currentDevice != null && currentDevice.controller().isConnected()) {
                readData();
                Thread.sleep(READ_DELAY_MILLISECONDS);
            }
            LOGGER.info("Lost connection to controller");
        } catch (JoystickException e) {
            LOGGER.info("JoystickException - possible controller unplug/replug");
        } catch (InterruptedException e) {
            // ok, nbd...
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Something unexpected happened", e);
        }
    }

    private void readData() throws JoystickException {
        joystickState.setDirty(false);
        if (currentDevice == null) {
            return;
        }

        Arrays.asList(ControllerButton.values()).forEach(this::updateJoystickButtonState);
        Arrays.asList(ControllerAxis.values()).forEach(this::updateJoystickAxisState);

        if (joystickState.isDirty()) {
            notifyJoystickUpdated();
        }
    }

    private void updateJoystickAxisState(ControllerAxis controllerAxis) throws JoystickException {
        try {
            // We might have rounding errors from the controller, ignore the low value range
            float value = currentDevice.controller().getAxisState(controllerAxis);
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

    private void updateJoystickButtonState(ControllerButton controllerButton) throws JoystickException {
        try {
            boolean value = availableButtons.contains(controllerButton) && currentDevice.controller().isButtonPressed(controllerButton);
            JoystickControl button = Utils.getJoystickButtonFromControllerButton(controllerButton);
            joystickState.setButton(button, value);
        } catch (ControllerUnpluggedException e) {
            throw new JoystickException("Couldn't read value from joystick button", e);
        } catch (Exception e){
            LOGGER.log(Level.SEVERE, "Something unexpected happened", e);
        }
    }

    @Override
    public List<JoystickDevice> getDevices() {
        if (controllerManager == null) {
            return Collections.emptyList();
        }

        int numControllers;
        try {
            controllerManager.update();
            numControllers = controllerManager.getNumControllers();
        } catch (Exception e) {
            return Collections.emptyList();
        }

        return IntStream.range(0, numControllers).mapToObj(i -> {
            try {
                ControllerIndex controllerIndex = controllerManager.getControllerIndex(i);
                return new JamepadJoystickDevice(controllerIndex.getName(), controllerIndex);
            } catch (ControllerUnpluggedException e) {
                return null;
            }
        }).filter(Objects::nonNull).map(c -> (JoystickDevice) c).toList();
    }

    @Override
    public Optional<JoystickDevice> getCurrentDevice() {
        return Optional.ofNullable(currentDevice);
    }

    @Override
    public void initialize() throws IOException {
        if (isRunning) {
            return;
        }

        Configuration configuration = new Configuration();
        configuration.maxNumControllers = MAX_NUM_CONTROLLERS;

        controllerManager = new ControllerManager(configuration, "/com/willwinder/ugs/nbp/joystick/gamecontrollerdb.txt");

        isRunning = true; // mainLoop will run until this flag is set false
        joystickReadThread.execute(this::mainLoop);
    }

    @Override
    public void destroy() {
        isRunning = false; // causes the mainLoop to exit gracefully
    }
}

package com.willwinder.ugs.nbp.joystick;

import com.studiohartman.jamepad.ControllerAxis;
import com.studiohartman.jamepad.ControllerButton;
import com.willwinder.ugs.nbp.joystick.model.JoystickAxis;
import com.willwinder.ugs.nbp.joystick.model.JoystickButton;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class JoystickUtilsTest {

    @Test
    public void getJoystickAxisShoulConvertAllControllerAxises() {
        Arrays.asList(ControllerAxis.values()).forEach(controllerAxis -> {
            JoystickAxis axis = Utils.getJoystickAxisFromControllerAxis(controllerAxis);
            Assert.assertNotNull(axis);
        });
    }

    @Test
    public void getJoystickButtonShoulConvertAllControllerButtons() {
        Arrays.asList(ControllerButton.values()).forEach(controllerButton -> {
            JoystickButton button = Utils.getJoystickButtonFromControllerButton(controllerButton);
            Assert.assertNotNull(button);
        });
    }
}

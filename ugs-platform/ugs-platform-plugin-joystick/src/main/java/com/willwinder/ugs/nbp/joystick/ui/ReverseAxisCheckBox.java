package com.willwinder.ugs.nbp.joystick.ui;

import com.willwinder.ugs.nbp.joystick.Settings;
import com.willwinder.ugs.nbp.joystick.model.JoystickControl;
import com.willwinder.ugs.nbp.joystick.service.JoystickService;
import com.willwinder.universalgcodesender.i18n.Localization;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ReverseAxisCheckBox extends JCheckBox {

    private final JoystickControl joystickControl;

    public ReverseAxisCheckBox(JoystickService joystickService, JoystickControl joystickControl) {
        setText(Localization.getString("platform.plugin.joystick.reverseAxis"));
        addActionListener(this::reverseAxis);
        setSelected(Settings.isReverseAxis(joystickControl));
        this.joystickControl = joystickControl;
    }

    private void reverseAxis(ActionEvent actionEvent) {
        Settings.setReverseAxis(joystickControl, isSelected());
    }
}

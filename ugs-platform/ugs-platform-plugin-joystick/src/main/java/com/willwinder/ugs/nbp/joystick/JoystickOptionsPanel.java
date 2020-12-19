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
package com.willwinder.ugs.nbp.joystick;

import com.willwinder.ugs.nbp.joystick.model.JoystickControl;
import com.willwinder.ugs.nbp.joystick.model.JoystickState;
import com.willwinder.ugs.nbp.joystick.service.JoystickService;
import com.willwinder.ugs.nbp.joystick.service.JoystickServiceListener;
import com.willwinder.ugs.nbp.joystick.ui.BindActionButton;
import com.willwinder.ugs.nbp.joystick.ui.ReverseAxisCheckBox;
import com.willwinder.ugs.nbp.joystick.ui.StatusLabel;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.options.AbstractOptionsPanel;
import com.willwinder.universalgcodesender.i18n.Localization;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JoystickOptionsPanel extends AbstractOptionsPanel implements JoystickServiceListener {
    private static final List<JoystickControl> REVERSIBLE_CONTROLS = Arrays.asList(JoystickControl.LEFT_X, JoystickControl.LEFT_Y, JoystickControl.RIGHT_X, JoystickControl.RIGHT_Y);

    private final JoystickService joystickService;
    private final Map<JoystickControl, StatusLabel> statusLabelMap = new HashMap<>();
    private JPanel panel;
    private JCheckBox activeCheckbox;
    private JSpinner thresholdSpinner;

    JoystickOptionsPanel(JoystickOptionsPanelController controller) {
        super(controller);
        joystickService = CentralLookup.getDefault().lookup(JoystickService.class);
        joystickService.addListener(this);

        setLayout(new BorderLayout());
        clear();
    }

    @Override
    public void load() {
        if (panel != null) {
            this.remove(panel);
        }
        joystickService.setActivateActionDispatcher(false);

        panel = new JPanel(new MigLayout("inset 5"));

        activeCheckbox = new JCheckBox(Localization.getString("platform.plugin.joystick.activate"), Settings.isActive());
        panel.add(activeCheckbox, "wrap, spanx");

        panel.add(new JSeparator(SwingConstants.HORIZONTAL), "wrap, spanx");
        panel.add(new JLabel(Localization.getString("platform.plugin.joystick.buttonControls")), "wrap, spanx, hmin 24");

        for (JoystickControl joystickControl : JoystickControl.getDigitalControls()) {
            String name = Localization.getString(joystickControl.getLocalization());
            StatusLabel label = new StatusLabel(name);
            statusLabelMap.put(joystickControl, label);
            panel.add(label, "wmin 100, hmin 24");
            panel.add(new BindActionButton(joystickService, joystickControl), "wmin 150, hmin 24, wrap");
        }

        panel.add(new JSeparator(SwingConstants.HORIZONTAL), "wrap, spanx");
        panel.add(new JLabel(Localization.getString("platform.plugin.joystick.analogControls")), "wrap, spanx, hmin 24");

        panel.add(new JLabel(Localization.getString("platform.plugin.joystick.axisThreshold")), "wmin 100, hmin 24");
        thresholdSpinner = new JSpinner(new SpinnerNumberModel(Settings.getAxisThreshold() * 100, 0, 100, 1));
        thresholdSpinner.addChangeListener(this::onThresholdChange);
        panel.add(thresholdSpinner, "wmin 150, hmin 24, wrap");

        for (JoystickControl joystickControl : JoystickControl.getAnalogControls()) {
            String name = Localization.getString(joystickControl.getLocalization());
            StatusLabel label = new StatusLabel(name);
            statusLabelMap.put(joystickControl, label);
            panel.add(label, "wmin 100, hmin 24");


            JCheckBox reverseAxis = null;
            String wrap = ", wrap";
            if (REVERSIBLE_CONTROLS.contains(joystickControl)) {
                reverseAxis = new ReverseAxisCheckBox(joystickService, joystickControl);
                wrap = "";
            }
            panel.add(new BindActionButton(joystickService, joystickControl), "wmin 150, hmin 24" + wrap);
            if (reverseAxis != null) {
                panel.add(reverseAxis, "wrap");
            }
        }

        add(panel, BorderLayout.CENTER);
        SwingUtilities.invokeLater(changer::changed);
    }

    private void onThresholdChange(ChangeEvent changeEvent) {
        Settings.setAxisThreshold(((Double)thresholdSpinner.getValue()).intValue() / 100f);
    }

    @Override
    public void store() {
        joystickService.setActivateActionDispatcher(true);

        if (activeCheckbox.isSelected()) {
            joystickService.initialize();
        } else {
            joystickService.destroy();
        }

        Settings.setActive(activeCheckbox.isSelected());
    }

    @Override
    public boolean valid() {
        return true;
    }

    @Override
    public void onUpdate(JoystickState state) {
        for (JoystickControl control : JoystickControl.getDigitalControls()) {
            StatusLabel label = statusLabelMap.get(control);
            boolean isPressed = state.getButton(control);
            label.setActive(isPressed);
        }

        for (JoystickControl control : JoystickControl.getAnalogControls()) {
            StatusLabel label = statusLabelMap.get(control);
            float value = state.getAxis(control);
            label.setActive(value != 0);
        }
    }
}

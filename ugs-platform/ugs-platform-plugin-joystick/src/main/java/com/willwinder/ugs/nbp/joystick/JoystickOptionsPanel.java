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
package com.willwinder.ugs.nbp.joystick;

import com.willwinder.ugs.nbp.joystick.model.JoystickControl;
import com.willwinder.ugs.nbp.joystick.model.JoystickState;
import com.willwinder.ugs.nbp.joystick.service.JoystickService;
import com.willwinder.ugs.nbp.joystick.service.JoystickServiceListener;
import com.willwinder.ugs.nbp.joystick.ui.BindActionButton;
import com.willwinder.ugs.nbp.joystick.ui.JoystickOptionsActivateRow;
import com.willwinder.ugs.nbp.joystick.ui.ReverseAxisCheckBox;
import com.willwinder.ugs.nbp.joystick.ui.StatusLabel;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.options.AbstractOptionsPanel;
import com.willwinder.universalgcodesender.i18n.Localization;
import net.miginfocom.swing.MigLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class JoystickOptionsPanel extends AbstractOptionsPanel implements JoystickServiceListener {
    private static final List<JoystickControl> REVERSIBLE_CONTROLS = Arrays.asList(JoystickControl.LEFT_X, JoystickControl.LEFT_Y, JoystickControl.RIGHT_X, JoystickControl.RIGHT_Y);

    private final transient JoystickService joystickService;
    private final Map<JoystickControl, StatusLabel> statusLabelMap = new EnumMap<>(JoystickControl.class);
    private JPanel leftPanel;
    private JPanel rightPanel;
    private JSpinner thresholdSpinner;

    JoystickOptionsPanel(JoystickOptionsPanelController controller) {
        super(controller);
        joystickService = CentralLookup.getDefault().lookup(JoystickService.class);
        joystickService.addListener(this);
        setLayout(new MigLayout("fill"));
        removeAll();
    }

    @Override
    public void load() {
        JoystickOptionsActivateRow activateRow;
        removeAll();
        activateRow = new JoystickOptionsActivateRow(joystickService);
        activateRow.addActionListener(event -> {
            leftPanel.setVisible(activateRow.isActive());
            rightPanel.setVisible(activateRow.isActive());
        });
        add(activateRow, "growx, spanx, wrap, gapbottom 10");

        createLeftPanel();
        createRightPanel();
        SwingUtilities.invokeLater(changer::changed);
    }

    private void createRightPanel() {

        rightPanel = new JPanel(new MigLayout("fillx"));
        rightPanel.setVisible(Settings.isActive());
        rightPanel.setBorder(new CompoundBorder(new TitledBorder(Localization.getString("platform.plugin.joystick.analogControls")), new EmptyBorder(5, 5, 5, 5)));

        for (JoystickControl joystickControl : JoystickControl.getAnalogControls()) {
            String name = Localization.getString(joystickControl.getLocalization());
            StatusLabel label = new StatusLabel(name);
            statusLabelMap.put(joystickControl, label);
            rightPanel.add(label, "wmin 150, hmin 24");

            JCheckBox reverseAxis = null;
            String wrap = ", wrap";
            if (REVERSIBLE_CONTROLS.contains(joystickControl)) {
                reverseAxis = new ReverseAxisCheckBox(joystickService, joystickControl);
                wrap = "";
            }
            rightPanel.add(new BindActionButton(joystickService, joystickControl), "w 150:150:150, hmin 24" + wrap);
            if (reverseAxis != null) {
                rightPanel.add(reverseAxis, "wrap");
            }
        }

        rightPanel.add(new JSeparator(SwingConstants.HORIZONTAL), "hmin 24, gaptop 20, growx, wrap, spanx");
        rightPanel.add(new JLabel("<html><body><p style='width: 320px;'>" + Localization.getString("platform.plugin.joystick.axisThreshold.description") + "</p></body></html>"), "spanx, grow, wrap, gapbottom 10");

        rightPanel.add(new JLabel(Localization.getString("platform.plugin.joystick.axisThreshold")), "wmin 100, hmin 24");
        thresholdSpinner = new JSpinner(new SpinnerNumberModel(Settings.getAxisThreshold() * 100, 0, 100, 1));
        thresholdSpinner.addChangeListener(this::onThresholdChange);
        rightPanel.add(thresholdSpinner, "wmin 150, hmin 24, wrap");

        add(rightPanel, "grow");
    }

    private void createLeftPanel() {
        leftPanel = new JPanel(new MigLayout("fillx"));
        leftPanel.setVisible(Settings.isActive());
        leftPanel.setBorder(new TitledBorder(Localization.getString("platform.plugin.joystick.buttonControls")));

        for (JoystickControl joystickControl : JoystickControl.getDigitalControls()) {
            String name = Localization.getString(joystickControl.getLocalization());
            StatusLabel label = new StatusLabel(name);
            statusLabelMap.put(joystickControl, label);
            leftPanel.add(label, "wmin 150, hmin 24");
            leftPanel.add(new BindActionButton(joystickService, joystickControl), "w 150:150:150, hmin 24, wrap");
        }
        add(leftPanel, "grow, gapright 10");
    }

    private void onThresholdChange(ChangeEvent changeEvent) {
        Settings.setAxisThreshold(((Double) thresholdSpinner.getValue()).intValue() / 100f);
    }

    @Override
    public void store() {
        joystickService.setActivateActionDispatcher(true);
    }

    @Override
    public boolean valid() {
        return true;
    }

    @Override
    public void cancel() {
        joystickService.setActivateActionDispatcher(true);
    }

    @Override
    public void onUpdate(JoystickState state) {
        for (JoystickControl control : JoystickControl.getDigitalControls()) {
            StatusLabel label = statusLabelMap.get(control);
            if (label != null) {
                boolean isPressed = state.getButton(control);
                label.setActive(isPressed);
            }
        }

        for (JoystickControl control : JoystickControl.getAnalogControls()) {
            StatusLabel label = statusLabelMap.get(control);
            float value = state.getAxis(control);
            if (label != null) {
                label.setActive(value != 0);
                label.setAnalogValue(value);
            }
        }
    }

    @Override
    public void onControllerChanged() {
        // Not used
    }
}

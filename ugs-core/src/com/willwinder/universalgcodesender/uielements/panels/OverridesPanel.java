/*
    Copyright 2016-2024 Will Winder

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
package com.willwinder.universalgcodesender.uielements.panels;

import com.willwinder.universalgcodesender.firmware.IOverrideManager;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.listeners.OverrideType;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStatusEvent;
import com.willwinder.universalgcodesender.uielements.components.OverrideRadioButtons;
import com.willwinder.universalgcodesender.uielements.components.OverrideSpeedSlider;
import com.willwinder.universalgcodesender.uielements.components.OverrideToggleButtons;
import static com.willwinder.universalgcodesender.uielements.panels.OverrideLabels.TOGGLE_SHORT;
import net.miginfocom.swing.MigLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Send speed override commands to the backend.
 *
 * @author wwinder
 */
public final class OverridesPanel extends JPanel implements UGSEventListener {
    private final transient BackendAPI backend;
    private final JPanel overridesControlsPanel = new JPanel(new MigLayout("fillx, inset 0"));
    private final JLabel notConnectedLabel = new JLabel("Not connected", SwingConstants.CENTER);
    private final JLabel notSupportedLabel = new JLabel("<html>" + OverrideLabels.NOT_SUPPORTED + "</html>", SwingConstants.CENTER);
    private final Map<OverrideType, OverrideSpeedSlider> speedSliders = new ConcurrentHashMap<>();
    private final Map<OverrideType, OverrideRadioButtons> speedButtons = new ConcurrentHashMap<>();
    private boolean overridesPanelInitiated = false;
    private OverrideToggleButtons overrideToggleButtons;

    public OverridesPanel(BackendAPI backend) {
        this.backend = backend;
        if (backend != null) {
            backend.addUGSEventListener(this);
        }

        setLayout(new MigLayout("fillx, hidemode 3"));
        add(overridesControlsPanel, "grow");
        add(notConnectedLabel, "spanx, growx, gaptop 16, wrap");
        add(notSupportedLabel, "spanx, growx, gaptop 16, wrap");
        updateControls();
    }

    public void updateControls() {
        if (backend.getControllerState() == ControllerState.DISCONNECTED || backend.getControllerState() == ControllerState.CONNECTING) {
            removeComponents(); // show not connected label
            return;
        } else if (!backend.getController().getCapabilities().hasOverrides() || (backend.getController().getOverrideManager().getSliderTypes().isEmpty() && backend.getController().getOverrideManager().getToggleTypes().isEmpty())) {
            showNotSupportedPanel(); // show not suported label
            return;
        } else if (!overridesPanelInitiated) {
            initAndShowOverridesPanel(); // show panel
        }

        setEnabled(backend.getController().getOverrideManager().isAvailable());
    }

    /*
     * This function will change the apearance of the panel
     * to show the `Not Suported` message.
     */
    private void showNotSupportedPanel() {
        notSupportedLabel.setVisible(true);
        notConnectedLabel.setVisible(false);
        overridesControlsPanel.setVisible(false);
        revalidate();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        Arrays.stream(getComponents()).forEach(c -> c.setEnabled(enabled));
    }

    /*
     * This function will be called with an event.
     * The event contains information on the buttons.
     * e.g. if the flood button is toggled.
     */
    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt instanceof ControllerStateEvent) {
            updateControls();
        } else if (evt instanceof ControllerStatusEvent controllerStatusEvent) {
            ControllerStatus status = controllerStatusEvent.getStatus();
            if (status.getOverrides() != null) {
                IOverrideManager overrideManager = backend.getController().getOverrideManager();
                speedSliders.keySet().forEach(type -> {
                    int speedTargetValue = overrideManager.getSliderTargetValue(type);
                    speedSliders.get(type).setValue(speedTargetValue);
                });
                speedButtons.keySet().forEach(type -> {
                    int speedTargetValue = overrideManager.getSliderTargetValue(type);
                    speedButtons.get(type).setValue(speedTargetValue);
                });
                overrideManager.getToggleTypes().forEach(type -> overrideToggleButtons.setSelected(type, overrideManager.isToggled(type)));
            }
        }
    }

    private void removeComponents() {
        overridesControlsPanel.removeAll();
        speedSliders.clear();
        overridesControlsPanel.setVisible(false);
        notConnectedLabel.setVisible(true);
        notSupportedLabel.setVisible(false);
        overridesPanelInitiated = false;
        revalidate();
    }

    /*
     * Initialize the panel with the default apearance.
     */
    private void initAndShowOverridesPanel() {
        overridesPanelInitiated = true;
        overridesControlsPanel.setVisible(true);
        notSupportedLabel.setVisible(false);
        notConnectedLabel.setVisible(false);

        overridesControlsPanel.removeAll();
        IOverrideManager overrideManager = backend.getController().getOverrideManager();
        // add toggle buttons
        createAndAddToggleButtons(overrideManager);
        // create Rapid radio buttons
        overrideManager.getRadioTypes().forEach(this::createAndAddRadioButtons);
        // create Feed- and Spindle sliders.
        overrideManager.getSliderTypes().forEach(this::createAndAddSpeedSlider);
        
        revalidate();
    }

    /*
     * This function creates three toggle buttons: Spindle, Mist Coolant, Flood Cooland
     */
    private void createAndAddToggleButtons(IOverrideManager overrideManager) {
        if (overrideManager.getToggleTypes().isEmpty()) {
            return;
        }
        overridesControlsPanel.add(new JLabel(TOGGLE_SHORT), "spanx, grow, wrap, gaptop 10"); // short lable
        overrideToggleButtons = new OverrideToggleButtons(overrideManager); // add three toggle buttons
        overridesControlsPanel.add(overrideToggleButtons, "growx, w 40::");
    }

    private void createAndAddRadioButtons(OverrideType type) {
        IOverrideManager overrideManager = backend.getController().getOverrideManager();
        OverrideRadioButtons radioButtons = new OverrideRadioButtons(overrideManager, type);
        radioButtons.addChangeListener(l -> updateRadio(type, radioButtons.getValue()));
        speedButtons.put(type, radioButtons);
        overridesControlsPanel.add(new JLabel(type.getLabel()), "newline, spanx, wrap, gaptop 10");
        overridesControlsPanel.add(radioButtons, "spanx, grow, wrap");
    }

    private void createAndAddSpeedSlider(OverrideType type) {
        IOverrideManager overrideManager = backend.getController().getOverrideManager();

        OverrideSpeedSlider speedSlider = new OverrideSpeedSlider(overrideManager, type);
        speedSlider.addChangeListener(l -> updateSpeed(type, speedSlider.getValue()));
        speedSliders.put(type, speedSlider);
        overridesControlsPanel.add(new JLabel(type.getLabel()), "newline, spanx, wrap, gaptop 10");
        overridesControlsPanel.add(speedSlider, "spanx, grow, wrap");
    }

    private void updateSpeed(OverrideType type, int value) {
        if (backend.getController() == null || !backend.isConnected()) {
            return;
        }

        backend.getController().getOverrideManager().setSliderTarget(type, value);
    }

    private void updateRadio(OverrideType type, int value) {
        if (backend.getController() == null || !backend.isConnected()) {
            return;
        }

        backend.getController().getOverrideManager().setRadioTarget(type, value);
    }
}

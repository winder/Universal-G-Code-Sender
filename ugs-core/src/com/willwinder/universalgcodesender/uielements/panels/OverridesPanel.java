/*
    Copyright 2016-2018 Will Winder

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
import static com.willwinder.universalgcodesender.uielements.panels.OverrideLabels.TOGGLE_SHORT;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import net.miginfocom.swing.MigLayout;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Send speed override commands to the backend.
 *
 * @author wwinder
 */
public final class OverridesPanel extends JPanel implements UGSEventListener {
    private final transient BackendAPI backend;
    private final JPanel overridesControlsPanel = new JPanel(new MigLayout("fillx"));
    private final JLabel notConnectedLabel = new JLabel("Not connected", SwingConstants.CENTER);
    private final JLabel notSupportedLabel = new JLabel("<html>" + OverrideLabels.NOT_SUPPORTED + "</html>", SwingConstants.CENTER);
    private final Map<OverrideType, JSlider> speedSliders = new ConcurrentHashMap<>();
    private final Map<OverrideType, JToggleButton> toggleButtons = new ConcurrentHashMap<>();

    private boolean overridesPanelInitiated = false;

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
            removeComponents();
            return;
        } else if (!backend.getController().getCapabilities().hasOverrides() || (backend.getController().getOverrideManager().getSpeedTypes().isEmpty() && backend.getController().getOverrideManager().getToggleTypes().isEmpty())) {
            showNotSupportedPanel();
            return;
        } else if (!overridesPanelInitiated) {
            initAndShowOverridesPanel();
        }

        setEnabled(backend.getController().getOverrideManager().isAvailable());
    }

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

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt instanceof ControllerStateEvent) {
            updateControls();
        } else if (evt instanceof ControllerStatusEvent controllerStatusEvent) {
            ControllerStatus status = controllerStatusEvent.getStatus();
            if (status.getOverrides() != null) {
                speedSliders.keySet().forEach(type -> speedSliders.get(type).setValue(backend.getController().getOverrideManager().getSpeedTargetValue(type)));
                toggleButtons.keySet().forEach(type -> toggleButtons.get(type).setSelected(backend.getController().getOverrideManager().isToggled(type)));
            }
        }
    }

    private void removeComponents() {
        overridesControlsPanel.removeAll();
        speedSliders.clear();
        toggleButtons.clear();
        overridesControlsPanel.setVisible(false);
        notConnectedLabel.setVisible(true);
        notSupportedLabel.setVisible(false);
        overridesPanelInitiated = false;
        revalidate();
    }

    private void initAndShowOverridesPanel() {
        overridesPanelInitiated = true;
        overridesControlsPanel.setVisible(true);
        notSupportedLabel.setVisible(false);
        notConnectedLabel.setVisible(false);

        overridesControlsPanel.removeAll();
        IOverrideManager overrideManager = backend.getController().getOverrideManager();
        if (!overrideManager.getToggleTypes().isEmpty()) {
            overridesControlsPanel.add(new JLabel(TOGGLE_SHORT), "spanx, grow, wrap, gaptop 10");
            overrideManager.getToggleTypes().forEach(this::createAndAddToggleButtons);
        }

        overrideManager.getSpeedTypes().forEach(this::createAndAddSpeedSlider);
        revalidate();
    }

    private void createAndAddToggleButtons(OverrideType overrideType) {
        IOverrideManager overrideManager = backend.getController().getOverrideManager();
        JToggleButton toggleSpindle = new JToggleButton(overrideType.name());
        toggleSpindle.setAction(new AbstractAction(overrideType.getLabel()) {
            @Override
            public void actionPerformed(ActionEvent e) {
                overrideManager.toggle(overrideType);
                toggleSpindle.setSelected(!overrideManager.isToggled(overrideType));
                ThreadHelper.invokeLater(() -> toggleSpindle.setSelected(overrideManager.isToggled(overrideType)), 200);
            }
        });
        overridesControlsPanel.add(toggleSpindle, "growx");
        toggleButtons.put(overrideType, toggleSpindle);
    }

    private void createAndAddSpeedSlider(OverrideType type) {
        IOverrideManager overrideManager = backend.getController().getOverrideManager();

        JSlider speedSlider = new JSlider(0, overrideManager.getSpeedMax(type), overrideManager.getSpeedDefault(type));
        speedSlider.setMinorTickSpacing(0);
        speedSlider.setMajorTickSpacing(10);
        speedSliders.put(type, speedSlider);

        Dictionary<Integer, JComponent> dict = new Hashtable<>();
        for (int i = 0; i <= overrideManager.getSpeedMax(type); i += 100) {
            dict.put(i, new JLabel(i + "%"));
        }

        speedSlider.setLabelTable(dict);
        speedSlider.setPaintLabels(true);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintTrack(true);
        speedSlider.addChangeListener(l -> updateSpeed(type, speedSlider));
        overridesControlsPanel.add(new JLabel(type.getLabel()), "spanx, grow, newline, wrap, gaptop 10");
        overridesControlsPanel.add(speedSlider, "spanx, grow, wrap");
    }

    private void updateSpeed(OverrideType type, JSlider slider) {
        if (backend.getController() == null || !backend.isConnected()) {
            return;
        }

        backend.getController().getOverrideManager().setSpeedTarget(type, slider.getValue());
    }
}

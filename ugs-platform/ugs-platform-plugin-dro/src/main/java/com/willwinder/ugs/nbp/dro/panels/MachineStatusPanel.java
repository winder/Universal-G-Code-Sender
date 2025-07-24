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
package com.willwinder.ugs.nbp.dro.panels;

import com.willwinder.ugs.nbp.dro.FontManager;
import com.willwinder.universalgcodesender.Capabilities;
import com.willwinder.universalgcodesender.Utils;
import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.listeners.EnabledPins;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStatusEvent;
import com.willwinder.universalgcodesender.model.events.SettingChangedEvent;
import com.willwinder.universalgcodesender.uielements.components.PopupEditor;
import com.willwinder.universalgcodesender.uielements.components.RoundedPanel;
import com.willwinder.universalgcodesender.uielements.helpers.SteppedSizeManager;
import com.willwinder.universalgcodesender.uielements.helpers.ThemeColors;
import com.willwinder.universalgcodesender.utils.Settings;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * DRO style display panel with current controller state.
 */
public class MachineStatusPanel extends JPanel implements UGSEventListener, AxisPanelListener {
    private static final Logger LOGGER = Logger.getLogger(MachineStatusPanel.class.getName());
    private static final int COMMON_RADIUS = 7;
    public static final String PANEL_CONSTRAINTS = "growx";

    private final RoundedPanel activeStatePanel = new RoundedPanel(COMMON_RADIUS);
    private final JLabel activeStateValueLabel = new JLabel(" ");

    private final JLabel feedValue = new JLabel("0");
    private final JLabel spindleSpeedValue = new JLabel("0");

    private final JLabel gStatesLabel = new JLabel();

    private final RoundedPanel pinStatePanel = new RoundedPanel(COMMON_RADIUS);
    private final JLabel pinStatesLabel = new JLabel();

    private final transient FontManager fontManager = new FontManager();

    private final transient BackendAPI backend;

    private final JPanel axisPanel = new JPanel();
    private Units units;
    private final Map<Axis, AxisPanel> axisPanels = new EnumMap<>(Axis.class);
    private final DecimalFormat decimalFormatter = new DecimalFormat("0.000");


    public MachineStatusPanel(BackendAPI backend) {
        this.backend = backend;
        if (this.backend != null) {
            this.backend.addUGSEventListener(this);
        }

        initFonts();
        initComponents();
        initSizer();

        if (this.backend != null && this.backend.getSettings().getPreferredUnits() == Units.MM) {
            setUnits(Units.MM);
        } else {
            setUnits(Units.INCH);
        }

        updateControls();
    }

    private void initSizer() {
        SteppedSizeManager sizer = new SteppedSizeManager(this,
                new Dimension(200, 200),
                new Dimension(240, 200),
                new Dimension(300, 200));
        sizer.addListener(fontManager::applyFonts);
    }

    private void initFonts() {
        fontManager.init();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        fontManager.registerFonts(ge);
    }

    private void initComponents() {
        String debug = "";
        setLayout(new MigLayout(debug + "fillx, wrap 1, inset 5", "grow"));

        activeStateValueLabel.setForeground(ThemeColors.VERY_DARK_GREY);
        activeStateValueLabel.setText(Translations.OFFLINE);

        activeStatePanel.setLayout(new MigLayout(debug + "fill, inset 0 5 0 5"));
        activeStatePanel.setBackground(Color.BLACK);
        activeStatePanel.setForeground(ThemeColors.VERY_DARK_GREY);
        activeStatePanel.add(activeStateValueLabel, "al center");
        activeStateValueLabel.setBorder(BorderFactory.createEmptyBorder());
        add(activeStatePanel, PANEL_CONSTRAINTS);

        // Default to showing X, Y, Z (defined in Axis)
        axisPanel.setLayout(new MigLayout(debug + "fillx, wrap 1, hidemode 3, inset 0 0 0 0", "grow"));
        Stream.of(Axis.values()).forEach(this::initializeAxisPanel);
        add(axisPanel, PANEL_CONSTRAINTS);

        // show feed rate
        JPanel speedPanel = new JPanel(new MigLayout(debug + "fillx, wrap 2, inset 0", "[al right][]"));
        speedPanel.setOpaque(false);
        JLabel feedLabel = new JLabel(Localization.getString("gcode.setting.feed"));
        speedPanel.add(feedLabel);
        speedPanel.add(feedValue, "pad 2 0 0 0");
        
        // show spindle
        JLabel spindleSpeedLabel = new JLabel(Localization.getString("overrides.spindle.short"));
        speedPanel.add(spindleSpeedLabel);
        speedPanel.add(spindleSpeedValue, "pad 2 0 0 0");
        add(speedPanel, PANEL_CONSTRAINTS);

        add(gStatesLabel, "align center");

        Color transparent = new Color(0, 0, 0, 0);
        pinStatePanel.setLayout(new MigLayout("insets 0 5 0 5"));
        pinStatePanel.setBackground(transparent);
        resetStatePinComponents();
        pinStatePanel.add(pinStatesLabel);
        add(pinStatePanel, "align center");
        setAllCaps(feedLabel, feedValue, spindleSpeedLabel, spindleSpeedValue);

        fontManager.addActiveStateLabel(activeStateValueLabel);
        fontManager.addPropertyLabel(feedLabel, spindleSpeedLabel, pinStatesLabel, gStatesLabel);
        fontManager.addSpeedLabel(feedValue, spindleSpeedValue);
        fontManager.applyFonts(0);
    }

    private void initializeAxisPanel(Axis axis) {
        AxisPanel panel = new AxisPanel(axis, fontManager);
        panel.setVisible(axis.isLinear());
        panel.setEnabled(false);
        axisPanels.put(axis, panel);
        axisPanel.add(panel, PANEL_CONSTRAINTS);
        panel.addListener(this);
    }

    private void resetStatePinComponents() {
        pinStatesLabel.setText("");
        pinStatesLabel.setForeground(ThemeColors.GREY);
        pinStatePanel.setForeground(ThemeColors.GREY);
    }

    private void setAllCaps(JLabel... labels) {
        Arrays.stream(labels).forEach(l -> l.setText(l.getText().toUpperCase()));
    }

    @Override
    public void setEnabled(boolean enabled) {
        // Disable this functionality as the styling will get messed up otherwise
    }

    private void setUnits(Units u) {
        if (u == null || units == u) return;
        units = u;
        switch (u) {
            case MM:
            case INCH:
                break;
            default:
                units = Units.MM;
                break;
        }
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt instanceof ControllerStateEvent) {
            updateControls();
        } else if (evt instanceof ControllerStatusEvent controllerStatusEvent) {
            onControllerStatusReceived(controllerStatusEvent.getStatus());
        } else if (evt instanceof SettingChangedEvent) {
            if (backend.getController() != null) {
                onControllerStatusReceived(backend.getController().getControllerStatus());
            }
            updateControls();
        }
    }

    /**
     * Enable and disable the different axes based on capabilities and configuration.
     */
    private void updateControls() {
        Settings settings = backend.getSettings();
        if (!backend.isConnected()) {
            axisPanels.forEach((key, value) -> {
                value.setEnabled(false);
                value.setVisible(key.isLinear());
                value.setShowMachinePosition(settings.isShowMachinePosition());
            });

            // Clear out the status color.
            this.updateStatePanel(ControllerState.DISCONNECTED);
            resetStatePinComponents();
            return;
        }

        Capabilities cap = backend.getController().getCapabilities();
        boolean enabled = backend.getControllerState() == ControllerState.IDLE;
        for (Axis a : Axis.values()) {
            // don't hide every axis while capabilities are being detected.
            boolean visible = (cap.hasAxis(a) || a.isLinear()) && settings.isAxisEnabled(a);
            axisPanels.get(a).setEnabled(enabled);
            axisPanels.get(a).setVisible(visible);
            axisPanels.get(a).setShowMachinePosition(settings.isShowMachinePosition());
        }
    }

    private void onControllerStatusReceived(ControllerStatus status) {
        this.updateStatePanel(status.getState());
        resetStatePinComponents();

        updatePinStates(status);

        this.setUnits(backend.getSettings().getPreferredUnits());

        Arrays.stream(Axis.values())
                .filter(axisPanels::containsKey)
                .forEach(axis -> {
                    if (status.getMachineCoord() != null) {
                        Position machineCoord = status.getMachineCoord().getPositionIn(units);
                        axisPanels.get(axis).setMachinePosition(machineCoord.get(axis));
                    }

                    if (status.getWorkCoord() != null) {
                        Position workCoord = status.getWorkCoord().getPositionIn(units);
                        axisPanels.get(axis).setWorkPosition(workCoord.get(axis));
                    }
                });

        // Use real-time values if available, otherwise show the target values.
        int feedSpeed = status.getFeedSpeed() != null
                ? (int) (status.getFeedSpeed() * UnitUtils.scaleUnits(status.getFeedSpeedUnits(), backend.getSettings().getPreferredUnits()))
                : (int) this.backend.getGcodeState().feedRate;
        this.feedValue.setText(Integer.toString(feedSpeed));

        int spindleSpeed = status.getSpindleSpeed() != null
                ? status.getSpindleSpeed().intValue()
                : (int) this.backend.getGcodeState().spindleSpeed;
        this.spindleSpeedValue.setText(Integer.toString(spindleSpeed));

        GcodeState state = backend.getGcodeState();
        if (state == null) {
            gStatesLabel.setText("--");
        } else {
            gStatesLabel.setText(
                    String.join(" ",
                            state.currentMotionMode.toString(),
                            state.units.toString(),
                            state.feedMode.toString(),
                            state.distanceMode.toString(),
                            state.offset.toString(),
                            state.plane.code.toString()));
        }
    }

    private void updatePinStates(ControllerStatus status) {
        if (status.getEnabledPins() == null) {
            return;
        }

        EnabledPins ep = status.getEnabledPins();

        List<String> enabled = new ArrayList<>();
        if (ep.x()) enabled.add(Translations.PIN_X);
        if (ep.y()) enabled.add(Translations.PIN_Y);
        if (ep.z()) enabled.add(Translations.PIN_Z);
        if (ep.a()) enabled.add(Translations.PIN_A);
        if (ep.b()) enabled.add(Translations.PIN_B);
        if (ep.c()) enabled.add(Translations.PIN_C);
        if (ep.probe()) enabled.add(Translations.PIN_PROBE);
        if (ep.door()) enabled.add(Translations.PIN_DOOR);
        if (ep.hold()) enabled.add(Translations.PIN_HOLD);
        if (ep.softReset()) enabled.add(Translations.PIN_SOFT_RESET);
        if (ep.cycleStart()) enabled.add(Translations.PIN_CYCLE_STARY);

        if (!enabled.isEmpty()) {
            pinStatesLabel.setText(String.join(" ", enabled));
            pinStatesLabel.setForeground(ThemeColors.RED);
            pinStatePanel.setForeground(ThemeColors.RED);
        }
    }

    private void updateStatePanel(ControllerState state) {
        String text = Utils.getControllerStateText(state);
        activeStatePanel.setBackground(Utils.getControllerStateBackgroundColor(state));
        activeStateValueLabel.setForeground(Utils.getControllerStateForegroundColor(state));
        activeStateValueLabel.setText(text.toUpperCase());
    }

    @Override
    public void onWorkPositionClick(JComponent component, Axis axis) {
        if (backend.isConnected() && backend.isIdle()) {
            String text = decimalFormatter.format(backend.getWorkPosition().getPositionIn(units).get(axis));
            PopupEditor popupEditor = new PopupEditor(component, "Set " + axis + " work position", text);
            popupEditor.setVisible(true);
            popupEditor.addPopupListener(value -> {
                try {
                    backend.setWorkPositionUsingExpression(axis, value);
                } catch (Exception e) {
                    LOGGER.warning(String.format("Could not set work position '%s' on axis '%s'", value, axis));
                }
            });
        }
    }
}

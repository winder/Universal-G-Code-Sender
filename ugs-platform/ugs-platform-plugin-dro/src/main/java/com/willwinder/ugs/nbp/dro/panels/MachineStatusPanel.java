/*
    Copyright 2016-2020 Will Winder

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
import com.willwinder.universalgcodesender.Utils;
import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.ControllerStateListener;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.listeners.ControllerStatus.EnabledPins;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.uielements.components.PopupEditor;
import com.willwinder.universalgcodesender.uielements.components.RoundedPanel;
import com.willwinder.universalgcodesender.uielements.helpers.*;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * DRO style display panel with current controller state.
 */
public class MachineStatusPanel extends JPanel implements UGSEventListener, ControllerStateListener, AxisPanel.AxisPanelListener {
    private static final Logger LOGGER = Logger.getLogger(MachineStatusPanel.class.getName());
    private static final int COMMON_RADIUS = 7;
    private static final Duration REFRESH_RATE = Duration.ofSeconds(1);

    private final String OFFLINE = Localization.getString("mainWindow.status.offline").toUpperCase();
    private final String ALARM = Localization.getString("mainWindow.status.alarm").toUpperCase();
    private final String PIN_X = Localization.getString("machineStatus.pin.x").toUpperCase();
    private final String PIN_Y = Localization.getString("machineStatus.pin.y").toUpperCase();
    private final String PIN_Z = Localization.getString("machineStatus.pin.z").toUpperCase();
    private final String PIN_PROBE = Localization.getString("machineStatus.pin.probe").toUpperCase();
    private final String PIN_DOOR = Localization.getString("machineStatus.pin.door").toUpperCase();
    private final String PIN_HOLD = Localization.getString("machineStatus.pin.hold").toUpperCase();
    private final String PIN_SOFT_RESET = Localization.getString("machineStatus.pin.softReset").toUpperCase();
    private final String PIN_CYCLE_STARY = Localization.getString("machineStatus.pin.cycleStart").toUpperCase();

    private final RoundedPanel activeStatePanel = new RoundedPanel(COMMON_RADIUS);
    private final JLabel activeStateValueLabel = new JLabel(" ");

    private final JLabel feedValue = new JLabel("0");
    private final JLabel spindleSpeedValue = new JLabel("0");

    private final JLabel gStatesLabel = new JLabel();

    private final RoundedPanel pinStatePanel = new RoundedPanel(COMMON_RADIUS);
    private final JLabel pinStatesLabel = new JLabel();

    private final FontManager fontManager = new FontManager();

    private final BackendAPI backend;
    private final Timer statePollTimer;

    private Units units;
    private Map<Axis, AxisPanel> axisPanels = new HashMap<>();
    private final DecimalFormat decimalFormatter = new DecimalFormat("0.000");


    public MachineStatusPanel(BackendAPI backend) {
        this.backend = backend;
        if (this.backend != null) {
            this.backend.addUGSEventListener(this);
            this.backend.addControllerStateListener(this);
        }
        statePollTimer = createTimer();

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
                new Dimension(160, 330),
                new Dimension(240, 420),
                new Dimension(310, 420));
        sizer.addListener(fontManager::applyFonts);
    }

    private void initFonts() {
        fontManager.init();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        fontManager.registerFonts(ge);
    }

    private void initComponents() {

        String debug = "";
        //String debug = "debug, ";
        setLayout(new MigLayout(debug + "fillx, wrap 1, inset 5", "grow"));

        activeStateValueLabel.setForeground(ThemeColors.VERY_DARK_GREY);
        activeStateValueLabel.setText(OFFLINE);

        activeStatePanel.setLayout(new MigLayout(debug + "fill, inset 0 5 0 5"));
        activeStatePanel.setBackground(Color.BLACK);
        activeStatePanel.setForeground(ThemeColors.VERY_DARK_GREY);
        activeStatePanel.add(activeStateValueLabel, "al center");
        activeStateValueLabel.setBorder(BorderFactory.createEmptyBorder());
        add(activeStatePanel, "growx");

        Stream.of(Axis.X, Axis.Y, Axis.Z).forEach(this::createAxisPanel);

        JPanel speedPanel = new JPanel(new MigLayout(debug + "fillx, wrap 2, inset 0", "[al right][]"));
        speedPanel.setOpaque(false);
        JLabel feedLabel = new JLabel(Localization.getString("gcode.setting.feed"));
        speedPanel.add(feedLabel);
        speedPanel.add(feedValue, "pad 2 0 0 0");
        JLabel spindleSpeedLabel = new JLabel(Localization.getString("overrides.spindle.short"));
        speedPanel.add(spindleSpeedLabel);
        speedPanel.add(spindleSpeedValue, "pad 2 0 0 0");
        add(speedPanel, "growx");

        add(gStatesLabel, "align center");

        Color transparent = new Color(0, 0, 0, 0);

        pinStatePanel.setLayout(new MigLayout("insets 0 5 0 5"));
        pinStatePanel.setBackground(transparent);
        resetStatePinComponents();
        pinStatePanel.add(pinStatesLabel);
        add(pinStatePanel, "align center");

        Color bkg = getBackground();
        int value = bkg.getRed() + bkg.getBlue() + bkg.getGreen();
        boolean panelIsLight = value > 385;
        Color panelTextColor;
        if (panelIsLight) panelTextColor = Color.BLACK;
        else panelTextColor = ThemeColors.ORANGE;
        setForegroundColor(panelTextColor, feedLabel, feedValue, spindleSpeedLabel, spindleSpeedValue, gStatesLabel);

        setAllCaps(feedLabel, feedValue, spindleSpeedLabel, spindleSpeedValue);

        fontManager.addActiveStateLabel(activeStateValueLabel);
        fontManager.addPropertyLabel(feedLabel, spindleSpeedLabel, pinStatesLabel, gStatesLabel);
        fontManager.addSpeedLabel(feedValue, spindleSpeedValue);
        fontManager.applyFonts(0);

        statePollTimer.start();
    }

    private void createAxisPanel(Axis axis) {
        AxisPanel axisPanel = new AxisPanel(axis, fontManager);
        axisPanels.put(axis, axisPanel);
        add(axisPanel, "growx, span 2");
        axisPanel.addListener(this);
    }

    private void resetStatePinComponents() {
        pinStatesLabel.setText(ALARM);
        pinStatesLabel.setForeground(ThemeColors.GREY);
        pinStatePanel.setForeground(ThemeColors.GREY);
    }

    private void setForegroundColor(Color color, JComponent... components) {
        Arrays.stream(components).forEach(c -> c.setForeground(color));
    }

    private void setAllCaps(JLabel... labels) {
        Arrays.stream(labels).forEach(l -> l.setText(l.getText().toUpperCase()));
    }

    private Timer createTimer() {
        return new Timer((int) REFRESH_RATE.toMillis(), (ae) -> EventQueue.invokeLater(() -> {
            if (!backend.isConnected()) {
                activeStateValueLabel.setText(OFFLINE);
                activeStatePanel.setBackground(Color.BLACK);
            }
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
        }));
    }

    private void setUnits(Units u) {
        if (u == null || units == u) return;
        units = u;
        switch (u) {
            case MM:
                break;
            case INCH:
                break;
            default:
                units = Units.MM;
                break;
        }
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt.isStateChangeEvent()) {
            updateControls();
        }
        if (evt.isControllerStatusEvent()) {
            onControllerStatusReceived(evt.getControllerStatus());
        }
        if (evt.isSettingChangeEvent() && backend.getController() != null && backend.getController().getControllerStatus() != null) {
            onControllerStatusReceived(backend.getController().getControllerStatus());
        }
    }

    private void updateControls() {
        axisPanels.values().forEach(c -> c.setEnabled(backend.isIdle()));

        if (!backend.isConnected()) {
            // Clear out the status color.
            this.updateStatePanel(ControllerState.UNKNOWN);
            resetStatePinComponents();
        }
    }

    private void onControllerStatusReceived(ControllerStatus status) {
        this.updateStatePanel(status.getState());
        resetStatePinComponents();

        if (status.getEnabledPins() != null) {

            EnabledPins ep = status.getEnabledPins();

            List<String> enabled = new ArrayList<>();
            if (ep.X) enabled.add(PIN_X);
            if (ep.Y) enabled.add(PIN_Y);
            if (ep.Z) enabled.add(PIN_Z);
            if (ep.Probe) enabled.add(PIN_PROBE);
            if (ep.Door) enabled.add(PIN_DOOR);
            if (ep.Hold) enabled.add(PIN_HOLD);
            if (ep.SoftReset) enabled.add(PIN_SOFT_RESET);
            if (ep.CycleStart) enabled.add(PIN_CYCLE_STARY);

            if (!enabled.isEmpty()) {
                enabled.add(0, ALARM + ":");
                pinStatesLabel.setText(String.join(" ", enabled));
                pinStatesLabel.setForeground(ThemeColors.RED);
                pinStatePanel.setForeground(ThemeColors.RED);
            }
        }

        this.setUnits(backend.getSettings().getPreferredUnits());

        Arrays.stream(Axis.values())
                .filter(axis -> axisPanels.containsKey(axis))
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
                : (int) this.backend.getGcodeState().speed;
        this.feedValue.setText(Integer.toString(feedSpeed));

        int spindleSpeed = status.getSpindleSpeed() != null
                ? status.getSpindleSpeed().intValue()
                : (int) this.backend.getGcodeState().spindleSpeed;
        this.spindleSpeedValue.setText(Integer.toString(spindleSpeed));
    }

    private void updateStatePanel(ControllerState state) {

        Color background = ThemeColors.GREY;
        String text = Utils.getControllerStateText(state);
        if (state == ControllerState.ALARM) {
            background = ThemeColors.RED;
        } else if (state == ControllerState.HOLD) {
            background = ThemeColors.ORANGE;
        } else if (state == ControllerState.DOOR) {
            background = ThemeColors.ORANGE;
        } else if (state == ControllerState.RUN) {
            background = ThemeColors.GREEN;
        } else if (state == ControllerState.JOG) {
            background = ThemeColors.GREEN;
        } else if (state == ControllerState.HOME) {
            background = ThemeColors.GREEN;
        } else if (state == ControllerState.CHECK) {
            background = ThemeColors.LIGHT_BLUE;
        }

        this.activeStatePanel.setBackground(background);
        this.activeStateValueLabel.setText(text.toUpperCase());
    }

    @Override
    public void onResetClick(JComponent component, Axis axis) {
        try {
            backend.resetCoordinateToZero(axis);
        } catch (Exception e) {
            LOGGER.warning(String.format("Could not reset work position on axis '%s'", axis));
        }
    }

    @Override
    public void onWorkPositionClick(JComponent component, Axis axis) {
        if (backend.isConnected() && backend.isIdle()) {
            String text = decimalFormatter.format(backend.getWorkPosition().get(axis));
            PopupEditor popupEditor = new PopupEditor(component, "Set " + axis + " work position", text);
            popupEditor.setVisible(true);
            popupEditor.addPopupListener((value) -> {
                try {
                    backend.setWorkPositionUsingExpression(axis, value);
                } catch (Exception e) {
                    LOGGER.warning(String.format("Could not set work position '%s' on axis '%s'", value, axis));
                }
            });
        }
    }
}

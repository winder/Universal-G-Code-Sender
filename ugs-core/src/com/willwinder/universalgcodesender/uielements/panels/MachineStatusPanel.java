/*
    Copyright 2016-2019 Will Winder

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

import com.willwinder.universalgcodesender.Utils;
import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.ControllerStateListener;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.listeners.ControllerStatus.EnabledPins;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.Axis;
import static com.willwinder.universalgcodesender.model.Axis.*;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.uielements.components.RoundedPanel;
import com.willwinder.universalgcodesender.uielements.components.WorkCoordinateTextField;
import com.willwinder.universalgcodesender.uielements.helpers.MachineStatusFontManager;
import com.willwinder.universalgcodesender.uielements.helpers.SteppedSizeManager;
import com.willwinder.universalgcodesender.uielements.helpers.ThemeColors;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.willwinder.universalgcodesender.utils.GUIHelpers.displayErrorDialog;

/**
 * DRO style display panel with current controller state.
 */
public class MachineStatusPanel extends JPanel implements UGSEventListener, ControllerStateListener {

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

    private final JLabel machinePositionXValue = new JLabel("0.000");
    private final JLabel machinePositionYValue = new JLabel("0.000");
    private final JLabel machinePositionZValue = new JLabel("0.000");

    private final JTextField workPositionXValue;
    private final JTextField workPositionYValue;
    private final JTextField workPositionZValue;

    private final JLabel feedValue = new JLabel("0");
    private final JLabel spindleSpeedValue = new JLabel("0");

    private final JLabel gStatesLabel = new JLabel();

    private final RoundedPanel pinStatePanel = new RoundedPanel(COMMON_RADIUS);
    private final JLabel pinStatesLabel = new JLabel();

    private List<JComponent> axisResetControls = new ArrayList<>(3);

    private final MachineStatusFontManager machineStatusFontManager = new MachineStatusFontManager();

    private final BackendAPI backend;
    private final Timer statePollTimer;

    private Units units;
    private final DecimalFormat decimalFormatter;


    public MachineStatusPanel(BackendAPI backend) {
        this.backend = backend;
        if (this.backend != null) {
            this.backend.addUGSEventListener(this);
            this.backend.addControllerStateListener(this);
        }
        decimalFormatter = new DecimalFormat("0.000");
        statePollTimer = createTimer();
        workPositionXValue = new WorkCoordinateTextField(backend, X);
        workPositionYValue = new WorkCoordinateTextField(backend, Y);
        workPositionZValue = new WorkCoordinateTextField(backend, Z);

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
                new Dimension(200, 375),
                new Dimension(240, 460),
                new Dimension(310, 570));
        sizer.addListener(machineStatusFontManager::applyFonts);
    }

    private void initFonts() {
        machineStatusFontManager.init();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        machineStatusFontManager.registerFonts(ge);
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

        addAxisPanel(X, workPositionXValue, machinePositionXValue);
        addAxisPanel(Y, workPositionYValue, machinePositionYValue);
        addAxisPanel(Z, workPositionZValue, machinePositionZValue);

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

        machineStatusFontManager.addActiveStateLabel(activeStateValueLabel);
        machineStatusFontManager.addPropertyLabel(feedLabel, spindleSpeedLabel, pinStatesLabel, gStatesLabel);
        machineStatusFontManager.addSpeedLabel(feedValue, spindleSpeedValue);
        machineStatusFontManager.applyFonts(0);

        statePollTimer.start();
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

    private void addAxisPanel(Axis axis, JTextField work, JComponent machine) {
        RoundedPanel axisPanel = new RoundedPanel(COMMON_RADIUS);
        axisPanel.setBackground(ThemeColors.VERY_DARK_GREY);
        axisPanel.setForeground(ThemeColors.LIGHT_BLUE);
        axisPanel.setLayout(new MigLayout("fillx, wrap 2, inset 7, gap 0", "[left][grow, right]"));

        RoundedPanel resetPanel = new RoundedPanel(COMMON_RADIUS);
        resetPanel.setForeground(ThemeColors.LIGHT_BLUE);
        resetPanel.setBackground(ThemeColors.DARK_BLUE_GREY);
        resetPanel.setBackgroundDisabled(ThemeColors.VERY_DARK_GREY);
        resetPanel.setHoverBackground(ThemeColors.MED_BLUE_GREY);
        resetPanel.setLayout(new MigLayout("inset 5 15 5 15"));
        JLabel axisLabel = new JLabel(String.valueOf(axis));
        axisLabel.setForeground(ThemeColors.LIGHT_BLUE);
        resetPanel.add(axisLabel, "al center, dock center, id axis");
        JLabel zeroLabel = new JLabel("0");
        zeroLabel.setForeground(ThemeColors.LIGHT_BLUE);
        resetPanel.add(zeroLabel, "pos (axis.x + axis.w - 4) (axis.y + axis.h - 25)");

        machine.setForeground(ThemeColors.LIGHT_BLUE);
        axisPanel.add(resetPanel, "sy 2");
        axisPanel.add(work, "grow, gapleft 5");
        axisPanel.add(machine, "span 2");

        machineStatusFontManager.addAxisResetLabel(axisLabel);
        machineStatusFontManager.addAxisResetZeroLabel(zeroLabel);
        machineStatusFontManager.addWorkCoordinateLabel(work);
        machineStatusFontManager.addMachineCoordinateLabel(machine);

        add(axisPanel,"growx, span 2");

        resetPanel.addClickListener(() -> resetCoordinateButton(axis) );
        axisResetControls.add(axisPanel);
    }

    private Timer createTimer() {
        return new Timer((int) REFRESH_RATE.toMillis(), (ae) -> EventQueue.invokeLater(() -> {
            if (! backend.isConnected()) {
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
        switch(u) {
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
        axisResetControls.forEach(c -> c.setEnabled(backend.isIdle()));

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

            if (! enabled.isEmpty()) {
                enabled.add(0, ALARM + ":");
                pinStatesLabel.setText(String.join(" ", enabled));
                pinStatesLabel.setForeground(ThemeColors.RED);
                pinStatePanel.setForeground(ThemeColors.RED);
            }
        }

        this.setUnits(backend.getSettings().getPreferredUnits());

        if (status.getMachineCoord() != null) {
            Position machineCoord = status.getMachineCoord().getPositionIn(units);
            this.setPositionValueColor(this.machinePositionXValue, this.machinePositionXValue.getText(), machineCoord.x);
            this.machinePositionXValue.setText(decimalFormatter.format(machineCoord.x));

            this.setPositionValueColor(this.machinePositionYValue, this.machinePositionYValue.getText(), machineCoord.y);
            this.machinePositionYValue.setText(decimalFormatter.format(machineCoord.y));

            this.setPositionValueColor(this.machinePositionZValue, this.machinePositionZValue.getText(), machineCoord.z);
            this.machinePositionZValue.setText(decimalFormatter.format(machineCoord.z));
        }

        if (status.getWorkCoord() != null) {
            Position workCoord = status.getWorkCoord().getPositionIn(units);
            if (!workPositionXValue.isFocusOwner()) {
                this.setPositionValueColor(this.workPositionXValue, this.workPositionXValue.getText(), workCoord.x);
                this.workPositionXValue.setText(decimalFormatter.format(workCoord.x));
            }

            if (!workPositionYValue.isFocusOwner()) {
                this.setPositionValueColor(this.workPositionYValue, this.workPositionYValue.getText(), workCoord.y);
                this.workPositionYValue.setText(decimalFormatter.format(workCoord.y));
            }

            if (!workPositionZValue.isFocusOwner()) {
                this.setPositionValueColor(this.workPositionZValue, this.workPositionZValue.getText(), workCoord.z);
                this.workPositionZValue.setText(decimalFormatter.format(workCoord.z));
            }
        }

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

    private void setPositionValueColor(JComponent label, String oldValue, double newValue) {
        if (!oldValue.equals(decimalFormatter.format(newValue))) {
            label.setForeground(ThemeColors.RED);
        } else {
            label.setForeground(ThemeColors.LIGHT_BLUE);
        }
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
        } else if (state == ControllerState.IDLE) {
            background = ThemeColors.GREY;
        }

        this.activeStatePanel.setBackground(background);
        this.activeStateValueLabel.setText(text.toUpperCase());
    }

    private void resetCoordinateButton(Axis coord) {
        try {
            this.backend.resetCoordinateToZero(coord);
        } catch (Exception ex) {
            displayErrorDialog(ex.getMessage());
        }
    }
}

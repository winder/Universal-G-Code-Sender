/*
    Copyright 2016-2017 Will Winder

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

import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.listeners.ControllerListenerAdapter;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.listeners.ControllerStatus.EnabledPins;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.uielements.components.RoundedPanel;
import com.willwinder.universalgcodesender.uielements.helpers.FontManager;
import com.willwinder.universalgcodesender.uielements.helpers.SteppedSizeManager;
import com.willwinder.universalgcodesender.uielements.helpers.ThemeColors;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
public class MachineStatusPanel extends JPanel implements UGSEventListener {

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

    private final JLabel machinePositionXValue = new JLabel("0.00");
    private final JLabel machinePositionYValue = new JLabel("0.00");
    private final JLabel machinePositionZValue = new JLabel("0.00");

    private final JLabel workPositionXValue = new JLabel("0.00");
    private final JLabel workPositionYValue = new JLabel("0.00");
    private final JLabel workPositionZValue = new JLabel("0.00");

    private final JLabel feedValue = new JLabel("0");
    private final JLabel spindleSpeedValue = new JLabel("0");

    private final JLabel gStatesLabel = new JLabel();

    private final RoundedPanel pinStatePanel = new RoundedPanel(COMMON_RADIUS);
    private final JLabel pinStatesLabel = new JLabel(" ");

    private List<JComponent> axisResetControls = new ArrayList<>(3);

    private final FontManager fontManager = new FontManager();

    private final BackendAPI backend;
    private final Timer statePollTimer;

    private Units units;
    private DecimalFormat decimalFormatter;

    public MachineStatusPanel(BackendAPI backend) {
        this.backend = backend;
        if (this.backend != null) {
            this.backend.addUGSEventListener(this);
            this.backend.addControllerListener(createControllerListener());
        }
        statePollTimer = createTimer();

        initFonts();
        initComponents();
        initSizer();

        if (this.backend.getSettings().getDefaultUnits().equals(Units.MM.abbreviation)) {
            setUnits(Units.MM);
        } else {
            setUnits(Units.INCH);
        }

        updateControls();
    }

    private void initSizer() {
        SteppedSizeManager sizer = new SteppedSizeManager(this,
                new Dimension(240, 495),
                new Dimension(310, 570));
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
        activeStateValueLabel.setFont(fontManager.getActiveStateFont());

        activeStatePanel.setLayout(new MigLayout(debug + "fill, inset 0 5 0 5"));
        if (backend.getSettings().isDisplayStateColor()) {
            activeStatePanel.setBackground(Color.BLACK);
        } else {
            activeStatePanel.setBackground(Color.WHITE);
        }
        activeStatePanel.setForeground(ThemeColors.VERY_DARK_GREY);
        activeStatePanel.add(activeStateValueLabel, "al center");
        activeStateValueLabel.setBorder(BorderFactory.createEmptyBorder());
        add(activeStatePanel, "growx");

        addAxisPanel('X', workPositionXValue, machinePositionXValue);
        addAxisPanel('Y', workPositionYValue, machinePositionYValue);
        addAxisPanel('Z', workPositionZValue, machinePositionZValue);

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

        JPanel pinStateLayoutPanel = new JPanel(new MigLayout("insets 0"));
        pinStateLayoutPanel.setBackground(transparent);
        JLabel pinAlarmLabel = new JLabel(ALARM);
        pinStateLayoutPanel.add(pinAlarmLabel);

        pinStatePanel.setLayout(new MigLayout("insets 0 5 0 5"));
        pinStatePanel.setBackground(transparent);
        pinStatePanel.setForeground(ThemeColors.GREY);
        pinStatePanel.add(pinStatesLabel);
        pinStateLayoutPanel.add(pinStatePanel);

        add(pinStateLayoutPanel, "align center");

        Color bkg = getBackground();
        int value = bkg.getRed() + bkg.getBlue() + bkg.getGreen();
        boolean panelIsLight = value > 385;
        Color panelTextColor;
        if (panelIsLight) panelTextColor = Color.BLACK;
        else panelTextColor = ThemeColors.ORANGE;
        pinAlarmLabel.setForeground(ThemeColors.GREY);
        pinStatesLabel.setForeground(ThemeColors.GREY);
        setForegroundColor(panelTextColor, feedLabel, feedValue, spindleSpeedLabel, spindleSpeedValue, gStatesLabel);

        setAllCaps(feedLabel, feedValue, spindleSpeedLabel, spindleSpeedValue);

        fontManager.addPropertyLabel(feedLabel, spindleSpeedLabel, pinAlarmLabel, pinStatesLabel, gStatesLabel);
        fontManager.addSpeedLabel(feedValue, spindleSpeedValue);
        fontManager.applyFonts(0);

        statePollTimer.start();
    }

    private void setForegroundColor(Color color, JComponent... components) {
        Arrays.stream(components).forEach(c -> c.setForeground(color));
    }

    private void setAllCaps(JLabel... labels) {
        Arrays.stream(labels).forEach(l -> l.setText(l.getText().toUpperCase()));
    }

    private void addAxisPanel(char axis, JLabel work, JLabel machine) {
        RoundedPanel axisPanel = new RoundedPanel(COMMON_RADIUS);
        axisPanel.setBackground(ThemeColors.VERY_DARK_GREY);
        axisPanel.setForeground(ThemeColors.LIGHT_BLUE);
        axisPanel.setLayout(new MigLayout("fillx, wrap 2, inset 7, gap 0", "[left][grow, right]"));

        RoundedPanel resetPanel = new RoundedPanel(COMMON_RADIUS);
        resetPanel.setForeground(ThemeColors.LIGHT_BLUE);
        resetPanel.setBackground(ThemeColors.DARK_BLUE_GREY);
        resetPanel.setHoverBackground(ThemeColors.MED_BLUE_GREY);
        resetPanel.setLayout(new MigLayout("inset 10 20 10 20"));
        JLabel axisLabel = new JLabel(String.valueOf(axis));
        axisLabel.setForeground(ThemeColors.LIGHT_BLUE);
        resetPanel.add(axisLabel, "al center, dock center, id axis");
        JLabel zeroLabel = new JLabel("0");
        zeroLabel.setForeground(ThemeColors.LIGHT_BLUE);
        resetPanel.add(zeroLabel, "pos (axis.x + axis.w - 4) (axis.y + axis.h - 20)");

        work.setForeground(ThemeColors.LIGHT_BLUE);
        machine.setForeground(ThemeColors.LIGHT_BLUE);
        axisPanel.add(resetPanel, "sy 2");
        axisPanel.add(work);
        axisPanel.add(machine, "span 2");

        fontManager.addAxisResetLabel(axisLabel);
        fontManager.addAxisResetZeroLabel(zeroLabel);
        fontManager.addWorkCoordinateLabel(work);
        fontManager.addMachineCoordinateLabel(machine);

        add(axisPanel,"growx, span 2");

        resetPanel.addClickListener(() -> resetCoordinateButton(axis) );
        axisResetControls.add(resetPanel);
    }

    private Timer createTimer() {
        return new Timer((int) REFRESH_RATE.toMillis(), (ae) -> EventQueue.invokeLater(() -> {
            if (! backend.isConnected()) {
                activeStateValueLabel.setText(OFFLINE);
                if (backend.getSettings().isDisplayStateColor()) {
                    activeStatePanel.setBackground(Color.BLACK);
                }
            }
            GcodeState state = backend.getGcodeState();
            if (state == null) {
                gStatesLabel.setText("--");
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append(state.currentMotionMode).append(" ");
                sb.append(state.units).append(" ");
                sb.append(state.feedMode).append(" ");
                sb.append(state.distanceMode).append(" ");
                sb.append(state.offset).append(" ");
                sb.append(state.plane.code).append(" ");
                trimLastSpace(sb);
                gStatesLabel.setText(sb.toString());
            }
        }));
    }

    private void setUnits(Units u) {
        if (u == null || units == u) return;
        units = u;
        switch(u) {
            case MM:
                this.decimalFormatter = new DecimalFormat("0.00");
                break;
            case INCH:
                this.decimalFormatter = new DecimalFormat("0.000");
                break;
            default:
                units = Units.MM;
                this.decimalFormatter = new DecimalFormat("0.00");
                break;
        }
    }

    @Override
    public void doLayout() {
        super.doLayout();
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt.isStateChangeEvent()) {
            updateControls();
        }
    }

    private void updateControls() {
        axisResetControls.forEach(c -> c.setEnabled(backend.isIdle()));

        if (!backend.isConnected()) {
            // Clear out the status color.
            this.setStatusColorForState("");
        }
    }

    private ControllerListener createControllerListener() {
        return new ControllerListenerAdapter() {
            @Override
            public void statusStringListener(ControllerStatus status) {
                onStatusStringReceived(status);
            }
        };
    }

    private void onStatusStringReceived(ControllerStatus status) {
        this.activeStateValueLabel.setText(status.getState().toUpperCase());
        this.setStatusColorForState(status.getState());

        if (status.getEnabledPins() != null) {

            EnabledPins ep = status.getEnabledPins();

            StringBuilder sb = new StringBuilder();
            if (ep.X) sb.append(PIN_X).append(" ");
            if (ep.Y) sb.append(PIN_Y).append(" ");
            if (ep.Z) sb.append(PIN_Z).append(" ");
            if (ep.Probe) sb.append(PIN_PROBE).append(" ");
            if (ep.Door) sb.append(PIN_DOOR).append(" ");
            if (ep.Hold) sb.append(PIN_HOLD).append(" ");
            if (ep.SoftReset) sb.append(PIN_SOFT_RESET).append(" ");
            if (ep.CycleStart) sb.append(PIN_CYCLE_STARY).append(" ");
            trimLastSpace(sb);
            pinStatesLabel.setText(sb.toString());
            pinStatesLabel.setForeground(ThemeColors.RED);
            pinStatePanel.setForeground(ThemeColors.RED);
        } else {
            pinStatesLabel.setText(" ");
            pinStatesLabel.setForeground(ThemeColors.GREY);
        }

        if (status.getMachineCoord() != null) {
            this.setUnits(status.getMachineCoord().getUnits());

            this.setPositionValueColor(this.machinePositionXValue, status.getMachineCoord().x);
            this.machinePositionXValue.setText(decimalFormatter.format(status.getMachineCoord().x));

            this.setPositionValueColor(this.machinePositionYValue, status.getMachineCoord().y);
            this.machinePositionYValue.setText(decimalFormatter.format(status.getMachineCoord().y));

            this.setPositionValueColor(this.machinePositionZValue, status.getMachineCoord().z);
            this.machinePositionZValue.setText(decimalFormatter.format(status.getMachineCoord().z));
        }

        if (status.getWorkCoord() != null) {
            this.setUnits(status.getWorkCoord().getUnits());

            this.setPositionValueColor(this.workPositionXValue, status.getWorkCoord().x);
            this.workPositionXValue.setText(decimalFormatter.format(status.getWorkCoord().x));

            this.setPositionValueColor(this.workPositionYValue, status.getWorkCoord().y);
            this.workPositionYValue.setText(decimalFormatter.format(status.getWorkCoord().y));

            this.setPositionValueColor(this.workPositionZValue, status.getWorkCoord().z);
            this.workPositionZValue.setText(decimalFormatter.format(status.getWorkCoord().z));
        }

        // Use real-time values if available, otherwise show the target values.
        int feedSpeed = status.getFeedSpeed() != null
                ? status.getFeedSpeed().intValue()
                : (int) this.backend.getGcodeState().speed;
        this.feedValue.setText(Integer.toString(feedSpeed));

        int spindleSpeed = status.getSpindleSpeed() != null
                ? status.getSpindleSpeed().intValue()
                : (int) this.backend.getGcodeState().spindleSpeed;
        this.spindleSpeedValue.setText(Integer.toString(spindleSpeed));
    }

    private void trimLastSpace(StringBuilder sb) {
        if (sb.length() > 0) sb.deleteCharAt(sb.length() - 1);
    }

    private void setPositionValueColor(JLabel label, double newValue) {
        if (!label.getText().equals(decimalFormatter.format(newValue))) {
            label.setForeground(ThemeColors.RED);
        } else {
            label.setForeground(ThemeColors.LIGHT_BLUE);
        }
    }

    private void setStatusColorForState(String state) {
        if (backend.getSettings().isDisplayStateColor()) {
            Color background = ThemeColors.GREY;
            if (state.equalsIgnoreCase("Alarm")) {
                background = ThemeColors.RED;
            } else if (state.equalsIgnoreCase("Hold")) {
                background = ThemeColors.ORANGE;
            } else if (state.equalsIgnoreCase("Run")) {
                background = ThemeColors.GREEN;
            } else if (state.equalsIgnoreCase("Jog")) {
                background = ThemeColors.GREEN;
            } else if (state.equalsIgnoreCase("Check")) {
                background = ThemeColors.LIGHT_BLUE;
            }
            this.activeStatePanel.setBackground(background);
        }
    }

    private void resetCoordinateButton(char coord) {
        try {
            this.backend.resetCoordinateToZero(coord);
        } catch (Exception ex) {
            displayErrorDialog(ex.getMessage());
        }
    }
}

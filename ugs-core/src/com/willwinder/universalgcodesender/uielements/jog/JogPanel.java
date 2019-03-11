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
package com.willwinder.universalgcodesender.uielements.jog;

import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.Alarm;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.services.JogService;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.utils.Settings;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class JogPanel extends JPanel implements UGSEventListener, ControllerListener {

    private final StepSizeSpinner xyStepSizeSpinner = new StepSizeSpinner();
    private final StepSizeSpinner zStepSizeSpinner = new StepSizeSpinner();
    private final JLabel stepSizeLabel = new JLabel(Localization.getString("mainWindow.swing.stepSizeLabel"));
    private final JLabel stepSizeLabelZ = new JLabel(Localization.getString("mainWindow.swing.stepSizeZLabel"));

    private final StepSizeSpinner feedRateSpinner = new StepSizeSpinner();
    private final JLabel feedRateLabel = new JLabel(Localization.getString("mainWindow.swing.feedRateLabel"));

    private final JButton unitButton = new JButton();
    private final JCheckBox keyboardMovementEnabled = new JCheckBox(Localization.getString("mainWindow.swing.arrowMovementEnabled"));

    private final JButton xMinusButton = new JButton("X-");
    private final JButton xPlusButton = new JButton("X+");
    private final JButton yMinusButton = new JButton("Y-");
    private final JButton yPlusButton = new JButton("Y+");
    private final JButton zMinusButton = new JButton("Z-");
    private final JButton zPlusButton = new JButton("Z+");

    private final BackendAPI backend;
    private final JogService jogService;

    private final boolean showKeyboardToggle;

    public JogPanel(BackendAPI backend, JogService jogService, boolean showKeyboardToggle) {
        this.backend = backend;
        this.showKeyboardToggle = showKeyboardToggle;

        this.jogService = jogService;

        if (this.backend != null) {
            this.backend.addUGSEventListener(this);
            this.backend.addControllerListener(this);
            loadSettings();
        }

        initComponents();

        // Update jog service whenever the spinner is changed.
        xyStepSizeSpinner.addChangeListener(cl -> jogService.setStepSizeXY(xyStepSizeSpinner.getValue()));
        zStepSizeSpinner.addChangeListener(cl -> jogService.setStepSizeZ(zStepSizeSpinner.getValue()));
        feedRateSpinner.addChangeListener(cl -> jogService.setFeedRate(feedRateSpinner.getValue()));

        // Hookup buttons to actions.
        unitButton.addActionListener(e -> toggleUnits());
        xPlusButton.addActionListener(e -> xPlusButtonActionPerformed());
        xMinusButton.addActionListener(e -> xMinusButtonActionPerformed());
        yPlusButton.addActionListener(e -> yPlusButtonActionPerformed());
        yMinusButton.addActionListener(e -> yMinusButtonActionPerformed());
        zPlusButton.addActionListener(e -> zPlusButtonActionPerformed());
        zMinusButton.addActionListener(e -> zMinusButtonActionPerformed());
    }

    private void keyboardMovementSelected() {
        backend.getSettings().setManualModeEnabled(keyboardMovementEnabled.isSelected());
    }

    private void initComponents() {
        updateUnitButton();

        double feedRate = backend.getSettings().getJogFeedRate();
        feedRateSpinner.setModel(new SpinnerNumberModel(feedRate, null, null, 10));

        keyboardMovementEnabled.setSelected(showKeyboardToggle && backend.getSettings().isManualModeEnabled());
        keyboardMovementEnabled.addActionListener(al -> keyboardMovementSelected());

        // MigLayout... 3rd party layout library.
        MigLayout layout = new MigLayout("fillx, wrap 4");
        setLayout(layout);

        if (showKeyboardToggle) {
            add(keyboardMovementEnabled, "al left, span 4");
        }

        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout("ins 0, fillx, wrap 2"));

        panel.add(stepSizeLabel);
        panel.add(xyStepSizeSpinner, "growx");
        panel.add(stepSizeLabelZ);
        panel.add(zStepSizeSpinner, "growx");
        panel.add(feedRateLabel);
        panel.add(feedRateSpinner, "growx");

        add(unitButton, "grow");
        add(panel, "grow, span 3");


        add(xMinusButton, "spany 2, w 60!, h 50!");
        add(yPlusButton, "w 60!, h 50!");
        add(xPlusButton, "spany 2, w 60!, h 50!");
        add(zPlusButton, "w 60!, h 50!");
        add(yMinusButton, "w 60!, h 50!");
        add(zMinusButton, "w 60!, h 50!");

        updateManualControls(false);
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt.isStateChangeEvent() || evt.isSettingChangeEvent()) {
            syncWithJogService();
            updateControls();
        }
    }

    private void syncWithJogService() {
        Settings s = backend.getSettings();
        xyStepSizeSpinner.setValue(s.getManualModeStepSize());
        zStepSizeSpinner.setValue(s.getzJogStepSize());
        feedRateSpinner.setValue(s.getJogFeedRate());
    }

    private void updateControls() {
        keyboardMovementEnabled.setSelected(backend.getSettings().isManualModeEnabled());
        syncWithJogService();

        updateUnitButton();

        switch (backend.getControlState()) {
            case COMM_DISCONNECTED:
            case COMM_SENDING_PAUSED:
            case COMM_CHECK:
            default:
                updateManualControls(false);
                break;
            case COMM_IDLE:
            case COMM_SENDING:
                updateManualControls(!backend.isSendingFile());
                break;
        }
    }

    private void toggleUnits() {
        if (getUnits() == Units.MM) {
            jogService.setUnits(Units.INCH);
        } else {
            jogService.setUnits(Units.MM);
        }
        updateUnitButton();
    }

    private void updateUnitButton() {
        if (getUnits() == Units.INCH){
            unitButton.setText("inch");
        } else {
            unitButton.setText("mm");
        }
    }

    public void increaseStepActionPerformed() {
        jogService.increaseXYStepSize();
        xyStepSizeSpinner.setValue(getxyStepSize());
    }

    public void decreaseStepActionPerformed() {
        jogService.decreaseXYStepSize();
        xyStepSizeSpinner.setValue(getxyStepSize());
    }

    public void multiplyStepActionPerformed() {
        jogService.multiplyXYStepSize();
        xyStepSizeSpinner.setValue(getxyStepSize());
    }

    public void divideStepActionPerformed() {
        jogService.divideXYStepSize();
        xyStepSizeSpinner.setValue(getxyStepSize());
    }

    @Override
    public void controlStateChange(UGSEvent.ControlState state) {
    }

    @Override
    public void fileStreamComplete(String filename, boolean success) {

    }

    @Override
    public void receivedAlarm(Alarm alarm) {

    }

    @Override
    public void commandSkipped(GcodeCommand command) {

    }

    @Override
    public void commandSent(GcodeCommand command) {

    }

    @Override
    public void commandComment(String comment) {
    }

    @Override
    public void probeCoordinates(Position p) {
    }

    @Override
    public void commandComplete(GcodeCommand command) {

    }

    @Override
    public void statusStringListener(ControllerStatus status) {
    }

    public void saveSettings() {
        backend.getSettings().setManualModeEnabled(keyboardMovementEnabled.isSelected());
    }

    public void loadSettings() {
        syncWithJogService();
        keyboardMovementEnabled.setSelected(backend.getSettings().isManualModeEnabled());
        updateUnitButton();
    }

    private Units getUnits() {
        return jogService.getUnits();
    }

    private double getxyStepSize() {
        double stepSize = xyStepSizeSpinner.getValue();
        backend.getSettings().setManualModeStepSize(stepSize);
        return stepSize;
    }

    private double getzStepSize() {
        double stepSize = zStepSizeSpinner.getValue();
        backend.getSettings().setzJogStepSize(stepSize);
        return stepSize;
    }

    public boolean isKeyboardMovementEnabled() {
        return keyboardMovementEnabled.isSelected() && xPlusButton.isEnabled();
    }

    public void doJog(int x, int y) {
        try {
            jogService.adjustManualLocationXY(x, y);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doJog(int z) {
        try {
            jogService.adjustManualLocationZ(z);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void xPlusButtonActionPerformed() {
        this.doJog(1, 0);
    }

    public void xMinusButtonActionPerformed() {
        doJog(-1, 0);
    }


    public void yPlusButtonActionPerformed() {
        doJog(0, 1);
    }

    public void yMinusButtonActionPerformed() {
        doJog(0, -1);
    }

    public void zPlusButtonActionPerformed() {
        doJog(1);
    }

    public void zMinusButtonActionPerformed() {
        doJog(-1);
    }

    public void updateManualControls(boolean enabled) {
        xMinusButton.setEnabled(enabled);
        xPlusButton.setEnabled(enabled);
        yMinusButton.setEnabled(enabled);
        yPlusButton.setEnabled(enabled);
        zMinusButton.setEnabled(enabled);
        zPlusButton.setEnabled(enabled);
        stepSizeLabel.setEnabled(enabled);
        stepSizeLabelZ.setEnabled(enabled);
        xyStepSizeSpinner.setEnabled(enabled);
        feedRateLabel.setEnabled(enabled);
        feedRateSpinner.setEnabled(enabled);
        unitButton.setEnabled(enabled);

        boolean zStepEnabled = enabled && backend.getSettings().useZStepSize();
        zStepSizeSpinner.setEnabled(zStepEnabled);
        stepSizeLabelZ.setEnabled(zStepEnabled);
    }
}

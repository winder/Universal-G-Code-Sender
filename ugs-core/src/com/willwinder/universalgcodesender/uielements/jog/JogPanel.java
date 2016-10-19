package com.willwinder.universalgcodesender.uielements.jog;

import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.services.JogService;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.uielements.IChanged;
import javax.swing.BorderFactory;
import net.miginfocom.swing.MigLayout;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JCheckBox;

public class JogPanel extends JPanel implements UGSEventListener, ControllerListener, IChanged {

    private final StepSizeSpinner xyStepSizeSpinner = new StepSizeSpinner();
    private final StepSizeSpinner zStepSizeSpinner = new StepSizeSpinner();
    private final JLabel stepSizeLabel = new JLabel(Localization.getString("mainWindow.swing.stepSizeLabel"));

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

    private boolean statusUpdated = false;
    private final boolean showKeyboardToggle;

    public JogPanel(BackendAPI backend, JogService jogService, boolean showKeyboardToggle) {
        setBorder(BorderFactory.createTitledBorder(
                Localization.getString("mainWindow.swing.keyboardMovementPanel")));
        this.backend = backend;
        this.showKeyboardToggle = showKeyboardToggle;

        this.jogService = jogService;
        if (jogService != null) {
            jogService.addChangeListener(this);
        }

        if (this.backend != null) {
            this.backend.addUGSEventListener(this);
            this.backend.addControllerListener(this);
            loadSettings();
        }

        initComponents();

        // Update jog service whenever the spinner is changed.
        xyStepSizeSpinner.addChangeListener(cl -> jogService.setStepSize(xyStepSizeSpinner.getValue()));
        zStepSizeSpinner.addChangeListener(cl -> jogService.setStepSize(zStepSizeSpinner.getValue()));

        // Hookup buttons to actions.
        unitButton.addActionListener(e -> unitButtonActionPerformed());
        xPlusButton.addActionListener(e -> xPlusButtonActionPerformed());
        xMinusButton.addActionListener(e -> xMinusButtonActionPerformed());
        yPlusButton.addActionListener(e -> yPlusButtonActionPerformed());
        yMinusButton.addActionListener(e -> yMinusButtonActionPerformed());
        zPlusButton.addActionListener(e -> zPlusButtonActionPerformed());
        zMinusButton.addActionListener(e -> zMinusButtonActionPerformed());
    }

    private void initComponents() {
        keyboardMovementEnabled.setSelected(showKeyboardToggle ? 
                backend.getSettings().isManualModeEnabled():
                false);

        // MigLayout... 3rd party layout library.
        MigLayout layout = new MigLayout("fill, wrap 4");
        setLayout(layout);

        if (showKeyboardToggle) {
            add(keyboardMovementEnabled, "al left, span 4");
        }

        add(unitButton, "grow");
//        add(stepSizeLabel, "al right");
        add(xyStepSizeSpinner, "span 3, split 2, al left, w 75!");
        add(zStepSizeSpinner, "w 75!");

        add(xMinusButton, "spany 2, w 50!, h 50!");
        add(yPlusButton, "w 50!, h 50!");
        add(xPlusButton, "spany 2, w 50!, h 50!");
        add(zPlusButton, "w 50!, h 50!");
        add(yMinusButton, "w 50!, h 50!");
        add(zMinusButton, "w 50!, h 50!");

        updateManualControls(false);
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt.isStateChangeEvent()) {
            updateControls();
        }
    }

    private void updateControls() {
        keyboardMovementEnabled.setSelected(backend.getSettings().isManualModeEnabled());
        xyStepSizeSpinner.setValue(backend.getSettings().getManualModeStepSize());
        boolean unitsAreMM = backend.getSettings().getDefaultUnits().equals(Units.MM.abbreviation);
        updateUnitButton(unitsAreMM);

        switch (backend.getControlState()) {
            case COMM_DISCONNECTED:
                updateManualControls(false);
                statusUpdated = false;
                break;
            case COMM_IDLE:
                //if (statusUpdated) {
                    updateManualControls(true);
                //}
                break;
            case COMM_SENDING:
                updateManualControls(false);
                break;
            case COMM_SENDING_PAUSED:
                break;
            default:
        }
    }

    private void updateUnitButton(boolean unitsAreMM) {
        if (unitsAreMM) {
            unitButton.setText(Units.MM.abbreviation);
        } else {
            unitButton.setText(Units.MM.abbreviation);
        }
    }

    public void increaseStepActionPerformed() {
        jogService.increaseStepSize();
        xyStepSizeSpinner.setValue(getxyStepSize());
    }

    public void decreaseStepActionPerformed() {
        jogService.decreaseStepSize();
        xyStepSizeSpinner.setValue(getxyStepSize());
    }

    public void multiplyStepActionPerformed() {
        jogService.multiplyStepSize();
        xyStepSizeSpinner.setValue(getxyStepSize());
    }

    public void divideStepActionPerformed() {
        jogService.divideStepSize();
        xyStepSizeSpinner.setValue(getxyStepSize());
    }

    @Override
    public void controlStateChange(UGSEvent.ControlState state) {
    }

    @Override
    public void fileStreamComplete(String filename, boolean success) {

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
    public void commandComplete(GcodeCommand command) {

    }

    @Override
    public void messageForConsole(MessageType type, String msg) {

    }

    @Override
    public void postProcessData(int numRows) {

    }

    @Override
    public void statusStringListener(ControllerStatus status) {
        if (!statusUpdated) {
            if (backend.isConnected()) {
                updateManualControls(true);
            }
        }
        statusUpdated = true;
    }

    public void saveSettings() {
        backend.getSettings().setManualModeEnabled(keyboardMovementEnabled.isSelected());
    }

    public void loadSettings() {
        keyboardMovementEnabled.setSelected(backend.getSettings().isManualModeEnabled());
        xyStepSizeSpinner.setValue(jogService.getStepSize());
        zStepSizeSpinner.setValue(jogService.getStepSizeZ());
        boolean unitsAreMM = getUnits() == Units.MM;
        updateUnitButton(unitsAreMM);
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
            jogService.adjustManualLocation(x, y, 0);
            //backend.adjustManualLocation(x, y, 0, getxyStepSize(), getUnits());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doJog(int z) {
        try {
            jogService.adjustManualLocation(0, 0, z);
            //backend.adjustManualLocation(0, 0, z, getzStepSize(), getUnits());
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

    public void unitButtonActionPerformed() {
        updateUnitButton(!unitButton.getText().equals(Units.MM.abbreviation));
    }

    public void updateManualControls(boolean enabled) {
        keyboardMovementEnabled.setEnabled(enabled);

        xMinusButton.setEnabled(enabled);
        xPlusButton.setEnabled(enabled);
        yMinusButton.setEnabled(enabled);
        yPlusButton.setEnabled(enabled);
        zMinusButton.setEnabled(enabled);
        zPlusButton.setEnabled(enabled);
        stepSizeLabel.setEnabled(enabled);
        xyStepSizeSpinner.setEnabled(enabled);
        zStepSizeSpinner.setEnabled(enabled);
        feedRateLabel.setEnabled(enabled);
        feedRateSpinner.setEnabled(enabled);
        unitButton.setEnabled(enabled);
    }

    @Override
    public void changed() {
    }


}

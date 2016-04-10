package com.willwinder.universalgcodesender.uielements.jog;

import com.willwinder.universalgcodesender.Utils;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.uielements.StepSizeSpinnerModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;

public class JogPanel extends JPanel implements UGSEventListener, ControllerListener {

    private final JSpinner stepSizeSpinner = new JSpinner();
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

    public JogPanel() {
        this(null);
    }

    public JogPanel(BackendAPI backend) {
        setBorder(BorderFactory.createTitledBorder(Localization.getString("mainWindow.swing.keyboardMovementPanel")));
        setMinimumSize(new java.awt.Dimension(247, 200));
        setPreferredSize(new java.awt.Dimension(247, 200));
        setMaximumSize(new java.awt.Dimension(247, 200));


        this.backend = backend;
        if (this.backend != null) {
            this.backend.addUGSEventListener(this);
            this.backend.addControllerListener(this);
            loadSettings();
        }
        unitButton.addActionListener(e -> JogPanel.this.unitButtonActionPerformed());
        xPlusButton.addActionListener(e -> JogPanel.this.xPlusButtonActionPerformed());
        xMinusButton.addActionListener(e -> JogPanel.this.xMinusButtonActionPerformed());
        yPlusButton.addActionListener(e -> JogPanel.this.yPlusButtonActionPerformed());
        yMinusButton.addActionListener(e -> JogPanel.this.yMinusButtonActionPerformed());
        zPlusButton.addActionListener(e -> JogPanel.this.zPlusButtonActionPerformed());
        zMinusButton.addActionListener(e -> JogPanel.this.zMinusButtonActionPerformed());
        stepSizeSpinner.setModel(new StepSizeSpinnerModel(1.0, 0.0, null, 1.0));
        initComponents();
    }


    private void initComponents() {
        // MigLayout... 3rd party layout library.
        MigLayout layout = new MigLayout("fill, wrap 4");
        setLayout(layout);
        add(keyboardMovementEnabled, "al left, span 4");
        add(stepSizeLabel, "al right");
        add(stepSizeSpinner, "grow, span 2");
        add(unitButton, "grow");
        add(xMinusButton, "spany 2, w 50!, h 50!");
        add(yPlusButton, "w 50!, h 50!");
        add(xPlusButton, "spany 2, w 50!, h 50!");
        add(zPlusButton, "w 50!, h 50!");
        add(yMinusButton, "w 50!, h 50!");
        add(zMinusButton, "w 50!, h 50!");
    }

    @Override
    public void doLayout() {
        super.doLayout();
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
//        if (evt.isStateChangeEvent()) {
//            updateControls();
//        }
    }

    private void updateControls() {
        keyboardMovementEnabled.setSelected(backend.getSettings().isManualModeEnabled());
        setStepSize(backend.getSettings().getManualModeStepSize());
        boolean unitsAreMM = backend.getSettings().getDefaultUnits().equals("mm");
        updateUnitButton(unitsAreMM);
    }

    private void updateUnitButton(boolean unitsAreMM) {
        if (unitsAreMM) {
            unitButton.setText("mm");
        } else {
            unitButton.setText("\"");
        }
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
    public void messageForConsole(String msg, Boolean verbose) {

    }

    @Override
    public void postProcessData(int numRows) {

    }

    @Override
    public void statusStringListener(String state, Position machineCoord, Position workCoord) {

    }

    public void saveSettings() {
        backend.getSettings().setDefaultUnits(unitButton.getText().equals("\"") ? "inch" : "mm");
        backend.getSettings().setManualModeStepSize(getStepSize());
        backend.getSettings().setManualModeEnabled(keyboardMovementEnabled.isSelected());
    }

    public void loadSettings() {
        keyboardMovementEnabled.setSelected(backend.getSettings().isManualModeEnabled());
        setStepSize(backend.getSettings().getManualModeStepSize());
        boolean unitsAreMM = backend.getSettings().getDefaultUnits().equals("mm");
        updateUnitButton(unitsAreMM);
    }

    private com.willwinder.universalgcodesender.model.Utils.Units getUnits() {
        return unitButton.getText().equals("mm") ? com.willwinder.universalgcodesender.model.Utils.Units.MM : com.willwinder.universalgcodesender.model.Utils.Units.INCH;
    }

    private double getStepSize() {
        try {
            this.stepSizeSpinner.commitEdit();
        } catch (ParseException e) {
            this.stepSizeSpinner.setValue(0.0);
        }
        BigDecimal bd = new BigDecimal(this.stepSizeSpinner.getValue().toString()).setScale(3, RoundingMode.HALF_EVEN);
        return bd.doubleValue();
    }

    private void setStepSize(double val) {
        if (val < 0) {
            val = 0;
        }
        BigDecimal bd = new BigDecimal(val).setScale(3, RoundingMode.HALF_EVEN);
        val = bd.doubleValue();
        this.stepSizeSpinner.setValue(val);
    }

    public void increaseStepActionPerformed(java.awt.event.ActionEvent evt) {
        double stepSize = this.getStepSize();
        if (stepSize >= 1) {
            stepSize++;
        } else if (stepSize >= 0.1) {
            stepSize = stepSize + 0.1;
        } else if (stepSize >= 0.01) {
            stepSize = stepSize + 0.01;
        } else {
            stepSize = 0.01;
        }
        this.setStepSize(stepSize);
    }

    public void decreaseStepActionPerformed(java.awt.event.ActionEvent evt) {
        double stepSize = this.getStepSize();
        if (stepSize > 1) {
            stepSize--;
        } else if (stepSize > 0.1) {
            stepSize = stepSize - 0.1;
        } else if (stepSize > 0.01) {
            stepSize = stepSize - 0.01;
        }
        this.setStepSize(stepSize);
    }

    public void divideStepActionPerformed(java.awt.event.ActionEvent evt) {
        double stepSize = this.getStepSize();

        if (stepSize > 100) {
            stepSize = 100;
        } else if (stepSize <= 100 && stepSize > 10) {
            stepSize = 10;
        } else if (stepSize <= 10 && stepSize > 1) {
            stepSize = 1;
        } else if (stepSize <= 1 && stepSize > 0.1) {
            stepSize = 0.1;
        } else if (stepSize <= 0.1 ) {
            stepSize = 0.01;
        }

        this.setStepSize(stepSize);
    }

    public void multiplyStepActionPerformed(java.awt.event.ActionEvent evt) {
        double stepSize = this.getStepSize();

        if (stepSize < 0.01) {
            stepSize = 0.01;
        } else if (stepSize >= 0.01 && stepSize < 0.1) {
            stepSize = 0.1;
        }  else if (stepSize >= 0.1 && stepSize < 1) {
            stepSize = 1;
        }  else if (stepSize >= 1 && stepSize < 10) {
            stepSize = 10;
        }  else if (stepSize >= 10) {
            stepSize = 100;
        }

        this.setStepSize(stepSize);
    }

    public boolean isKeyboardMovementEnabled() {
        return keyboardMovementEnabled.isSelected() && xPlusButton.isEnabled();
    }

    public void adjustManualLocation(int x, int y, int z) {
        try {
            backend.adjustManualLocation(x, y, z, getStepSize(), getUnits());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void xPlusButtonActionPerformed() {
        adjustManualLocation(1, 0, 0);
    }

    public void xMinusButtonActionPerformed() {
        this.adjustManualLocation(-1, 0, 0);

    }


    public void yPlusButtonActionPerformed() {
        this.adjustManualLocation(0, 1, 0);

    }

    public void yMinusButtonActionPerformed() {
        this.adjustManualLocation(0, -1, 0);

    }


    public void zPlusButtonActionPerformed() {
        this.adjustManualLocation(0, 0, 1);

    }

    public void zMinusButtonActionPerformed() {
        this.adjustManualLocation(0, 0, -1);

    }

    public void unitButtonActionPerformed() {
        updateUnitButton(!unitButton.getText().equals("mm"));
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
        stepSizeSpinner.setEnabled(enabled);
        unitButton.setEnabled(enabled);
//        inchRadioButton.setEnabled(enabled);
//        mmRadioButton.setEnabled(enabled);
    }


}

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
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;

public class JogPanel extends JPanel implements UGSEventListener, ControllerListener {

    private final StepSizeSpinner stepSizeSpinner = new StepSizeSpinner();
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
//        setMinimumSize(new java.awt.Dimension(247, 200));
//        setPreferredSize(new java.awt.Dimension(247, 200));
//        setMaximumSize(new java.awt.Dimension(247, 200));


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
        addKeyboardListener();
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

        updateManualControls(false);
    }

    private void addKeyboardListener() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(new KeyEventDispatcher() {
                    @Override
                    public boolean dispatchKeyEvent(KeyEvent e) {
                        // Check context.
                        if (((isKeyboardMovementEnabled()) &&
                                e.getID() == KeyEvent.KEY_PRESSED)) {
                            switch (e.getKeyCode()) {
                                case KeyEvent.VK_RIGHT:
                                case KeyEvent.VK_KP_RIGHT:
                                case KeyEvent.VK_NUMPAD6:
                                    xPlusButtonActionPerformed();
                                    e.consume();
                                    return true;
                                case KeyEvent.VK_LEFT:
                                case KeyEvent.VK_KP_LEFT:
                                case KeyEvent.VK_NUMPAD4:
                                    xMinusButtonActionPerformed();
                                    e.consume();
                                    return true;
                                case KeyEvent.VK_UP:
                                case KeyEvent.VK_KP_UP:
                                case KeyEvent.VK_NUMPAD8:
                                    yPlusButtonActionPerformed();
                                    e.consume();
                                    return true;
                                case KeyEvent.VK_DOWN:
                                case KeyEvent.VK_KP_DOWN:
                                case KeyEvent.VK_NUMPAD2:
                                    yMinusButtonActionPerformed();
                                    e.consume();
                                    return true;
                                case KeyEvent.VK_PAGE_UP:
                                case KeyEvent.VK_NUMPAD9:
                                    zPlusButtonActionPerformed();
                                    e.consume();
                                    return true;
                                case KeyEvent.VK_PAGE_DOWN:
                                case KeyEvent.VK_NUMPAD3:
                                    zMinusButtonActionPerformed();
                                    e.consume();
                                    return true;
                                case KeyEvent.VK_ADD:
                                    increaseStepActionPerformed();
                                    e.consume();
                                    return true;
                                case KeyEvent.VK_SUBTRACT:
                                    decreaseStepActionPerformed();
                                    e.consume();
                                    return true;
                                case KeyEvent.VK_DIVIDE:
                                    divideStepActionPerformed();
                                    e.consume();
                                    return true;
                                case KeyEvent.VK_MULTIPLY:
                                    multiplyStepActionPerformed();
                                    e.consume();
                                    return true;
                                case KeyEvent.VK_INSERT:
                                case KeyEvent.VK_NUMPAD0:
                                    //resetCoordinatesButtonActionPerformed(null);
                                    e.consume();
                                    return true;
                                default:
                                    break;
                            }
                        }

                        return false;
                    }
                });
    }


    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt.isStateChangeEvent()) {
            updateControls();
        }
    }

    private void updateControls() {
        keyboardMovementEnabled.setSelected(backend.getSettings().isManualModeEnabled());
        setStepSize(backend.getSettings().getManualModeStepSize());
        boolean unitsAreMM = backend.getSettings().getDefaultUnits().equals("mm");
        updateUnitButton(unitsAreMM);

        updateManualControls(backend.isConnected());
    }

    private void updateUnitButton(boolean unitsAreMM) {
        if (unitsAreMM) {
            unitButton.setText("mm");
        } else {
            unitButton.setText("\"");
        }
    }

    private void increaseStepActionPerformed() {
        stepSizeSpinner.increaseStep();
    }

    private void decreaseStepActionPerformed() {
        stepSizeSpinner.decreaseStep();
    }

    private void multiplyStepActionPerformed() {
        stepSizeSpinner.multiplyStep();
    }

    private void divideStepActionPerformed() {
        stepSizeSpinner.divideStep();
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
        double stepSize = stepSizeSpinner.getValue();
        backend.getSettings().setManualModeStepSize(stepSize);
        return stepSize;
    }

    private void setStepSize(double val) {
        stepSizeSpinner.setValue(val);
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
    }


}

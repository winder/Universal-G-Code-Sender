package com.willwinder.universalgcodesender.uielements.jog;

import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.Utils;
import com.willwinder.universalgcodesender.model.Utils.Units;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class JogPanel extends JPanel implements UGSEventListener, ControllerListener {

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

    private boolean statusUpdated = false;

    /**
     * No-Arg constructor to make this control work in the UI builder tools
     * @deprecated Use constructor with BackendAPI.
     */
    @Deprecated
    public JogPanel() {
        this(null);
    }

    public JogPanel(BackendAPI backend) {
        setBorder(BorderFactory.createTitledBorder(Localization.getString("mainWindow.swing.keyboardMovementPanel")));
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
        initComponents();
        addKeyboardListener();
    }

    private void initComponents() {
        // MigLayout... 3rd party layout library.
        MigLayout layout = new MigLayout("fill, wrap 4");
        setLayout(layout);
        add(keyboardMovementEnabled, "al left, span 4");

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
        xyStepSizeSpinner.setValue(backend.getSettings().getManualModeStepSize());
        boolean unitsAreMM = backend.getSettings().getDefaultUnits().equals(Units.MM.abbreviation);
        updateUnitButton(unitsAreMM);

        switch (backend.getControlState()) {
            case COMM_DISCONNECTED:
                updateManualControls(false);
                statusUpdated = false;
                break;
            case COMM_IDLE:
                if (statusUpdated) {
                    updateManualControls(true);
                }
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

    private void increaseStepActionPerformed() {
        xyStepSizeSpinner.increaseStep();
    }

    private void decreaseStepActionPerformed() {
        xyStepSizeSpinner.decreaseStep();
    }

    private void multiplyStepActionPerformed() {
        xyStepSizeSpinner.multiplyStep();
    }

    private void divideStepActionPerformed() {
        xyStepSizeSpinner.divideStep();
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
    public void statusStringListener(String state, Position machineCoord, Position workCoord) {
        if (!statusUpdated) {
            if (backend.isConnected()) {
                updateManualControls(true);
            }
        }
        statusUpdated = true;
    }

    public void saveSettings() {
        backend.getSettings().setDefaultUnits(unitButton.getText().equals(Units.INCH.abbreviation) ? Units.INCH.abbreviation : Units.MM.abbreviation);
        backend.getSettings().setManualModeStepSize(getxyStepSize());
        backend.getSettings().setzJogStepSize(getzStepSize());
        backend.getSettings().setManualModeEnabled(keyboardMovementEnabled.isSelected());
    }

    public void loadSettings() {
        keyboardMovementEnabled.setSelected(backend.getSettings().isManualModeEnabled());
        xyStepSizeSpinner.setValue(backend.getSettings().getManualModeStepSize());
        zStepSizeSpinner.setValue(backend.getSettings().getzJogStepSize());
        boolean unitsAreMM = backend.getSettings().getDefaultUnits().equals(Units.MM.abbreviation);
        updateUnitButton(unitsAreMM);
    }

    private com.willwinder.universalgcodesender.model.Utils.Units getUnits() {
        return unitButton.getText().equals(Units.MM.abbreviation) ? com.willwinder.universalgcodesender.model.Utils.Units.MM : com.willwinder.universalgcodesender.model.Utils.Units.INCH;
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
            backend.adjustManualLocation(x, y, 0, getxyStepSize(), getUnits());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doJog(int z) {
        try {
            backend.adjustManualLocation(0, 0, z, getzStepSize(), getUnits());
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
        unitButton.setEnabled(enabled);
    }


}

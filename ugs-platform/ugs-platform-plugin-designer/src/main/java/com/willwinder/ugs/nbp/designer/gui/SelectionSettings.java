package com.willwinder.ugs.nbp.designer.gui;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.cuttable.CutType;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionEvent;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionListener;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.Settings;
import com.willwinder.universalgcodesender.Utils;
import net.miginfocom.swing.MigLayout;
import org.openide.util.ImageUtilities;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.util.Arrays;
import java.util.EnumMap;

public class SelectionSettings extends JPanel implements SelectionListener {
    private final JTextField widthTextField;
    private final JTextField rotation;
    private final ButtonGroup buttonGroup;
    private transient Controller controller;
    private final JSpinner depthSpinner;
    private final JSpinner feedSpeedSpinner;
    private final JSpinner plungeSpeedSpinner;
    private final JSpinner toolDiameterSpinner;
    private final JTextField heightTextField;
    private transient Entity entity;
    private final EnumMap<CutType, JToggleButton> cutTypeButtonMap = new EnumMap<>(CutType.class);

    public SelectionSettings(Controller controller) {
        this();
        updateController(controller);
    }

    public SelectionSettings() {
        setLayout(new MigLayout("fill, wrap 2"));

        heightTextField = new JTextField("0");
        add(new JLabel("Height"));
        add(heightTextField, "grow");

        widthTextField = new JTextField("0");
        add(new JLabel("Width"));
        add(widthTextField, "grow");

        rotation = new JTextField("0");
        add(new JLabel("Rotation"));
        add(rotation, "grow");

        add(new JSeparator(), "grow, spanx, wrap");

        cutTypeButtonMap.put(CutType.NONE, new JToggleButton(ImageUtilities.loadImageIcon("img/cutnone32.png", false)));
        cutTypeButtonMap.put(CutType.POCKET, new JToggleButton(ImageUtilities.loadImageIcon("img/cutpocket32.png", false)));
        cutTypeButtonMap.put(CutType.INSIDE_PATH, new JToggleButton(ImageUtilities.loadImageIcon("img/cutinside32.png", false)));
        cutTypeButtonMap.put(CutType.OUTSIDE_PATH, new JToggleButton(ImageUtilities.loadImageIcon("img/cutoutside32.png", false)));
        cutTypeButtonMap.put(CutType.ON_PATH, new JToggleButton(ImageUtilities.loadImageIcon("img/cutonpath32.png", false)));


        buttonGroup = new ButtonGroup();
        cutTypeButtonMap.keySet().forEach(key -> {
            JToggleButton button = cutTypeButtonMap.get(key);
            buttonGroup.add(button);
            button.addActionListener((event) -> {
                this.controller.getSelectionManager().getSelection().forEach((selectedEntity -> {
                    if (selectedEntity instanceof Cuttable) {
                        ((Cuttable) selectedEntity).setCutType(key);
                    }
                }));
            });
        });

        add(cutTypeButtonMap.get(CutType.NONE), "span 2, split");
        add(cutTypeButtonMap.get(CutType.POCKET), "split");
        add(cutTypeButtonMap.get(CutType.INSIDE_PATH), "split");
        add(cutTypeButtonMap.get(CutType.OUTSIDE_PATH), "split");
        add(cutTypeButtonMap.get(CutType.ON_PATH), "wrap");

        SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(100d, 0d, 100.0d, 0.1d);
        depthSpinner = new JSpinner(spinnerNumberModel);
        depthSpinner.setPreferredSize(depthSpinner.getPreferredSize());
        add(new JLabel("Cut depth"));
        add(depthSpinner);
        depthSpinner.addChangeListener(this::onDepthChange);

        add(new JSeparator(), "grow, spanx, wrap");


        spinnerNumberModel = new SpinnerNumberModel(1000, 0, 100000, 100);
        feedSpeedSpinner = new JSpinner(spinnerNumberModel);
        add(new JLabel("Feed speed"));
        add(feedSpeedSpinner);
        feedSpeedSpinner.addChangeListener(this::onFeedSpeedChange);

        spinnerNumberModel = new SpinnerNumberModel(1000, 1, 100000, 100);
        plungeSpeedSpinner = new JSpinner(spinnerNumberModel);
        add(new JLabel("Plunge speed"));
        add(plungeSpeedSpinner);
        plungeSpeedSpinner.addChangeListener(this::onPlungeSpeedChange);

        spinnerNumberModel = new SpinnerNumberModel(3d, 0.1d, 30d, 0.1d);
        toolDiameterSpinner = new JSpinner(spinnerNumberModel);
        toolDiameterSpinner.setPreferredSize(plungeSpeedSpinner.getPreferredSize().getSize());
        add(new JLabel("Tool diameter"));
        add(toolDiameterSpinner);
        toolDiameterSpinner.addChangeListener(this::onToolDiameterChange);

        setEnabled(false);
    }

    public void updateController(Controller controller) {
        if (this.controller != null) {
            this.controller.getSelectionManager().removeSelectionListener(this);
        }
        this.controller = controller;
        this.controller.getSelectionManager().addSelectionListener(this);
    }

    private void onToolDiameterChange(ChangeEvent changeEvent) {
        controller.getSelectionManager().getSelection().forEach(selectedEntity -> {
            if (selectedEntity instanceof Cuttable) {
                ((Cuttable) selectedEntity).setToolDiameter((Double) toolDiameterSpinner.getValue());
            }
        });
        controller.getSettings().setToolDiameter((Double) toolDiameterSpinner.getValue());
    }

    private void onPlungeSpeedChange(ChangeEvent changeEvent) {
        controller.getSettings().setPlungeSpeed((Integer) plungeSpeedSpinner.getValue());
    }

    private void onFeedSpeedChange(ChangeEvent changeEvent) {
        controller.getSettings().setFeedSpeed((Integer) feedSpeedSpinner.getValue());
    }

    private void onDepthChange(ChangeEvent event) {
        if (this.entity == null) {
            return;
        }
        //this.shape.getCutSettings().setDepth((Double) depthSpinner.getValue());
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        Arrays.stream(getComponents()).forEach(component -> {
            component.setEnabled(enabled);
        });

        if (!enabled) {
            depthSpinner.setValue(0d);
        }
    }

    @Override
    public void onSelectionEvent(SelectionEvent selectionEvent) {
        if (this.controller.getSelectionManager().getSelection().isEmpty()) {
            this.entity = null;
            setEnabled(false);
            return;
        } else {
            setEnabled(true);
        }

        this.entity = this.controller.getSelectionManager().getSelection().get(0);

        Settings settings = controller.getSettings();
        feedSpeedSpinner.setValue(settings.getFeedSpeed());
        plungeSpeedSpinner.setValue(settings.getPlungeSpeed());
        if (controller.getSelectionManager().getSelection().size() == 1) {
            Entity selectedEntity = controller.getSelectionManager().getSelection().get(0);
            widthTextField.setVisible(true);
            widthTextField.setText("" + selectedEntity.getSize().width);

            heightTextField.setVisible(true);
            heightTextField.setText("" + selectedEntity.getSize().height);

            rotation.setText(Utils.formatter.format(selectedEntity.getRotation()));
            rotation.setEnabled(true);
            if (selectedEntity instanceof Cuttable) {
                JToggleButton jToggleButton = cutTypeButtonMap.get(((Cuttable) selectedEntity).getCutType());
                jToggleButton.setSelected(true);

                toolDiameterSpinner.setVisible(true);
                toolDiameterSpinner.setValue(((Cuttable) selectedEntity).getToolDiameter());
            }

        } else {
            widthTextField.setText("");
            widthTextField.setVisible(false);

            heightTextField.setText("");
            heightTextField.setVisible(false);

            rotation.setText("");
            rotation.setVisible(false);

            toolDiameterSpinner.setVisible(false);
        }
    }
}

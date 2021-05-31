package com.willwinder.ugs.nbp.designer.gui;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.cuttable.CutType;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.Settings;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionEvent;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionListener;
import com.willwinder.universalgcodesender.Utils;
import net.miginfocom.swing.MigLayout;
import org.openide.util.ImageUtilities;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SelectionSettings extends JPanel implements SelectionListener {
    private final JTextField width;
    private final JTextField rotation;
    private final ButtonGroup buttonGroup;
    private Controller controller;
    private final JSpinner depthSpinner;
    private final JSpinner feedSpeedSpinner;
    private final JSpinner plungeSpeedSpinner;
    private final JSpinner toolDiameterSpinner;
    private final JTextField height;
    private Entity shape;
    private final Map<CutType, JToggleButton> cutTypeButtonMap = new HashMap<>();

    public SelectionSettings(Controller controller) {
        this();
        updateController(controller);
    }

    public SelectionSettings() {
        setLayout(new MigLayout("fill, wrap 2"));

        height = new JTextField("0");
        add(new JLabel("Height"));
        add(height, "grow");

        width = new JTextField("0");
        add(new JLabel("Width"));
        add(width, "grow");

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
                this.controller.getSelectionManager().getSelection().forEach((entity -> {
                    if(entity instanceof Cuttable) {
                        ((Cuttable)entity).setCutType(key);
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
        controller.getSettings().setToolDiameter((Double) toolDiameterSpinner.getValue());
    }

    private void onPlungeSpeedChange(ChangeEvent changeEvent) {
        controller.getSettings().setPlungeSpeed((Integer) plungeSpeedSpinner.getValue());
    }

    private void onFeedSpeedChange(ChangeEvent changeEvent) {
        controller.getSettings().setFeedSpeed((Integer) feedSpeedSpinner.getValue());
    }

    private void onDepthChange(ChangeEvent event) {
        if (this.shape == null) {
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
            this.shape = null;
            setEnabled(false);
            return;
        } else {
            setEnabled(true);
        }

        this.shape = this.controller.getSelectionManager().getSelection().get(0);

        Settings settings = controller.getSettings();
        feedSpeedSpinner.setValue(settings.getFeedSpeed());
        plungeSpeedSpinner.setValue(settings.getPlungeSpeed());
        toolDiameterSpinner.setValue(settings.getToolDiameter());
        if (controller.getSelectionManager().getSelection().size() == 1) {
            Entity entity = controller.getSelectionManager().getSelection().get(0);
            width.setEnabled(true);
            width.setText("" + entity.getSize().width);

            height.setEnabled(true);
            height.setText("" + entity.getSize().height);

            rotation.setText(Utils.formatter.format(entity.getRotation()));
            rotation.setEnabled(true);
            if(entity instanceof Cuttable) {
                JToggleButton jToggleButton = cutTypeButtonMap.get(((Cuttable) entity).getCutType());
                jToggleButton.setSelected(true);
            }
        } else {
            width.setText("");
            width.setEnabled(false);

            height.setText("");
            height.setEnabled(false);

            rotation.setText("");
            rotation.setEnabled(false);
        }
    }
}

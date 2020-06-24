package com.willwinder.ugs.designer.gui;


import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.logic.Settings;
import com.willwinder.ugs.designer.logic.events.ControllerEventType;
import com.willwinder.ugs.designer.logic.events.ControllerListener;
import com.willwinder.ugs.designer.logic.selection.SelectionEvent;
import com.willwinder.ugs.designer.logic.selection.SelectionListener;
import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.cut.CutType;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.util.Arrays;

public class SelectionSettings extends JPanel implements SelectionListener, ControllerListener {
    private final Controller controller;
    private final JList<CutType> cutType;
    private final JSpinner depthSpinner;
    private final JSpinner feedSpeedSpinner;
    private final JSpinner plungeSpeedSpinner;
    private final JSpinner toolDiameterSpinner;
    private Entity shape;

    public SelectionSettings(Controller controller) {
        setPreferredSize(new Dimension(150, 100));
        this.controller = controller;
        this.controller.addListener(this);
        this.controller.getSelectionManager().addSelectionListener(this);

        SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(1000, 0, 100000, 100);
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

        JSeparator separator = new JSeparator();
        separator.setPreferredSize(plungeSpeedSpinner.getPreferredSize());
        add(separator);

        DefaultListModel<CutType> listModel = new DefaultListModel<>();
        Arrays.asList(CutType.values()).forEach(listModel::addElement);

        //Create the list and put it in a scroll pane.
        cutType = new JList<>(listModel);
        cutType.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cutType.setSelectedIndex(0);
        cutType.addListSelectionListener(this::onCutTypeChange);
        cutType.setVisibleRowCount(3);
        JScrollPane listScrollPane = new JScrollPane(cutType);
        add(new JLabel("Cut type"));
        add(listScrollPane);

        spinnerNumberModel = new SpinnerNumberModel(100d, 0d, 100.0d, 0.1d);
        depthSpinner = new JSpinner(spinnerNumberModel);
        depthSpinner.setPreferredSize(plungeSpeedSpinner.getPreferredSize());
        add(new JLabel("Cut depth"));
        add(depthSpinner);
        depthSpinner.addChangeListener(this::onDepthChange);

        setEnabled(false);
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
        if(this.shape == null) {
            return;
        }
        this.shape.getCutSettings().setDepth((Double) depthSpinner.getValue());
    }

    private void onCutTypeChange(ListSelectionEvent event) {
        if(this.shape == null) {
            return;
        }

        CutType selectedValue = cutType.getSelectedValue();
        this.shape.getCutSettings().setCutType(selectedValue);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        cutType.setEnabled(enabled);
        depthSpinner.setEnabled(enabled);

        if(!enabled) {
            cutType.setSelectedIndex(0);
            depthSpinner.setValue(0d);
        }
    }

    @Override
    public void onSelectionEvent(SelectionEvent selectionEvent) {
        System.out.println("Selection settings: " + selectionEvent);
        if(this.controller.getSelectionManager().getShapes().isEmpty()){
            this.shape = null;
            setEnabled(false);
            return;
        } else {
            setEnabled(true);
        }

        this.shape = this.controller.getSelectionManager().getShapes().get(0);

        int index = 0;
        for(int i = 0; i < cutType.getModel().getSize(); i++) {
            if(cutType.getModel().getElementAt(i) == this.shape.getCutSettings().getCutType()) {
                index = i;
                break;
            }
        }
        cutType.setSelectedIndex(index);

        depthSpinner.setValue(this.shape.getCutSettings().getDepth());

        Settings settings = controller.getSettings();
        feedSpeedSpinner.setValue(settings.getFeedSpeed());
        plungeSpeedSpinner.setValue(settings.getPlungeSpeed());
        toolDiameterSpinner.setValue(settings.getToolDiameter());
    }

    @Override
    public void onControllerEvent(ControllerEventType event) {
        this.controller.getSelectionManager().addSelectionListener(this);
    }
}

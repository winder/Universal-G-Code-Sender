/*
    Copyright 2021 Will Winder

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
package com.willwinder.ugs.nbp.designer.gui;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntityEvent;
import com.willwinder.ugs.nbp.designer.entities.EntityListener;
import com.willwinder.ugs.nbp.designer.entities.EventType;
import com.willwinder.ugs.nbp.designer.entities.cuttable.CutType;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionEvent;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionListener;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.actions.ChangeCutSettingsAction;
import com.willwinder.ugs.nbp.designer.model.Size;
import com.willwinder.universalgcodesender.Utils;
import com.willwinder.universalgcodesender.utils.SwingHelpers;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.ImageUtilities;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.EnumMap;

/**
 * @author Joacim Breiler
 */
public class SelectionSettingsPanel extends JPanel implements SelectionListener, DocumentListener, EntityListener, ChangeListener {
    private final JTextField widthTextField;
    private final JTextField rotation;
    private final ButtonGroup buttonGroup;
    private final JTextField posXTextField;
    private final JTextField posYTextField;
    private final JLabel cutDepthLabel;
    private final JPanel cutTypePanel;
    private transient Controller controller;
    private final JSpinner depthSpinner;
    private final JTextField heightTextField;
    private final EnumMap<CutType, JToggleButton> cutTypeButtonMap = new EnumMap<>(CutType.class);

    public SelectionSettingsPanel(Controller controller) {
        this();
        updateController(controller);
    }

    public SelectionSettingsPanel() {
        setLayout(new MigLayout("fill, wrap 2", "[] 10 [grow]"));

        posXTextField = new JTextField("0");
        posXTextField.getDocument().addDocumentListener(this);
        posYTextField = new JTextField("0");
        posYTextField.getDocument().addDocumentListener(this);
        add(new JLabel("X", SwingConstants.RIGHT), "grow");
        add(posXTextField, "grow");
        add(new JLabel("Y", SwingConstants.RIGHT), "grow");
        add(posYTextField, "grow");

        widthTextField = new JTextField("0");
        widthTextField.getDocument().addDocumentListener(this);
        heightTextField = new JTextField("0");
        heightTextField.getDocument().addDocumentListener(this);
        add(new JLabel("Width", SwingConstants.RIGHT), "grow");
        add(widthTextField, "grow");
        add(new JLabel("Height", SwingConstants.RIGHT), "grow");
        add(heightTextField, "grow");


        rotation = new JTextField("0");
        rotation.getDocument().addDocumentListener(this);
        add(new JLabel("Rotation", SwingConstants.RIGHT), "grow");
        add(rotation, "grow");

        add(new JSeparator(), "spanx, wrap");

        cutTypeButtonMap.put(CutType.NONE, new JToggleButton(ImageUtilities.loadImageIcon("img/cutnone32.png", false)));
        cutTypeButtonMap.put(CutType.POCKET, new JToggleButton(ImageUtilities.loadImageIcon("img/cutpocket32.png", false)));
        cutTypeButtonMap.put(CutType.INSIDE_PATH, new JToggleButton(ImageUtilities.loadImageIcon("img/cutinside32.png", false)));
        cutTypeButtonMap.put(CutType.OUTSIDE_PATH, new JToggleButton(ImageUtilities.loadImageIcon("img/cutoutside32.png", false)));
        cutTypeButtonMap.put(CutType.ON_PATH, new JToggleButton(ImageUtilities.loadImageIcon("img/cutonpath32.png", false)));

        buttonGroup = new ButtonGroup();
        cutTypeButtonMap.keySet().forEach(key -> {
            JToggleButton button = cutTypeButtonMap.get(key);
            buttonGroup.add(button);
            button.addActionListener(event -> {
                stateChanged(null);
            });
        });

        cutTypePanel = new JPanel(new MigLayout("fill, insets 0"));
        cutTypePanel.add(cutTypeButtonMap.get(CutType.NONE));
        cutTypePanel.add(cutTypeButtonMap.get(CutType.POCKET));
        cutTypePanel.add(cutTypeButtonMap.get(CutType.INSIDE_PATH));
        cutTypePanel.add(cutTypeButtonMap.get(CutType.OUTSIDE_PATH));
        cutTypePanel.add(cutTypeButtonMap.get(CutType.ON_PATH));
        add(cutTypePanel, "grow, spanx, wrap");

        SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(100d, 0d, 100.0d, 0.1d);
        depthSpinner = new JSpinner(spinnerNumberModel);
        depthSpinner.setPreferredSize(depthSpinner.getPreferredSize());

        cutDepthLabel = new JLabel("Cut depth");
        add(cutDepthLabel);
        add(depthSpinner, "grow, wrap");
        depthSpinner.addChangeListener(this);
        setEnabled(false);
    }

    public void updateController(Controller controller) {
        if (this.controller != null) {
            this.controller.getSelectionManager().removeSelectionListener(this);
        }
        this.controller = controller;
        this.controller.getSelectionManager().addSelectionListener(this);
        this.controller.getSelectionManager().addListener(this);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        Arrays.stream(getComponents()).forEach(component -> component.setEnabled(enabled));
        SwingHelpers.traverse(cutTypePanel, component -> component.setEnabled(enabled));
        if (!enabled) {
            depthSpinner.setValue(0d);
        }
    }

    @Override
    public void onSelectionEvent(SelectionEvent selectionEvent) {
        onEvent(new EntityEvent(controller.getSelectionManager(), EventType.MOVED));
    }

    private void setFieldValue(JTextField textField, String value) {
        textField.setVisible(true);
        textField.setEnabled(true);
        textField.getDocument().removeDocumentListener(this);
        textField.setText(value);
        textField.getDocument().addDocumentListener(this);
    }

    private void setFieldValue(JSpinner spinner, Object value) {
        spinner.setVisible(true);
        spinner.setEnabled(true);
        spinner.removeChangeListener(this);
        spinner.setValue(value);
        spinner.addChangeListener(this);
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        changedUpdate(e);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        changedUpdate(e);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        controller.getSelectionManager().removeListener(this);

        if (StringUtils.isNotEmpty(rotation.getText()) && e.getDocument() == rotation.getDocument()) {
            try {
                double angle = Double.parseDouble(rotation.getText());
                controller.getSelectionManager().setRotation(angle);
                controller.getDrawing().repaint();
            } catch (NumberFormatException ex) {
                // never mind
            }
        }

        if (StringUtils.isNotEmpty(posXTextField.getText()) && StringUtils.isNotEmpty(posYTextField.getText()) && (e.getDocument() == posXTextField.getDocument() || e.getDocument() == posYTextField.getDocument())) {
            double x = Double.parseDouble(posXTextField.getText());
            double y = Double.parseDouble(posYTextField.getText());
            Point2D position = controller.getSelectionManager().getPosition();
            position.setLocation(x - position.getX(), y - position.getY());
            controller.getSelectionManager().move(position);
            controller.getDrawing().repaint();
        }

        if (StringUtils.isNotEmpty(widthTextField.getText()) && StringUtils.isNotEmpty(heightTextField.getText()) && (e.getDocument() == widthTextField.getDocument() || e.getDocument() == heightTextField.getDocument())) {
            try {
                double width = Double.parseDouble(widthTextField.getText());
                double height = Double.parseDouble(heightTextField.getText());
                if (width <= 1 || height <= 0) {
                    return;
                }
                controller.getSelectionManager().setSize(new Size(width, height));
                controller.getDrawing().repaint();
            } catch (NumberFormatException ex) {
                // never mind
            }
        }

        controller.getSelectionManager().addListener(this);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (controller == null || controller.getSelectionManager() == null) {
            return;
        }

        CutType cutType = cutTypeButtonMap.keySet().stream().filter(key -> cutTypeButtonMap.get(key).isSelected()).findFirst().orElse(CutType.NONE);
        controller.getSelectionManager().getSelection().forEach(selectedEntity -> {
            if (selectedEntity instanceof Cuttable) {
                Cuttable cuttable = (Cuttable) selectedEntity;
                ChangeCutSettingsAction changeCutSettingsAction = new ChangeCutSettingsAction(controller, cuttable, (Double) depthSpinner.getValue(), cutType);
                controller.getUndoManager().addAction(changeCutSettingsAction);
                changeCutSettingsAction.actionPerformed(null);
            }
        });
        onEvent(new EntityEvent(controller.getSelectionManager(), EventType.SETTINGS_CHANGED));
    }

    @Override
    public void onEvent(EntityEvent entityEvent) {
        if (this.controller.getSelectionManager().getSelection().isEmpty()) {
            setEnabled(false);
            return;
        } else {
            setEnabled(true);
        }

        depthSpinner.setModel(new SpinnerNumberModel(controller.getSettings().getStockThickness(), 0d, controller.getSettings().getStockThickness(), 0.1d));


        Entity selectedEntity = controller.getSelectionManager().getSelection().get(0);
        if (selectedEntity instanceof Cuttable) {
            Cuttable cuttable = (Cuttable) selectedEntity;
            JToggleButton cutTypeButton = cutTypeButtonMap.get(cuttable.getCutType());
            cutTypeButton.setSelected(true);
            setFieldValue(depthSpinner, cuttable.getCutDepth());

            final boolean hasCutTypeSelection = cuttable.getCutType() != CutType.NONE;
            depthSpinner.setEnabled(hasCutTypeSelection);
            cutDepthLabel.setEnabled(hasCutTypeSelection);
        }


        Point2D position = controller.getSelectionManager().getPosition();
        setFieldValue(posXTextField, Utils.formatter.format(position.getX()));
        setFieldValue(posYTextField, Utils.formatter.format(position.getY()));

        setFieldValue(widthTextField, Utils.formatter.format(controller.getSelectionManager().getSize().getWidth()));
        setFieldValue(heightTextField, Utils.formatter.format(controller.getSelectionManager().getSize().getHeight()));
        setFieldValue(rotation, Utils.formatter.format(controller.getSelectionManager().getRotation()));
        controller.getDrawing().repaint();
    }
}

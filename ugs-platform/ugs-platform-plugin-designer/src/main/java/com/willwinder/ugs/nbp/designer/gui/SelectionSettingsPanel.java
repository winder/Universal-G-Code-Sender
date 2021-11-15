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

import com.willwinder.ugs.nbp.designer.Utils;
import com.willwinder.ugs.nbp.designer.actions.ChangeCutSettingsAction;
import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntityEvent;
import com.willwinder.ugs.nbp.designer.entities.EntityListener;
import com.willwinder.ugs.nbp.designer.entities.EventType;
import com.willwinder.ugs.nbp.designer.entities.cuttable.CutType;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Text;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionEvent;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionListener;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.model.Size;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultFormatter;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Point2D;
import java.util.Arrays;

/**
 * @author Joacim Breiler
 */
public class SelectionSettingsPanel extends JPanel implements SelectionListener, DocumentListener, EntityListener, ChangeListener, ItemListener {
    private final JTextField textTextField;
    private final JTextField widthTextField;
    private final JTextField rotation;
    private final JTextField posXTextField;
    private final JTextField posYTextField;
    private final JLabel cutDepthLabel;
    private final JComboBox<CutType> cutTypeComboBox;
    private final JComboBox<Font> fontDropDown;
    private transient Controller controller;
    private final JSpinner depthSpinner;
    private final JTextField heightTextField;

    public SelectionSettingsPanel(Controller controller) {
        setLayout(new MigLayout("fill, wrap 2", "[] 10 [grow]"));
        textTextField = new JTextField();
        add(new JLabel("Text", SwingConstants.RIGHT), "grow");
        add(textTextField, "grow");

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Font[] fontNames = Arrays.stream(ge.getAllFonts()).distinct().toArray(Font[]::new);
        fontDropDown = new JComboBox<>(fontNames);
        add(new JLabel("Font", SwingConstants.RIGHT), "grow");
        add(fontDropDown, "grow");
        fontDropDown.addItemListener(this);

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

        add(new JSeparator(), "grow, spanx, wrap");

        cutTypeComboBox = new JComboBox<>();
        Arrays.stream(CutType.values()).forEach(cutTypeComboBox::addItem);
        cutTypeComboBox.setSelectedItem(CutType.NONE);
        cutTypeComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                CutType cutType = (CutType) value;
                setText(cutType.getName());
                setIcon(new CutTypeIcon(cutType, CutTypeIcon.Size.SMALL));
                return this;
            }
        });
        cutTypeComboBox.addItemListener(this);


        JLabel cutTypeLabel = new JLabel("Cut type", SwingConstants.RIGHT);
        add(cutTypeLabel);
        add(cutTypeComboBox, " grow, wrap");

        SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(100d, 0d, 100.0d, 0.1d);
        depthSpinner = new JSpinner(spinnerNumberModel);

        // Make the spinner commit the value immediately
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(depthSpinner);
        depthSpinner.setEditor(editor);
        JFormattedTextField jtf = editor.getTextField();
        DefaultFormatter formatter = (DefaultFormatter) jtf.getFormatter();
        formatter.setCommitsOnValidEdit(true);

        depthSpinner.setPreferredSize(depthSpinner.getPreferredSize());
        depthSpinner.setModel(new SpinnerNumberModel(0, 0d, 100, 0.1d));
        depthSpinner.addChangeListener(this);

        cutDepthLabel = new JLabel("Cut depth", SwingConstants.RIGHT);
        add(cutDepthLabel);
        add(depthSpinner, "grow, wrap");
        setEnabled(false);

        if (this.controller != null) {
            this.controller.getSelectionManager().removeSelectionListener(this);
        }
        this.controller = controller;
        this.controller.getSelectionManager().addSelectionListener(this);
        this.controller.getSelectionManager().addListener(this);
        depthSpinner.setModel(new SpinnerNumberModel(controller.getSettings().getStockThickness(), 0d, controller.getSettings().getStockThickness(), 0.1d));
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        Arrays.stream(getComponents()).forEach(component -> component.setEnabled(enabled));
        if (!enabled) {
            setFieldValue(depthSpinner, 0d);
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
        boolean enabled = spinner.isEnabled();
        spinner.setVisible(true);
        spinner.setEnabled(true);
        spinner.removeChangeListener(this);
        spinner.setValue(value);
        spinner.addChangeListener(this);
        spinner.setEnabled(enabled);
    }


    private void setFieldValue(JComboBox comboBox, Object value) {
        boolean enabled = comboBox.isEnabled();
        comboBox.setVisible(true);
        comboBox.setEnabled(true);
        comboBox.removeItemListener(this);
        comboBox.setSelectedItem(value);
        comboBox.addItemListener(this);
        comboBox.setEnabled(enabled);
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
                double angle = Utils.parseDouble(rotation.getText());
                controller.getSelectionManager().setRotation(angle);
                controller.getDrawing().repaint();
            } catch (NumberFormatException ex) {
                // never mind
            }
        }

        if (StringUtils.isNotEmpty(posXTextField.getText()) && StringUtils.isNotEmpty(posYTextField.getText()) && (e.getDocument() == posXTextField.getDocument() || e.getDocument() == posYTextField.getDocument())) {
            double x = Utils.parseDouble(posXTextField.getText());
            double y = Utils.parseDouble(posYTextField.getText());
            Point2D position = controller.getSelectionManager().getPosition();
            position.setLocation(x - position.getX(), y - position.getY());
            controller.getSelectionManager().move(position);
            controller.getDrawing().repaint();
        }

        if (StringUtils.isNotEmpty(widthTextField.getText()) && StringUtils.isNotEmpty(heightTextField.getText()) && (e.getDocument() == widthTextField.getDocument() || e.getDocument() == heightTextField.getDocument())) {
            try {
                double width = Utils.parseDouble(widthTextField.getText());
                double height = Utils.parseDouble(heightTextField.getText());
                if (width <= 1 || height <= 0) {
                    return;
                }
                controller.getSelectionManager().setSize(new Size(width, height));
                controller.getDrawing().repaint();
            } catch (NumberFormatException ex) {
                // never mind
            }
        }

        if(!controller.getSelectionManager().isEmpty()) {
            Entity entity = controller.getSelectionManager().getSelection().get(0);
            if(entity instanceof Text) {
                ((Text)entity).setText(textTextField.getText());
                ((Text)entity).setFontFamily((String) fontDropDown.getSelectedItem());
            }
            controller.getDrawing().repaint();
        }

        controller.getSelectionManager().addListener(this);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (controller == null || controller.getSelectionManager() == null) {
            return;
        }

        CutType cutType = (CutType) cutTypeComboBox.getSelectedItem();
        String fontFamily = (String) fontDropDown.getSelectedItem();
        controller.getSelectionManager().getSelection().forEach(selectedEntity -> {
            if (selectedEntity instanceof Cuttable) {
                Cuttable cuttable = (Cuttable) selectedEntity;
                ChangeCutSettingsAction changeCutSettingsAction = new ChangeCutSettingsAction(controller, cuttable, (Double) depthSpinner.getValue(), cutType);
                controller.getUndoManager().addAction(changeCutSettingsAction);
                changeCutSettingsAction.actionPerformed(null);
            }

            if (selectedEntity instanceof Text) {
                // TODO fix undoable action
                Text text = (Text) selectedEntity;
                text.setFontFamily(fontFamily);
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

        Entity selectedEntity = controller.getSelectionManager().getSelection().get(0);
        if (selectedEntity instanceof Cuttable) {
            Cuttable cuttable = (Cuttable) selectedEntity;

            setFieldValue(cutTypeComboBox, cuttable.getCutType());
            setFieldValue(depthSpinner, cuttable.getCutDepth());

            final boolean hasCutTypeSelection = cuttable.getCutType() != CutType.NONE;
            depthSpinner.setEnabled(hasCutTypeSelection);
            cutDepthLabel.setEnabled(hasCutTypeSelection);
        }

        textTextField.setEnabled(selectedEntity instanceof Text);
        if (selectedEntity instanceof Text) {
            setFieldValue(textTextField, ((Text)selectedEntity).getText());
            setFieldValue(fontDropDown, ((Text)selectedEntity).getFontFamily());
        }


        Point2D position = controller.getSelectionManager().getPosition();
        setFieldValue(posXTextField, Utils.toString(position.getX()));
        setFieldValue(posYTextField, Utils.toString(position.getY()));

        setFieldValue(widthTextField, Utils.toString(controller.getSelectionManager().getSize().getWidth()));
        setFieldValue(heightTextField, Utils.toString(controller.getSelectionManager().getSize().getHeight()));
        setFieldValue(rotation, Utils.toString(controller.getSelectionManager().getRotation()));
        controller.getDrawing().repaint();
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        stateChanged(null);
    }
}

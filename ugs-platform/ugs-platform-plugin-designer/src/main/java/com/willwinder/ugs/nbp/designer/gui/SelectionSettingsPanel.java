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

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultFormatter;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Joacim Breiler
 */
public class SelectionSettingsPanel extends JPanel implements SelectionListener, DocumentListener, EntityListener, ChangeListener, ItemListener {
    private final JTextField widthTextField;
    private final JTextField rotation;
    private final JTextField posXTextField;
    private final JTextField posYTextField;
    private final JLabel startDepthLabel;
    private final JLabel targetDepthLabel;
    private final JComboBox<CutType> cutTypeComboBox;
    private final JSpinner startDepthSpinner;
    private final JSpinner targetDepthSpinner;
    private final JTextField heightTextField;
    private JLabel textLabel;
    private JComboBox<String> fontDropDown;
    private JLabel fontLabel;
    private JSeparator fontSeparator;
    private JTextField textTextField;
    private transient Controller controller;

    public SelectionSettingsPanel(Controller controller) {
        setLayout(new MigLayout("fill, wrap 2, hidemode 3", "[sg label] 10 [grow]"));

        addTextSettingFields();

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

        SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(0d, 0d, 100.0d, 0.1d);
        startDepthSpinner = new JSpinner(spinnerNumberModel);

        // Make the spinner commit the value immediately
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(startDepthSpinner);
        startDepthSpinner.setEditor(editor);
        JFormattedTextField jtf = editor.getTextField();
        DefaultFormatter formatter = (DefaultFormatter) jtf.getFormatter();
        formatter.setCommitsOnValidEdit(true);

        startDepthSpinner.setPreferredSize(startDepthSpinner.getPreferredSize());
        startDepthSpinner.addChangeListener(this);


        spinnerNumberModel = new SpinnerNumberModel(0d, 0d, 100.0d, 0.1d);
        targetDepthSpinner = new JSpinner(spinnerNumberModel);

        // Make the spinner commit the value immediately
        editor = new JSpinner.NumberEditor(targetDepthSpinner);
        targetDepthSpinner.setEditor(editor);
        jtf = editor.getTextField();
        formatter = (DefaultFormatter) jtf.getFormatter();
        formatter.setCommitsOnValidEdit(true);

        targetDepthSpinner.setPreferredSize(targetDepthSpinner.getPreferredSize());
        targetDepthSpinner.addChangeListener(this);

        startDepthLabel = new JLabel("Start depth", SwingConstants.RIGHT);
        add(startDepthLabel);
        add(startDepthSpinner, "grow, wrap");

        targetDepthLabel = new JLabel("Target depth", SwingConstants.RIGHT);
        add(targetDepthLabel);
        add(targetDepthSpinner, "grow, wrap");
        setEnabled(false);

        if (this.controller != null) {
            this.controller.getSelectionManager().removeSelectionListener(this);
        }
        this.controller = controller;
        this.controller.getSelectionManager().addSelectionListener(this);
        this.controller.getSelectionManager().addListener(this);
        targetDepthSpinner.setModel(new SpinnerNumberModel(controller.getSettings().getStockThickness(), 0d, controller.getSettings().getStockThickness(), 0.1d));
    }

    private void addTextSettingFields() {
        textTextField = new JTextField();
        textTextField.setVisible(false);
        textTextField.getDocument().addDocumentListener(this);
        textLabel = new JLabel("Text", SwingConstants.RIGHT);
        textLabel.setVisible(false);

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fontNames = Arrays.stream(ge.getAvailableFontFamilyNames()).distinct().toArray(String[]::new);
        fontDropDown = new JComboBox<>(fontNames);
        fontDropDown.setRenderer(new FontDropDownRenderer());
        fontDropDown.addItemListener(this);
        fontDropDown.setVisible(false);
        Dimension minimumSize = fontDropDown.getMinimumSize();
        fontDropDown.setMinimumSize(new Dimension(100, minimumSize.height));

        fontLabel = new JLabel("Font", SwingConstants.RIGHT);
        fontLabel.setVisible(false);
        fontSeparator = new JSeparator(SwingConstants.HORIZONTAL);
        fontSeparator.setVisible(false);

        add(textLabel, "grow");
        add(textTextField, "grow");
        add(fontLabel, "grow");
        add(fontDropDown, "grow");
        add(fontSeparator, "grow, spanx, wrap");
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        Arrays.stream(getComponents()).forEach(component -> component.setEnabled(enabled));
        if (!enabled) {
            setFieldValue(targetDepthSpinner, 0d);
            setFieldValue(startDepthSpinner, 0d);
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


    private void setFieldValue(JComboBox<?> comboBox, Object value) {
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

        if (!controller.getSelectionManager().isEmpty()) {
            Entity entity = controller.getSelectionManager().getSelection().get(0);
            if (entity instanceof Text) {
                ((Text) entity).setText(textTextField.getText());
                ((Text) entity).setFontFamily((String) fontDropDown.getSelectedItem());
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

        List<Cuttable> cuttables = controller.getSelectionManager().getSelection().stream()
                .filter(Cuttable.class::isInstance)
                .map(Cuttable.class::cast).collect(Collectors.toList());

        if (!cuttables.isEmpty()) {
            double startDepth = (Double) startDepthSpinner.getValue();
            double targetDepth = Math.max((Double) targetDepthSpinner.getValue(), startDepth);
            ChangeCutSettingsAction changeCutSettingsAction = new ChangeCutSettingsAction(controller, cuttables, startDepth, targetDepth, cutType);
            changeCutSettingsAction.actionPerformed(null);
            controller.getUndoManager().addAction(changeCutSettingsAction);
        }

        controller.getSelectionManager().getSelection().stream()
                .filter(Text.class::isInstance)
                .map(Text.class::cast)
                // TODO fix undoable action
                .forEach(text -> text.setFontFamily(fontFamily));

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
            setFieldValue(startDepthSpinner, cuttable.getStartDepth());
            setFieldValue(targetDepthSpinner, cuttable.getTargetDepth());

            final boolean hasCutTypeSelection = cuttable.getCutType() != CutType.NONE;
            startDepthSpinner.setEnabled(hasCutTypeSelection);
            startDepthLabel.setEnabled(hasCutTypeSelection);
            targetDepthSpinner.setEnabled(hasCutTypeSelection);
            targetDepthLabel.setEnabled(hasCutTypeSelection);
        }

        boolean isTextCuttable = selectedEntity instanceof Text;
        textTextField.setVisible(isTextCuttable);
        textLabel.setVisible(isTextCuttable);
        fontLabel.setVisible(isTextCuttable);
        fontDropDown.setVisible(isTextCuttable);
        fontSeparator.setVisible(isTextCuttable);
        if (isTextCuttable) {
            setFieldValue(textTextField, ((Text) selectedEntity).getText());
            setFieldValue(fontDropDown, ((Text) selectedEntity).getFontFamily());
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

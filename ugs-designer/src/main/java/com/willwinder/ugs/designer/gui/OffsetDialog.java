/*
    Copyright 2021-2024 Will Winder

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
package com.willwinder.ugs.designer.gui;

import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.entities.EntityGroup;
import com.willwinder.ugs.designer.entities.cuttable.Path;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.logic.Tool;
import com.willwinder.ugs.designer.model.OffsetDirection;
import com.willwinder.ugs.designer.model.OffsetJoinStyle;
import com.willwinder.ugs.designer.utils.OffsetUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.List;

/**
 * @author Joacim Breiler
 */
public class OffsetDialog extends JDialog implements ChangeListener, WindowListener {
    public static final int PADDING = 2;
    public static final String PANEL_LAYOUT_CONFIG = "fill, insets 2";
    public static final String SPINNER_COL_CONSTRAINTS = "width 60:100:100, wrap";

    private JSpinner offsetDistance;
    private JComboBox<OffsetDirection> directionBox;
    private JComboBox<OffsetJoinStyle> joinStyleBox;
    private JCheckBox unionResultsCheckbox;
    private JButton cancelButton;
    private JButton okButton;

    private final transient Controller controller;
    private final transient EntityGroup entityGroup = new EntityGroup();

    public OffsetDialog(Controller controller) {
        super(SwingUtilities.getWindowAncestor(controller.getDrawing()), ModalityType.APPLICATION_MODAL);
        this.controller = controller;
        this.controller.getModel().insertEntity(entityGroup);
        entityGroup.setName("Temporary offset group");
        setTitle("Offset selection");
        setPreferredSize(new Dimension(500, 300));
        setLayout(new MigLayout("fill, insets 5", "", ""));
        setResizable(true);

        createComponents();
        addEventListeners();

        pack();
        setLocationRelativeTo(SwingUtilities.getWindowAncestor(controller.getDrawing()));
    }

    private void addEventListeners() {
        offsetDistance.addChangeListener(this);
        directionBox.addActionListener(_ -> stateChanged(null));
        joinStyleBox.addActionListener(_ -> stateChanged(null));
        unionResultsCheckbox.addActionListener(_ -> stateChanged(null));
        cancelButton.addActionListener(_ -> onCancel());
        okButton.addActionListener(_ -> onOk());
        addWindowListener(this);
    }

    private void createComponents() {
        JPanel offsetPanel = new JPanel(new MigLayout(PANEL_LAYOUT_CONFIG));
        offsetPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Offset Settings"),
                BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING)));
        offsetPanel.add(new JLabel("Offset distance", SwingConstants.TRAILING), "grow");
        offsetDistance = new JSpinner(new SpinnerNumberModel(1d, 1, 1000, 1));
        offsetPanel.add(offsetDistance, SPINNER_COL_CONSTRAINTS);

        offsetPanel.add(new JLabel("Offset direction", SwingConstants.TRAILING), "grow");
        directionBox = new JComboBox<>(OffsetDirection.values());
        offsetPanel.add(directionBox, SPINNER_COL_CONSTRAINTS);
        add(offsetPanel, "grow");

        JPanel optionsPanel = new JPanel(new MigLayout(PANEL_LAYOUT_CONFIG));
        optionsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Options"),
                BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING)));
        optionsPanel.add(new JLabel("Join style", SwingConstants.TRAILING), "grow");
        joinStyleBox = new JComboBox<>(OffsetJoinStyle.values());
        optionsPanel.add(joinStyleBox, SPINNER_COL_CONSTRAINTS);

        unionResultsCheckbox = new JCheckBox("Union all", false);
        optionsPanel.add(new JLabel("Boolean operation", SwingConstants.TRAILING), "grow");
        optionsPanel.add(unionResultsCheckbox, SPINNER_COL_CONSTRAINTS);
        add(optionsPanel, "grow, wrap");

        JPanel buttonPanel = new JPanel(new MigLayout("insets 5", "[center, grow]"));
        cancelButton = new JButton("Cancel");
        buttonPanel.add(cancelButton);

        okButton = new JButton("OK");
        buttonPanel.add(okButton);
        add(buttonPanel, "dock south");
        getRootPane().setDefaultButton(okButton);
    }

    private void onCancel() {
        controller.getModel().removeEntity(entityGroup);
        setVisible(false);
        dispose();
    }

    private void onOk() {
        List<Entity> children = entityGroup.getChildren();
        List<Entity> selection = controller.getSelectionManager().getSelection();

        controller.getModel().removeEntity(entityGroup);
        controller.addEntities(children);

        controller.getSelectionManager().clearSelection();
        controller.getSelectionManager().addSelection(children);
        controller.getSelectionManager().addSelection(selection);
        controller.repaintDrawing();

        setVisible(false);
        dispose();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        entityGroup.removeAll();

        List<Entity> selection = controller.getSelectionManager().getSelection().stream()
                .map(Entity::copy)
                .toList();

        double offset = (Double) offsetDistance.getValue();
        if (offset <= 0 || selection.isEmpty()) {
            controller.setTool(Tool.SELECT);
            return;
        }

        List<Path> offsetPaths = OffsetUtils.offset(selection, offset,
                (OffsetDirection) directionBox.getSelectedItem(),
                (OffsetJoinStyle) joinStyleBox.getSelectedItem(),
                unionResultsCheckbox.isSelected());
        offsetPaths.forEach(entityGroup::addChild);
        controller.repaintDrawing();
    }

    @Override
    public void windowOpened(WindowEvent e) {}
    @Override
    public void windowClosing(WindowEvent e) {
        controller.getModel().removeEntity(entityGroup);
    }
    @Override
    public void windowClosed(WindowEvent e) {}
    @Override
    public void windowIconified(WindowEvent e) {}
    @Override
    public void windowDeiconified(WindowEvent e) {}
    @Override
    public void windowActivated(WindowEvent e) {}
    @Override
    public void windowDeactivated(WindowEvent e) {}
}

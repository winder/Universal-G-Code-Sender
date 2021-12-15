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
import com.willwinder.ugs.nbp.designer.entities.EntityGroup;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Joacim Breiler
 */
public class MultiplyDialog extends JDialog implements ChangeListener, WindowListener {
    public static final int PADDING = 2;
    public static final String PANEL_LAYOUT_CONFIG = "fill, insets 2";

    private JSpinner xCountSpinner;
    private JSpinner xSpacingSpinner;
    private JSpinner yCountSpinner;
    private JSpinner ySpacingSpinner;
    private JButton cancelButton;
    private JButton okButton;

    private final transient Controller controller;
    private final transient EntityGroup entityGroup = new EntityGroup();

    public MultiplyDialog(Controller controller) {
        super(SwingUtilities.getWindowAncestor(controller.getDrawing()), ModalityType.APPLICATION_MODAL);
        this.controller = controller;
        this.controller.getDrawing().insertEntity(entityGroup);
        entityGroup.setName("Temporary multiplier group");
        setTitle("Multiply object");
        setPreferredSize(new Dimension(360, 200));
        setLayout(new MigLayout("fill, insets 5", "", ""));
        setResizable(false);

        createComponents();
        addEventListeners();

        pack();
        setLocationRelativeTo(SwingUtilities.getWindowAncestor(controller.getDrawing()));
    }

    private void addEventListeners() {
        xCountSpinner.addChangeListener(this);
        xSpacingSpinner.addChangeListener(this);
        yCountSpinner.addChangeListener(this);
        ySpacingSpinner.addChangeListener(this);
        cancelButton.addActionListener(event -> onCancel());
        okButton.addActionListener(event -> onOk());
        addWindowListener(this);
    }

    private void createComponents() {
        JPanel horizontalPanel = new JPanel(new MigLayout(PANEL_LAYOUT_CONFIG));
        horizontalPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Horizontal"),
                BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING)));
        horizontalPanel.add(new JLabel("X Columns", SwingConstants.TRAILING), "grow");
        xCountSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        horizontalPanel.add(xCountSpinner, "wrap");

        horizontalPanel.add(new JLabel("X Spacing", SwingConstants.TRAILING), "grow");
        xSpacingSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        horizontalPanel.add(xSpacingSpinner, "wrap");
        add(horizontalPanel, "grow");

        JPanel verticalPanel = new JPanel(new MigLayout(PANEL_LAYOUT_CONFIG));
        verticalPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Vertical"),
                BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING)));
        verticalPanel.add(new JLabel("Y Rows", SwingConstants.TRAILING), "grow");
        yCountSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        verticalPanel.add(yCountSpinner, "wrap");

        verticalPanel.add(new JLabel("Y Spacing", SwingConstants.TRAILING), "grow");
        ySpacingSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        verticalPanel.add(ySpacingSpinner, "wrap");
        add(verticalPanel, "grow, wrap");

        JPanel buttonPanel = new JPanel(new MigLayout("insets 0", "[center, grow]"));
        cancelButton = new JButton("Cancel");
        buttonPanel.add(cancelButton);

        okButton = new JButton("OK");
        buttonPanel.add(okButton);
        add(buttonPanel, "dock south");
        getRootPane().setDefaultButton(okButton);
    }

    private void onCancel() {
        controller.getDrawing().removeEntity(entityGroup);
        setVisible(false);
        dispose();
    }

    private void onOk() {
        List<Entity> children = entityGroup.getChildren();
        List<Entity> selection = controller.getSelectionManager().getSelection();

        controller.getDrawing().removeEntity(entityGroup);
        controller.addEntities(children);

        // Reselect the entities
        controller.getSelectionManager().clearSelection();
        controller.getSelectionManager().addSelection(children);
        controller.getSelectionManager().addSelection(selection);
        controller.getDrawing().repaint();

        setVisible(false);
        dispose();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        entityGroup.removeAll();

        EntityGroup selection = new EntityGroup();
        selection.addAll(controller.getSelectionManager().getSelection().stream()
                .map(Entity::copy)
                .collect(Collectors.toList()));

        for (int x = 0; x < (int) xCountSpinner.getValue(); x++) {
            for (int y = 0; y < (int) yCountSpinner.getValue(); y++) {
                if (x >= 1 || y >= 1) {
                    EntityGroup clone = (EntityGroup) selection.copy();
                    Point2D position = clone.getPosition();
                    double newX = position.getX() + (x * clone.getSize().getWidth()) + (x * (int) xSpacingSpinner.getValue());
                    double newY = position.getY() + (y * clone.getSize().getHeight()) + (y * (int) ySpacingSpinner.getValue());
                    clone.setPosition(new Point2D.Double(newX, newY));
                    entityGroup.addAll(clone.getChildren());
                }
            }
        }
        controller.getDrawing().repaint();
    }

    @Override
    public void windowOpened(WindowEvent e) {
        // Not used
    }

    @Override
    public void windowClosing(WindowEvent e) {
        controller.getDrawing().removeEntity(entityGroup);
    }

    @Override
    public void windowClosed(WindowEvent e) {
        // Not used
    }

    @Override
    public void windowIconified(WindowEvent e) {
        // Not used
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
        // Not used
    }

    @Override
    public void windowActivated(WindowEvent e) {
        // Not used
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
        // Not used
    }
}

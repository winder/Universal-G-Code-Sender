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
package com.willwinder.ugs.nbp.designer.gui;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntityGroup;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Path;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.Tool;
import com.willwinder.ugs.nbp.designer.model.Size;
import net.miginfocom.swing.MigLayout;
import org.locationtech.jts.awt.ShapeReader;
import org.locationtech.jts.awt.ShapeWriter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.operation.buffer.BufferOp;
import org.locationtech.jts.operation.buffer.BufferParameters;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Joacim Breiler
 */
public class OffsetDialog extends JDialog implements ChangeListener, WindowListener {
    public static final int PADDING = 2;
    public static final String PANEL_LAYOUT_CONFIG = "fill, insets 5";
    public static final String SPINNER_COL_CONSTRAINTS = "width 60:100:100, wrap";

    private JSpinner offsetDistance;
    private JSpinner xDelta;
    private JSpinner yDelta;
    private JComboBox<String> directionBox;
    private JButton cancelButton;
    private JButton okButton;

    private final transient Controller controller;
    private final transient EntityGroup entityGroup = new EntityGroup();

    public OffsetDialog(Controller controller) {
        super(SwingUtilities.getWindowAncestor(controller.getDrawing()), ModalityType.APPLICATION_MODAL);
        this.controller = controller;
        this.controller.getDrawing().insertEntity(entityGroup);
        entityGroup.setName("Temporary offset group");
        setTitle("Offset selection");
        setPreferredSize(new Dimension(400, 200));
        setLayout(new MigLayout("fill, insets 5", "", ""));
        setResizable(true);

        createComponents();
        addEventListeners();

        pack();
        setLocationRelativeTo(SwingUtilities.getWindowAncestor(controller.getDrawing()));
    }

    private void addEventListeners() {
        offsetDistance.addChangeListener(this);
        xDelta.addChangeListener(this);
        yDelta.addChangeListener(this);
        directionBox.addActionListener(event -> stateChanged(null));
        cancelButton.addActionListener(event -> onCancel());
        okButton.addActionListener(event -> onOk());
        addWindowListener(this);
    }

    private void createComponents() {
        JPanel horizontalPanel = new JPanel(new MigLayout(PANEL_LAYOUT_CONFIG));
        horizontalPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Offset"),
                BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING)));
        horizontalPanel.add(new JLabel("Offset distance", SwingConstants.TRAILING), "grow");
        offsetDistance = new JSpinner(new SpinnerNumberModel(1d, 1, 1000, 1));
        horizontalPanel.add(offsetDistance, SPINNER_COL_CONSTRAINTS);

        horizontalPanel.add(new JLabel("X Delta", SwingConstants.TRAILING), "grow");
        xDelta = new JSpinner(new SpinnerNumberModel(0d, 0, 1000, .1));
        horizontalPanel.add(xDelta, SPINNER_COL_CONSTRAINTS);
        horizontalPanel.add(new JLabel("Y Delta", SwingConstants.TRAILING), "grow");
        yDelta = new JSpinner(new SpinnerNumberModel(0d, 0, 1000, .1));
        horizontalPanel.add(yDelta, SPINNER_COL_CONSTRAINTS);

        horizontalPanel.add(new JLabel("Offset direction", SwingConstants.TRAILING), "grow");
        directionBox = new JComboBox<>(new String[]{"Outward", "Inward", "Both"});
        horizontalPanel.add(directionBox, SPINNER_COL_CONSTRAINTS);

        add(horizontalPanel, "grow");


        JPanel buttonPanel = new JPanel(new MigLayout("insets 5", "[center, grow]"));
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
                .toList());

        float offset = Double.valueOf((double) offsetDistance.getValue()).floatValue();
        if (offset <= 0 || selection.getChildren().isEmpty()) {
            controller.setTool(Tool.SELECT);
            return;
        }

        String direction = (String) directionBox.getSelectedItem();

        selection.getChildren().forEach(entity -> {
            Shape absoluteShape = entity.getShape();
            List<Path> offsetPaths = createDirectionalOffsetPaths(absoluteShape, offset, direction);

            for (Path offsetPath : offsetPaths) {
                entityGroup.addChild(offsetPath);
            }
        });

        controller.getDrawing().repaint();
    }

    /**
     * Create offset paths based on direction.
     * Creates proper offset shapes without positioning artifacts.
     */
    private List<Path> createDirectionalOffsetPaths(Shape shape, float offset, String direction) {
        List<Path> paths = new ArrayList<>();

        try {
            GeometryFactory gf = new GeometryFactory();
            ShapeReader reader = new ShapeReader(gf);
            Geometry geom = reader.read(shape.getPathIterator(new AffineTransform()));

            if (geom == null || geom.isEmpty()) {
                return paths;
            }

            // Use buffer parameters for sharp corners on geometric shapes
            BufferParameters bufferParams = new BufferParameters();
            bufferParams.setEndCapStyle(BufferParameters.CAP_FLAT);
            bufferParams.setJoinStyle(BufferParameters.JOIN_MITRE);
            bufferParams.setQuadrantSegments(8);
            bufferParams.setMitreLimit(2.0);
            bufferParams.setSingleSided(false);

            if ("Outward".equals(direction)) {
                Geometry buffered = BufferOp.bufferOp(geom, offset, bufferParams);
                if (buffered != null && !buffered.isEmpty()) {
                    paths.add(shapeToPath(buffered));
                }
            } else if ("Inward".equals(direction)) {
                Geometry buffered = BufferOp.bufferOp(geom, -offset, bufferParams);
                if (buffered != null && !buffered.isEmpty()) {
                    paths.add(shapeToPath(buffered));
                } else {
                    // Fallback: try with round joins which are more forgiving for inward offsets
                    BufferParameters roundParams = new BufferParameters();
                    roundParams.setEndCapStyle(BufferParameters.CAP_ROUND);
                    roundParams.setJoinStyle(BufferParameters.JOIN_ROUND);
                    roundParams.setQuadrantSegments(8);

                    Geometry bufferedRound = BufferOp.bufferOp(geom, -offset, roundParams);
                    if (bufferedRound != null && !bufferedRound.isEmpty()) {
                        paths.add(shapeToPath(bufferedRound));
                    }
                }
            } else if ("Both".equals(direction)) {
                Geometry bufferedOut = BufferOp.bufferOp(geom, offset, bufferParams);
                if (bufferedOut != null && !bufferedOut.isEmpty()) {
                    paths.add(shapeToPath(bufferedOut));
                }

                Geometry bufferedIn = BufferOp.bufferOp(geom, -offset, bufferParams);
                if (bufferedIn != null && !bufferedIn.isEmpty()) {
                    paths.add(shapeToPath(bufferedIn));
                } else {
                    // Fallback for inward buffer
                    BufferParameters roundParams = new BufferParameters();
                    roundParams.setEndCapStyle(BufferParameters.CAP_ROUND);
                    roundParams.setJoinStyle(BufferParameters.JOIN_ROUND);
                    roundParams.setQuadrantSegments(8);

                    Geometry bufferedRound = BufferOp.bufferOp(geom, -offset, roundParams);
                    if (bufferedRound != null && !bufferedRound.isEmpty()) {
                        paths.add(shapeToPath(bufferedRound));
                    }
                }
            }

        } catch (Exception e) {
            // Silently handle errors - offset operation failed
        }

        return paths;
    }

    /**
     * Convert JTS Geometry to Path.
     */
    private Path shapeToPath(Geometry geometry) {
        ShapeWriter writer = new ShapeWriter();
        Shape offsetShape = writer.toShape(geometry);
        Path offsetPath = new Path();
        offsetPath.append(offsetShape);
        return offsetPath;
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

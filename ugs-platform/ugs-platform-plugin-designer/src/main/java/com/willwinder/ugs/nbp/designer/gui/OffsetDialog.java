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
    public static final String PANEL_LAYOUT_CONFIG = "fill, insets 2";
    public static final String SPINNER_COL_CONSTRAINTS = "width 60:100:100, wrap";

    private JSpinner offsetDistance;
    private JComboBox<String> directionBox;
    private JComboBox<String> joinStyleBox;
    private JCheckBox unionResultsCheckbox;
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
        directionBox.addActionListener(e -> stateChanged(null));
        joinStyleBox.addActionListener(e -> stateChanged(null));
        unionResultsCheckbox.addActionListener(e -> stateChanged(null));
        cancelButton.addActionListener(e -> onCancel());
        okButton.addActionListener(e -> onOk());
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
        directionBox = new JComboBox<>(new String[]{"Outward", "Inward", "Both"});
        offsetPanel.add(directionBox, SPINNER_COL_CONSTRAINTS);
        add(offsetPanel, "grow");

        JPanel optionsPanel = new JPanel(new MigLayout(PANEL_LAYOUT_CONFIG));
        optionsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Options"),
                BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING)));
        optionsPanel.add(new JLabel("Join style", SwingConstants.TRAILING), "grow");
        joinStyleBox = new JComboBox<>(new String[]{"Round", "Bevel", "Mitre"});
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
        controller.getDrawing().removeEntity(entityGroup);
        setVisible(false);
        dispose();
    }

    private void onOk() {
        List<Entity> children = entityGroup.getChildren();
        List<Entity> selection = controller.getSelectionManager().getSelection();

        controller.getDrawing().removeEntity(entityGroup);
        controller.addEntities(children);

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

        float offset = ((Double) offsetDistance.getValue()).floatValue();
        if (offset <= 0 || selection.getChildren().isEmpty()) {
            controller.setTool(Tool.SELECT);
            return;
        }

        String direction = (String) directionBox.getSelectedItem();
        boolean unionResults = unionResultsCheckbox.isSelected();

        List<Path> allOffsetPaths = new ArrayList<>();
        for (Entity entity : selection.getChildren()) {
            Shape shape = getEntityShape(entity);
            if (shape == null) continue;
            allOffsetPaths.addAll(createDirectionalOffsetPaths(shape, offset, direction));
        }

        if (unionResults && allOffsetPaths.size() > 1) {
            Path unionedPath = performUnionOperation(allOffsetPaths);
            if (unionedPath != null) {
                entityGroup.addChild(unionedPath);
            } else {
                allOffsetPaths.forEach(entityGroup::addChild);
            }
        } else {
            allOffsetPaths.forEach(entityGroup::addChild);
        }
        controller.getDrawing().repaint();
    }

    private Path performUnionOperation(List<Path> paths) {
        try {
            GeometryFactory gf = new GeometryFactory();
            ShapeReader reader = new ShapeReader(gf);
            Geometry unionGeometry = null;
            for (Path path : paths) {
                Shape pathShape = path.getShape();
                if (pathShape == null) continue;
                Shape flattenedShape = flattenShape(pathShape);
                Geometry geom = reader.read(flattenedShape.getPathIterator(new AffineTransform()));
                if (geom == null || geom.isEmpty()) continue;
                if (!geom.isValid()) geom = geom.buffer(0);
                unionGeometry = (unionGeometry == null) ? geom : unionGeometry.union(geom);
            }
            if (unionGeometry != null && !unionGeometry.isEmpty()) {
                return shapeToPath(unionGeometry);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private Shape getEntityShape(Entity entity) {
        Shape shape = entity.getShape();
        if (shape != null && shape.getBounds2D().getWidth() > 0 && shape.getBounds2D().getHeight() > 0) {
            return shape;
        }
        Shape relativeShape = entity.getRelativeShape();
        if (relativeShape != null && entity.getTransform() != null) {
            Shape transformedShape = entity.getTransform().createTransformedShape(relativeShape);
            if (transformedShape != null && transformedShape.getBounds2D().getWidth() > 0) {
                return transformedShape;
            }
        }
        java.awt.geom.Rectangle2D bounds = entity.getBounds();
        if (bounds.getWidth() > 0 && bounds.getHeight() > 0) {
            return new java.awt.geom.Rectangle2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
        }
        return null;
    }

    private List<Path> createDirectionalOffsetPaths(Shape shape, float offset, String direction) {
        List<Path> paths = new ArrayList<>();
        try {
            GeometryFactory gf = new GeometryFactory();
            ShapeReader reader = new ShapeReader(gf);
            Shape flattenedShape = flattenShape(shape);
            Geometry geom = reader.read(flattenedShape.getPathIterator(new AffineTransform()));
            if (geom == null || geom.isEmpty()) return paths;
            if (!geom.isValid()) geom = geom.buffer(0);

            if ("Outward".equals(direction)) {
                Geometry buffered = createOffset(geom, offset);
                if (buffered != null && !buffered.isEmpty()) paths.add(shapeToPath(buffered));
            } else if ("Inward".equals(direction)) {
                Geometry buffered = createOffset(geom, -offset);
                if (buffered != null && !buffered.isEmpty()) paths.add(shapeToPath(buffered));
            } else if ("Both".equals(direction)) {
                Geometry bufferedOut = createOffset(geom, offset);
                if (bufferedOut != null && !bufferedOut.isEmpty()) paths.add(shapeToPath(bufferedOut));
                Geometry bufferedIn = createOffset(geom, -offset);
                if (bufferedIn != null && !bufferedIn.isEmpty()) paths.add(shapeToPath(bufferedIn));
            }
        } catch (Exception ignored) {}
        return paths;
    }

    private Shape flattenShape(Shape shape) {
        double flatness = 0.1;
        java.awt.geom.PathIterator pathIterator = shape.getPathIterator(new AffineTransform(), flatness);
        java.awt.geom.Path2D.Double flattenedPath = new java.awt.geom.Path2D.Double();
        flattenedPath.append(pathIterator, false);
        return flattenedPath;
    }

    private Geometry createOffset(Geometry geom, float offset) {
        try {
            BufferParameters bufferParams = createBufferParameters();
            return BufferOp.bufferOp(geom, offset, bufferParams);
        } catch (Exception e) {
            BufferParameters roundParams = new BufferParameters();
            roundParams.setEndCapStyle(BufferParameters.CAP_ROUND);
            roundParams.setJoinStyle(BufferParameters.JOIN_ROUND);
            roundParams.setQuadrantSegments(16);
            return BufferOp.bufferOp(geom, offset, roundParams);
        }
    }

    private BufferParameters createBufferParameters() {
        BufferParameters bufferParams = new BufferParameters();
        bufferParams.setEndCapStyle(BufferParameters.CAP_FLAT);
        bufferParams.setQuadrantSegments(16);
        bufferParams.setSingleSided(false);

        String joinStyle = (String) joinStyleBox.getSelectedItem();
        if ("Round".equals(joinStyle)) {
            bufferParams.setJoinStyle(BufferParameters.JOIN_ROUND);
        } else if ("Bevel".equals(joinStyle)) {
            bufferParams.setJoinStyle(BufferParameters.JOIN_BEVEL);
        } else if ("Mitre".equals(joinStyle)) {
            bufferParams.setJoinStyle(BufferParameters.JOIN_MITRE);
            bufferParams.setMitreLimit(10.0);
        } else {
            bufferParams.setJoinStyle(BufferParameters.JOIN_ROUND);
        }
        return bufferParams;
    }

    private Path shapeToPath(Geometry geometry) {
        ShapeWriter writer = new ShapeWriter();
        Shape offsetShape = writer.toShape(geometry);
        Path offsetPath = new Path();
        offsetPath.append(offsetShape);
        return offsetPath;
    }

    @Override
    public void windowOpened(WindowEvent e) {}
    @Override
    public void windowClosing(WindowEvent e) {
        controller.getDrawing().removeEntity(entityGroup);
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

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
        setPreferredSize(new Dimension(400, 300));
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
            System.out.println("Processing entity type: " + entity.getClass().getSimpleName());

            // Try multiple ways to get the shape for different entity types
            Shape shape = getEntityShape(entity);
            if (shape == null) {
                System.out.println("Could not extract shape from entity");
                return;
            }

            System.out.println("Extracted shape bounds: " + shape.getBounds2D());

            List<Path> offsetPaths = createDirectionalOffsetPaths(shape, offset, direction);
            System.out.println("Generated " + offsetPaths.size() + " offset paths");

            for (Path offsetPath : offsetPaths) {
                entityGroup.addChild(offsetPath);
            }
        });

        System.out.println("Final entityGroup has " + entityGroup.getChildren().size() + " children");
        controller.getDrawing().repaint();
    }

    /**
     * Extract shape from entity using multiple fallback methods for different entity types.
     */
    private Shape getEntityShape(Entity entity) {
        try {
            // Method 1: Try getShape() (works for most entities including rectangles)
            Shape shape = entity.getShape();
            if (shape != null && shape.getBounds2D().getWidth() > 0 && shape.getBounds2D().getHeight() > 0) {
                System.out.println("Successfully got shape using getShape()");
                return shape;
            }
        } catch (Exception e) {
            System.out.println("getShape() failed: " + e.getMessage());
        }

        try {
            // Method 2: Try combining relative shape with transform (fallback for problematic entities)
            Shape relativeShape = entity.getRelativeShape();
            if (relativeShape != null) {
                AffineTransform transform = entity.getTransform();
                if (transform != null) {
                    Shape transformedShape = transform.createTransformedShape(relativeShape);
                    if (transformedShape != null && transformedShape.getBounds2D().getWidth() > 0) {
                        System.out.println("Successfully got shape using relative + transform");
                        return transformedShape;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Relative shape + transform failed: " + e.getMessage());
        }

        try {
            // Method 3: Build shape from bounds (last resort)
            java.awt.geom.Rectangle2D bounds = entity.getBounds();
            if (bounds.getWidth() > 0 && bounds.getHeight() > 0) {
                System.out.println("Using bounds as rectangle shape (last resort)");
                return new java.awt.geom.Rectangle2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
            }
        } catch (Exception e) {
            System.out.println("Bounds fallback failed: " + e.getMessage());
        }

        System.out.println("All shape extraction methods failed");
        return null;
    }

    /**
     * Create offset paths based on direction.
     * Creates proper offset shapes for all entity types (rectangles, ellipses, paths, etc.).
     */
    private List<Path> createDirectionalOffsetPaths(Shape shape, float offset, String direction) {
        List<Path> paths = new ArrayList<>();

        try {
            GeometryFactory gf = new GeometryFactory();
            ShapeReader reader = new ShapeReader(gf);

            // Flatten the shape to handle curves (ellipses, arcs, etc.)
            Shape flattenedShape = flattenShape(shape);

            // Convert the flattened shape to JTS geometry
            Geometry geom = reader.read(flattenedShape.getPathIterator(new AffineTransform()));
            System.out.println("JTS Geometry type: " + (geom != null ? geom.getGeometryType() : "null"));
            System.out.println("JTS Geometry bounds: " + (geom != null ? geom.getEnvelopeInternal() : "null"));
            System.out.println("JTS Geometry area: " + (geom != null ? geom.getArea() : "null"));

            if (geom == null || geom.isEmpty()) {
                System.out.println("Geometry is null or empty, returning empty paths");
                return paths;
            }

            // Ensure the geometry is valid - fix any topology issues
            if (!geom.isValid()) {
                System.out.println("Geometry is invalid, fixing with buffer(0)");
                geom = geom.buffer(0);
            }

            // Use different buffer strategies based on direction and shape characteristics
            if ("Outward".equals(direction)) {
                System.out.println("Creating outward offset...");
                Geometry buffered = createOutwardOffset(geom, offset);
                if (buffered != null && !buffered.isEmpty()) {
                    paths.add(shapeToPath(buffered));
                    System.out.println("Successfully created outward offset");
                } else {
                    System.out.println("Outward offset failed or empty");
                }
            } else if ("Inward".equals(direction)) {
                System.out.println("Creating inward offset...");
                Geometry buffered = createInwardOffset(geom, offset);
                if (buffered != null && !buffered.isEmpty()) {
                    paths.add(shapeToPath(buffered));
                    System.out.println("Successfully created inward offset");
                } else {
                    System.out.println("Inward offset failed or empty");
                }
            } else if ("Both".equals(direction)) {
                System.out.println("Creating both offsets...");
                Geometry bufferedOut = createOutwardOffset(geom, offset);
                if (bufferedOut != null && !bufferedOut.isEmpty()) {
                    paths.add(shapeToPath(bufferedOut));
                    System.out.println("Successfully created outward offset");
                } else {
                    System.out.println("Outward offset failed or empty");
                }

                Geometry bufferedIn = createInwardOffset(geom, offset);
                if (bufferedIn != null && !bufferedIn.isEmpty()) {
                    paths.add(shapeToPath(bufferedIn));
                    System.out.println("Successfully created inward offset");
                } else {
                    System.out.println("Inward offset failed or empty");
                }
            }

        } catch (Exception e) {
            System.err.println("Exception in createDirectionalOffsetPaths: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("Returning " + paths.size() + " paths");
        return paths;
    }

    /**
     * Flatten curved shapes (ellipses, arcs) into linear segments that JTS can handle.
     */
    private Shape flattenShape(Shape shape) {
        try {
            // Use a high-resolution flattening to preserve curve quality
            double flatness = 0.1; // Lower values = higher precision
            java.awt.geom.PathIterator pathIterator = shape.getPathIterator(new AffineTransform(), flatness);

            java.awt.geom.Path2D.Double flattenedPath = new java.awt.geom.Path2D.Double();
            flattenedPath.append(pathIterator, false);

            System.out.println("Successfully flattened shape for JTS compatibility");
            return flattenedPath;

        } catch (Exception e) {
            System.out.println("Shape flattening failed: " + e.getMessage() + ", using original shape");
            return shape;
        }
    }

    /**
     * Create outward offset using buffer parameters optimized for expansion.
     */
    private Geometry createOutwardOffset(Geometry geom, float offset) {
        try {
            BufferParameters bufferParams = new BufferParameters();
            bufferParams.setEndCapStyle(BufferParameters.CAP_FLAT);
            bufferParams.setJoinStyle(BufferParameters.JOIN_MITRE);
            bufferParams.setQuadrantSegments(16); // Higher resolution for smooth curves
            bufferParams.setMitreLimit(2.0);
            bufferParams.setSingleSided(false);

            return BufferOp.bufferOp(geom, offset, bufferParams);
        } catch (Exception e) {
            // Fallback to round joins for problematic geometries
            BufferParameters roundParams = new BufferParameters();
            roundParams.setEndCapStyle(BufferParameters.CAP_ROUND);
            roundParams.setJoinStyle(BufferParameters.JOIN_ROUND);
            roundParams.setQuadrantSegments(16);

            return BufferOp.bufferOp(geom, offset, roundParams);
        }
    }

    /**
     * Create inward offset with multiple fallback strategies.
     */
    private Geometry createInwardOffset(Geometry geom, float offset) {
        try {
            // First try with mitre joins for sharp corners
            BufferParameters bufferParams = new BufferParameters();
            bufferParams.setEndCapStyle(BufferParameters.CAP_FLAT);
            bufferParams.setJoinStyle(BufferParameters.JOIN_MITRE);
            bufferParams.setQuadrantSegments(16);
            bufferParams.setMitreLimit(2.0);
            bufferParams.setSingleSided(false);

            Geometry buffered = BufferOp.bufferOp(geom, -offset, bufferParams);
            if (buffered != null && !buffered.isEmpty()) {
                return buffered;
            }
        } catch (Exception e) {
            // Continue to fallback
        }

        try {
            // Fallback 1: Round joins (more forgiving for complex shapes)
            BufferParameters roundParams = new BufferParameters();
            roundParams.setEndCapStyle(BufferParameters.CAP_ROUND);
            roundParams.setJoinStyle(BufferParameters.JOIN_ROUND);
            roundParams.setQuadrantSegments(16);

            Geometry buffered = BufferOp.bufferOp(geom, -offset, roundParams);
            if (buffered != null && !buffered.isEmpty()) {
                return buffered;
            }
        } catch (Exception e) {
            // Continue to fallback
        }

        // Fallback 2: Try with smaller offset if the original is too large
        try {
            double maxDimension = Math.max(geom.getEnvelopeInternal().getWidth(), geom.getEnvelopeInternal().getHeight());
            float safeOffset = Math.min(offset, (float)(maxDimension * 0.4)); // Max 40% of shape size

            BufferParameters safeParams = new BufferParameters();
            safeParams.setEndCapStyle(BufferParameters.CAP_ROUND);
            safeParams.setJoinStyle(BufferParameters.JOIN_ROUND);
            safeParams.setQuadrantSegments(8);

            return BufferOp.bufferOp(geom, -safeOffset, safeParams);
        } catch (Exception e) {
            return null;
        }
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

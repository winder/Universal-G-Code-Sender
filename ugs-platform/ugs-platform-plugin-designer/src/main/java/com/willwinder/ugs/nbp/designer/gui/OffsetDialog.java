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

import clipper2.Clipper;
import clipper2.core.Path64;
import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntityGroup;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Path;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.Tool;
import com.willwinder.ugs.nbp.designer.model.Size;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Joacim Breiler
 */
public class OffsetDialog extends JDialog implements ChangeListener, WindowListener {
    public static final int PADDING = 2;
    public static final String PANEL_LAYOUT_CONFIG = "fill, insets 2";
    public static final String SPINNER_COL_CONSTRAINTS = "width 60:100:100, wrap";

    private JSpinner offsetDistance;
    private JSpinner xDelta;
    private JSpinner yDelta;
    private JToggleButton joinShapes;
    private JComboBox<String> mode;
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
        joinShapes.addChangeListener(this);
        mode.addActionListener(event -> stateChanged(null));
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
        add(horizontalPanel, "grow");

        JPanel verticalPanel = new JPanel(new MigLayout(PANEL_LAYOUT_CONFIG));
        verticalPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Options"),
                BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING)));
        verticalPanel.add(new JLabel("Join shapes", SwingConstants.TRAILING), "grow");
        joinShapes = new JToggleButton("Join shapes");
        verticalPanel.add(joinShapes, SPINNER_COL_CONSTRAINTS);

        verticalPanel.add(new JLabel("Y Spacing", SwingConstants.TRAILING), "grow");
        mode = new JComboBox<>(new String[]{"None", "Round", "Bevel"});
        verticalPanel.add(mode, SPINNER_COL_CONSTRAINTS);
        add(verticalPanel, "grow, wrap");

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
        selection.getChildren().forEach(entity -> {
            Rectangle2D b = entity.getBounds();
            double x = b.getX() - offset;
            double y = b.getY() - offset;
            double w = b.getWidth() + 2 * offset;
            double h = b.getHeight() + 2 * offset;

            Path offsetPath = createOuterOffsetPath(entity.getRelativeShape(), offset);
            if (offsetPath != null) {
                //controller.addEntity(offsetPath);
                offsetPath.setPosition(new Point2D.Double(x, y));
                offsetPath.setSize(new Size(w, h));
                entityGroup.addChild(offsetPath);
            }
        });
        controller.getDrawing().repaint();
    }


    /**
     * Create a new Path that is an offset of the current path.
     * This method uses BasicStroke to create the offset shape.
     *
     * @param offset The distance to offset the path.
     * @return A new Path representing the offset, or null if the operation fails.
     */
    public Path createOffsetPath(Shape shape, Float offset) {
        Path64 point64s = shapeToPath64(shape);
        Clipper.OffsetPath(point64s, offset.longValue(), offset.longValue());
        java.awt.Shape strokedShape = new java.awt.BasicStroke(
                offset * 2, // width of the stroke
                getCapRound(),
                getJoinRound()
        ).createStrokedShape(shape);

        Path offsetPath = new Path();
        offsetPath.append(strokedShape);
        return offsetPath;
    }
    // Shape to Path64
    public static Path64 shapeToPath64(Shape shape) {
        Path64 path64 = new Path64();
        PathIterator pi = shape.getPathIterator(null);
        double[] coords = new double[6];
        while (!pi.isDone()) {
            int type = pi.currentSegment(coords);
            if (type == PathIterator.SEG_MOVETO || type == PathIterator.SEG_LINETO) {
                path64.add(new clipper2.core.Point64((long) coords[0], (long) coords[1]));
            }
            pi.next();
        }
        return path64;
    }

    // Path64 to Shape
    public static Shape path64ToShape(Path64 path64) {
        GeneralPath gp = new GeneralPath();
        boolean first = true;
        for (clipper2.core.Point64 pt : path64) {
            if (first) {
                gp.moveTo(pt.x, pt.y);
                first = false;
            } else {
                gp.lineTo(pt.x, pt.y);
            }
        }
        gp.closePath();
        return gp;
    }

    private int getJoinRound() {
        String capMode = (String) mode.getSelectedItem();
        if (capMode == null) {
            return BasicStroke.JOIN_ROUND;
        }
        return switch (capMode){
            case "Bevel" -> BasicStroke.JOIN_BEVEL;
            case "Round" -> BasicStroke.JOIN_ROUND;
            case "None" -> BasicStroke.JOIN_MITER;
            default -> BasicStroke.JOIN_ROUND;
        };
    }

    private int getCapRound() {
        String capMode = (String) mode.getSelectedItem();
        if (capMode == null) {
            return BasicStroke.CAP_ROUND;
        }
        return switch (capMode) {
            case "Bevel" -> BasicStroke.CAP_BUTT;
            case "Round" -> BasicStroke.CAP_ROUND;
            case "None" -> BasicStroke.CAP_SQUARE;
            default -> BasicStroke.CAP_ROUND;
        };
    }

    /**
     * Create a new Path that is an outer offset of the current path.
     * This method uses BasicStroke to create the offset shape.
     *
     * @param offset The distance to offset the path outward.
     * @return A new Path representing the outer offset, or null if the operation fails.
     */
    public Path createOuterOffsetPath(Shape shape, float offset) {

        Shape strokedShape = new BasicStroke(
                offset * 2,
                getCapRound(),
                getJoinRound()
        ).createStrokedShape(shape);

        Area outlineArea = new Area(strokedShape);
        Area originalArea = new Area(shape);

        outlineArea.add(originalArea);

        Path outerOffsetPath = new Path();
        outerOffsetPath.append(outlineArea);
        return outerOffsetPath;
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

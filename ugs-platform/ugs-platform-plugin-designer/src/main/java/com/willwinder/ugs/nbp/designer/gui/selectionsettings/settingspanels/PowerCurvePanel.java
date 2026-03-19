/*
    Copyright 2026 Will Winder

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
package com.willwinder.ugs.nbp.designer.gui.selectionsettings.settingspanels;

import com.willwinder.ugs.nbp.designer.entities.cuttable.MonotoneCubicSpline;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * A Photoshop-style curve editor panel for mapping pixel brightness to laser power output.
 * <p>
 * X-axis: input brightness (0-255), Y-axis: output power (0-255).
 * Click to add control points, drag to move, double-click or Ctrl+click to remove.
 * The displayed white curve and the LUT preview are both driven by {@link MonotoneCubicSpline},
 * so the UI and the raster processing share the same curve definition.
 */
public class PowerCurvePanel extends JPanel {

    public static final String PROPERTY_CURVE_CHANGED = "curveChanged";

    private static final int CANVAS_SIZE = 200;
    private static final int POINT_RADIUS = 5;
    private static final int HIT_THRESHOLD = 12;
    private static final Color BG_COLOR = new Color(0x1A, 0x1A, 0x1A);
    private static final Color GRID_COLOR = new Color(0x33, 0x33, 0x33);
    private static final Color REFERENCE_COLOR = new Color(0x55, 0x55, 0x55);
    private static final Color CURVE_COLOR = Color.WHITE;
    private static final Color POINT_FILL = new Color(0x4A, 0x90, 0xD9);
    private static final Color PREVIEW_FILL = new Color(0x4A, 0x90, 0xD9, 0x30);
    private static final Color PREVIEW_STROKE = new Color(0x4A, 0x90, 0xD9, 0x80);

    private static final Cursor CURSOR_ADD = buildTextCursor("+");
    private static final Cursor CURSOR_REMOVE = buildTextCursor("−");
    private static final Cursor CURSOR_MOVE = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
    private static final Cursor CURSOR_DEFAULT = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);

    private final List<Point> controlPoints = new ArrayList<>();
    private final CurveCanvas canvas;
    private int draggingIndex = -1;

    public PowerCurvePanel() {
        setLayout(new BorderLayout(0, 4));

        controlPoints.add(new Point(0, 0));
        controlPoints.add(new Point(255, 255));

        canvas = new CurveCanvas();

        JButton resetBtn = new JButton("Reset");
        resetBtn.addActionListener(e -> resetToLinear());

        JLabel hint = new JLabel("<html><small><font color='#888888'>" +
                "Click: add &nbsp;|&nbsp; Drag: move &nbsp;|&nbsp; " +
                "Ctrl+click or double-click: remove</font></small></html>");

        JPanel buttonPanel = new JPanel(new BorderLayout(4, 0));
        buttonPanel.setOpaque(false);
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        btnRow.setOpaque(false);
        btnRow.add(resetBtn);
        buttonPanel.add(btnRow, BorderLayout.WEST);
        buttonPanel.add(hint, BorderLayout.CENTER);

        add(canvas, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Sets the control points from an array of [x, y] pairs.
     */
    public void setControlPoints(int[][] points) {
        controlPoints.clear();
        for (int[] point : points) {
            controlPoints.add(new Point(point[0], point[1]));
        }
        canvas.repaint();
    }

    /**
     * Returns the control points as an array of [x, y] pairs.
     */
    public int[][] getControlPoints() {
        int[][] result = new int[controlPoints.size()][2];
        for (int i = 0; i < controlPoints.size(); i++) {
            result[i][0] = controlPoints.get(i).x;
            result[i][1] = controlPoints.get(i).y;
        }
        return result;
    }

    public void resetToLinear() {
        setControlPoints(MonotoneCubicSpline.defaultControlPoints());
        fireCurveChanged();
    }

    private void fireCurveChanged() {
        firePropertyChange(PROPERTY_CURVE_CHANGED, null, getControlPoints());
    }

    private Point toLogical(Point screen) {
        int w = Math.max(1, canvas.getWidth());
        int h = Math.max(1, canvas.getHeight());
        int x = Math.round((float) screen.x / w * 255);
        int y = Math.round((1f - (float) screen.y / h) * 255);
        return new Point(
                Math.max(0, Math.min(255, x)),
                Math.max(0, Math.min(255, y))
        );
    }

    private Point toScreen(Point logical) {
        int w = canvas.getWidth();
        int h = canvas.getHeight();
        int sx = Math.round((float) logical.x / 255 * w);
        int sy = Math.round((1f - (float) logical.y / 255) * h);
        return new Point(sx, sy);
    }

    private int findNearPoint(Point logical) {
        for (int i = 0; i < controlPoints.size(); i++) {
            Point p = controlPoints.get(i);
            if (Math.hypot(p.x - logical.x, p.y - logical.y) < HIT_THRESHOLD) {
                return i;
            }
        }
        return -1;
    }

    private AffineTransform createLogicalToScreenTransform(int width, int height) {
        return new AffineTransform((double) width / 255.0, 0, 0, (double) -height / 255.0, 0, height);
    }

    private int insertControlPoint(Point logical) {
        // Find the insertion index (keep list sorted by x)
        int insertIndex = 0;
        while (insertIndex < controlPoints.size() && controlPoints.get(insertIndex).x < logical.x) {
            insertIndex++;
        }

        // Compute the valid x range between the two neighboring points, exclusive
        int minX = (insertIndex == 0) ? 1 : controlPoints.get(insertIndex - 1).x + 1;
        int maxX = (insertIndex >= controlPoints.size()) ? 254 : controlPoints.get(insertIndex).x - 1;
        if (minX > maxX) {
            return -1; // no room between neighbors
        }

        logical.x = Math.max(minX, Math.min(maxX, logical.x));
        controlPoints.add(insertIndex, logical);
        return insertIndex;
    }

    /**
     * Inner class for the actual drawing canvas.
     */
    private class CurveCanvas extends JPanel {

        private Point lastHoveredLogical = new Point(128, 128);

        CurveCanvas() {
            setPreferredSize(new Dimension(CANVAS_SIZE, CANVAS_SIZE));
            setBackground(BG_COLOR);
            setCursor(CURSOR_ADD);
            setFocusable(true);

            MouseAdapter mouseHandler = new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    requestFocusInWindow();
                    lastHoveredLogical = toLogical(e.getPoint());
                    updateCursor(lastHoveredLogical, e.isControlDown());
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    Point logical = toLogical(e.getPoint());
                    int nearestIndex = findNearPoint(logical);

                    if (e.isControlDown()) {
                        if (nearestIndex > 0 && nearestIndex < controlPoints.size() - 1) {
                            controlPoints.remove(nearestIndex);
                            draggingIndex = -1;
                            repaint();
                            fireCurveChanged();
                        }
                        return;
                    }

                    draggingIndex = nearestIndex;
                    if (draggingIndex < 0) {
                        draggingIndex = insertControlPoint(logical);
                        if (draggingIndex >= 0) {
                            repaint();
                            fireCurveChanged();
                        }
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (draggingIndex >= 0) {
                        draggingIndex = -1;
                        fireCurveChanged();
                    }
                    updateCursor(toLogical(e.getPoint()), e.isControlDown());
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        Point logical = toLogical(e.getPoint());
                        int idx = findNearPoint(logical);
                        if (idx > 0 && idx < controlPoints.size() - 1) {
                            controlPoints.remove(idx);
                            repaint();
                            fireCurveChanged();
                        }
                    }
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (draggingIndex < 0) {
                        return;
                    }

                    Point logical = toLogical(e.getPoint());
                    if (draggingIndex == 0) {
                        logical.x = 0;
                    } else if (draggingIndex == controlPoints.size() - 1) {
                        logical.x = 255;
                    } else {
                        // Interior point: stay strictly between its neighbors, never at 0 or 255
                        int minX = Math.max(1, controlPoints.get(draggingIndex - 1).x + 1);
                        int maxX = Math.min(254, controlPoints.get(draggingIndex + 1).x - 1);
                        logical.x = Math.max(minX, Math.min(maxX, logical.x));
                    }

                    controlPoints.set(draggingIndex, logical);
                    repaint();
                }
            };

            addMouseListener(mouseHandler);
            addMouseMotionListener(mouseHandler);

            // Update cursor immediately when Ctrl is pressed or released
            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
                        updateCursor(lastHoveredLogical, true);
                    }
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
                        updateCursor(lastHoveredLogical, false);
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(BG_COLOR);
            g2.fillRect(0, 0, w, h);

            g2.setColor(GRID_COLOR);
            g2.setStroke(new BasicStroke(0.5f));
            for (int i = 1; i < 4; i++) {
                int px = (int) (i / 4.0 * w);
                int py = (int) (i / 4.0 * h);
                g2.drawLine(px, 0, px, h);
                g2.drawLine(0, py, w, py);
            }

            g2.setColor(REFERENCE_COLOR);
            g2.setStroke(new BasicStroke(1f));
            g2.drawLine(0, h, w, 0);

            int[][] curvePoints = getControlPoints();
            Path2D logicalPath = MonotoneCubicSpline.buildCurvePath(curvePoints);
            Path2D screenPath = (Path2D) logicalPath.createTransformedShape(createLogicalToScreenTransform(w, h));

            // Fill area under the curve, reusing the same screen path
            Path2D area = new Path2D.Double();
            area.moveTo(0, h);
            area.append(screenPath, true);
            area.lineTo(w, h);
            area.closePath();

            g2.setColor(PREVIEW_FILL);
            g2.fill(area);
            g2.setColor(PREVIEW_STROKE);
            g2.setStroke(new BasicStroke(1f));
            g2.draw(area);

            g2.setColor(CURVE_COLOR);
            g2.setStroke(new BasicStroke(2f));
            g2.draw(screenPath);

            // Control points
            for (Point cp : controlPoints) {
                Point sp = toScreen(cp);
                g2.setColor(POINT_FILL);
                g2.fill(new Ellipse2D.Double(
                        sp.x - POINT_RADIUS, sp.y - POINT_RADIUS,
                        POINT_RADIUS * 2, POINT_RADIUS * 2));
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new Ellipse2D.Double(
                        sp.x - POINT_RADIUS, sp.y - POINT_RADIUS,
                        POINT_RADIUS * 2, POINT_RADIUS * 2));
            }
            g2.dispose();
        }
    } // end CurveCanvas

    /** Picks the right cursor based on current hover position and modifier state. */
    private void updateCursor(Point logical, boolean ctrlDown) {
        int idx = findNearPoint(logical);
        Cursor cursor;
        if (idx >= 0) {
            boolean removable = idx > 0 && idx < controlPoints.size() - 1;
            cursor = (ctrlDown && removable) ? CURSOR_REMOVE : CURSOR_MOVE;
        } else {
            cursor = ctrlDown ? CURSOR_DEFAULT : CURSOR_ADD;
        }
        canvas.setCursor(cursor);
    }

    /**
     * Builds a custom cursor with a text symbol centred on a 32×32 image.
     * Falls back to the system crosshair if custom cursors are unsupported.
     */
    private static Cursor buildTextCursor(String symbol) {
        try {
            int size = 32;
            BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
            var fm = g.getFontMetrics();
            int tx = (size - fm.stringWidth(symbol)) / 2;
            int ty = (size - fm.getHeight()) / 2 + fm.getAscent();
            // Dark outline for legibility on any background
            g.setColor(new Color(0, 0, 0, 180));
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    g.drawString(symbol, tx + dx, ty + dy);
                }
            }
            g.setColor(Color.WHITE);
            g.drawString(symbol, tx, ty);
            g.dispose();
            return Toolkit.getDefaultToolkit().createCustomCursor(img, new Point(size / 2, size / 2), symbol);
        } catch (Exception ex) {
            return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
        }
    }
}

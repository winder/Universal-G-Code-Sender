package com.willwinder.ugs.designer.gui;

import com.willwinder.ugs.designer.entities.cuttable.MonotoneCubicSpline;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
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
 * @author Albert Giro github.com/giro-dev
 * A reusable Photoshop-style curve editor canvas.
 * <p>
 * Maps input (X-axis, 0–255) to output (Y-axis, 0–255) through a smooth
 * Catmull-Rom spline defined by user-editable control points.
 * <ul>
 *   <li>Click to add a control point</li>
 *   <li>Drag to move a control point</li>
 *   <li>Double-click or Ctrl+click to remove a control point</li>
 * </ul>
 * Endpoint X-positions are fixed at 0 and 255; only their Y-values can be changed.
 * <p>
 * Fires a {@value #PROPERTY_CURVE_CHANGED} {@link java.beans.PropertyChangeEvent}
 * whenever the curve is modified (on mouse release, point add, or point remove).
 */
public class CurveCanvas extends JPanel {

    public static final String PROPERTY_CURVE_CHANGED = "curveChanged";

    private static final int DEFAULT_SIZE = 200;
    private static final int POINT_RADIUS = 5;
    private static final int HIT_THRESHOLD = 12;

    private static final Color BG_COLOR = new Color(0x85, 0x85, 0x85);
    private static final Color GRID_COLOR = new Color(0x33, 0x33, 0x33);
    private static final Color REFERENCE_COLOR = new Color(0x55, 0x55, 0x55);
    private static final Color CURVE_COLOR = Color.WHITE;
    private static final Color POINT_FILL = new Color(0x4A, 0x90, 0xD9);
    private static final Color PREVIEW_FILL = new Color(0x4A, 0x90, 0xD9, 0x30);
    private static final Color PREVIEW_STROKE = new Color(0x4A, 0x90, 0xD9, 0x80);

    private static final Color LABEL_COLOR = new Color(0xDD, 0xDD, 0xDD);
    private static final Font LABEL_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 11);

    private static final Cursor CURSOR_ADD = buildTextCursor("+");
    private static final Cursor CURSOR_REMOVE = buildTextCursor("-");
    private static final Cursor CURSOR_MOVE = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
    private static final Cursor CURSOR_DEFAULT = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);

    private final List<Point> controlPoints = new ArrayList<>();
    private final String xAxisLabel;
    private final String yAxisLabel;
    private int draggingIndex = -1;
    private Point lastHoveredLogical = new Point(128, 128);

    /** Creates a canvas with no axis labels. */
    public CurveCanvas() {
        this(null, null);
    }

    /**
     * Creates a canvas with optional axis labels.
     *
     * @param xAxisLabel label drawn along the bottom edge, or {@code null} to omit
     * @param yAxisLabel label drawn along the left edge (rotated), or {@code null} to omit
     */
    public CurveCanvas(String xAxisLabel, String yAxisLabel) {
        this.xAxisLabel = xAxisLabel;
        this.yAxisLabel = yAxisLabel;

        setPreferredSize(new Dimension(DEFAULT_SIZE, DEFAULT_SIZE));
        setBackground(BG_COLOR);
        setCursor(CURSOR_ADD);
        setFocusable(true);

        controlPoints.add(new Point(0, 0));
        controlPoints.add(new Point(255, 255));

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

    /**
     * Sets the control points from an array of [x, y] pairs.
     */
    public void setControlPoints(int[][] points) {
        controlPoints.clear();
        for (int[] point : points) {
            controlPoints.add(new Point(point[0], point[1]));
        }
        repaint();
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

    /** Resets the curve to a linear identity mapping. */
    public void resetToLinear() {
        setControlPoints(MonotoneCubicSpline.defaultControlPoints());
        fireCurveChanged();
    }

    /**
     * Returns {@code null} while dragging (hides the tooltip during interaction),
     * otherwise returns the usage hint so Swing shows it after the initial delay.
     */
    @Override
    public String getToolTipText(MouseEvent e) {
        return draggingIndex >= 0 ? null : super.getToolTipText(e);
    }

    /**
     * Anchors the tooltip 16 px right and 16 px below the cursor
     * so it never obscures the point being edited.
     */
    @Override
    public Point getToolTipLocation(MouseEvent e) {
        return new Point(e.getX() + 16, e.getY() + 16);
    }

    // -----------------------------------------------------------------
    // Painting
    // -----------------------------------------------------------------

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
        Path2D screenPath = (Path2D) logicalPath.createTransformedShape(
                createLogicalToScreenTransform(w, h));

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

        // Axis labels
        g2.setFont(LABEL_FONT);
        g2.setColor(LABEL_COLOR);
        var fm = g2.getFontMetrics();

        if (xAxisLabel != null) {
            int lx = (w - fm.stringWidth(xAxisLabel)) / 2;
            g2.drawString(xAxisLabel, lx, h - fm.getDescent() - 2);
        }

        if (yAxisLabel != null) {
            AffineTransform prev = g2.getTransform();
            g2.rotate(-Math.PI / 2);
            int lx = -(h + fm.stringWidth(yAxisLabel)) / 2;
            g2.drawString(yAxisLabel, lx, fm.getAscent() + 2);
            g2.setTransform(prev);
        }

        g2.dispose();
    }

    // -----------------------------------------------------------------
    // Coordinate conversion & hit testing
    // -----------------------------------------------------------------

    private Point toLogical(Point screen) {
        int w = Math.max(1, getWidth());
        int h = Math.max(1, getHeight());
        int x = Math.round((float) screen.x / w * 255);
        int y = Math.round((1f - (float) screen.y / h) * 255);
        return new Point(
                Math.max(0, Math.min(255, x)),
                Math.max(0, Math.min(255, y))
        );
    }

    private Point toScreen(Point logical) {
        int w = getWidth();
        int h = getHeight();
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
        return new AffineTransform(
                (double) width / 255.0, 0,
                0, (double) -height / 255.0,
                0, height);
    }

    private int insertControlPoint(Point logical) {
        int insertIndex = 0;
        while (insertIndex < controlPoints.size() && controlPoints.get(insertIndex).x < logical.x) {
            insertIndex++;
        }

        int minX = (insertIndex == 0) ? 1 : controlPoints.get(insertIndex - 1).x + 1;
        int maxX = (insertIndex >= controlPoints.size()) ? 254 : controlPoints.get(insertIndex).x - 1;
        if (minX > maxX) {
            return -1;
        }

        logical.x = Math.max(minX, Math.min(maxX, logical.x));
        controlPoints.add(insertIndex, logical);
        return insertIndex;
    }

    // -----------------------------------------------------------------
    // Cursor & events
    // -----------------------------------------------------------------

    private void updateCursor(Point logical, boolean ctrlDown) {
        int idx = findNearPoint(logical);
        Cursor cursor;
        if (idx >= 0) {
            boolean removable = idx > 0 && idx < controlPoints.size() - 1;
            cursor = (ctrlDown && removable) ? CURSOR_REMOVE : CURSOR_MOVE;
        } else {
            cursor = ctrlDown ? CURSOR_DEFAULT : CURSOR_ADD;
        }
        setCursor(cursor);
    }

    private void fireCurveChanged() {
        firePropertyChange(PROPERTY_CURVE_CHANGED, null, getControlPoints());
    }

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
            g.setColor(new Color(0, 0, 0, 180));
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    g.drawString(symbol, tx + dx, ty + dy);
                }
            }
            g.setColor(Color.WHITE);
            g.drawString(symbol, tx, ty);
            g.dispose();
            return Toolkit.getDefaultToolkit().createCustomCursor(
                    img, new Point(size / 2, size / 2), symbol);
        } catch (Exception ex) {
            return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
        }
    }
}

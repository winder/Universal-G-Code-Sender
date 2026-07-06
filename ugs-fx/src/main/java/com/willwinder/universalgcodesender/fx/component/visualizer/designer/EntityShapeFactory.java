package com.willwinder.universalgcodesender.fx.component.visualizer.designer;

import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.entities.controls.Control;
import com.willwinder.ugs.designer.entities.controls.CreateEllipseControl;
import com.willwinder.ugs.designer.entities.controls.CreateLineControl;
import com.willwinder.ugs.designer.entities.controls.CreateOffsetControl;
import com.willwinder.ugs.designer.entities.controls.CreatePointControl;
import com.willwinder.ugs.designer.entities.controls.CreateRectangleControl;
import com.willwinder.ugs.designer.entities.controls.CreateTextControl;
import com.willwinder.ugs.designer.entities.controls.EditTextControl;
import com.willwinder.ugs.designer.entities.controls.HighlightModelControl;
import com.willwinder.ugs.designer.entities.controls.MoveControl;
import com.willwinder.ugs.designer.entities.controls.ResizeControl;
import com.willwinder.ugs.designer.entities.controls.RotationControl;
import com.willwinder.ugs.designer.entities.controls.SelectionControl;
import com.willwinder.ugs.designer.entities.controls.VertexControl;
import com.willwinder.ugs.designer.entities.controls.VertexControlSelector;
import com.willwinder.ugs.designer.entities.controls.ZoomControl;
import com.willwinder.ugs.designer.logic.ControllerFactory;
import com.willwinder.universalgcodesender.fx.component.visualizer.DragHandler;
import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Extrude;
import eu.mihosoft.vrl.v3d.Vector3d;
import javafx.scene.DepthTest;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Affine;
import org.fxyz3d.geometry.Point3D;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.util.GeometryFixer;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class EntityShapeFactory {
    private static final Logger LOGGER = Logger.getLogger(EntityShapeFactory.class.getName());
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    private EntityShapeFactory() {
    }

    /**
     * Fill and outline meshes for an entity. Either can be null (an open path has no
     * fill, a degenerate path may have no border).
     */
    public record EntityNodes(MeshView fill, MeshView border) {
    }

    public static EntityNodes create(Entity entity) {
        if (entity == null) {
            return null;
        }
        Shape shape = entity.getShape();
        MeshView fill = createFillMesh(shape, Color.WHITE);
        MeshView border = createBorderMesh(shape, Color.DODGERBLUE);
        return new EntityNodes(fill, border);
    }

    /**
     * Builds the (expensive) extruded fill mesh from a shape. Callers that move/resize/rotate an
     * entity should build this once from the entity's <em>relative</em> shape and reposition it
     * with {@link #toFxTransform(AffineTransform)} instead of rebuilding it, since the relative
     * geometry is unchanged by those operations.
     */
    public static MeshView createFill(Shape shape, Color color) {
        return createFillMesh(shape, color);
    }

    /**
     * Builds the (cheap) outline tube mesh from a shape.
     */
    public static MeshView createBorder(Shape shape, Color color) {
        return createBorderMesh(shape, color);
    }

    /**
     * Converts an AWT {@link AffineTransform} to a JavaFX 2D {@link Affine}, leaving the extrusion
     * depth (z) untouched. Lets a cached fill mesh, built in the entity's relative coordinates, be
     * placed at the entity's current position/rotation/scale without rebuilding it.
     */
    public static Affine toFxTransform(AffineTransform t) {
        return new Affine(
                t.getScaleX(), t.getShearX(), t.getTranslateX(),
                t.getShearY(), t.getScaleY(), t.getTranslateY());
    }

    static Node createControlNode(Control control, DragHandler dragHandler) {
        Graphics2DAdapter graphics2DAdapter = new Graphics2DAdapter();
        control.render(graphics2DAdapter, ControllerFactory.getController().getDrawing());

        // Many controls short-circuit render() based on tool/selection state but still
        // expose a stale shape via getShape(). Only build a node when the control
        // actually drew something, otherwise the handles linger after clearSelection.
        if (!graphics2DAdapter.drewAnything()) return null;

        Shape shape = control.getShape();
        if (shape == null) return null;
        Color color = getColor(control);
        MeshView node = createFillMesh(shape, color);
        if (node == null) return null;
        node.setDepthTest(DepthTest.DISABLE);
        node.setUserData(dragHandler);
        addHoverFeedback(node, color);
        return node;
    }

    /**
     * Lightens a handle's colour while the pointer is over it, giving the user feedback that it
     * can be grabbed. Transparent controls (selection rectangle, model highlight, creation and
     * vertex overlays) have nothing visible to recolour, so they are left untouched.
     */
    private static void addHoverFeedback(MeshView node, Color color) {
        if (color == null || color.getOpacity() == 0 || !(node.getMaterial() instanceof PhongMaterial material)) {
            return;
        }

        Color hoverColor = color.interpolate(Color.WHITE, 0.6);
        node.setOnMouseEntered(event -> material.setDiffuseColor(hoverColor));
        node.setOnMouseExited(event -> material.setDiffuseColor(color));
    }

    /**
     * Builds a thin outline mesh for a live drawing preview (rubber band) from a raw shape.
     * Works for open paths (e.g. a line) as well as closed shapes. Returns null for a null shape.
     */
    public static MeshView createPreviewBorder(Shape shape) {
        if (shape == null) {
            return null;
        }
        MeshView border = createBorderMesh(shape, Color.DODGERBLUE);
        if (border != null) {
            border.setDepthTest(DepthTest.DISABLE);
        }
        return border;
    }

    private static Color getColor(Control control) {
        if (control instanceof ResizeControl) {
            return Color.DODGERBLUE;
        } else if (control instanceof RotationControl) {
            return Color.DODGERBLUE;
        } else if (control instanceof MoveControl) {
            return Color.BLUE;
        } else if (control instanceof SelectionControl) {
            return Color.TRANSPARENT;
        } else if (control instanceof HighlightModelControl) {
            return Color.TRANSPARENT;
        } else if (control instanceof CreatePointControl ||
                control instanceof CreateEllipseControl ||
                control instanceof CreateLineControl ||
                control instanceof CreateOffsetControl ||
                control instanceof CreateRectangleControl ||
                control instanceof CreateTextControl ||
                control instanceof EditTextControl ||
                control instanceof ZoomControl) {
            return Color.TRANSPARENT;
        } else if (control instanceof VertexControlSelector || control instanceof VertexControl) {
            return Color.TRANSPARENT;
        } else {
            return Color.RED;
        }
    }

    private static MeshView createFillMesh(Shape shape, Color color) {
        CSG csg = shapeToCSG(shape, 0.1);
        if (csg == null) return null;
        MeshView fill = csg.getMesh();
        fill.setMaterial(new PhongMaterial(color));
        fill.setCullFace(CullFace.NONE);
        return fill;
    }

    public static CSG shapeToCSG(Shape shape, double depth) {
        List<List<Vector3d>> subPaths = collectClosedSubPaths(shape);
        if (subPaths.isEmpty()) return null;
        
        Geometry filled = null;
        for (List<Vector3d> ring : subPaths) {
            Geometry polygon = toValidPolygon(ring);
            if (polygon == null || polygon.isEmpty()) continue;
            filled = (filled == null) ? polygon : filled.symDifference(polygon);
        }
        if (filled == null || filled.isEmpty()) return null;

        CSG result = null;
        for (int i = 0; i < filled.getNumGeometries(); i++) {
            if (!(filled.getGeometryN(i) instanceof org.locationtech.jts.geom.Polygon polygon)) continue;
            CSG solid = extrudePolygon(polygon, depth);
            if (solid == null) continue;
            result = (result == null) ? solid : result.dumbUnion(solid);
        }

        return result;
    }

    /**
     * Extrudes one JTS polygon (its exterior shell minus any interior holes) into a solid.
     */
    private static CSG extrudePolygon(org.locationtech.jts.geom.Polygon polygon, double depth) {
        List<Vector3d> shell = toRing(polygon.getExteriorRing());
        if (shell.size() < 3) return null;

        CSG solid;
        try {
            solid = Extrude.points(new Vector3d(0, 0, depth), shell);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "CSG extrusion failed for outer boundary", e);
            return null;
        }

        for (int h = 0; h < polygon.getNumInteriorRing(); h++) {
            List<Vector3d> hole = toRing(polygon.getInteriorRingN(h));
            if (hole.size() < 3) continue;
            try {
                solid = solid.difference(Extrude.points(new Vector3d(0, 0, depth), hole));
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "CSG hole subtraction failed", e);
            }
        }

        return solid;
    }

    /**
     * Builds a JTS polygon from a ring, repairing it into valid geometry when the raw outline
     * self-intersects. {@link GeometryFixer} may return a {@code MultiPolygon} for a figure-eight
     * style outline, which the caller iterates over.
     */
    private static Geometry toValidPolygon(List<Vector3d> ring) {
        Coordinate[] coords = new Coordinate[ring.size() + 1];
        for (int i = 0; i < ring.size(); i++) {
            coords[i] = new Coordinate(ring.get(i).x, ring.get(i).y);
        }
        coords[ring.size()] = new Coordinate(ring.get(0).x, ring.get(0).y);

        Geometry polygon = GEOMETRY_FACTORY.createPolygon(coords);
        return polygon.isValid() ? polygon : GeometryFixer.fix(polygon);
    }

    /**
     * Converts a JTS ring back to vertices for {@link Extrude#points}. JTS repeats the first
     * coordinate as the last to close the ring; Extrude.points closes the ring itself, so the
     * trailing duplicate is dropped to avoid a zero-length edge.
     */
    private static List<Vector3d> toRing(LineString ring) {
        Coordinate[] coords = ring.getCoordinates();
        List<Vector3d> points = new ArrayList<>(Math.max(0, coords.length - 1));
        for (int i = 0; i < coords.length - 1; i++) {
            points.add(new Vector3d(coords[i].x, coords[i].y, 0));
        }
        return points;
    }

    private static List<List<Vector3d>> collectClosedSubPaths(Shape shape) {
        List<List<Vector3d>> result = new ArrayList<>();
        List<Vector3d> current = new ArrayList<>();
        PathIterator it = shape.getPathIterator(null, 0.1);
        double[] coords = new double[6];

        while (!it.isDone()) {
            int type = it.currentSegment(coords);
            switch (type) {
                case PathIterator.SEG_MOVETO -> {
                    current = new ArrayList<>();
                    current.add(new Vector3d(coords[0], coords[1], 0));
                }
                case PathIterator.SEG_LINETO -> current.add(new Vector3d(coords[0], coords[1], 0));
                case PathIterator.SEG_CLOSE -> {
                    List<Vector3d> ring = sanitizeRing(current);
                    if (ring.size() >= 3 && Math.abs(signedArea(ring)) > 1e-6) {
                        result.add(ring);
                    }
                    current = new ArrayList<>();
                }
            }
            it.next();
        }
        return result;
    }

    /**
     * Removes zero-length edges from a flattened ring. {@link PathIterator} flattening routinely
     * emits coincident consecutive vertices — most notably a final {@code SEG_LINETO} that lands
     * back on the {@code SEG_MOVETO} point right before {@code SEG_CLOSE}. The ear-clipping
     * triangulator inside {@code Extrude.points} cannot find a convex corner when the ring contains
     * such duplicates and throws {@code IllegalStateException: Unable to find a convex corner},
     * so we drop them (including the wrap-around first/last duplicate) before extruding.
     */
    private static List<Vector3d> sanitizeRing(List<Vector3d> pts) {
        List<Vector3d> result = new ArrayList<>(pts.size());
        for (Vector3d p : pts) {
            if (result.isEmpty() || !coincident(result.get(result.size() - 1), p)) {
                result.add(p);
            }
        }
        while (result.size() >= 2 && coincident(result.get(0), result.get(result.size() - 1))) {
            result.remove(result.size() - 1);
        }
        return result;
    }

    private static boolean coincident(Vector3d a, Vector3d b) {
        return Math.abs(a.x - b.x) < 1e-6 && Math.abs(a.y - b.y) < 1e-6;
    }

    private static double signedArea(List<Vector3d> pts) {
        double area = 0;
        int n = pts.size();
        for (int i = 0; i < n; i++) {
            Vector3d a = pts.get(i);
            Vector3d b = pts.get((i + 1) % n);
            area += a.x * b.y - b.x * a.y;
        }
        return area * 0.5;
    }

    private static MeshView createBorderMesh(Shape shape, Color color) {
        float r = 0.2f;
        TriangleMesh mesh = new TriangleMesh();
        mesh.getTexCoords().addAll(0, 0);

        PathIterator it = shape.getPathIterator(null, 0.1);
        double[] coords = new double[6];
        double prevX = 0, prevY = 0, moveX = 0, moveY = 0;

        while (!it.isDone()) {
            int type = it.currentSegment(coords);
            switch (type) {
                case PathIterator.SEG_MOVETO -> {
                    moveX = prevX = coords[0];
                    moveY = prevY = coords[1];
                }
                case PathIterator.SEG_LINETO -> {
                    appendTube(mesh, prevX, prevY, coords[0], coords[1], r);
                    prevX = coords[0];
                    prevY = coords[1];
                }
                case PathIterator.SEG_CLOSE -> {
                    appendTube(mesh, prevX, prevY, moveX, moveY, r);
                    prevX = moveX;
                    prevY = moveY;
                }
            }
            it.next();
        }

        MeshView view = new MeshView(mesh);
        view.setMaterial(new PhongMaterial(color));
        view.setCullFace(CullFace.NONE);
        return view;
    }

    private static void appendTube(TriangleMesh mesh, double x1, double y1, double x2, double y2, float r) {
        Point3D p1 = new Point3D((float) x1, (float) y1, 0);
        Point3D p2 = new Point3D((float) x2, (float) y2, 0);
        Point3D dir = p2.substract(p1).normalize();

        Point3D ref = Math.abs(dir.getZ()) > 0.9 ? new Point3D(1, 0, 0) : new Point3D(0, 0, 1);
        Point3D normal = dir.crossProduct(ref).normalize();
        Point3D binormal = dir.crossProduct(normal).normalize();

        Point3D[] offsets = {normal.multiply(r), normal.multiply(-0.5f * r).add(binormal.multiply((float) (Math.sqrt(3) * 0.5 * r))), normal.multiply(-0.5f * r).substract(binormal.multiply((float) (Math.sqrt(3) * 0.5 * r)))};

        int base = mesh.getPoints().size() / 3;
        for (Point3D o : offsets) {
            Point3D a = p1.add(o);
            mesh.getPoints().addAll(a.getX(), a.getY(), a.getZ());
        }
        for (Point3D o : offsets) {
            Point3D b = p2.add(o);
            mesh.getPoints().addAll(b.getX(), b.getY(), b.getZ());
        }

        for (int j = 0; j < 3; j++) {
            int a0 = base + j, a1 = base + (j + 1) % 3;
            int b0 = base + j + 3, b1 = base + (j + 1) % 3 + 3;
            mesh.getFaces().addAll(a0, 0, b0, 0, a1, 0, a1, 0, b0, 0, b1, 0);
        }
    }

}
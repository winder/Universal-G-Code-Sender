package com.willwinder.universalgcodesender.fx.component.visualizer.designer;

import com.willwinder.ugs.designer.entities.entities.Entity;
import com.willwinder.ugs.designer.entities.entities.controls.Control;
import com.willwinder.ugs.designer.logic.ControllerFactory;
import com.willwinder.universalgcodesender.fx.component.visualizer.DragHandler;
import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Extrude;
import eu.mihosoft.vrl.v3d.Vector3d;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import org.fxyz3d.geometry.Point3D;

import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class EntityShapeFactory {
    private static final Logger LOGGER = Logger.getLogger(EntityShapeFactory.class.getName());

    private EntityShapeFactory() {
    }

    public static Node create(Entity entity) {
        if (entity == null) {
            return null;
        }
        if (entity instanceof com.willwinder.ugs.designer.entities.entities.cuttable.Path path) {
            return createPolylineNode(path);

        }

        return null;
    }

    static Node createControlNode(Control control, DragHandler dragHandler) {
        Graphics2DAdapter graphics2DAdapter = new Graphics2DAdapter();
        control.render(graphics2DAdapter, ControllerFactory.getController().getDrawing());

        Shape shape = control.getShape();
        if (shape == null) return null;
        Rectangle2D bounds = shape.getBounds2D();
        if (bounds.getWidth() == 0 || bounds.getHeight() == 0) return null;

        MeshView fill = createFilledMesh(bounds, Color.DODGERBLUE);
        Group node = new Group(fill);
        node.setUserData(dragHandler);
        return node;
    }

    private static MeshView createFilledMesh(Rectangle2D bounds, Color color) {
        float x = (float) bounds.getX();
        float y = (float) bounds.getY();
        float w = (float) bounds.getWidth();
        float h = (float) bounds.getHeight();
        float z = -0.2f;

        TriangleMesh mesh = new TriangleMesh();
        mesh.getTexCoords().addAll(0, 0);
        mesh.getPoints().addAll(
                x,     y,     z,
                x + w, y,     z,
                x + w, y + h, z,
                x,     y + h, z
        );
        mesh.getFaces().addAll(
                0, 0, 1, 0, 2, 0,
                0, 0, 2, 0, 3, 0,
                0, 0, 2, 0, 1, 0,
                0, 0, 3, 0, 2, 0
        );

        MeshView view = new MeshView(mesh);
        view.setMaterial(new PhongMaterial(color));
        view.setCullFace(CullFace.NONE);
        return view;
    }

    private static Node createPolylineNode(com.willwinder.ugs.designer.entities.entities.cuttable.Path path) {
        Shape shape = path.getShape();
        MeshView border = createBorderMesh(shape, Color.DODGERBLUE);
        CSG csg = shapeToCSG(shape, 0.5);
        if (csg == null) {
            return new Group(border);
        }
        MeshView fill = csg.getMesh();
        fill.setMaterial(new PhongMaterial(Color.WHITE));
        fill.setCullFace(CullFace.NONE);
        return new Group(fill, border);
    }

    private static CSG shapeToCSG(Shape shape, double depth) {
        List<List<Vector3d>> subPaths = collectClosedSubPaths(shape);
        if (subPaths.isEmpty()) return null;

        CSG result = null;
        for (List<Vector3d> outer : subPaths) {
            if (signedArea(outer) >= 0) continue; // holes handled below

            CSG solid;
            try {
                solid = Extrude.points(new Vector3d(0, 0, depth), outer);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "CSG extrusion failed for outer boundary", e);
                continue;
            }

            java.awt.geom.Path2D outerPath = toPath2D(outer);
            for (List<Vector3d> hole : subPaths) {
                if (signedArea(hole) <= 0) continue; // only holes (negative area)
                Vector3d c = centroid(hole);
                if (!outerPath.contains(c.x, c.y)) continue;
                try {
                    solid = solid.difference(Extrude.points(new Vector3d(0, 0, depth), hole));
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "CSG hole subtraction failed", e);
                }
            }

            result = result == null ? solid : result.dumbUnion(solid);
        }

        return result;
    }

    private static List<List<Vector3d>> collectClosedSubPaths(Shape shape) {
        List<List<Vector3d>> result = new ArrayList<>();
        List<Vector3d> current = new ArrayList<>();
        PathIterator it = shape.getPathIterator(null, 0.5);
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
                    if (current.size() >= 3) result.add(new ArrayList<>(current));
                    current = new ArrayList<>();
                }
            }
            it.next();
        }
        return result;
    }

    /** Shoelace signed area. Positive = clockwise in AWT (Y-down) = outer boundary. */
    private static double signedArea(List<Vector3d> pts) {
        double area = 0;
        int n = pts.size();
        for (int i = 0; i < n; i++) {
            Vector3d a = pts.get(i), b = pts.get((i + 1) % n);
            area += a.x * b.y - b.x * a.y;
        }
        return area / 2.0;
    }

    private static Vector3d centroid(List<Vector3d> pts) {
        double cx = 0, cy = 0;
        for (Vector3d p : pts) { cx += p.x; cy += p.y; }
        return new Vector3d(cx / pts.size(), cy / pts.size(), 0);
    }

    private static java.awt.geom.Path2D toPath2D(List<Vector3d> pts) {
        java.awt.geom.Path2D.Double path = new java.awt.geom.Path2D.Double();
        path.moveTo(pts.get(0).x, pts.get(0).y);
        for (int i = 1; i < pts.size(); i++) path.lineTo(pts.get(i).x, pts.get(i).y);
        path.closePath();
        return path;
    }

    private static MeshView createBorderMesh(Shape shape, Color color) {
        float r = 0.1f;
        TriangleMesh mesh = new TriangleMesh();
        mesh.getTexCoords().addAll(0, 0);

        PathIterator it = shape.getPathIterator(null, 0.5);
        double[] coords = new double[6];
        double prevX = 0, prevY = 0, moveX = 0, moveY = 0;

        while (!it.isDone()) {
            int type = it.currentSegment(coords);
            switch (type) {
                case PathIterator.SEG_MOVETO -> { moveX = prevX = coords[0]; moveY = prevY = coords[1]; }
                case PathIterator.SEG_LINETO -> {
                    appendTube(mesh, prevX, prevY, coords[0], coords[1], r);
                    prevX = coords[0]; prevY = coords[1];
                }
                case PathIterator.SEG_CLOSE -> {
                    appendTube(mesh, prevX, prevY, moveX, moveY, r);
                    prevX = moveX; prevY = moveY;
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

        Point3D[] offsets = {
            normal.multiply(r),
            normal.multiply(-0.5f * r).add(binormal.multiply((float) (Math.sqrt(3) * 0.5 * r))),
            normal.multiply(-0.5f * r).substract(binormal.multiply((float) (Math.sqrt(3) * 0.5 * r)))
        };

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
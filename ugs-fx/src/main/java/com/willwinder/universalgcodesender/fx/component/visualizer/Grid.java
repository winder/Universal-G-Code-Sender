package com.willwinder.universalgcodesender.fx.component.visualizer;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.gcode.GcodeStats;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.FileState;
import com.willwinder.universalgcodesender.model.events.FileStateEvent;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.Line;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import org.fxyz3d.geometry.Point3D;

import java.util.ArrayList;
import java.util.List;

public class Grid extends Group {


    private final MeshView meshView;
    private final BackendAPI backend;

    private final DoubleProperty minX = new SimpleDoubleProperty(0);
    private final DoubleProperty minY = new SimpleDoubleProperty(0);
    private final DoubleProperty maxX = new SimpleDoubleProperty(100);
    private final DoubleProperty maxY = new SimpleDoubleProperty(100);
    double  gridSize = 10;

    public Grid() {
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        backend.addUGSEventListener(this::onEvent);

        PhongMaterial material = new PhongMaterial(Color.DARKGRAY);
        meshView = new MeshView();
        meshView.setMaterial(material);
        meshView.setCullFace(CullFace.NONE);
        regenerateMesh();
        getChildren().add(meshView);
    }

    private void regenerateMesh() {
        List<Line> lineSegments = new ArrayList<>();

        double x1 = Math.round((minX.get() - gridSize) / gridSize) * gridSize;
        double y1 = Math.round((minY.get() - gridSize) / gridSize) * gridSize;
        double x2 = Math.round((maxX.get() + gridSize) / gridSize) * gridSize;
        double y2 = Math.round((maxY.get() + gridSize) / gridSize) * gridSize;

        for (double x = x1; x <= x2; x += gridSize) {
            for (double y = y1; y <= y2; y += gridSize) {
                lineSegments.add(new Line(x1, y, x2, y));
                lineSegments.add(new Line(x, y1, x, y2));
            }

        }
        TriangleMesh mesh = pointsToMesh(lineSegments);
        meshView.setMesh(mesh);
    }

    private void onEvent(UGSEvent ugsEvent) {
        if (ugsEvent instanceof FileStateEvent fileStateEvent) {
            if (fileStateEvent.getFileState() == FileState.FILE_LOADED) {
                GcodeStats gcodeStats = backend.getGcodeStats();
                minX.set(gcodeStats.getMin().getX());
                minY.set(gcodeStats.getMin().getY());
                maxX.set(gcodeStats.getMax().getX());
                maxY.set(gcodeStats.getMax().getY());
                regenerateMesh();
            }
        }
    }


    private TriangleMesh pointsToMesh(List<Line> lineSegments) {
        TriangleMesh mesh = new TriangleMesh();
        float width = 0.06f;

        for (int i = 0; i < lineSegments.size(); i++) {
            Line lineSegment = lineSegments.get(i);
            Point3D p1 = new Point3D(lineSegment.getStartX(), lineSegment.getStartY(), 0);
            Point3D p2 = new Point3D(lineSegment.getEndX(), lineSegment.getEndY(), 0);

            // Compute direction and a perpendicular vector for width
            Point3D dir = p2.substract(p1).normalize();
            Point3D perp = dir.crossProduct(new Point3D(0, 0, 1)).normalize().multiply(width);
            if (perp.magnitude() == 0) { // If dir is parallel to Z, use X axis
                perp = new Point3D(width, 0, width);
            }

            // Add 4 points for the rectangle
            int baseIndex = mesh.getPoints().size() / 3;

            Point3D p1a = p1.add(perp);
            Point3D p1b = p1.substract(perp);
            Point3D p2a = p2.add(perp);
            Point3D p2b = p2.substract(perp);

            // Add texCoords for each segment
            mesh.getTexCoords().addAll(0f, 0, 0, 0);

            int texBase = (i % 2 == 0) ? 0 : 2;

            // Two triangles per segment (rectangle)
            mesh.getFaces().addAll(
                    baseIndex, texBase, baseIndex + 2, texBase, baseIndex + 1, texBase,
                    baseIndex + 2, texBase, baseIndex + 3, texBase, baseIndex + 1, texBase
            );

            mesh.getPoints().addAll(
                    p1a.getX(), p1a.getY(), p1a.getZ(),
                    p1b.getX(), p1b.getY(), p1b.getZ(),
                    p2a.getX(), p2a.getY(), p2a.getZ(),
                    p2b.getX(), p2b.getY(), p2b.getZ()
            );
        }
        return mesh;
    }
}

package com.willwinder.universalgcodesender.fx.visualizer;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.gcode.DefaultCommandCreator;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.model.events.FileState;
import com.willwinder.universalgcodesender.model.events.FileStateEvent;
import com.willwinder.universalgcodesender.utils.GcodeStreamReader;
import com.willwinder.universalgcodesender.utils.IGcodeStreamReader;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import com.willwinder.universalgcodesender.visualizer.GcodeViewParse;
import com.willwinder.universalgcodesender.visualizer.LineSegment;
import com.willwinder.universalgcodesender.visualizer.VisualizerUtils;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import org.fxyz3d.geometry.Point3D;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class GcodeModel extends Group {
    public static final Point3D ZERO = new Point3D(0, 0, 0);
    public static final double ARC_SEGMENT_LENGTH = 0.8;
    private final GcodeViewParse gcvp;
    private final MeshView meshView;

    private List<LineSegment> loadModel(GcodeViewParse gcvp, String gcodeFile) throws IOException, GcodeParserException {
        try (IGcodeStreamReader gsr = new GcodeStreamReader(new File(gcodeFile), new DefaultCommandCreator())) {
            return gcvp.toObjFromReader(gsr, ARC_SEGMENT_LENGTH);
        } catch (GcodeStreamReader.NotGcodeStreamFile e) {
            List<String> linesInFile;
            linesInFile = VisualizerUtils.readFiletoArrayList(gcodeFile);
            return gcvp.toObjRedux(linesInFile, ARC_SEGMENT_LENGTH);
        }
    }

    private Image createColorStripeTexture() {
        int width = 2;
        int height = 2;
        WritableImage image = new WritableImage(width, height);
        PixelWriter writer = image.getPixelWriter();

        writer.setColor(0, 0, Color.RED);
        writer.setColor(1, 0, Color.RED);
        writer.setColor(0, 1, Color.GREEN);
        writer.setColor(1, 1, Color.GREEN);

        return image;
    }

    public GcodeModel() {
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseMap(createColorStripeTexture());

        meshView = new MeshView();
        meshView.setMaterial(material);
        meshView.setCullFace(CullFace.NONE);

        getChildren().add(meshView);
        BackendAPI backendAPI = CentralLookup.getDefault().lookup(BackendAPI.class);
        gcvp = new GcodeViewParse();
        backendAPI.addUGSEventListener(event -> ThreadHelper.invokeLater(() -> {
            if (event instanceof FileStateEvent) {
                if (((FileStateEvent) event).getFileState() == FileState.FILE_LOADED) {

                    try {
                        List<LineSegment> lineSegments = loadModel(gcvp, backendAPI.getGcodeFile().getAbsolutePath());
                        TriangleMesh mesh = pointsToMesh(lineSegments);
                        meshView.setMesh(mesh);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        }));


    }

    private TriangleMesh pointsToMesh(List<LineSegment> lineSegments) {
        TriangleMesh mesh = new TriangleMesh();
        float width = 0.05f; // Thin width for visual line approximation

        for (int i = 0; i < lineSegments.size(); i++) {
            LineSegment lineSegment = lineSegments.get(i);
            Point3D p1 = toPoint(lineSegment.getStart().getPositionIn(UnitUtils.Units.MM));
            Point3D p2 = toPoint(lineSegment.getEnd().getPositionIn(UnitUtils.Units.MM));

            // Compute direction and a perpendicular vector for width
            Point3D dir = p2.substract(p1).normalize();
            Point3D perp = dir.crossProduct(ZERO.add(0, 0, 1)).normalize().multiply(width);
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
            float v = (lineSegment.isArc()) ? 0f : 1f; // red for first, green for second
            mesh.getTexCoords().addAll(0f, v, 0, v);

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

    private Point3D toPoint(Position pos) {
        return new Point3D(pos.getX(), pos.getY(), pos.getZ());
    }
}

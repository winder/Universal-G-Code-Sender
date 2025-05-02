package com.willwinder.universalgcodesender.fx.component.visualizer;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.gcode.DefaultCommandCreator;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.model.events.CommandEvent;
import com.willwinder.universalgcodesender.model.events.FileState;
import com.willwinder.universalgcodesender.model.events.FileStateEvent;
import com.willwinder.universalgcodesender.utils.GcodeStreamReader;
import com.willwinder.universalgcodesender.utils.IGcodeStreamReader;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import com.willwinder.universalgcodesender.visualizer.GcodeViewParse;
import com.willwinder.universalgcodesender.visualizer.LineSegment;
import com.willwinder.universalgcodesender.visualizer.VisualizerUtils;
import javafx.scene.Group;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GcodeModel extends Group {
    public static final Point3D ZERO = new Point3D(0, 0, 0);
    public static final double ARC_SEGMENT_LENGTH = 0.8;
    private final GcodeViewParse gcvp;
    private final MeshView meshView;
    private final BackendAPI backendAPI;
    private Map<Integer, List<Integer>> lineToTextureMap = new HashMap<>();

    WritableImage texture = new WritableImage(1, 2);

    private List<LineSegment> loadModel(GcodeViewParse gcvp, String gcodeFile) throws IOException, GcodeParserException {
        try (IGcodeStreamReader gsr = new GcodeStreamReader(new File(gcodeFile), new DefaultCommandCreator())) {
            return gcvp.toObjFromReader(gsr, ARC_SEGMENT_LENGTH);
        } catch (GcodeStreamReader.NotGcodeStreamFile e) {
            List<String> linesInFile;
            linesInFile = VisualizerUtils.readFiletoArrayList(gcodeFile);
            return gcvp.toObjRedux(linesInFile, ARC_SEGMENT_LENGTH);
        }
    }

    public GcodeModel() {

        // Create a 1x4 texture map for G0, G1, G2, G3
        WritableImage texture = new WritableImage(1, 5);
        texture.getPixelWriter().setColor(0, 0, Color.YELLOW);    // G0
        texture.getPixelWriter().setColor(0, 1, Color.BLUE);   // G1
        texture.getPixelWriter().setColor(0, 2, Color.RED);     // G2/G3
        texture.getPixelWriter().setColor(0, 3, Color.GREEN);     // Z only
        texture.getPixelWriter().setColor(0, 4, Color.GRAY);     // Z only




        meshView = new MeshView();
        meshView.setCullFace(CullFace.NONE);

        getChildren().add(meshView);
        backendAPI = CentralLookup.getDefault().lookup(BackendAPI.class);
        gcvp = new GcodeViewParse();
        backendAPI.addUGSEventListener(this::onEvent);
    }

    private void onEvent(UGSEvent event) {
        if (event instanceof FileStateEvent) {
            if (((FileStateEvent) event).getFileState() == FileState.FILE_LOADED) {
                ThreadHelper.invokeLater(() -> {
                    try {
                        List<LineSegment> lineSegments = loadModel(gcvp, backendAPI.getGcodeFile().getAbsolutePath());
                        TriangleMesh mesh = pointsToMesh(lineSegments);
                        meshView.setMesh(mesh);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        } else if(event instanceof CommandEvent commandEvent) {
            if (commandEvent.getCommand().isDone()) {
                PixelWriter pixelWriter = texture.getPixelWriter();
                lineToTextureMap.getOrDefault(commandEvent.getCommand().getCommandNumber(), new ArrayList<>()).forEach(
                       texIndex -> pixelWriter.setColor(0, texIndex, Color.GRAY)
               );
            }
        }
    }

    private TriangleMesh pointsToMesh(List<LineSegment> lineSegments) {
        TriangleMesh mesh = new TriangleMesh();
        float width = 0.04f; // Thin width for visual line approximation
        lineToTextureMap = new HashMap<>();

        texture = new WritableImage(1, lineSegments.size());
        PixelWriter writer = texture.getPixelWriter();
        for (int i = 0; i < lineSegments.size(); i++) {
            writer.setColor(0, i, Color.GREEN); // Default: unprocessed
        }
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseMap(texture);
        meshView.setMaterial(material);

        for (int i = 0; i < lineSegments.size(); i++) {
            float v = (i + 0.5f) / lineSegments.size(); // Center of texel
            mesh.getTexCoords().addAll(0f, v);          // one per segment
        }


        for (int i = 0; i < lineSegments.size(); i++) {
            LineSegment lineSegment = lineSegments.get(i);
            List lineSegmentTextureIndexes = lineToTextureMap.getOrDefault(lineSegment.getLineNumber(), new ArrayList<>());
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

            int texIndex = i;
            texture.getPixelWriter().setColor(0, texIndex, getColor(lineSegment));
            lineSegmentTextureIndexes.add(texIndex);
            lineToTextureMap.put(lineSegment.getLineNumber(), lineSegmentTextureIndexes);

            // Two triangles per segment (rectangle)
            mesh.getFaces().addAll(
                    baseIndex, texIndex, baseIndex + 2, texIndex, baseIndex + 1, texIndex,
                    baseIndex + 2, texIndex, baseIndex + 3, texIndex, baseIndex + 1, texIndex
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

    private Color getColor(LineSegment lineSegment) {
        if (lineSegment.isArc()) {
            return Color.RED;
        } else if( lineSegment.isFastTraverse() ){
            return Color.YELLOW;
        } else if(lineSegment.isZMovement()) {
            return Color.GREEN;
        } else {
            return Color.BLUE;
        }
    }

    private Point3D toPoint(Position pos) {
        return new Point3D(pos.getX(), pos.getY(), pos.getZ());
    }
}

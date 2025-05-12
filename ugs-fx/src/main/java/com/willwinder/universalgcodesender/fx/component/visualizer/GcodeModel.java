package com.willwinder.universalgcodesender.fx.component.visualizer;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import static com.willwinder.universalgcodesender.fx.helper.Colors.blend;
import static com.willwinder.universalgcodesender.fx.helper.Colors.interpolate;
import com.willwinder.universalgcodesender.gcode.DefaultCommandCreator;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.model.events.CommandEvent;
import com.willwinder.universalgcodesender.model.events.FileState;
import com.willwinder.universalgcodesender.model.events.FileStateEvent;
import com.willwinder.universalgcodesender.model.events.SettingChangedEvent;
import com.willwinder.universalgcodesender.model.events.StreamEvent;
import com.willwinder.universalgcodesender.model.events.StreamEventType;
import com.willwinder.universalgcodesender.utils.GcodeStreamReader;
import com.willwinder.universalgcodesender.utils.IGcodeStreamReader;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import com.willwinder.universalgcodesender.visualizer.GcodeViewParse;
import com.willwinder.universalgcodesender.visualizer.LineSegment;
import com.willwinder.universalgcodesender.visualizer.VisualizerUtils;
import javafx.scene.Group;
import javafx.scene.paint.Color;
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
import java.util.logging.Level;
import java.util.logging.Logger;

public class GcodeModel extends Group {
    private static final Logger LOGGER = Logger.getLogger(GcodeModel.class.getName());
    public static final Point3D ZERO = new Point3D(0, 0, 0);
    public static final double ARC_SEGMENT_LENGTH = 0.8;
    private final GcodeViewParse gcvp;
    private final MeshView meshView;
    private final BackendAPI backendAPI;

    private GcodeModelMaterial material = new GcodeModelMaterial(0);
    private Map<Integer, List<Integer>> lineToTextureMap = new HashMap<>();

    private Color rapidColor;
    private Color arcColor;
    private Color plungeColor;
    private Color feedMinColor;
    private Color feedMaxColor;
    private Color spindleMinColor;
    private Color spindleMaxColor;
    private Color completedColor;

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
        meshView = new MeshView();
        meshView.setCullFace(CullFace.NONE);

        getChildren().add(meshView);
        backendAPI = CentralLookup.getDefault().lookup(BackendAPI.class);
        gcvp = new GcodeViewParse();
        backendAPI.addUGSEventListener(this::onEvent);

        addSettingListeners();
    }

    private void addSettingListeners() {
        VisualizerSettings.getInstance().colorRapidProperty().map(Color::web).addListener((s, o, n) -> rapidColor = n);
        rapidColor = VisualizerSettings.getInstance().colorRapidProperty().map(Color::web).getValue();

        VisualizerSettings.getInstance().colorFeedMinProperty().map(Color::web).addListener((s, o, n) -> feedMinColor = n);
        feedMinColor = VisualizerSettings.getInstance().colorFeedMinProperty().map(Color::web).getValue();

        VisualizerSettings.getInstance().colorFeedMaxProperty().map(Color::web).addListener((s, o, n) -> feedMaxColor = n);
        feedMaxColor = VisualizerSettings.getInstance().colorFeedMaxProperty().map(Color::web).getValue();

        VisualizerSettings.getInstance().colorArcProperty().map(Color::web).addListener((s, o, n) -> arcColor = n);
        arcColor = VisualizerSettings.getInstance().colorArcProperty().map(Color::web).getValue();

        VisualizerSettings.getInstance().colorPlungeProperty().map(Color::web).addListener((s, o, n) -> plungeColor = n);
        plungeColor = VisualizerSettings.getInstance().colorPlungeProperty().map(Color::web).getValue();

        VisualizerSettings.getInstance().colorSpindleMinProperty().map(Color::web).addListener((s, o, n) -> spindleMinColor = n);
        spindleMinColor = VisualizerSettings.getInstance().colorSpindleMinProperty().map(Color::web).getValue();

        VisualizerSettings.getInstance().colorSpindleMaxProperty().map(Color::web).addListener((s, o, n) -> spindleMaxColor = n);
        spindleMaxColor = VisualizerSettings.getInstance().colorSpindleMaxProperty().map(Color::web).getValue();

        VisualizerSettings.getInstance().colorCompletedProperty().map(Color::web).addListener((s, o, n) -> completedColor = n);
        completedColor = VisualizerSettings.getInstance().colorCompletedProperty().map(Color::web).getValue();
    }

    private void onEvent(UGSEvent event) {
        if (event instanceof FileStateEvent fileStateEvent) {
            if (fileStateEvent.getFileState() == FileState.FILE_LOADED) {
                ThreadHelper.invokeLater(() -> {
                    try {
                        List<LineSegment> lineSegments = loadModel(gcvp, backendAPI.getGcodeFile().getAbsolutePath());
                        TriangleMesh mesh = pointsToMesh(lineSegments);
                        meshView.setMesh(mesh);
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Could not load model", e);
                    }
                });
            }
        } else if (event instanceof StreamEvent streamEvent) {
            if (streamEvent.getType() == StreamEventType.STREAM_COMPLETE || streamEvent.getType() == StreamEventType.STREAM_CANCELED) {
                material.reset();
            }
        } else if (event instanceof CommandEvent commandEvent) {
            if (commandEvent.getCommand().isDone()) {
                lineToTextureMap.getOrDefault(commandEvent.getCommand().getCommandNumber(), new ArrayList<>()).forEach(
                        lineIndex -> material.updateLineColor(lineIndex, completedColor)
                );
            }
        } else if (event instanceof SettingChangedEvent) {
            addSettingListeners();
        }
    }

    private TriangleMesh pointsToMesh(List<LineSegment> lineSegments) {
        TriangleMesh mesh = new TriangleMesh();
        float width = 0.05f; // Thin width for visual line approximation
        lineToTextureMap = new HashMap<>();

        material = new GcodeModelMaterial(lineSegments.size());
        meshView.setMaterial(material);

        for (int i = 0; i < lineSegments.size(); i++) {
            float v = (i + 0.5f) / lineSegments.size(); // Center of texel
            mesh.getTexCoords().addAll(0f, v);          // one per segment
        }


        for (int i = 0; i < lineSegments.size(); i++) {
            LineSegment lineSegment = lineSegments.get(i);
            List<Integer> lineSegmentTextureIndexes = lineToTextureMap.getOrDefault(lineSegment.getLineNumber(), new ArrayList<>());
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

            material.setLineColor(i, getColor(lineSegment));
            lineSegmentTextureIndexes.add(i);
            lineToTextureMap.put(lineSegment.getLineNumber(), lineSegmentTextureIndexes);

            // Two triangles per segment (rectangle)
            mesh.getFaces().addAll(
                    baseIndex, i, baseIndex + 2, i, baseIndex + 1, i,
                    baseIndex + 2, i, baseIndex + 3, i, baseIndex + 1, i
            );

            mesh.getPoints().addAll(
                    p1a.getX(), p1a.getY(), p1a.getZ(),
                    p1b.getX(), p1b.getY(), p1b.getZ(),
                    p2a.getX(), p2a.getY(), p2a.getZ(),
                    p2b.getX(), p2b.getY(), p2b.getZ()
            );
        }

        material.reset();
        return mesh;
    }


    private Color getColor(LineSegment lineSegment) {
        if (lineSegment.isArc()) {
            return arcColor;
        } else if (lineSegment.isFastTraverse()) {
            return rapidColor;
        } else if (lineSegment.isZMovement()) {
            return plungeColor;
        } else {
            return getFeedColor(lineSegment.getFeedRate(), lineSegment.getSpindleSpeed());
        }
    }

    private Color getFeedColor(double feedRate, double spindleSpeed) {
        double currentSpindleSpeed = Math.max(spindleSpeed, 0.1);
        double currentFeedRate = Math.max(feedRate, 0.1);
        double maxFeedRate = gcvp.getMaxFeedRate();
        double maxSpindleSpeed = gcvp.getMaxSpindleSpeed();

        double feedRatePercent = currentFeedRate / maxFeedRate;

        Color feedColor = maxFeedRate < 0.01 ? feedMaxColor : interpolate(feedMinColor, feedMaxColor, feedRatePercent);

        double speedPercent = currentSpindleSpeed / maxSpindleSpeed;
        Color speedColor = maxSpindleSpeed < 0.1 ? spindleMaxColor : interpolate(spindleMinColor, spindleMaxColor, speedPercent);
        return blend(speedColor, feedColor);
    }

    private Point3D toPoint(Position pos) {
        return new Point3D(pos.getX(), pos.getY(), pos.getZ());
    }
}

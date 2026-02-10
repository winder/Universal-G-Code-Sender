/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.universalgcodesender.fx.component.visualizer;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import static com.willwinder.universalgcodesender.fx.helper.Colors.blend;
import static com.willwinder.universalgcodesender.fx.helper.Colors.interpolate;
import com.willwinder.universalgcodesender.fx.settings.VisualizerSettings;
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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GcodeModel extends Group {
    private static final Logger LOGGER = Logger.getLogger(GcodeModel.class.getName());
    public static final double ARC_SEGMENT_LENGTH = 0.8;
    private final GcodeViewParse gcvp;
    private final MeshView meshView;
    private final BackendAPI backendAPI;
    private final float lineWidth;

    private GcodeModelMaterial material = new GcodeModelMaterial(0);

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
            return gcvp.toObjFromReader(gsr, ARC_SEGMENT_LENGTH, 0);
        } catch (GcodeStreamReader.NotGcodeStreamFile e) {
            List<String> linesInFile;
            linesInFile = VisualizerUtils.readFiletoArrayList(gcodeFile);
            return gcvp.toObjRedux(linesInFile, ARC_SEGMENT_LENGTH, 0);
        }
    }

    public GcodeModel() {
        meshView = new MeshView();
        meshView.setCullFace(CullFace.NONE);

        getChildren().add(meshView);
        backendAPI = CentralLookup.getDefault().lookup(BackendAPI.class);
        gcvp = new GcodeViewParse();
        backendAPI.addUGSEventListener(this::onEvent);
        lineWidth = 0.03f;
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
                material.updateLineColor(commandEvent.getCommand().getCommandNumber(), completedColor);
            }
        } else if (event instanceof SettingChangedEvent) {
            addSettingListeners();
        }
    }

    private TriangleMesh pointsToMesh(List<LineSegment> lineSegments) {
        TriangleMesh mesh = new TriangleMesh();

        material = new GcodeModelMaterial(lineSegments.size());
        meshView.setMaterial(material);

        for (int i = 0; i < lineSegments.size(); i++) {
            float[] uv = material.getTextureUV(i);
            mesh.getTexCoords().addAll(uv[0], uv[1]);
        }

        float r = lineWidth;

        for (int i = 0; i < lineSegments.size(); i++) {
            LineSegment segment = lineSegments.get(i);

            Point3D p1 = toPoint(segment.getStart().getPositionIn(UnitUtils.Units.MM));
            Point3D p2 = toPoint(segment.getEnd().getPositionIn(UnitUtils.Units.MM));

            Point3D dir = p2.substract(p1).normalize();

            // Pick a stable reference vector
            Point3D ref = Math.abs(dir.getZ()) > 0.9
                    ? new Point3D(1, 0, 0)
                    : new Point3D(0, 0, 1);

            Point3D normal = dir.crossProduct(ref).normalize();
            Point3D binormal = dir.crossProduct(normal).normalize();

            // Triangle cross-section (120° apart)
            Point3D[] offsets = new Point3D[]{
                    normal.multiply(r),
                    normal.multiply(-0.5f * r).add(binormal.multiply((float) Math.sqrt(3) * 0.5f * r)),
                    normal.multiply(-0.5f * r).substract(binormal.multiply((float) Math.sqrt(3) * 0.5f * r))
            };

            int base = mesh.getPoints().size() / 3;

            // Add vertices
            for (Point3D o : offsets) {
                Point3D a = p1.add(o);
                mesh.getPoints().addAll(a.getX(), a.getY(), a.getZ());
            }

            for (Point3D o : offsets) {
                Point3D b = p2.add(o);
                mesh.getPoints().addAll(b.getX(), b.getY(), b.getZ());
            }

            material.setLineColor(i, getColor(segment));

            // Side faces (6 triangles)
            for (int j = 0; j < 3; j++) {
                int a0 = base + j;
                int a1 = base + (j + 1) % 3;
                int b0 = base + j + 3;
                int b1 = base + (j + 1) % 3 + 3;

                mesh.getFaces().addAll(
                        a0, i, b0, i, a1, i,
                        a1, i, b0, i, b1, i
                );
            }
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

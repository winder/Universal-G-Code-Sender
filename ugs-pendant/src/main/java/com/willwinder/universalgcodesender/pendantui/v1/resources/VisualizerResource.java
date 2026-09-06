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
package com.willwinder.universalgcodesender.pendantui.v1.resources;

import com.willwinder.universalgcodesender.gcode.DefaultCommandCreator;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.pendantui.v1.model.ToolpathPoint;
import com.willwinder.universalgcodesender.pendantui.v1.model.ToolpathSegment;
import com.willwinder.universalgcodesender.utils.GcodeStreamReader;
import com.willwinder.universalgcodesender.utils.IGcodeStreamReader;
import com.willwinder.universalgcodesender.visualizer.GcodeViewParse;
import com.willwinder.universalgcodesender.visualizer.LineSegment;
import com.willwinder.universalgcodesender.visualizer.VisualizerUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides toolpath geometry for the currently loaded gcode file, so a browser-based 3D
 * visualizer can render it without re-implementing gcode parsing in JavaScript. Reuses the
 * exact same parsing path as the desktop 3D view
 * (see ugs-platform-visualizer's GcodeModel.loadModel()).
 */
@Tag(name = "Visualizer", description = "Endpoints for retrieving toolpath geometry")
@Path("/visualizer")
public class VisualizerResource {

    private static final double ARC_TOLERANCE = 0.01;

    @Inject
    private BackendAPI backendAPI;

    @GET
    @Path("getToolpath")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get the toolpath line segments for the currently loaded gcode file")
    public List<ToolpathSegment> getToolpath() throws IOException, GcodeParserException {
        File gcodeFile = backendAPI.getGcodeFile();
        if (gcodeFile == null) {
            return Collections.emptyList();
        }

        List<LineSegment> lineSegments = parseToolpath(gcodeFile);
        return lineSegments.stream()
                .filter(VisualizerResource::hasFinitePositions)
                .map(VisualizerResource::toToolpathSegment)
                .collect(Collectors.toList());
    }

    private List<LineSegment> parseToolpath(File gcodeFile) throws IOException, GcodeParserException {
        GcodeViewParse gcvp = new GcodeViewParse();
        try (IGcodeStreamReader gsr = new GcodeStreamReader(gcodeFile, new DefaultCommandCreator())) {
            return gcvp.toObjFromReaderWithArcTolerance(gsr, ARC_TOLERANCE, 0);
        } catch (GcodeStreamReader.NotGcodeStreamFile e) {
            List<String> lines = VisualizerUtils.readFiletoArrayList(gcodeFile.getAbsolutePath());
            return gcvp.toObjReduxWithArcTolerance(lines, ARC_TOLERANCE, 0);
        }
    }

    /**
     * The first segment(s) of a file can run from an undefined starting position (before the
     * first move that actually sets X/Y/Z), which GcodeViewParse represents as NaN coordinates.
     * Jackson serializes a NaN double as the string "NaN", which would silently corrupt a
     * numeric consumer like a WebGL vertex buffer - so these segments are dropped instead.
     */
    private static boolean hasFinitePositions(LineSegment lineSegment) {
        return isFinite(lineSegment.getStart()) && isFinite(lineSegment.getEnd());
    }

    private static boolean isFinite(Position position) {
        return Double.isFinite(position.x) && Double.isFinite(position.y) && Double.isFinite(position.z);
    }

    private static ToolpathSegment toToolpathSegment(LineSegment lineSegment) {
        return new ToolpathSegment(
                new ToolpathPoint(lineSegment.getStart().x, lineSegment.getStart().y, lineSegment.getStart().z),
                new ToolpathPoint(lineSegment.getEnd().x, lineSegment.getEnd().y, lineSegment.getEnd().z),
                lineSegment.isFastTraverse(),
                lineSegment.isArc());
    }
}

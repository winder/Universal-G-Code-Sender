/*
    Copyright 2024 Will Winder

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
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.services.LookupService;
import com.willwinder.universalgcodesender.services.SendProgressService;
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
import java.util.ArrayList;
import java.util.List;

@Tag(name = "Visualizer", description = "Endpoints for G-code toolpath visualization")
@Path("/visualizer")
public class VisualizerResource {
    private static final double ARC_TOLERANCE = 0.1;

    @Inject
    private BackendAPI backendAPI;

    private final SendProgressService sendProgress = LookupService.lookup(SendProgressService.class);

    public static class ToolpathResponse {
        private boolean hasFile;
        private String fileName;
        private boolean truncated;
        private long totalSegments;
        private long completedRows;
        private double minX;
        private double maxX;
        private double minY;
        private double maxY;
        private double minZ;
        private double maxZ;
        /**
         * Compact segment tuples: [x1, y1, z1, x2, y2, z2, type, lineNumber]
         * type: 0 = rapid, 1 = linear, 2 = arc, 3 = plunge
         */
        private List<double[]> segments = new ArrayList<>();

        public boolean isHasFile() {
            return hasFile;
        }

        public void setHasFile(boolean hasFile) {
            this.hasFile = hasFile;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public boolean isTruncated() {
            return truncated;
        }

        public void setTruncated(boolean truncated) {
            this.truncated = truncated;
        }

        public long getTotalSegments() {
            return totalSegments;
        }

        public void setTotalSegments(long totalSegments) {
            this.totalSegments = totalSegments;
        }

        public long getCompletedRows() {
            return completedRows;
        }

        public void setCompletedRows(long completedRows) {
            this.completedRows = completedRows;
        }

        public double getMinX() {
            return minX;
        }

        public void setMinX(double minX) {
            this.minX = minX;
        }

        public double getMaxX() {
            return maxX;
        }

        public void setMaxX(double maxX) {
            this.maxX = maxX;
        }

        public double getMinY() {
            return minY;
        }

        public void setMinY(double minY) {
            this.minY = minY;
        }

        public double getMaxY() {
            return maxY;
        }

        public void setMaxY(double maxY) {
            this.maxY = maxY;
        }

        public double getMinZ() {
            return minZ;
        }

        public void setMinZ(double minZ) {
            this.minZ = minZ;
        }

        public double getMaxZ() {
            return maxZ;
        }

        public void setMaxZ(double maxZ) {
            this.maxZ = maxZ;
        }

        public List<double[]> getSegments() {
            return segments;
        }

        public void setSegments(List<double[]> segments) {
            this.segments = segments;
        }
    }

    @GET
    @Path("toolpath")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get parsed G-code toolpath segments for web visualization")
    public ToolpathResponse getToolpath() throws Exception {
        ToolpathResponse response = new ToolpathResponse();
        File file = backendAPI.getProcessedGcodeFile();
        if (file == null || !file.exists()) {
            file = backendAPI.getGcodeFile();
        }
        if (file == null || !file.exists()) {
            response.setHasFile(false);
            return response;
        }

        response.setHasFile(true);
        response.setFileName(file.getName());
        response.setCompletedRows(sendProgress != null ? sendProgress.getNumCompletedRows() : 0);

        GcodeViewParse parser = new GcodeViewParse();
        List<LineSegment> lineSegments;
        try (IGcodeStreamReader gsr = new GcodeStreamReader(file, new DefaultCommandCreator())) {
            lineSegments = parser.toObjFromReaderWithArcTolerance(gsr, ARC_TOLERANCE, 0);
        } catch (GcodeStreamReader.NotGcodeStreamFile e) {
            List<String> lines = VisualizerUtils.readFiletoArrayList(file.getAbsolutePath());
            lineSegments = parser.toObjReduxWithArcTolerance(lines, ARC_TOLERANCE, 0);
        }

        response.setTotalSegments(lineSegments.size());
        int maxSegments = backendAPI.getSettings().getPendantToolpathMaxSegments();
        int step = 1;
        if (lineSegments.size() > maxSegments) {
            step = (int) Math.ceil((double) lineSegments.size() / maxSegments);
            response.setTruncated(true);
        }

        double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;

        List<double[]> compact = new ArrayList<>(Math.min(lineSegments.size(), maxSegments));
        for (int i = 0; i < lineSegments.size(); i++) {
            LineSegment ls = lineSegments.get(i);
            Position p1 = ls.getStart();
            Position p2 = ls.getEnd();

            double x1 = Double.isNaN(p1.x) ? 0 : p1.x;
            double y1 = Double.isNaN(p1.y) ? 0 : p1.y;
            double z1 = Double.isNaN(p1.z) ? 0 : p1.z;
            double x2 = Double.isNaN(p2.x) ? 0 : p2.x;
            double y2 = Double.isNaN(p2.y) ? 0 : p2.y;
            double z2 = Double.isNaN(p2.z) ? 0 : p2.z;

            minX = Math.min(minX, Math.min(x1, x2));
            maxX = Math.max(maxX, Math.max(x1, x2));
            minY = Math.min(minY, Math.min(y1, y2));
            maxY = Math.max(maxY, Math.max(y1, y2));
            minZ = Math.min(minZ, Math.min(z1, z2));
            maxZ = Math.max(maxZ, Math.max(z1, z2));

            if (i % step == 0) {
                int type = 1;
                if (ls.isFastTraverse()) {
                    type = 0;
                } else if (ls.isArc()) {
                    type = 2;
                } else if (ls.isZMovement() && x1 == x2 && y1 == y2) {
                    type = 3;
                }
                compact.add(new double[]{x1, y1, z1, x2, y2, z2, type, ls.getLineNumber()});
            }
        }

        if (minX == Double.POSITIVE_INFINITY) {
            minX = maxX = minY = maxY = minZ = maxZ = 0;
        }
        response.setMinX(minX);
        response.setMaxX(maxX);
        response.setMinY(minY);
        response.setMaxY(maxY);
        response.setMinZ(minZ);
        response.setMaxZ(maxZ);
        response.setSegments(compact);
        return response;
    }
}

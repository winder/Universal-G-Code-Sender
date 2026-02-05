/*
 * This file is part of JGCGen.
 *
 * JGCGen is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JGCGen is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JGCGen.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.ugs.nbp.designer.io.gcode;

import com.willwinder.ugs.nbp.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.nbp.designer.io.gcode.path.GcodePath;
import com.willwinder.ugs.nbp.designer.io.gcode.path.Segment;
import com.willwinder.ugs.nbp.designer.io.gcode.toolpaths.DrillCenterToolPath;
import com.willwinder.ugs.nbp.designer.io.gcode.toolpaths.LaserFillToolPath;
import com.willwinder.ugs.nbp.designer.io.gcode.toolpaths.LaserOutlineToolPath;
import com.willwinder.ugs.nbp.designer.io.gcode.toolpaths.LaserRasterToolPath;
import com.willwinder.ugs.nbp.designer.io.gcode.toolpaths.OutlineToolPath;
import com.willwinder.ugs.nbp.designer.io.gcode.toolpaths.PocketToolPath;
import com.willwinder.ugs.nbp.designer.io.gcode.toolpaths.SurfaceToolPath;
import com.willwinder.ugs.nbp.designer.io.gcode.toolpaths.ToolPathStats;
import com.willwinder.ugs.nbp.designer.io.gcode.toolpaths.ToolPathUtils;
import com.willwinder.ugs.nbp.designer.io.gcode.writer.GrblGcodeWriter;
import com.willwinder.ugs.nbp.designer.model.Settings;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Calle Laakkonen
 * @author Joacim Breiler
 */
public class SimpleGcodeRouter {
    private static final Logger LOGGER = Logger.getLogger(SimpleGcodeRouter.class.getSimpleName());
    private final Settings settings;

    public SimpleGcodeRouter(Settings settings) {
        this.settings = settings;
    }

    public void toGcode(List<Cuttable> entities, Writer writer) throws IOException {
        try {
            toGcode(writer, getGcodePathFromCuttables(entities));
        } catch (IOException e) {
            throw new RuntimeException("An error occured while trying to generate gcode", e);
        }
    }

    private GcodePath getGcodePathFromCuttables(List<Cuttable> cuttables) {
        GcodePath gcodePath = new GcodePath();
        int index = 0;

        for (Cuttable cuttable : cuttables) {

            index++;
            gcodePath.addSegment(new Segment(" " + cuttable.getName() + " - " + cuttable.getCutType().getName() + " (" + index + "/" + cuttables.size() + ")"));
            if (cuttable.getIncludeInExport()) {
                switch (cuttable.getCutType()) {
                    case POCKET:
                        PocketToolPath simplePocket = new PocketToolPath(settings, cuttable);
                        simplePocket.setStartDepth(cuttable.getStartDepth());
                        simplePocket.setTargetDepth(cuttable.getTargetDepth());
                        simplePocket.appendGcodePath(gcodePath, settings);
                        break;
                    case SURFACE:
                        SurfaceToolPath surfaceToolPath = new SurfaceToolPath(settings, cuttable);
                        surfaceToolPath.setStartDepth(cuttable.getStartDepth());
                        surfaceToolPath.setTargetDepth(cuttable.getTargetDepth());
                        surfaceToolPath.appendGcodePath(gcodePath, settings);
                        break;
                    case OUTSIDE_PATH:
                        OutlineToolPath simpleOutsidePath = new OutlineToolPath(settings, cuttable);
                        simpleOutsidePath.setOffset(settings.getToolDiameter() / 2d);
                        simpleOutsidePath.setStartDepth(cuttable.getStartDepth());
                        simpleOutsidePath.setTargetDepth(cuttable.getTargetDepth());
                        simpleOutsidePath.appendGcodePath(gcodePath, settings);
                        break;
                    case INSIDE_PATH:
                        OutlineToolPath simpleInsidePath = new OutlineToolPath(settings, cuttable);
                        simpleInsidePath.setOffset(-settings.getToolDiameter() / 2d);
                        simpleInsidePath.setStartDepth(cuttable.getStartDepth());
                        simpleInsidePath.setTargetDepth(cuttable.getTargetDepth());
                        simpleInsidePath.appendGcodePath(gcodePath, settings);
                        break;
                    case ON_PATH:
                        OutlineToolPath simpleOnPath = new OutlineToolPath(settings, cuttable);
                        simpleOnPath.setStartDepth(cuttable.getStartDepth());
                        simpleOnPath.setTargetDepth(cuttable.getTargetDepth());
                        simpleOnPath.appendGcodePath(gcodePath, settings);
                        break;
                    case CENTER_DRILL:
                        DrillCenterToolPath drillToolPath = new DrillCenterToolPath(settings, cuttable);
                        drillToolPath.setStartDepth(cuttable.getStartDepth());
                        drillToolPath.setTargetDepth(cuttable.getTargetDepth());
                        drillToolPath.appendGcodePath(gcodePath, settings);
                        break;
                    case LASER_ON_PATH:
                        LaserOutlineToolPath laserOutlineToolPath = new LaserOutlineToolPath(settings, cuttable);
                        laserOutlineToolPath.appendGcodePath(gcodePath, settings);
                        break;
                    case LASER_FILL:
                        LaserFillToolPath laserFillToolPath = new LaserFillToolPath(settings, cuttable);
                        laserFillToolPath.appendGcodePath(gcodePath, settings);
                        break;
                    case LASER_RASTER:
                        LaserRasterToolPath laserRasterToolPath = new LaserRasterToolPath(settings, cuttable);
                        laserRasterToolPath.appendGcodePath(gcodePath, settings);
                        break;
                    default:
                }
            }
        }
        return gcodePath;
    }

    protected void toGcode(Writer writer, GcodePath path) throws IOException {
        ToolPathStats toolPathStats = ToolPathUtils.getToolPathStats(path);
        LOGGER.info("Generated a tool path with total length of " + Math.round(toolPathStats.getTotalFeedLength()) + "mm and " + Math.round(toolPathStats.getTotalRapidLength()) + "mm of rapid movement");

        GrblGcodeWriter simpleGcodeWriter = new GrblGcodeWriter(settings, writer);
        simpleGcodeWriter.begin();
        for (Segment segment : path.getSegments()) {
            simpleGcodeWriter.writeSegment(segment);
        }
        simpleGcodeWriter.end();
        writer.flush();
    }
}

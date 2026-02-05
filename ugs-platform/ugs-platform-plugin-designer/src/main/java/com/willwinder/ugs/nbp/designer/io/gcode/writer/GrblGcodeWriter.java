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
package com.willwinder.ugs.nbp.designer.io.gcode.writer;

import com.willwinder.ugs.nbp.designer.io.gcode.path.Segment;
import com.willwinder.ugs.nbp.designer.model.Settings;
import com.willwinder.universalgcodesender.Utils;
import com.willwinder.universalgcodesender.gcode.util.Code;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.PartialPosition;
import static com.willwinder.universalgcodesender.utils.MathUtils.isEqual;
import com.willwinder.universalgcodesender.utils.Version;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.Writer;

public class GrblGcodeWriter implements GcodeWriter {
    private static final String HEADER = "; This file was generated with \"Universal Gcode Sender " + Version.getVersionString() + "\"\n\n";

    private final Settings settings;
    private final Writer writer;
    private Integer currentSpindle = null;
    private Integer currentFeed = null;
    private PartialPosition currentPoint;
    private boolean hasFeedRateSet = false;
    private boolean hasStartedSpindle = false;

    public GrblGcodeWriter(Settings settings, Writer writer) {
        this.settings = settings;
        this.writer = writer;
    }

    @Override
    public void begin() throws IOException {
        writer.write(HEADER);
        writer.write(Code.G21.name() + " ; millimeters\n");
        writer.write(Code.G90.name() + " ; absolute coordinate\n");
        writer.write(Code.G17.name() + " ; XY plane\n");
        writer.write(Code.G94.name() + " ; units per minute feed rate mode\n");
        writer.write("\n");
        writer.write("; Tool: " + settings.getToolDiameter() + "mm\n");
        writer.write("; Depth per pass: " + settings.getDepthPerPass() + "mm\n");
        writer.write("; Plunge speed: " + settings.getPlungeSpeed() + "mm/min\n");
        writer.write("; Safe height: " + settings.getSafeHeight() + "mm\n");
        writer.write("; Tool step over: " + settings.getToolStepOver() + "mm\n");
        writer.write("; Spindle start command: " + settings.getSpindleDirection() + "\n");
    }

    @Override
    public void writeSegment(Segment segment) throws IOException {
        if (StringUtils.isNotEmpty(segment.getLabel())) {
            writer.write(";" + segment.getLabel() + "\n");
        }

        if (segment.getSpindleSpeed() != null && !segment.getSpindleSpeed().equals(currentSpindle) && !hasStartedSpindle) {
            writer.write(settings.getSpindleDirection() + " S" + segment.getSpindleSpeed() + "\n");
            hasStartedSpindle = true;
            currentSpindle = segment.getSpindleSpeed();
        }

        switch (segment.type) {
            case SEAM -> {
                if (!hasFeedRateSet && segment.getFeedSpeed() != null) {
                    writer.write("F" + segment.getFeedSpeed() + " ");
                    hasFeedRateSet = true;
                }
            }

            case MOVE -> {
                writer.write(("G0 " + getPointFormattedGCode(segment)).trim() + "\n");
                hasFeedRateSet = false;
            }

            case POINT -> {
                writer.write("G1 F" + settings.getPlungeSpeed() + " "
                        + getPointFormattedGCode(segment) + "\n");
                hasFeedRateSet = false;
            }

            case LINE, CWARC, CCWARC -> {
                writer.write(segment.type.gcode + " ");
                if (segment.getFeedSpeed() != null && !segment.getFeedSpeed().equals(currentFeed)) {
                    writer.write("F" + segment.getFeedSpeed() + " ");
                    currentFeed = segment.getFeedSpeed();
                    hasFeedRateSet = true;
                }

                if (segment.getSpindleSpeed() != null && !segment.getSpindleSpeed().equals(currentSpindle)) {
                    writer.write("S" + segment.getSpindleSpeed() + " ");
                    currentSpindle = segment.getSpindleSpeed();
                }

                writer.write(getPointFormattedGCode(segment) + "\n");
            }
        }
    }

    private String getPointFormattedGCode(Segment segment) {
        StringBuilder result = new StringBuilder();
        PartialPosition newPoint = segment.getPoint();

        for (Axis axis : Axis.values()) {
            if (newPoint.hasAxis(axis) && (currentPoint == null || !currentPoint.hasAxis(axis) || !isEqual(newPoint.getAxis(axis), currentPoint.getAxis(axis), 0.0001))) {
                result.append(axis.name()).append(Utils.formatter.format(newPoint.getAxis(axis)));
            }
        }
        currentPoint = segment.point;
        return result.toString();
    }

    @Override
    public void end() throws IOException {
        writer.write("\n; Turning off spindle\n");
        writer.write(Code.M5.name());
        writer.append("\n");
    }
}
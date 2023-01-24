/*
    Copyright 2022 Will Winder

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
package com.willwinder.ugs.nbp.designer.io.gcode;

import com.willwinder.ugs.nbp.designer.entities.cuttable.CutType;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.nbp.designer.io.DesignWriter;
import com.willwinder.ugs.nbp.designer.io.DesignWriterException;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Writes the design as generated gcode.
 *
 * @author Joacim Breiler
 */
public class GcodeDesignWriter implements DesignWriter {

    @Override
    public void write(File file, Controller controller) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            write(fileOutputStream, controller);
        } catch (IOException e) {
            throw new DesignWriterException("Could not write gcode to file: " + file.getAbsolutePath(), e);
        }
    }

    @Override
    public void write(OutputStream outputStream, Controller controller) {
        try {
            SimpleGcodeRouter gcodeRouter = new SimpleGcodeRouter();
            gcodeRouter.setSafeHeight(controller.getSettings().getSafeHeight());
            gcodeRouter.setDepthPerPass(controller.getSettings().getDepthPerPass());
            gcodeRouter.setToolDiameter(controller.getSettings().getToolDiameter());
            gcodeRouter.setToolStepOver(controller.getSettings().getToolStepOver());
            gcodeRouter.setPlungeSpeed(controller.getSettings().getPlungeSpeed());
            gcodeRouter.setSafeHeight(controller.getSettings().getSafeHeight());
            gcodeRouter.setFeedSpeed(controller.getSettings().getFeedSpeed());
            gcodeRouter.setSpindleSpeed(controller.getSettings().getSpindleSpeed());

            List<Cuttable> cuttables = controller.getDrawing().getEntities().stream()
                    .filter(Cuttable.class::isInstance)
                    .map(Cuttable.class::cast)
                    .filter(cuttable -> !cuttable.isHidden())
                    .filter(cuttable -> cuttable.getCutType() != CutType.NONE)
                    .collect(Collectors.toList());

            String gcode = gcodeRouter.toGcode(cuttables);
            IOUtils.write(gcode, outputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new DesignWriterException("Could not write gcode to stream", e);
        }
    }
}

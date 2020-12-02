/*
    Copyright 2020 Will Winder


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
package com.willwinder.ugs.nbp.core.actions;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.gcode.processors.RotateProcessor;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.uielements.helpers.LoaderDialogHelper;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import com.willwinder.universalgcodesender.utils.GcodeStreamReader;
import com.willwinder.universalgcodesender.utils.IGcodeStreamReader;
import com.willwinder.universalgcodesender.utils.MathUtils;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import com.willwinder.universalgcodesender.visualizer.GcodeViewParse;
import com.willwinder.universalgcodesender.visualizer.LineSegment;
import com.willwinder.universalgcodesender.visualizer.VisualizerUtils;

import javax.swing.AbstractAction;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An abstract action for applying rotation to a loaded model
 */
public abstract class AbstractRotateAction extends AbstractAction implements UGSEventListener {

    public static final String ICON_BASE = "resources/icons/rotation0.svg";
    public static final double ARC_SEGMENT_LENGTH = 0.5;
    private final double rotation;
    private BackendAPI backend;

    public AbstractRotateAction(double rotation) {
        this.backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        this.backend.addUGSEventListener(this);
        this.rotation = rotation;
        setEnabled(isEnabled());
    }

    @Override
    public void UGSEvent(UGSEvent cse) {
        if (cse.isStateChangeEvent() || cse.isFileChangeEvent()) {
            java.awt.EventQueue.invokeLater(() -> setEnabled(isEnabled()));
        }
    }

    @Override
    public boolean isEnabled() {
        return backend.getGcodeFile() != null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isEnabled()) {
            return;
        }

        ThreadHelper.invokeLater(() -> {
            try {
                LoaderDialogHelper.showDialog("Rotating model", 1000, (Component) e.getSource());
                File gcodeFile = backend.getProcessedGcodeFile();
                Position center = getCenter(gcodeFile);
                RotateProcessor rotateProcessor = new RotateProcessor(center, rotation);
                backend.applyCommandProcessor(rotateProcessor);
                LoaderDialogHelper.closeDialog();
            } catch (Exception ex) {
                GUIHelpers.displayErrorDialog(ex.getLocalizedMessage());
            }
        });
    }

    private Position getCenter(File gcodeFile) throws IOException, GcodeParserException {
        List<LineSegment> lineSegments = parseGcodeLinesFromFile(gcodeFile);

        // We only care about carving motion, filter those commands out
        List<PartialPosition> pointList = lineSegments.parallelStream()
                .filter(lineSegment -> !lineSegment.isFastTraverse())
                .flatMap(lineSegment -> {
                    // We map both the start and end points in MM
                    PartialPosition start = PartialPosition.from(lineSegment.getStart());
                    PartialPosition end = PartialPosition.from(lineSegment.getEnd());
                    return Stream.of(start, end);
                })
                .distinct()
                .collect(Collectors.toList());

        return MathUtils.getCenter(pointList);
    }

    private List<LineSegment> parseGcodeLinesFromFile(File gcodeFile) throws IOException, GcodeParserException {
        List<LineSegment> result;

        GcodeViewParse gcvp = new GcodeViewParse();
        try (IGcodeStreamReader gsr = new GcodeStreamReader(gcodeFile)) {
            result = gcvp.toObjFromReader(gsr, ARC_SEGMENT_LENGTH);
        } catch (GcodeStreamReader.NotGcodeStreamFile e) {
            List<String> linesInFile = VisualizerUtils.readFiletoArrayList(gcodeFile.getAbsolutePath());
            result = gcvp.toObjRedux(linesInFile, ARC_SEGMENT_LENGTH);
        }

        return result;
    }
}

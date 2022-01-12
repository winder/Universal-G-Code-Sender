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
package com.willwinder.ugs.nbp.editor.actions;

import com.willwinder.ugs.nbp.editor.GcodeDataObject;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.gcode.processors.RotateProcessor;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.model.events.FileState;
import com.willwinder.universalgcodesender.model.events.FileStateEvent;
import com.willwinder.universalgcodesender.uielements.helpers.LoaderDialogHelper;
import com.willwinder.universalgcodesender.utils.*;
import com.willwinder.universalgcodesender.visualizer.GcodeViewParse;
import com.willwinder.universalgcodesender.visualizer.LineSegment;
import com.willwinder.universalgcodesender.visualizer.VisualizerUtils;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An abstract action for applying rotation to a loaded model
 */
public abstract class AbstractRotateAction extends CookieAction implements UGSEventListener {
    public static final double ARC_SEGMENT_LENGTH = 0.5;
    private final double rotation;
    private final transient BackendAPI backend;

    protected AbstractRotateAction(double rotation) {
        this.backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        this.backend.addUGSEventListener(this);
        this.rotation = rotation;
        setEnabled(isEnabled());
    }

    @Override
    public void UGSEvent(UGSEvent cse) {
        if (cse instanceof ControllerStateEvent || cse instanceof FileStateEvent) {
            EventQueue.invokeLater(() -> setEnabled(isEnabled()));
        }
    }

    @Override
    public boolean isEnabled() {
        return backend.getGcodeFile() != null && !backend.isSendingFile() && super.isEnabled();
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        if (!isEnabled()) {
            return;
        }

        ThreadHelper.invokeLater(() -> {
            try {
                LoaderDialogHelper.showDialog("Rotating model", 1000);
                File gcodeFile = backend.getProcessedGcodeFile();
                Position center = getCenter(gcodeFile);
                RotateProcessor rotateProcessor = new RotateProcessor(center, rotation);
                backend.applyCommandProcessor(rotateProcessor);
            } catch (Exception ex) {
                GUIHelpers.displayErrorDialog(ex.getLocalizedMessage());
            } finally {
                LoaderDialogHelper.closeDialog();
            }
        });
    }

    @Override
    public String getName() {
        return (String) getValue(NAME);
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    protected int mode() {
        return CookieAction.MODE_ANY;
    }

    @Override
    protected Class<?>[] cookieClasses() {
        return new Class[]{GcodeDataObject.class};
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

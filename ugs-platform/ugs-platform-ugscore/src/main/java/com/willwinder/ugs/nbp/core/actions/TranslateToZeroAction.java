package com.willwinder.ugs.nbp.core.actions;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.gcode.processors.TranslateProcessor;
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
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;

import javax.swing.AbstractAction;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ActionID(
        category = LocalizingService.CATEGORY_PROGRAM,
        id = "TranslateToZeroAction")
@ActionRegistration(
        iconBase = AbstractRotateAction.ICON_BASE,
        displayName = "Translate to zero",
        lazy = false)
@ActionReferences({
        @ActionReference(
                path = LocalizingService.MENU_PROGRAM,
                position = 1020)
})
public class TranslateToZeroAction extends AbstractAction implements UGSEventListener {

    public static final String ICON_BASE = "resources/icons/translate.svg";
    public static final double ARC_SEGMENT_LENGTH = 0.5;
    private BackendAPI backend;

    public TranslateToZeroAction() {
        this.backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        this.backend.addUGSEventListener(this);

        putValue("iconBase", ICON_BASE);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_BASE, false));
        putValue("menuText", "Translate to zero");
        putValue(NAME, "Translate to zero");

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
                LoaderDialogHelper.showDialog("Translating model", 1000, (Component) e.getSource());
                File gcodeFile = backend.getProcessedGcodeFile();
                Position lowerLeftCorner = getLowerLeftCorner(gcodeFile);
                TranslateProcessor translateProcessor = new TranslateProcessor(lowerLeftCorner);
                backend.applyCommandProcessor(translateProcessor);
                LoaderDialogHelper.closeDialog();
            } catch (Exception ex) {
                GUIHelpers.displayErrorDialog(ex.getLocalizedMessage());
            }
        });
    }

    private Position getLowerLeftCorner(File gcodeFile) throws IOException, GcodeParserException {
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

        return MathUtils.getLowerLeftCorner(pointList);
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

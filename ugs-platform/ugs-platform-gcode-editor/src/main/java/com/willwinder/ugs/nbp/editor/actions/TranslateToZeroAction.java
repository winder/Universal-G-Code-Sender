package com.willwinder.ugs.nbp.editor.actions;

import com.willwinder.ugs.nbp.editor.GcodeDataObject;
import com.willwinder.ugs.nbp.editor.GcodeLanguageConfig;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.gcode.processors.TranslateProcessor;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.model.events.FileStateEvent;
import com.willwinder.universalgcodesender.uielements.helpers.LoaderDialogHelper;
import com.willwinder.universalgcodesender.utils.*;
import com.willwinder.universalgcodesender.visualizer.GcodeViewParse;
import com.willwinder.universalgcodesender.visualizer.LineSegment;
import com.willwinder.universalgcodesender.visualizer.VisualizerUtils;
import org.netbeans.api.editor.EditorActionRegistration;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ActionID(
        category = LocalizingService.CATEGORY_PROGRAM,
        id = "TranslateToZeroAction")
@ActionRegistration(
        iconBase = TranslateToZeroAction.ICON_BASE,
        displayName = TranslateToZeroAction.NAME,
        lazy = false)
@ActionReferences({
        @ActionReference(
                path = LocalizingService.MENU_PROGRAM,
                position = 1221)
})
@EditorActionRegistration(
        name = "translate-to-zero",
        toolBarPosition = 13,
        mimeType = GcodeLanguageConfig.MIME_TYPE,
        iconResource = TranslateToZeroAction.ICON_BASE
)
public class TranslateToZeroAction extends CookieAction implements UGSEventListener {

    private static final Logger LOGGER = Logger.getLogger(TranslateToZeroAction.class.getSimpleName());
    public static final String NAME = "Translate to zero";
    public static final String ICON_BASE = "icons/translate.svg";
    public static final double ARC_SEGMENT_LENGTH = 0.5;
    private final transient BackendAPI backend;

    public TranslateToZeroAction() {
        this.backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        this.backend.addUGSEventListener(this);
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
    public String getName() {
        return NAME;
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        if (!isEnabled()) {
            return;
        }

        ThreadHelper.invokeLater(() -> {
            try {
                LoaderDialogHelper.showDialog("Translating model", 1000);
                File gcodeFile = backend.getProcessedGcodeFile();
                Position offset = getLowerLeftCorner(gcodeFile);
                offset.x = -offset.x;
                offset.y = -offset.y;
                offset.z = 0;

                TranslateProcessor translateProcessor = new TranslateProcessor(offset);
                backend.applyCommandProcessor(translateProcessor);
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Could not translate gcode", ex);
                GUIHelpers.displayErrorDialog(ex.getLocalizedMessage());
            } finally {
                LoaderDialogHelper.closeDialog();
            }
        });
    }

    @Override
    protected int mode() {
        return CookieAction.MODE_ANY;
    }

    @Override
    protected Class<?>[] cookieClasses() {
        return new Class[]{GcodeDataObject.class};
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
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

    @Override
    protected String iconResource() {
        return ICON_BASE;
    }
}

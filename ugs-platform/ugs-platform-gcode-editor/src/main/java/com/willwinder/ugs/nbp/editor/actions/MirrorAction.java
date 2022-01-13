package com.willwinder.ugs.nbp.editor.actions;

import com.willwinder.ugs.nbp.editor.GcodeDataObject;
import com.willwinder.ugs.nbp.editor.GcodeLanguageConfig;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.gcode.processors.MirrorProcessor;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ActionID(
        category = LocalizingService.CATEGORY_PROGRAM,
        id = "MirrorAction")
@ActionRegistration(
        iconBase = MirrorAction.ICON_BASE,
        displayName = MirrorAction.NAME,
        lazy = false)
@ActionReferences({
        @ActionReference(
                path = LocalizingService.MENU_PROGRAM,
                position = 1220)
})
@EditorActionRegistration(
        name = "mirror-gcode",
        toolBarPosition = 12,
        mimeType = GcodeLanguageConfig.MIME_TYPE,
        iconResource = MirrorAction.ICON_BASE
)
public class MirrorAction extends CookieAction implements UGSEventListener {

    public static final String ICON_BASE = "icons/mirror.svg";

    public static final String NAME = "Mirror";
    public static final double ARC_SEGMENT_LENGTH = 0.5;
    private final transient BackendAPI backend;

    public MirrorAction() {
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
    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    protected String iconResource() {
        return ICON_BASE;
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        if (!isEnabled()) {
            return;
        }

        ThreadHelper.invokeLater(() -> {
            try {
                LoaderDialogHelper.showDialog("Mirroring model", 1000);
                File gcodeFile = backend.getProcessedGcodeFile();
                Position center = getCenter(gcodeFile);
                MirrorProcessor translateProcessor = new MirrorProcessor(PartialPosition.from(center));
                backend.applyCommandProcessor(translateProcessor);
            } catch (Exception ex) {
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

    private Position getCenter(File gcodeFile) throws IOException, GcodeParserException {
        List<LineSegment> lineSegments = parseGcodeLinesFromFile(gcodeFile);

        // We only care about carving motion, filter those commands out
        List<PartialPosition> pointList = lineSegments.parallelStream()
                .filter(lineSegment -> !lineSegment.isFastTraverse())
                .flatMap(lineSegment -> {
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

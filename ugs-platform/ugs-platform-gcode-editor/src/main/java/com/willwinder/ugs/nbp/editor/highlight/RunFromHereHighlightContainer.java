package com.willwinder.ugs.nbp.editor.highlight;

import com.willwinder.ugs.nbp.editor.GcodeLanguageConfig;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.services.RunFromService;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.ReleasableHighlightsContainer;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.Element;
import java.lang.ref.WeakReference;

/**
 * When using "Run from here" this will make skipped gcode commands get a special style
 *
 * @author Joacim Breiler
 */
public class RunFromHereHighlightContainer extends AbstractHighlightsContainer implements RunFromService.RunFromServiceListener, ReleasableHighlightsContainer {
    /**
     * a reference to a font style from FontAndColors.xml
     */
    private static final String FONT_STYLE = "INACTIVE";
    private final AttributeSet highlightAttributes;
    private final OffsetsBag bag;
    private final WeakReference<Document> weakDoc;

    public RunFromHereHighlightContainer(Document doc) {
        FontColorSettings fontColorSettings = MimeLookup.getLookup(GcodeLanguageConfig.MIME_TYPE).lookup(FontColorSettings.class);
        highlightAttributes = fontColorSettings.getTokenFontColors(FONT_STYLE);

        RunFromService runFromService = CentralLookup.getDefault().lookup(RunFromService.class);
        runFromService.addListener(this);
        bag = new OffsetsBag(doc);
        weakDoc = new WeakReference<>(doc);
    }

    @Override
    public void runFromLineChanged(int lineNumber) {
        DataObject dataObject = NbEditorUtilities.getDataObject(weakDoc.get());
        if (dataObject == null) {
            clearHighlights();
            return;
        }

        EditorCookie pane = dataObject.getLookup().lookup(EditorCookie.class);
        JEditorPane[] panes = pane.getOpenedPanes();
        if (panes == null || panes.length == 0) {
            clearHighlights();
            return;
        }

        JEditorPane comp = panes[0];
        Element root = comp.getDocument().getDefaultRootElement();
        Element element = root.getElement(lineNumber + 1);
        bag.clear();
        if (element != null) {
            bag.addHighlight(0, element.getStartOffset(), highlightAttributes);
        }
        notifyHighlightsChanged();
    }

    @Override
    public void released() {
        RunFromService runFromService = CentralLookup.getDefault().lookup(RunFromService.class);
        runFromService.removeListener(this);
    }

    @Override
    public HighlightsSequence getHighlights(int startOffset, int endOffset) {
        return bag.getHighlights(startOffset, endOffset);
    }

    private void clearHighlights() {
        bag.clear();
        notifyHighlightsChanged();
    }

    private void notifyHighlightsChanged() {
        Document document = weakDoc.get();
        if (document != null) {
            fireHighlightsChange(document.getStartPosition().getOffset(), document.getEndPosition().getOffset());
        }
    }
}

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
package com.willwinder.ugs.nbp.editor.highlight;

import com.willwinder.ugs.nbp.editor.GcodeLanguageConfig;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.CommandEvent;
import com.willwinder.universalgcodesender.model.events.CommandEventType;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.ReleasableHighlightsContainer;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;

import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.Element;
import java.lang.ref.WeakReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * When a gcode file is sent this highlighter will style the sent lines
 *
 * @author Joacim Breiler
 */
public class SentCommandsHighlightContainer extends AbstractHighlightsContainer implements ReleasableHighlightsContainer, UGSEventListener {
    private static final Logger LOGGER = Logger.getLogger(SentCommandsHighlightContainer.class.getSimpleName());
    private static final String FONT_STYLE = "EXECUTED";

    private final AttributeSet highlightAttributes;
    private final OffsetsBag bag;
    private final WeakReference<Document> weakDocument;
    private final BackendAPI backend;

    public SentCommandsHighlightContainer(Document doc) {
        FontColorSettings fontColorSettings = MimeLookup.getLookup(GcodeLanguageConfig.MIME_TYPE).lookup(FontColorSettings.class);
        highlightAttributes = fontColorSettings.getTokenFontColors(FONT_STYLE);
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        backend.addUGSEventListener(this);

        bag = new OffsetsBag(doc);
        weakDocument = new WeakReference<>(doc);
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
        Document document = getDocument();
        if (document != null) {
            fireHighlightsChange(document.getStartPosition().getOffset(), document.getEndPosition().getOffset());
        }
    }

    private Document getDocument() {
        return weakDocument.get();
    }

    public void updateHighlight(int lineNumber) {
        Document document = getDocument();
        if (document == null) {
            clearHighlights();
            return;
        }

        Element root = document.getDefaultRootElement();
        Element element = root.getElement(lineNumber);
        bag.clear();
        if (element != null) {
            bag.addHighlight(0, element.getStartOffset(), highlightAttributes);
        }
        notifyHighlightsChanged();
    }

    @Override
    public void released() {
        clearHighlights();
        backend.removeUGSEventListener(this);
    }

    @Override
    public void UGSEvent(UGSEvent event) {
        if (event instanceof CommandEvent && backend.isSendingFile()) {
            handleCommandEvent((CommandEvent) event);
        } else if (event instanceof ControllerStateEvent) {
            handleControllerStateEvent((ControllerStateEvent) event);
        }
    }

    private void handleControllerStateEvent(ControllerStateEvent event) {
        if (event.getState() == ControllerState.IDLE) {
            clearHighlights();
        }
    }

    private void handleCommandEvent(CommandEvent event) {
        try {
            int lineNumber = event.getCommand().getCommandNumber();
            if (event.getCommandEventType() == CommandEventType.COMMAND_COMPLETE && lineNumber >= 0) {
                updateHighlight(lineNumber);
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE,"Could not update the highlighted lines, removing the listener to prevent further problems.");
            released();
        }
    }
}

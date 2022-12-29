/*
    Copyright 2021-2022 Will Winder

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
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.spi.editor.highlighting.HighlightsContainer;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.ReleasableHighlightsContainer;
import org.netbeans.spi.editor.highlighting.ZOrder;

import javax.swing.text.Document;

@MimeRegistrations({
        @MimeRegistration(mimeType = GcodeLanguageConfig.MIME_TYPE, service = org.netbeans.spi.editor.highlighting.HighlightsLayerFactory.class),
})
public class GcodeHighlightsLayerFactory implements org.netbeans.spi.editor.highlighting.HighlightsLayerFactory {

    /**
     * Returns an instance of the "Run from here" highlighter container.
     * If it already exists it will be returned, otherwise a new one will be added.
     *
     * @param doc the current document
     * @return an instance of the highlighter
     */
    public static RunFromHereHighlightContainer getRunFromHereHighlighter(Document doc) {
        RunFromHereHighlightContainer highlighter = (RunFromHereHighlightContainer) doc.getProperty(RunFromHereHighlightContainer.class);
        if (highlighter == null) {
            highlighter = new RunFromHereHighlightContainer(doc);
            doc.putProperty(RunFromHereHighlightContainer.class, highlighter);
        }
        return highlighter;
    }

    /**
     * Creates an instance of a highlighter for displaying sent rows.
     *
     * @param doc the current document
     * @return an instance of the highlighter
     */
    public static SentCommandsHighlightContainer getSentCommandsHighlighter(Document doc) {
        SentCommandsHighlightContainer highlighter = (SentCommandsHighlightContainer) doc.getProperty(SentCommandsHighlightContainer.class);
        if (highlighter == null) {
            highlighter = new SentCommandsHighlightContainer(doc);
            doc.putProperty(SentCommandsHighlightContainer.class, highlighter);
        }
        return highlighter;
    }

    /**
     * Attempt to unregister the highlighter containers for the given document
     *
     * @param document the document to which the highlight containers are associated
     */
    public static void release(Document document) {
        releaseHighlighter(document, SentCommandsHighlightContainer.class);
        releaseHighlighter(document, RunFromHereHighlightContainer.class);
    }

    private static void releaseHighlighter(Document doc, Class<? extends HighlightsContainer> clazz) {
        Object property = doc.getProperty(clazz);
        if (property instanceof ReleasableHighlightsContainer) {
            ((ReleasableHighlightsContainer) property).released();
        }
    }

    @Override
    public HighlightsLayer[] createLayers(Context context) {
        return new HighlightsLayer[]{
                HighlightsLayer.create(RunFromHereHighlightContainer.class.getName(), ZOrder.SHOW_OFF_RACK, true, getRunFromHereHighlighter(context.getDocument())),
                HighlightsLayer.create(SentCommandsHighlightContainer.class.getName(), ZOrder.TOP_RACK, true, getSentCommandsHighlighter(context.getDocument()))
        };
    }
}

/*
    Copyright 2016-2021 Will Winder

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
package com.willwinder.ugs.nbp.editor.renderer;

import com.willwinder.ugs.nbm.visualizer.renderables.GcodeModel;
import com.willwinder.ugs.nbm.visualizer.shared.GcodeRenderer;
import com.willwinder.ugs.nbm.visualizer.shared.Renderable;
import com.willwinder.ugs.nbm.visualizer.shared.RenderableUtils;
import com.willwinder.universalgcodesender.i18n.Localization;
import org.openide.util.Lookup;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.Element;

/**
 * Listens for editor events to notify visualizer, puts changes on the
 * HighlightEventBus as HighlightEvent objects.
 *
 * @author wwinder
 */
public class EditorListener implements CaretListener {
    private Highlight highlight = null;

    public EditorListener() {
        reset();
    }

    @Override
    public void caretUpdate(CaretEvent e) {
        if (e.getSource() instanceof JEditorPane) {
            JEditorPane jep = (JEditorPane) e.getSource();

            Element map = jep.getDocument().getDefaultRootElement();
            int startIndex = map.getElementIndex(jep.getSelectionStart());
            int endIndex = map.getElementIndex(jep.getSelectionEnd());

            if (highlight != null) {
                highlight.setHighlightedLines(startIndex, endIndex);
            }
        }
    }

    public void reset() {
        GcodeRenderer gcodeRenderer = Lookup.getDefault().lookup(GcodeRenderer.class);
        GcodeModel gcodeModel = null;
        for (Renderable renderable : gcodeRenderer.getRenderables()) {
            if (renderable.getClass() == GcodeModel.class) {
                gcodeModel = (GcodeModel) renderable;
            }
        }

        if (gcodeModel != null) {
            if (highlight != null) {
                highlight.setHighlightedLines(0, 0);
            } else {
                highlight = new Highlight(gcodeModel, Localization.getString("platform.visualizer.renderable.highlight"));
                RenderableUtils.registerRenderable(highlight);
            }
        }
    }
}

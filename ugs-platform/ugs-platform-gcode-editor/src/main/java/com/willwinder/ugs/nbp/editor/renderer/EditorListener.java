/*
    Copyright 2016-2019 Will Winder

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

import javax.swing.JEditorPane;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.Element;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Listens for editor events to notify visualizer, puts changes on the
 * HighlightEventBus as HighlightEvent objects.
 *
 * @author wwinder
 */
public class EditorListener implements CaretListener {
    private Highlight highlight = null;
    private EditorPosition position = null;

    public EditorListener() {
        GcodeRenderer gcodeRenderer = Lookup.getDefault().lookup(GcodeRenderer.class);
        GcodeModel gcodeModel = null;
        for (Renderable renderable : gcodeRenderer.getRenderables()) {
            if (renderable.getClass() == GcodeModel.class) {
                gcodeModel = (GcodeModel) renderable;
            }
        }

        if (gcodeModel != null) {
            highlight = new Highlight(gcodeModel, Localization.getString("platform.visualizer.renderable.highlight"));
            RenderableUtils.registerRenderable(highlight);

            position = new EditorPosition(gcodeModel, Localization.getString("platform.visualizer.renderable.editor-position"));
            RenderableUtils.registerRenderable(position);
        }
    }

    @Override
    public void caretUpdate(CaretEvent e) {
        if (e.getSource() instanceof JEditorPane) {
            JEditorPane jep = (JEditorPane) e.getSource();

            Element map = jep.getDocument().getDefaultRootElement();
            int startIndex = map.getElementIndex(jep.getSelectionStart());
            int endIndex = map.getElementIndex(jep.getSelectionEnd());

            Collection<Integer> selectedLines = new ArrayList<>();
            for (int i = startIndex; i <= endIndex; i++) {
                selectedLines.add(i);
            }

            highlight.setHighlightedLines(selectedLines);
            position.setLineNumber(endIndex);
        }
    }
}

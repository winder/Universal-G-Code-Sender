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
package com.willwinder.ugs.nbp.editor;

import com.willwinder.ugs.nbp.editor.renderer.EditorListener;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.openide.ErrorManager;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

import java.io.File;

@MultiViewElement.Registration(
        displayName = "#platform.window.editor.source",
        iconBase = "com/willwinder/ugs/nbp/editor/edit.png",
        mimeType = "text/xgcode",
        persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID = "Gcode",
        position = 1000
)
public class SourceMultiviewElement extends MultiViewEditorElement {

    private static EditorListener editorListener = new EditorListener();
    private final GcodeDataObject obj;
    private final GcodeFileListener fileListener;

    public SourceMultiviewElement(Lookup lookup) {
        super(lookup);
        obj = lookup.lookup(GcodeDataObject.class);
        fileListener = new GcodeFileListener();
    }

    @Override
    public void componentOpened() {
        super.componentOpened();

        // Load the file when the editor is active
        BackendAPI backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        try {
            if (backend.getGcodeFile() == null || !backend.getGcodeFile().getAbsolutePath().equalsIgnoreCase(obj.getPrimaryFile().getPath())) {
                backend.setGcodeFile(new File((obj.getPrimaryFile().getPath())));
            }
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
        }

        obj.getPrimaryFile().addFileChangeListener(fileListener);
    }

    @Override
    public void componentActivated() {
        super.componentActivated();
        editorListener.reset();
        if (getEditorPane() != null) {
            getEditorPane().addCaretListener(editorListener);
        }
    }

    @Override
    public void componentClosed() {
        obj.getPrimaryFile().removeFileChangeListener(fileListener);
        if (getEditorPane() != null) {
            getEditorPane().removeCaretListener(editorListener);
        }
        editorListener.reset();
        super.componentClosed();
    }

    @Override
    public Lookup getLookup() {
        return obj.getLookup();
    }
}

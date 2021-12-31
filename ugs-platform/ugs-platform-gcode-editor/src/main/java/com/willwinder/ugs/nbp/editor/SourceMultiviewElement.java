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
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

@MultiViewElement.Registration(
        displayName = "#platform.window.editor.source",
        iconBase = "com/willwinder/ugs/nbp/editor/edit.png",
        mimeType = GcodeLanguageConfig.MIME_TYPE,
        persistenceType = TopComponent.PERSISTENCE_NEVER,
        preferredID = "Gcode",
        position = 1000
)
public class SourceMultiviewElement extends MultiViewEditorElement implements UGSEventListener {
    private static final long serialVersionUID = 7255236202190135442L;
    private static EditorListener editorListener = new EditorListener();
    private final GcodeDataObject obj;
    private final GcodeFileListener fileListener;
    private final BackendAPI backend;

    public SourceMultiviewElement(Lookup lookup) {
        super(lookup);
        obj = lookup.lookup(GcodeDataObject.class);
        fileListener = new GcodeFileListener();
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        backend.addUGSEventListener(this);
    }

    @Override
    public void componentOpened() {
        super.componentOpened();
        EditorUtils.openFile(obj.getPrimaryFile());
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

    @Override
    public void UGSEvent(UGSEvent ugsEvent) {
        // Disable the editor if not idle or disconnected
        if (ugsEvent.isStateChangeEvent()) {
            ControllerState state = backend.getController().getControllerStatus().getState();
            getEditorPane().setEditable(state == ControllerState.IDLE || state == ControllerState.DISCONNECTED);
        }
    }
}

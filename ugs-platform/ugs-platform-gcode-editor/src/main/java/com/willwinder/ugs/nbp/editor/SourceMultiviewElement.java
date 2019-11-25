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
package com.willwinder.ugs.nbp.editor;

import com.willwinder.ugs.nbp.editor.renderer.EditorListener;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

import javax.swing.*;
import java.io.File;
import java.util.Collection;

@MultiViewElement.Registration(
        displayName = "#platform.window.editor.source",
        iconBase = "com/willwinder/ugs/nbp/editor/edit.png",
        mimeType = "text/xgcode",
        persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID = "Gcode",
        position = 1000
)
public class SourceMultiviewElement extends MultiViewEditorElement {

    private static EditorListener editorListener;

    public SourceMultiviewElement(Lookup lookup) {
        super(lookup);

        if (editorListener == null) {
           editorListener = new EditorListener();
        }

        FileUtil.addFileChangeListener(new FileChangeListener() {
            @Override
            public void fileFolderCreated(FileEvent fe) {

            }

            @Override
            public void fileDataCreated(FileEvent fe) {

            }

            @Override
            public void fileChanged(FileEvent fe) {
                BackendAPI backend = CentralLookup.getDefault().lookup(BackendAPI.class);
                if (backend.getGcodeFile().getPath().equals(fe.getFile().getPath())) {
                    try {
                        backend.setGcodeFile(new File(fe.getFile().getPath()));
                    } catch (Exception e) {
                        ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
                    }
                }
            }

            @Override
            public void fileDeleted(FileEvent fe) {

            }

            @Override
            public void fileRenamed(FileRenameEvent fe) {

            }

            @Override
            public void fileAttributeChanged(FileAttributeEvent fe) {

            }
        });
    }

    @Override
    public void componentActivated() {
        super.componentActivated();
        SwingUtilities.invokeLater(() -> getEditorPane().addCaretListener(editorListener));
    }

    @Override
    public void componentClosed() {
        getEditorPane().removeCaretListener(editorListener);
        super.componentClosed();
    }
}

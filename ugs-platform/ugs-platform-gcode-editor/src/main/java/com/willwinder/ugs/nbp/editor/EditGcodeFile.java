/**
 * Opens an editor and connects a listener.
 */
/*
    Copywrite 2016 Will Winder

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

import com.willwinder.ugs.nbp.lookup.CentralLookup;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.UGSEvent.FileState;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JEditorPane;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

@ActionID(
        category = "File",
        id = "com.willwinder.ugs.nbp.editor.EditGcodeFile"
)
@ActionRegistration(
        displayName = "#CTL_EditGcodeFile",
        lazy = false
)
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 1301),
    @ActionReference(path="Shortcuts", name="M-E")
})
@Messages("CTL_EditGcodeFile=Edit Gcode File...")
public final class EditGcodeFile extends AbstractAction implements ContextAwareAction, UGSEventListener {
    EditorListener el = new EditorListener();
    BackendAPI backend;
    
    public EditGcodeFile() {
        putValue(Action.NAME, org.openide.util.NbBundle.getMessage(EditGcodeFile.class, "EditGcodeFile.action.name")); // NOI18N

        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        backend.addUGSEventListener(this);
    }

    /**
     * If an editor is open and the file has changed, close the editor and open
     * a new one with the new file.
     */
    @Override
    public void UGSEvent(UGSEvent evt) {
        if (backend == null || backend.getGcodeFile() == null) return;

        if (evt.isFileChangeEvent() && evt.getFileState() == FileState.FILE_LOADING) {
            java.awt.EventQueue.invokeLater(() -> {
                if (getCurrentlyOpenedEditors().isEmpty()) return;

                openFile(backend.getGcodeFile());
            });
        }
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return new EditGcodeFile();
    }

    @Override
    public boolean isEnabled() {
        System.out.println("Is enabled: " + (backend.getGcodeFile() != null));
        return backend.getGcodeFile() != null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (backend == null || backend.getGcodeFile() == null) return;

        openFile(backend.getGcodeFile());
    }

    /**
     * Open an Editor Window in the application, ensuring that only one editor
     * is ever opened at the same time.
     */
    private void openFile(File f) {
        // Close any opened file.
        closeOpenFile();

        try {
            FileObject fo = FileUtil.toFileObject(backend.getGcodeFile());
            DataObject dOb = DataObject.find(fo);
            dOb.getLookup().lookup(OpenCookie.class).open();
            updateListener(true);
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void closeOpenFile() {
        updateListener(false);
        Collection<TopComponent> editors = getCurrentlyOpenedEditors();
        for (TopComponent editor : editors) {
            editor.close();
        }
    }
    
    /**
     * Get all the windows in the "Editor" mode, then filter to just editors.
     */
    private Collection<TopComponent> getCurrentlyOpenedEditors() {
        final ArrayList<TopComponent> result = new ArrayList<>();
        final WindowManager wm = WindowManager.getDefault();
        for (Mode mode : wm.getModes()) {
            if (wm.isEditorMode(mode)) {
                TopComponent[] opened = wm.getOpenedTopComponents(mode);
                for (TopComponent tc : opened) {
                    Node[] arr = tc.getActivatedNodes();
                    for (int j = 0; arr != null && j < arr.length; j++) {
                        EditorCookie ec = (EditorCookie) arr[j].getCookie(EditorCookie.class);
                        if (ec != null) {
                            JEditorPane[] panes = ec.getOpenedPanes();
                            if (panes != null) {
                                result.add(tc);
                                // USE panes
                            }
                        }
                    }

                }
            }
        }
        return result;
    }

    private void updateListener(Boolean enabled) {
        Node[] nodes = TopComponent.getRegistry().getCurrentNodes();
        if (nodes == null) return;
        for (Node n : nodes) {
            EditorCookie ec = (EditorCookie) n.getLookup().lookup(EditorCookie.class);
            if (ec != null) {
                JEditorPane[] panes = ec.getOpenedPanes();
                for (JEditorPane pane : panes) {
                    if (enabled) {
                        pane.addCaretListener(el);
                    } else {
                        pane.removeCaretListener(el);
                    }
                }
            }
        }
    }

}

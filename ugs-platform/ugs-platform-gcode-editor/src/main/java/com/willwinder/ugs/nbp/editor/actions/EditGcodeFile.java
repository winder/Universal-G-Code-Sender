/*
    Copyright 2016-2018 Will Winder

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
package com.willwinder.ugs.nbp.editor.actions;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.UGSEvent.FileState;
import org.apache.commons.lang3.StringUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Opens an editor and connects a listener.
 */
@ActionID(
        category = "File",
        id = "com.willwinder.ugs.nbp.editor.EditGcodeFile"
)
@ActionRegistration(
        displayName = "Edit Gcode File...",
        lazy = false
)
@ActionReferences({
  @ActionReference(path = "Menu/File", position = 1301),
        @ActionReference(path = "Shortcuts", name = "M-E")
})
public final class EditGcodeFile extends AbstractAction implements ContextAwareAction, UGSEventListener {
    private static final Logger LOGGER = Logger.getLogger(EditGcodeFile.class.getSimpleName());
    public static final String NAME = Localization.getString("platform.menu.edit");
    private final BackendAPI backend;

    public EditGcodeFile() {
        putValue(Action.NAME, NAME);

        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        backend.addUGSEventListener(this);
    }

    /**
     * If an editor is open and the file has changed, close the editor and open
     * a new one with the new file.
     */
    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt.isFileChangeEvent() && FileState.FILE_LOADING.equals(evt.getFileState())) {
            if (backend == null || backend.getGcodeFile() == null) return;

            java.awt.EventQueue.invokeLater(() -> {
                // Only open the editor if it has been activated
                if (getCurrentlyOpenedEditors().isEmpty()) {
                    return;
                }

                openFile();
            });
        }
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return new EditGcodeFile();
    }

    @Override
    public boolean isEnabled() {
        return backend.getGcodeFile() != null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (backend == null || backend.getGcodeFile() == null) {
            return;
        }

        openFile();
    }

    /**
     * Open an Editor Window in the application, ensuring that only one editor
     * is ever opened at the same time.
     */
    private void openFile() {
        try {
            FileObject fo = FileUtil.toFileObject(backend.getGcodeFile());
            DataObject dOb = DataObject.find(fo);
            dOb.getLookup().lookup(OpenCookie.class).open();
            java.awt.EventQueue.invokeLater(this::closeOpenFile);
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    private void closeOpenFile() {
        Collection<TopComponent> editors = getCurrentlyOpenedEditors();
        for (TopComponent editor : editors) {
            Optional<String> editorFilename = getEditorFilename(editor);
            String loadedFilename = StringUtils.replace(editorFilename.orElse(""), "\\", "/");
            String newFilename = StringUtils.replace(backend.getGcodeFile().getPath(), "\\", "/");
            if(!loadedFilename.equalsIgnoreCase(newFilename)) {
                LOGGER.info("Closing the previously opened file: " + loadedFilename + " and opens " + backend.getGcodeFile().getPath());
                editor.close();
            }
        }
    }

    private Optional<String> getEditorFilename(TopComponent editor) {
        if (editor.getActivatedNodes().length > 0) {
            return Optional.of(((DataNode) editor.getActivatedNodes()[0]).getDataObject().getPrimaryFile().getPath());
        }
        return Optional.empty();
    }

    /**
     * Get all the windows in the "Editor" mode, then filter to just editors.
     */
    private Collection<TopComponent> getCurrentlyOpenedEditors() {
        final ArrayList<TopComponent> result = new ArrayList<>();
        Collection<TopComponent> comps = TopComponent.getRegistry().getOpened();
        for (TopComponent tc : comps) {
            Node[] arr = tc.getActivatedNodes();
            for (int j = 0; arr != null && j < arr.length; j++) {
                EditorCookie ec = arr[j].getCookie(EditorCookie.class);
                if (ec != null) {
                    result.add(tc);
                }
            }
        }
        return result;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.ugs.nbp.editor;

import com.willwinder.ugs.nbp.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import javax.swing.Action;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
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
        displayName = "#CTL_EditGcodeFile"
)
@ActionReference(path = "Menu/File", position = 1301)
@Messages("CTL_EditGcodeFile=Edit Gcode File...")
public final class EditGcodeFile implements ActionListener {
    private void closeOpenFile() {
        Collection<TopComponent> editors = getCurrentlyOpenedEditors();
        for (TopComponent editor : editors) {
            editor.close();
        }
    }

    private Collection<TopComponent> getCurrentlyOpenedEditors() {
        final ArrayList<TopComponent> result = new ArrayList<>();
        final WindowManager wm = WindowManager.getDefault();
        for (Mode mode : wm.getModes()) {
            if (wm.isEditorMode(mode)) {
                result.addAll(Arrays.asList(wm.getOpenedTopComponents(mode)));
            }
        }
        return result;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        BackendAPI backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        if (backend == null || backend.getGcodeFile() == null) return;

        try {
            // Close any opened file.
            closeOpenFile();
            
            FileObject fo = FileUtil.toFileObject(backend.getGcodeFile());
            DataObject newDo = DataObject.find(fo);
            final Node node = newDo.getNodeDelegate();
            Action a = node.getPreferredAction();
            if (a instanceof ContextAwareAction) {
                a = ((ContextAwareAction) a).createContextAwareInstance(node.getLookup());
            }
            if (a != null) {
                a.actionPerformed(new ActionEvent(node, ActionEvent.ACTION_PERFORMED, "")); // NOI18N
            }
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }

    }
}

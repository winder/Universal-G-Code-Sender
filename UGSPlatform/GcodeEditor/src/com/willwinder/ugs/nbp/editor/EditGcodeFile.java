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
import java.io.File;
import java.io.IOException;
import javax.swing.Action;
import org.openide.*;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.text.DataEditorSupport;
import org.openide.text.DataEditorSupport.Env;
import org.openide.util.*;
import org.openide.util.NbBundle.Messages;

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

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            BackendAPI backend = CentralLookup.getDefault().lookup(BackendAPI.class);
            
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

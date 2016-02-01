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
import javax.swing.JEditorPane;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
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
        displayName = "#CTL_EditGcodeFile"
)
@ActionReference(path = "Menu/File", position = 1301)
@Messages("CTL_EditGcodeFile=Edit Gcode File...")
public final class EditGcodeFile implements ActionListener {
    EditorListener el = new EditorListener();

    private void closeOpenFile() {
        updateListener(false);
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

        /*
        EditorCookie ec = (EditorCookie) n[0].getCookie(EditorCookie.class);

        EditorListener el = new EditorListener();
        dOb.addPropertyChangeListener(el);
        fo.addFileChangeListener(el);
*/
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        BackendAPI backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        if (backend == null || backend.getGcodeFile() == null) return;

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
}

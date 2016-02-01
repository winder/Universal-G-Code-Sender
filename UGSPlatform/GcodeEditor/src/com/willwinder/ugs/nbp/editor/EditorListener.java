/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.ugs.nbp.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JEditorPane;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.Highlighter.Highlight;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;

/**
 *
 * @author wwinder
 */
public class EditorListener implements CaretListener {

    @Override
    public void caretUpdate(CaretEvent e) {
        JEditorPane jep = null;
        if (e.getSource() instanceof JEditorPane) {
            jep = (JEditorPane) e.getSource();
        }
        System.out.println("CARET EVENT: " + e.toString());
        System.out.println("Selected text: '" + jep.getSelectedText() + "'");
        System.out.println("Selection start: " + jep.getSelectionStart());
        System.out.println("Selection end: " + jep.getSelectionEnd());
    }
}

package com.willwinder.universalgcodesender.uielements.components;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.SwingUtilities;

/**
 * Used for focusing a field in an JOptionDialog.
 * 
 * Source from: https://tips4java.wordpress.com/2010/03/14/dialog-focus/
 */
public class RequestFocusListener implements HierarchyListener {

    @Override
    public void hierarchyChanged(HierarchyEvent e) {
        final Component c = e.getComponent();
        if (c.isShowing() && (e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
            Window toplevel = SwingUtilities.getWindowAncestor(c);
            toplevel.addWindowFocusListener(new WindowAdapter() {

                @Override
                public void windowGainedFocus(WindowEvent e) {
                    c.requestFocus();
                }
            });
        }
    }
}
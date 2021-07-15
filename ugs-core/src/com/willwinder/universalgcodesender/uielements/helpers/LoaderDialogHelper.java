/*
    Copyright 2020 Will Winder

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
package com.willwinder.universalgcodesender.uielements.helpers;

import com.willwinder.universalgcodesender.utils.ThreadHelper;
import net.miginfocom.swing.MigLayout;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Window;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;

/**
 * A helper class for displaying a modal loader dialog with a spinner icon and a title.
 *
 * @author Joacim Breiler
 */
public class LoaderDialogHelper {

    private static final String LAYOUT_CONSTRAINTS = "fill, wrap 2, inset 24, gap 6, wmin 120, hmin 64";
    private static JDialog dialog;
    private static long startTime;
    private static long minimumTimeToShow;

    private LoaderDialogHelper() {
    }

    /**
     * Shows a modal loader dialog. To close the dialog, use {@link LoaderDialogHelper#closeDialog()}.
     * If closing the window occurs before the given parameter minimumTimeToShow it will still
     * be visible until that time has passed. The intention is to make sure the user
     * is given time to read the title of the dialog.
     *
     * @param title             the title to display in the dialog
     * @param minimumTimeToShow the minimum time to show the dialog before the
     *                          loader dialog is closed
     */
    public static void showDialog(String title, int minimumTimeToShow) {
        showDialog(title, minimumTimeToShow, new JLabel());
    }

    /**
     * Shows a modal loader dialog with the given component as the parent.
     * To close the dialog, use {@link LoaderDialogHelper#closeDialog()}. If closing
     * the window occurs before the given parameter minimumTimeToShow it will still
     * be visible until that time has passed. The intention is to make sure the user
     * is given time to read the title of the dialog.
     *
     * @param title             the title to display in the dialog
     * @param minimumTimeToShow the minimum time to show the dialog before the
     *                          loader dialog is closed
     * @param parent            the parent component for finding the root window
     */
    public static void showDialog(String title, long minimumTimeToShow, Component parent) {
        LoaderDialogHelper.minimumTimeToShow = minimumTimeToShow;
        if (dialog != null) {
            dialog.dispose();
            dialog = null;
        }

        startTime = System.currentTimeMillis();
        Window window = SwingUtilities.getWindowAncestor(parent);
        dialog = new JDialog(window, title, ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);

        URL loaderUrl = LoaderDialogHelper.class.getResource("/resources/icons/loader.gif");
        JPanel panel = new JPanel(new MigLayout(LAYOUT_CONSTRAINTS));
        panel.add(new JLabel(new ImageIcon(loaderUrl)), "shrink");
        panel.add(new JLabel(title), "grow");

        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(window);

        ThreadHelper.invokeLater(() -> dialog.setVisible(true));
    }

    /**
     * Closes the dialog if open.  If closing the dialog occurs before
     * minimumTimeToShow the dialog will still be visible until that time has passed.
     * <p>
     * Make sure not calling this method in EventQueue thread as it will block until
     * the closing of the dialog is complete.
     */
    public static void closeDialog() {
        long timeToShowDialog = startTime + minimumTimeToShow - System.currentTimeMillis();
        if (timeToShowDialog < 0 || startTime == 0) {
            timeToShowDialog = 0;
        }

        // Closes the
        ScheduledFuture<?> scheduledFuture = ThreadHelper.invokeLater(() -> {
            if (dialog != null) {
                dialog.setVisible(false);
                dialog = null;
                startTime = 0;
            }
        }, timeToShowDialog);

        // Wait for the dialog to close
        try {
            scheduledFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
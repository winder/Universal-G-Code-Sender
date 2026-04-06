/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.universalgcodesender.uielements;

import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.uielements.helpers.ThemeColors;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Window;

/**
 * A utility class for showing modal dialogs with consistent styling and behavior.
 *
 * @author Joacim Breiler
 */
public final class DialogUtils {

    private DialogUtils() {
    }

    /**
     * Shows a modal dialog with a title and content.
     *
     * @param parent  the parent window
     * @param title   the title of the dialog
     * @param content the content of the dialog
     * @return true if the dialog was approved, false otherwise
     */
    public static boolean showModalDialog(Window parent, String title, JPanel content) {
        final boolean[] approved = {false};

        JDialog dialog = new JDialog(parent, title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JButton okButton = new JButton(Localization.getString("mainWindow.swing.okButton"));
        JButton cancelButton = new JButton(Localization.getString("mainWindow.swing.cancelButton"));

        okButton.addActionListener(e -> {
            approved[0] = true;
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new MigLayout("fillx, insets 10 10 10 10",
                "[left][grow][right]",
                "[]"));
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeColors.LIGHT_GREY));
        buttonPanel.add(cancelButton, "cell 0 0, alignx left");
        buttonPanel.add(okButton, "cell 2 0, alignx right");

        dialog.setLayout(new BorderLayout());
        dialog.setMinimumSize(content.getPreferredSize());
        dialog.add(content, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.getRootPane().setDefaultButton(okButton);

        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);

        return approved[0];
    }

    public static Window getParentWindow(Object component) {
        if (component instanceof java.awt.Component awtComponent) {
            return SwingUtilities.getWindowAncestor(awtComponent);
        }
        return null;
    }
}
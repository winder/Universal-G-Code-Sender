/*
    Copyright 2026 Damian Nikodem

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
package com.willwinder.ugs.designer.print;

import com.willwinder.universalgcodesender.i18n.Localization;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.print.PrinterJob;

/**
 * Modal dialog that previews a print job before it's dispatched to the printer. The user can
 * review each page, disable unwanted pages, shift the overall layout, and toggle the engineering
 * drawing template.
 *
 * @author Damian Nikodem
 */
public class PrintPreviewDialog extends JDialog {

    private final PrintPreviewPanel previewPanel;
    private boolean confirmed;

    public PrintPreviewDialog(Component parent, DesignPrintable printable, PrinterJob printerJob) {
        super(SwingUtilities.getWindowAncestor(parent), Localization.getString("designer.print.preview.title"),
                ModalityType.APPLICATION_MODAL);
        setLayout(new MigLayout("fill, insets 4", "[grow]", "[grow][]"));

        previewPanel = new PrintPreviewPanel(printable, printerJob);
        add(previewPanel, "grow, wrap");

        JPanel buttons = new JPanel(new MigLayout("insets 4", "[grow][][]"));
        JButton cancel = new JButton(Localization.getString("designer.print.preview.cancel"));
        cancel.addActionListener(e -> {
            confirmed = false;
            setVisible(false);
            dispose();
        });
        JButton print = new JButton(Localization.getString("designer.print.preview.print"));
        print.addActionListener(e -> {
            confirmed = true;
            setVisible(false);
            dispose();
        });
        buttons.add(new JPanel(), "growx");
        buttons.add(cancel);
        buttons.add(print);
        add(buttons, "growx");

        getRootPane().setDefaultButton(print);
        pack();
        setLocationRelativeTo(SwingUtilities.getWindowAncestor(parent));
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}

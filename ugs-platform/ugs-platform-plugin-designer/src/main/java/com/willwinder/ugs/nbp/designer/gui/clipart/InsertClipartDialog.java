/*
    Copyright 2022 Will Winder

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
package com.willwinder.ugs.nbp.designer.gui.clipart;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * A dialog that presents all clipart categories from multiple sources
 * and lists them with a preview.
 * <p>
 * The dialog will be blocking and when disposed will contain a selected clipart (or an empty optional)
 *
 * @author Joacim Breiler
 */
public class InsertClipartDialog extends JDialog implements ListSelectionListener, ActionListener {
    private static final Logger LOGGER = Logger.getLogger(InsertClipartDialog.class.getSimpleName());
    private final PreviewListPanel shapePreviewListPanel;
    private final JList<Category> categoriesList;

    public InsertClipartDialog() {
        super((JFrame) null, true);
        setTitle("Insert clipart");
        setPreferredSize(new Dimension(700, 480));
        setLayout(new BorderLayout());

        categoriesList = new JList<>(Category.values());
        categoriesList.addListSelectionListener(this);
        categoriesList.setCellRenderer(new CategoryCellRenderer());
        shapePreviewListPanel = new PreviewListPanel(this);

        JSplitPane jSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, new JScrollPane(categoriesList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), shapePreviewListPanel);
        add(jSplitPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new MigLayout("insets 0", "[right, grow]"));
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        setResizable(true);
        pack();
    }

    public static void main(String[] args) {
        InsertClipartDialog insertShapeDialog = new InsertClipartDialog();
        insertShapeDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        Optional<Clipart> clipart = insertShapeDialog.showDialog();
        if (clipart.isPresent()) {
            LOGGER.info(() -> "Selected: " + clipart);
        } else {
            LOGGER.info("Cancelled");
        }
    }

    public Optional<Clipart> showDialog() {
        setVisible(true);
        dispose();
        return shapePreviewListPanel.getSelectedClipart();
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        shapePreviewListPanel.setCategory(categoriesList.getSelectedValue());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        dispose();
    }
}

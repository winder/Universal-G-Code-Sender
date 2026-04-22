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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;

/**
 * Main content of the print preview dialog. Shows offset / fit-to-page / template controls plus
 * a grid of per-page thumbnails, and keeps the enablement state of those controls in sync with
 * the current {@link PrintConfig} and page count.
 *
 * @author Damian Nikodem
 */
public class PrintPreviewPanel extends JPanel {

    private final DesignPrintable printable;
    private final PrintConfig config;
    private final PrinterJob printerJob;

    private JSpinner offsetXSpinner;
    private JSpinner offsetYSpinner;
    private JTextField authorField;
    private JCheckBox fitToPageCheckBox;
    private JCheckBox includeTemplateCheckBox;
    private JButton pageSetupButton;
    private JPanel tilesPanel;
    private JScrollPane tilesScrollPane;

    public PrintPreviewPanel(DesignPrintable printable, PrinterJob printerJob) {
        this.printable = printable;
        this.config = printable.getConfig();
        this.printerJob = printerJob;

        setLayout(new MigLayout("fill, insets 6", "[grow]", "[][grow]"));
        setPreferredSize(new Dimension(900, 700));

        add(buildTopStrip(), "growx, wrap");

        tilesPanel = new JPanel();
        tilesScrollPane = new JScrollPane(tilesPanel);
        tilesScrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
        add(tilesScrollPane, "grow");

        rebuildTiles();
        updateControlEnablement();
    }

    private JPanel buildTopStrip() {
        JPanel panel = new JPanel(new MigLayout("insets 4", "[][][][][][][grow][]", ""));

        pageSetupButton = new JButton(Localization.getString("designer.print.preview.page-setup"));
        pageSetupButton.addActionListener(e -> onPageSetup());
        panel.add(pageSetupButton);

        panel.add(new JLabel(Localization.getString("designer.print.preview.offset.x")));
        offsetXSpinner = new JSpinner(new SpinnerNumberModel(config.getOffsetMmX(), -1000.0, 1000.0, 0.5));
        ((JSpinner.DefaultEditor) offsetXSpinner.getEditor()).getTextField().setColumns(5);
        offsetXSpinner.addChangeListener(e -> {
            config.setOffsetMmX(((Number) offsetXSpinner.getValue()).doubleValue());
            repaintTiles();
        });
        panel.add(offsetXSpinner);

        panel.add(new JLabel(Localization.getString("designer.print.preview.offset.y")));
        offsetYSpinner = new JSpinner(new SpinnerNumberModel(config.getOffsetMmY(), -1000.0, 1000.0, 0.5));
        ((JSpinner.DefaultEditor) offsetYSpinner.getEditor()).getTextField().setColumns(5);
        offsetYSpinner.addChangeListener(e -> {
            config.setOffsetMmY(((Number) offsetYSpinner.getValue()).doubleValue());
            repaintTiles();
        });
        panel.add(offsetYSpinner);

        panel.add(new JLabel(Localization.getString("designer.print.preview.author")));
        authorField = new JTextField(config.getAuthor(), 14);
        authorField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { sync(); }
            @Override public void removeUpdate(DocumentEvent e) { sync(); }
            @Override public void changedUpdate(DocumentEvent e) { sync(); }
            private void sync() {
                config.setAuthor(authorField.getText());
                repaintTiles();
            }
        });
        panel.add(authorField, "growx");

        JPanel checkBoxes = new JPanel(new MigLayout("insets 0"));
        fitToPageCheckBox = new JCheckBox(Localization.getString("designer.print.preview.fit-to-page"));
        fitToPageCheckBox.setSelected(config.isFitToPage());
        fitToPageCheckBox.addActionListener(e -> {
            config.setFitToPage(fitToPageCheckBox.isSelected());
            rebuildTiles();
            updateControlEnablement();
        });
        checkBoxes.add(fitToPageCheckBox, "wrap");

        includeTemplateCheckBox = new JCheckBox(Localization.getString("designer.print.preview.include-template"));
        includeTemplateCheckBox.setSelected(config.isIncludeTemplate());
        includeTemplateCheckBox.addActionListener(e -> {
            config.setIncludeTemplate(includeTemplateCheckBox.isSelected());
            repaintTiles();
        });
        checkBoxes.add(includeTemplateCheckBox);
        panel.add(checkBoxes, "wrap");

        return panel;
    }

    private void onPageSetup() {
        PageFormat current = config.getPageFormat();
        PageFormat updated = printerJob.pageDialog(current != null ? current : printerJob.defaultPage());
        if (updated != null) {
            config.setPageFormat(updated);
            rebuildTiles();
            updateControlEnablement();
        }
    }

    private void rebuildTiles() {
        tilesPanel.removeAll();
        int xCount;
        int yCount;
        if (config.isFitToPage()) {
            xCount = 1;
            yCount = 1;
        } else {
            xCount = Math.max(1, config.getXPageCount());
            yCount = Math.max(1, config.getYPageCount());
        }
        tilesPanel.setLayout(new GridLayout(yCount, xCount, 8, 8));
        int total = xCount * yCount;
        for (int i = 0; i < total; i++) {
            PrintPagePreviewTile tile = new PrintPagePreviewTile(
                    printable, i, config.isFitToPage(), this::repaintTiles);
            tilesPanel.add(tile);
        }
        tilesPanel.revalidate();
        tilesPanel.repaint();
    }

    private void repaintTiles() {
        tilesPanel.repaint();
    }

    private void updateControlEnablement() {
        boolean fitToPage = config.isFitToPage();
        boolean fitsOnePage = config.designFitsOnePage();

        offsetXSpinner.setEnabled(!fitToPage);
        offsetYSpinner.setEnabled(!fitToPage);

        if (fitToPage) {
            includeTemplateCheckBox.setEnabled(false);
            includeTemplateCheckBox.setToolTipText(Localization.getString("designer.print.preview.include-template.tooltip.fit-to-page"));
        } else if (!fitsOnePage) {
            includeTemplateCheckBox.setEnabled(false);
            includeTemplateCheckBox.setToolTipText(Localization.getString("designer.print.preview.include-template.tooltip.multipage"));
        } else {
            includeTemplateCheckBox.setEnabled(true);
            includeTemplateCheckBox.setToolTipText(null);
        }
    }
}

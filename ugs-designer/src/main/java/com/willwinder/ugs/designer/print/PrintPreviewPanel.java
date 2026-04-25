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
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterJob;

/**
 * Main content of the print preview dialog. Shows offset / fit-to-page / template controls plus
 * a grid of per-page thumbnails, and keeps the enablement state of those controls in sync with
 * the current {@link PrintConfig} and page count.
 *
 * @author Damian Nikodem
 */
public class PrintPreviewPanel extends JPanel {

    private static final String ORIENTATION_PORTRAIT_KEY = "designer.print.preview.orientation.portrait";
    private static final String ORIENTATION_LANDSCAPE_KEY = "designer.print.preview.orientation.landscape";
    private static final String[] SCALE_PRESETS = {"1:1", "1:1.25", "1:1.5", "1:2", "1:4"};

    private final DesignPrintable printable;
    private final PrintConfig config;
    private final PrinterJob printerJob;

    private JComboBox<PaperSize> paperCombo;
    private JComboBox<String> orientationCombo;
    private JSpinner offsetXSpinner;
    private JSpinner offsetYSpinner;
    private JTextField authorField;
    private JCheckBox fitToPageCheckBox;
    private JCheckBox includeTemplateCheckBox;
    private JButton pageSetupButton;
    private JComboBox<String> scaleCombo;
    private JTextField scaleEditor;
    private Color scaleEditorDefaultColor;
    private boolean suppressScaleFeedback;
    private JPanel tilesPanel;
    private final java.util.List<PrintPagePreviewTile> tiles = new java.util.ArrayList<>();

    public PrintPreviewPanel(DesignPrintable printable, PrinterJob printerJob) {
        this.printable = printable;
        this.config = printable.getConfig();
        this.printerJob = printerJob;

        setLayout(new MigLayout("fill, insets 6", "[grow]", "[][grow]"));
        setPreferredSize(new Dimension(900, 700));

        add(buildTopStrip(), "growx, wrap");

        tilesPanel = new JPanel();
        JScrollPane tilesScrollPane = new JScrollPane(tilesPanel);
        tilesScrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
        add(tilesScrollPane, "grow");

        rebuildTiles();
        updateControlEnablement();
    }

    private JPanel buildTopStrip() {
        JPanel panel = new JPanel(new MigLayout("insets 4, wrap 8",
                "[][][][grow][][][][grow]", ""));

        panel.add(new JLabel(Localization.getString("designer.print.preview.paper")));
        paperCombo = new JComboBox<>(PaperSize.values());
        paperCombo.setSelectedItem(PaperSize.findClosestMatch(
                config.getPageFormat() != null ? config.getPageFormat().getPaper() : null));
        paperCombo.addActionListener(e -> onPaperChanged());
        panel.add(paperCombo);

        panel.add(new JLabel(Localization.getString("designer.print.preview.orientation")));
        String portrait = Localization.getString(ORIENTATION_PORTRAIT_KEY);
        String landscape = Localization.getString(ORIENTATION_LANDSCAPE_KEY);
        orientationCombo = new JComboBox<>(new String[]{portrait, landscape});
        orientationCombo.setSelectedItem(
                config.getPageFormat() != null && config.getPageFormat().getOrientation() == PageFormat.PORTRAIT
                        ? portrait : landscape);
        orientationCombo.addActionListener(e -> onOrientationChanged());
        panel.add(orientationCombo);

        pageSetupButton = new JButton(Localization.getString("designer.print.preview.page-setup"));
        pageSetupButton.addActionListener(e -> onPageSetup());
        panel.add(pageSetupButton);

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
        panel.add(authorField, "span 2, growx");

        // Row 2: offsets + scale + toggles
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
        panel.add(offsetYSpinner, "growx");

        panel.add(new JLabel(Localization.getString("designer.print.preview.scale")));
        panel.add(buildScaleCombo(), "growx");

        fitToPageCheckBox = new JCheckBox(Localization.getString("designer.print.preview.fit-to-page"));
        fitToPageCheckBox.setSelected(config.isFitToPage());
        fitToPageCheckBox.addActionListener(e -> onFitToPageToggled());
        panel.add(fitToPageCheckBox);

        includeTemplateCheckBox = new JCheckBox(Localization.getString("designer.print.preview.include-template"));
        includeTemplateCheckBox.setSelected(config.isIncludeTemplate());
        includeTemplateCheckBox.addActionListener(e -> onIncludeTemplateToggled());
        panel.add(includeTemplateCheckBox);

        return panel;
    }

    private JComboBox<String> buildScaleCombo() {
        scaleCombo = new JComboBox<>(SCALE_PRESETS);
        scaleCombo.setEditable(true);
        scaleEditor = (JTextField) scaleCombo.getEditor().getEditorComponent();
        scaleEditor.setColumns(7);
        scaleEditor.setText(PrintConfig.formatScale(config.getScaleRatio()));
        scaleEditorDefaultColor = scaleEditor.getForeground();

        scaleEditor.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { onScaleEditorChanged(); }
            @Override public void removeUpdate(DocumentEvent e) { onScaleEditorChanged(); }
            @Override public void changedUpdate(DocumentEvent e) { onScaleEditorChanged(); }
        });
        scaleEditor.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                commitScaleEditor();
            }
        });
        // Dropdown selection and Enter both fire ActionEvent — treat them as a commit.
        scaleCombo.addActionListener(e -> {
            if (!suppressScaleFeedback) {
                commitScaleEditor();
            }
        });
        return scaleCombo;
    }

    private void onScaleEditorChanged() {
        if (suppressScaleFeedback) {
            return;
        }
        Double ratio = ScaleParser.parse(scaleEditor.getText());
        if (ratio == null) {
            scaleEditor.setForeground(Color.RED);
            return;
        }
        scaleEditor.setForeground(scaleEditorDefaultColor);
        boolean fitToPageWasOn = config.isFitToPage();
        if (fitToPageWasOn) {
            config.setFitToPage(false);
            fitToPageCheckBox.setSelected(false);
        }
        config.setScaleRatio(ratio);
        rebuildTiles();
        updateControlEnablement();
    }

    private void commitScaleEditor() {
        Double ratio = ScaleParser.parse(scaleEditor.getText());
        if (ratio == null) {
            // Invalid → reset to 1.0.
            config.setScaleRatio(1.0);
            setScaleEditorText("1:1");
            scaleEditor.setForeground(scaleEditorDefaultColor);
            rebuildTiles();
            updateControlEnablement();
            return;
        }
        config.setScaleRatio(ratio);
        setScaleEditorText(PrintConfig.formatScale(ratio));
        scaleEditor.setForeground(scaleEditorDefaultColor);
    }

    /** Programmatically updates the scale editor text without re-triggering the live listener. */
    private void setScaleEditorText(String text) {
        suppressScaleFeedback = true;
        try {
            scaleEditor.setText(text);
        } finally {
            suppressScaleFeedback = false;
        }
    }

    private void onPaperChanged() {
        PaperSize selected = (PaperSize) paperCombo.getSelectedItem();
        if (selected == null) {
            return;
        }
        PageFormat pageFormat = config.getPageFormat() != null
                ? (PageFormat) config.getPageFormat().clone() : new PageFormat();
        Paper paper = selected.toPaper();
        pageFormat.setPaper(paper);
        config.setPageFormat(pageFormat);
        if (config.isFitToPage()) {
            applyFitToPageScale();
        }
        rebuildTiles();
        updateControlEnablement();
    }

    private void onOrientationChanged() {
        String selected = (String) orientationCombo.getSelectedItem();
        int orientation = Localization.getString(ORIENTATION_PORTRAIT_KEY).equals(selected)
                ? PageFormat.PORTRAIT : PageFormat.LANDSCAPE;
        PageFormat pageFormat = config.getPageFormat() != null
                ? (PageFormat) config.getPageFormat().clone() : new PageFormat();
        pageFormat.setOrientation(orientation);
        config.setPageFormat(pageFormat);
        if (config.isFitToPage()) {
            applyFitToPageScale();
        }
        rebuildTiles();
        updateControlEnablement();
    }

    private void onPageSetup() {
        PageFormat current = config.getPageFormat();
        PageFormat updated = printerJob.pageDialog(current != null ? current : printerJob.defaultPage());
        if (updated != null) {
            config.setPageFormat(updated);
            paperCombo.setSelectedItem(PaperSize.findClosestMatch(updated.getPaper()));
            orientationCombo.setSelectedItem(Localization.getString(
                    updated.getOrientation() == PageFormat.PORTRAIT
                            ? ORIENTATION_PORTRAIT_KEY : ORIENTATION_LANDSCAPE_KEY));
            if (config.isFitToPage()) {
                applyFitToPageScale();
            }
            rebuildTiles();
            updateControlEnablement();
        }
    }

    private void onFitToPageToggled() {
        if (fitToPageCheckBox.isSelected()) {
            config.setFitToPage(true);
            applyFitToPageScale();
        } else {
            config.setFitToPage(false);
            config.setScaleRatio(1.0);
            setScaleEditorText("1:1");
        }
        rebuildTiles();
        updateControlEnablement();
    }

    private void onIncludeTemplateToggled() {
        config.setIncludeTemplate(includeTemplateCheckBox.isSelected());
        if (config.isFitToPage()) {
            // Available area changes depending on whether template is drawn.
            applyFitToPageScale();
            rebuildTiles();
        } else {
            repaintTiles();
        }
    }

    /** Recomputes the fit-to-page scale, writes it to the config, and syncs the editor. */
    private void applyFitToPageScale() {
        double scale = DesignPrintable.computeFitScale(
                printable.getSourceImage(),
                config.getPageFormat(),
                shouldTemplateBeDrawnInFitMode());
        config.setScaleRatio(scale);
        setScaleEditorText(PrintConfig.formatScale(scale));
        scaleEditor.setForeground(scaleEditorDefaultColor);
    }

    private boolean shouldTemplateBeDrawnInFitMode() {
        // Mirrors DesignPrintable.shouldDrawTemplate() when fitToPage is true.
        return config.isIncludeTemplate();
    }

    private void rebuildTiles() {
        tilesPanel.removeAll();
        tiles.clear();
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
                    printable, i, config.isFitToPage(), this::onTileDisableChanged);
            tiles.add(tile);
            tilesPanel.add(tile);
        }
        tilesPanel.revalidate();
        tilesPanel.repaint();
        refreshTileDisableStates();
    }

    private void onTileDisableChanged() {
        refreshTileDisableStates();
        tilesPanel.repaint();
    }

    private void refreshTileDisableStates() {
        for (PrintPagePreviewTile tile : tiles) {
            tile.refreshDisableState();
        }
    }

    private void repaintTiles() {
        tilesPanel.repaint();
    }

    /**
     * The "Include engineering template" checkbox is enabled whenever the template could actually
     * be drawn — that is, on any single-page layout (fit-to-page or a natural single-page design).
     * In multi-page layouts the checkbox is disabled because the template only makes sense when
     * the whole drawing sits on one sheet.
     */
    private void updateControlEnablement() {
        boolean fitToPage = config.isFitToPage();
        boolean singlePage = fitToPage || config.designFitsOnePage();

        offsetXSpinner.setEnabled(!fitToPage);
        offsetYSpinner.setEnabled(!fitToPage);

        includeTemplateCheckBox.setEnabled(singlePage);
        includeTemplateCheckBox.setToolTipText(singlePage ? null
                : Localization.getString("designer.print.preview.include-template.tooltip.multipage"));
    }
}

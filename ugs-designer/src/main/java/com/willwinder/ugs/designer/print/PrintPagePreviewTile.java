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

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.print.PageFormat;

/**
 * Renders a single page of the print preview: a paper-shaped panel showing the tile or fit-to-page
 * rendering, a label ("Page N"), and optionally a "disable" checkbox.
 *
 * @author Damian Nikodem
 */
public class PrintPagePreviewTile extends JPanel {

    private final DesignPrintable printable;
    private final int gridIndex;
    private final boolean isFitToPage;
    private final Runnable onDisableChanged;
    private JCheckBox disableCheckBox;

    public PrintPagePreviewTile(DesignPrintable printable, int gridIndex, boolean isFitToPage, Runnable onDisableChanged) {
        this.printable = printable;
        this.gridIndex = gridIndex;
        this.isFitToPage = isFitToPage;
        this.onDisableChanged = onDisableChanged;
        setLayout(new BorderLayout());
        setBackground(new Color(0xE8E8E8));
        setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        int displayIndex = gridIndex + 1;
        JLabel label = new JLabel(
                Localization.getString("designer.print.preview.page").replace("{0}", String.valueOf(displayIndex)),
                SwingConstants.LEFT);
        header.add(label, BorderLayout.WEST);

        if (!isFitToPage) {
            disableCheckBox = new JCheckBox(Localization.getString("designer.print.page.disable"));
            disableCheckBox.setOpaque(false);
            disableCheckBox.setSelected(printable.getConfig().isPageDisabled(gridIndex));
            disableCheckBox.addActionListener(e -> {
                printable.getConfig().setPageDisabled(gridIndex, disableCheckBox.isSelected());
                repaint();
                if (onDisableChanged != null) {
                    onDisableChanged.run();
                }
            });
            header.add(disableCheckBox, BorderLayout.EAST);
        }
        add(header, BorderLayout.NORTH);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            PageFormat pf = printable.getConfig().getPageFormat();
            if (pf == null) {
                return;
            }

            int headerHeight = getLayout() instanceof BorderLayout
                    ? (getComponentCount() > 0 ? getComponent(0).getHeight() : 0)
                    : 0;
            int paperAreaX = 8;
            int paperAreaY = headerHeight + 8;
            int paperAreaW = getWidth() - 16;
            int paperAreaH = getHeight() - headerHeight - 16;
            if (paperAreaW <= 0 || paperAreaH <= 0) {
                return;
            }

            double pageAspect = pf.getWidth() / pf.getHeight();
            double areaAspect = (double) paperAreaW / paperAreaH;
            int paperW;
            int paperH;
            if (areaAspect > pageAspect) {
                paperH = paperAreaH;
                paperW = (int) (paperH * pageAspect);
            } else {
                paperW = paperAreaW;
                paperH = (int) (paperW / pageAspect);
            }
            int paperX = paperAreaX + (paperAreaW - paperW) / 2;
            int paperY = paperAreaY + (paperAreaH - paperH) / 2;

            // Paper
            g2.setColor(Color.WHITE);
            g2.fillRect(paperX, paperY, paperW, paperH);
            g2.setColor(new Color(0x999999));
            g2.drawRect(paperX, paperY, paperW, paperH);

            if (printable.getConfig().isPageDisabled(gridIndex) && !isFitToPage) {
                // Draw an X across a disabled page.
                g2.setColor(new Color(200, 80, 80));
                g2.drawLine(paperX, paperY, paperX + paperW, paperY + paperH);
                g2.drawLine(paperX + paperW, paperY, paperX, paperY + paperH);
                return;
            }

            double scale = paperW / pf.getWidth();
            Graphics2D pageG = (Graphics2D) g2.create(paperX, paperY, paperW, paperH);
            try {
                pageG.scale(scale, scale);
                if (isFitToPage) {
                    printable.renderFitToPage(pageG, pf);
                } else {
                    printable.renderTile(pageG, pf, gridIndex);
                }
            } catch (RuntimeException ignored) {
                // Preview failures should not crash the UI.
            } finally {
                pageG.dispose();
            }
        } finally {
            g2.dispose();
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(180, 240);
    }
}

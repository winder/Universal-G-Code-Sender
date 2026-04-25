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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.time.format.DateTimeFormatter;

/**
 * Draws an "engineering drawing" title block border around a printed page. Coordinates are in
 * PageFormat imageable units (1/72 inch). The Graphics2D passed in must already be translated so
 * that (0,0) is the top-left corner of the imageable area.
 *
 * @author Damian Nikodem
 */
public class EngineeringTemplateRenderer {

    static final double BORDER_INSET_PT = 10.0;
    static final double TITLE_BLOCK_HEIGHT_PT = 90.0;
    private static final double ROW_HEIGHT_PT = TITLE_BLOCK_HEIGHT_PT / 2.0;
    private static final double CELL_LABEL_INSET_PT = 3.0;

    private static final Stroke BORDER_STROKE = new BasicStroke(1.5f);
    private static final Stroke DIVIDER_STROKE = new BasicStroke(1.0f);
    private static final Font LABEL_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 7);
    private static final Font VALUE_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 11);

    public void render(Graphics2D g, PageFormat pageFormat, PrintConfig config) {
        Stroke oldStroke = g.getStroke();
        Color oldColor = g.getColor();
        Font oldFont = g.getFont();

        try {
            g.setColor(Color.BLACK);

            double iw = pageFormat.getImageableWidth();
            double ih = pageFormat.getImageableHeight();
            double innerX = BORDER_INSET_PT;
            double innerY = BORDER_INSET_PT;
            double innerW = iw - 2 * BORDER_INSET_PT;
            double innerH = ih - 2 * BORDER_INSET_PT;
            if (innerW <= 0 || innerH <= 0) {
                return;
            }

            // Mask the strip between the imageable edge and the template's inner border so any
            // grid (or other) content rendered onto the source image doesn't bleed past the
            // template boundary.
            g.setColor(Color.WHITE);
            g.fill(new Rectangle2D.Double(0, 0, iw, innerY));
            g.fill(new Rectangle2D.Double(0, innerY + innerH, iw, ih - innerY - innerH));
            g.fill(new Rectangle2D.Double(0, innerY, innerX, innerH));
            g.fill(new Rectangle2D.Double(innerX + innerW, innerY, iw - innerX - innerW, innerH));
            g.setColor(Color.BLACK);

            g.setStroke(BORDER_STROKE);
            g.draw(new Rectangle2D.Double(innerX, innerY, innerW, innerH));

            double titleTop = innerY + innerH - TITLE_BLOCK_HEIGHT_PT;
            if (titleTop <= innerY) {
                return; // page too small for a title block — just draw the border
            }

            // Mask out the grid (or anything else) underneath the title block so the labels
            // remain readable on top of dense content.
            g.setColor(Color.WHITE);
            g.fill(new Rectangle2D.Double(innerX, titleTop, innerW, TITLE_BLOCK_HEIGHT_PT));
            g.setColor(Color.BLACK);

            g.setStroke(DIVIDER_STROKE);
            g.draw(new Line2D.Double(innerX, titleTop, innerX + innerW, titleTop));
            // Row divider
            double rowDividerY = titleTop + ROW_HEIGHT_PT;
            g.draw(new Line2D.Double(innerX, rowDividerY, innerX + innerW, rowDividerY));

            // Top row: FILE (0-60%), AUTHOR (60-100%)
            double topSplit = innerX + innerW * 0.60;
            g.draw(new Line2D.Double(topSplit, titleTop, topSplit, rowDividerY));

            drawCell(g, innerX, titleTop, topSplit - innerX, ROW_HEIGHT_PT,
                    Localization.getString("designer.print.template.file"),
                    config.getFilename().isEmpty() ? "—" : config.getFilename());
            drawCell(g, topSplit, titleTop, innerX + innerW - topSplit, ROW_HEIGHT_PT,
                    Localization.getString("designer.print.template.author"),
                    config.getAuthor().isEmpty() ? "—" : config.getAuthor());

            // Bottom row: SCALE (0-33%), DIMENSIONS (33-66%), DATE (66-100%)
            double cell1 = innerX + innerW / 3.0;
            double cell2 = innerX + innerW * 2.0 / 3.0;
            g.draw(new Line2D.Double(cell1, rowDividerY, cell1, innerY + innerH));
            g.draw(new Line2D.Double(cell2, rowDividerY, cell2, innerY + innerH));

            drawCell(g, innerX, rowDividerY, cell1 - innerX, ROW_HEIGHT_PT,
                    Localization.getString("designer.print.template.scale"),
                    config.formatScale());
            drawCell(g, cell1, rowDividerY, cell2 - cell1, ROW_HEIGHT_PT,
                    Localization.getString("designer.print.template.dimensions"),
                    config.formatDimensions());
            drawCell(g, cell2, rowDividerY, innerX + innerW - cell2, ROW_HEIGHT_PT,
                    Localization.getString("designer.print.template.date"),
                    config.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        } finally {
            g.setStroke(oldStroke);
            g.setColor(oldColor);
            g.setFont(oldFont);
        }
    }

    private void drawCell(Graphics2D g, double x, double y, double w, double h, String label, String value) {
        g.setFont(LABEL_FONT);
        FontMetrics labelMetrics = g.getFontMetrics();
        g.drawString(label, (float) (x + CELL_LABEL_INSET_PT),
                (float) (y + CELL_LABEL_INSET_PT + labelMetrics.getAscent()));

        g.setFont(VALUE_FONT);
        FontMetrics valueMetrics = g.getFontMetrics();
        String truncated = truncateToWidth(valueMetrics, value, w - 2 * CELL_LABEL_INSET_PT);
        float valueX = (float) (x + (w - valueMetrics.stringWidth(truncated)) / 2.0);
        float valueY = (float) (y + h * 0.70 + valueMetrics.getAscent() / 2.0);
        g.drawString(truncated, valueX, valueY);
    }

    private String truncateToWidth(FontMetrics fm, String text, double maxWidth) {
        if (text == null || text.isEmpty() || fm.stringWidth(text) <= maxWidth) {
            return text == null ? "" : text;
        }
        String ellipsis = "…";
        int ellipsisWidth = fm.stringWidth(ellipsis);
        int end = text.length();
        while (end > 0 && fm.stringWidth(text.substring(0, end)) + ellipsisWidth > maxWidth) {
            end--;
        }
        return text.substring(0, end) + ellipsis;
    }
}

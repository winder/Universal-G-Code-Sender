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

import java.awt.print.Paper;

/**
 * Common paper sizes offered in the print preview dropdown. Dimensions are always stored in the
 * portrait orientation (short edge = width); orientation is handled separately by the
 * {@link java.awt.print.PageFormat} wrapper.
 *
 * @author Damian Nikodem
 */
public enum PaperSize {
    LETTER("Letter", 8.5, 11.0, Unit.INCH),
    LEGAL("Legal", 8.5, 14.0, Unit.INCH),
    TABLOID("Tabloid (11×17)", 11.0, 17.0, Unit.INCH),
    A3("A3", 297.0, 420.0, Unit.MM),
    A4("A4", 210.0, 297.0, Unit.MM),
    A5("A5", 148.0, 210.0, Unit.MM);

    private static final double POINTS_PER_INCH = 72.0;
    private static final double MM_PER_INCH = 25.4;
    private static final double DEFAULT_MARGIN_PT = 0.5 * POINTS_PER_INCH;

    private final String displayName;
    private final double widthPt;
    private final double heightPt;

    PaperSize(String displayName, double width, double height, Unit unit) {
        this.displayName = displayName;
        this.widthPt = unit.toPoints(width);
        this.heightPt = unit.toPoints(height);
    }

    public double getWidthPt() {
        return widthPt;
    }

    public double getHeightPt() {
        return heightPt;
    }

    /** Returns a new {@link Paper} of this size with a 0.5 inch imageable-area margin. */
    public Paper toPaper() {
        Paper paper = new Paper();
        paper.setSize(widthPt, heightPt);
        paper.setImageableArea(DEFAULT_MARGIN_PT, DEFAULT_MARGIN_PT,
                widthPt - 2 * DEFAULT_MARGIN_PT, heightPt - 2 * DEFAULT_MARGIN_PT);
        return paper;
    }

    /**
     * Best-effort match of an existing {@link Paper} to one of these sizes. Compares either
     * portrait or landscape dimensions to accommodate whichever orientation the PageFormat holds.
     */
    public static PaperSize findClosestMatch(Paper paper) {
        if (paper == null) {
            return LETTER;
        }
        double w = paper.getWidth();
        double h = paper.getHeight();
        PaperSize best = LETTER;
        double bestDelta = Double.MAX_VALUE;
        for (PaperSize size : values()) {
            double d1 = Math.abs(size.widthPt - w) + Math.abs(size.heightPt - h);
            double d2 = Math.abs(size.widthPt - h) + Math.abs(size.heightPt - w);
            double d = Math.min(d1, d2);
            if (d < bestDelta) {
                bestDelta = d;
                best = size;
            }
        }
        return best;
    }

    @Override
    public String toString() {
        return displayName;
    }

    private enum Unit {
        INCH, MM;

        double toPoints(double value) {
            return this == INCH ? value * POINTS_PER_INCH : value * POINTS_PER_INCH / MM_PER_INCH;
        }
    }
}

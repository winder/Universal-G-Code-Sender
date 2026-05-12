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

import com.willwinder.universalgcodesender.model.UnitUtils;

import java.awt.print.PageFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Mutable configuration for a print job, shared between the preview dialog and the {@link DesignPrintable}
 * that actually renders each page. All geometric fields are captured in the same units the designer uses
 * elsewhere — mm for design-space measurements, points (1/72") for paper-space measurements.
 *
 * @author Damian Nikodem
 */
public class PrintConfig {

    /** Matches the scale used by {@code PrintDesignAction} when it asks {@code Drawing} for a BufferedImage. */
    private static final double MM_PER_INCH = 25.4;
    private static final double POINTS_PER_INCH = 72.0;

    private PageFormat pageFormat;
    private double offsetMmX;
    private double offsetMmY;
    private final Set<Integer> disabledPages = new HashSet<>();
    private boolean fitToPage;
    private boolean includeTemplate = true;

    private String filename = "";
    private String author = "";
    private LocalDate date = LocalDate.now();
    private UnitUtils.Units dimensionsUnits = UnitUtils.Units.MM;
    private double designWidthMm;
    private double designHeightMm;
    private double scaleRatio = 1.0;

    private int sourceImageWidth;
    private int sourceImageHeight;

    public PageFormat getPageFormat() {
        return pageFormat;
    }

    public void setPageFormat(PageFormat pageFormat) {
        this.pageFormat = pageFormat;
    }

    public double getOffsetMmX() {
        return offsetMmX;
    }

    public void setOffsetMmX(double offsetMmX) {
        this.offsetMmX = offsetMmX;
    }

    public double getOffsetMmY() {
        return offsetMmY;
    }

    public void setOffsetMmY(double offsetMmY) {
        this.offsetMmY = offsetMmY;
    }

    public Set<Integer> getDisabledPages() {
        return disabledPages;
    }

    public boolean isPageDisabled(int gridIndex) {
        return disabledPages.contains(gridIndex);
    }

    public void setPageDisabled(int gridIndex, boolean disabled) {
        if (disabled) {
            disabledPages.add(gridIndex);
        } else {
            disabledPages.remove(gridIndex);
        }
    }

    public boolean isFitToPage() {
        return fitToPage;
    }

    public void setFitToPage(boolean fitToPage) {
        this.fitToPage = fitToPage;
    }

    public boolean isIncludeTemplate() {
        return includeTemplate;
    }

    public void setIncludeTemplate(boolean includeTemplate) {
        this.includeTemplate = includeTemplate;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename == null ? "" : filename;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author == null ? "" : author;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public UnitUtils.Units getDimensionsUnits() {
        return dimensionsUnits;
    }

    public void setDimensionsUnits(UnitUtils.Units dimensionsUnits) {
        this.dimensionsUnits = dimensionsUnits == null ? UnitUtils.Units.MM : dimensionsUnits;
    }

    public double getDesignWidthMm() {
        return designWidthMm;
    }

    public void setDesignWidthMm(double designWidthMm) {
        this.designWidthMm = designWidthMm;
    }

    public double getDesignHeightMm() {
        return designHeightMm;
    }

    public void setDesignHeightMm(double designHeightMm) {
        this.designHeightMm = designHeightMm;
    }

    public double getScaleRatio() {
        return scaleRatio;
    }

    public void setScaleRatio(double scaleRatio) {
        this.scaleRatio = scaleRatio;
    }

    public int getSourceImageWidth() {
        return sourceImageWidth;
    }

    public int getSourceImageHeight() {
        return sourceImageHeight;
    }

    public void setSourceImageSize(int width, int height) {
        this.sourceImageWidth = width;
        this.sourceImageHeight = height;
    }

    /**
     * Sub-pixel slack for the page-count ratio. Without it, a source image that is 1 pixel larger
     * than the imageable width due to {@code Math.ceil} in the image creation tips {@code ceil}
     * from 1.0 to 2.0 and creates a phantom second page.
     */
    private static final double PAGE_COUNT_EPSILON = 1e-3;

    public int getXPageCount() {
        if (fitToPage || pageFormat == null) {
            return 1;
        }
        double effectiveWidthPt = mmToPt(designWidthMm * scaleRatio);
        if (effectiveWidthPt <= 0) {
            return 1;
        }
        double ratio = effectiveWidthPt / pageFormat.getImageableWidth();
        return Math.max(1, (int) Math.ceil(ratio - PAGE_COUNT_EPSILON));
    }

    public int getYPageCount() {
        if (fitToPage || pageFormat == null) {
            return 1;
        }
        double effectiveHeightPt = mmToPt(designHeightMm * scaleRatio);
        if (effectiveHeightPt <= 0) {
            return 1;
        }
        double ratio = effectiveHeightPt / pageFormat.getImageableHeight();
        return Math.max(1, (int) Math.ceil(ratio - PAGE_COUNT_EPSILON));
    }

    public int getTotalGridPages() {
        return getXPageCount() * getYPageCount();
    }

    /** Row-major list of grid indexes that are enabled, in the order they will be printed. */
    public List<Integer> buildEnabledPageOrder() {
        List<Integer> order = new ArrayList<>();
        int total = getTotalGridPages();
        for (int i = 0; i < total; i++) {
            if (!disabledPages.contains(i)) {
                order.add(i);
            }
        }
        return order;
    }

    /** {@code true} when the design — scaled by {@link #scaleRatio} — fits one page. */
    public boolean designFitsOnePage() {
        if (pageFormat == null) {
            return true;
        }
        double widthPt = mmToPt(designWidthMm * scaleRatio);
        double heightPt = mmToPt(designHeightMm * scaleRatio);
        double widthSlack = pageFormat.getImageableWidth() * PAGE_COUNT_EPSILON;
        double heightSlack = pageFormat.getImageableHeight() * PAGE_COUNT_EPSILON;
        return widthPt <= pageFormat.getImageableWidth() + widthSlack
                && heightPt <= pageFormat.getImageableHeight() + heightSlack;
    }

    public String formatDimensions() {
        if (dimensionsUnits == UnitUtils.Units.INCH) {
            double w = designWidthMm / MM_PER_INCH;
            double h = designHeightMm / MM_PER_INCH;
            return String.format("%.2f × %.2f in", w, h);
        }
        return String.format("%.1f × %.1f mm", designWidthMm, designHeightMm);
    }

    public String formatScale() {
        return formatScale(scaleRatio);
    }

    /**
     * Formats a scale factor as a human-readable ratio. Follows engineering convention:
     * reduction scales (less than 1:1) stay in {@code 1:N} form with {@code N} shown as an
     * integer when clean or a short decimal otherwise ({@code 0.5 → "1:2"},
     * {@code 0.8 → "1:1.25"}, {@code 0.3937 → "1:2.54"}); enlargement scales (greater than 1:1)
     * prefer simple integer fractions ({@code 1.25 → "5:4"}, {@code 2.0 → "2:1"}) and fall
     * back to {@code N:1} with a short decimal when no clean fraction exists.
     */
    public static String formatScale(double ratio) {
        if (ratio <= 0 || !Double.isFinite(ratio)) {
            return "1:1";
        }
        if (Math.abs(ratio - 1.0) < 1e-4) {
            return "1:1";
        }
        if (ratio < 1.0) {
            return "1:" + formatTerm(1.0 / ratio);
        }
        int[] integers = findIntegerRatio(ratio);
        if (integers != null) {
            return integers[0] + ":" + integers[1];
        }
        return formatTerm(ratio) + ":1";
    }

    /** Formats a single side of a ratio — integer when clean, otherwise up to 3 decimals, trimmed. */
    private static String formatTerm(double value) {
        long rounded = Math.round(value);
        if (Math.abs(value - rounded) < 1e-4) {
            return Long.toString(rounded);
        }
        String s = String.format(java.util.Locale.ROOT, "%.3f", value);
        if (s.contains(".")) {
            s = s.replaceAll("0+$", "");
            s = s.replaceAll("\\.$", "");
        }
        return s;
    }

    /**
     * Returns {@code {numerator, denominator}} if {@code ratio} can be represented as a simple
     * integer fraction (both terms ≤ 100 after GCD reduction, within a tight tolerance), else
     * {@code null}.
     */
    private static int[] findIntegerRatio(double ratio) {
        final int maxTerm = 100;
        final double tolerance = 1e-4;
        for (int denom = 1; denom <= maxTerm; denom++) {
            double numerDouble = ratio * denom;
            long numerRounded = Math.round(numerDouble);
            if (numerRounded <= 0 || numerRounded > maxTerm) {
                continue;
            }
            if (Math.abs(numerDouble - numerRounded) > tolerance * Math.max(1.0, numerDouble)) {
                continue;
            }
            int numer = (int) numerRounded;
            int g = gcd(numer, denom);
            int a = numer / g;
            int b = denom / g;
            if (a <= maxTerm && b <= maxTerm) {
                return new int[]{a, b};
            }
        }
        return null;
    }

    private static int gcd(int a, int b) {
        while (b != 0) {
            int t = b;
            b = a % b;
            a = t;
        }
        return a;
    }

    public static double mmToPt(double mm) {
        return mm * POINTS_PER_INCH / MM_PER_INCH;
    }
}

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

    public int getXPageCount() {
        if (fitToPage || pageFormat == null || sourceImageWidth <= 0) {
            return 1;
        }
        return Math.max(1, (int) Math.ceil(sourceImageWidth / pageFormat.getImageableWidth()));
    }

    public int getYPageCount() {
        if (fitToPage || pageFormat == null || sourceImageHeight <= 0) {
            return 1;
        }
        return Math.max(1, (int) Math.ceil(sourceImageHeight / pageFormat.getImageableHeight()));
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

    /** {@code true} when the rendered design fits within the current page's imageable area. */
    public boolean designFitsOnePage() {
        if (pageFormat == null) {
            return true;
        }
        return sourceImageWidth <= pageFormat.getImageableWidth()
                && sourceImageHeight <= pageFormat.getImageableHeight();
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
        if (scaleRatio <= 0) {
            return "1:1";
        }
        if (Math.abs(scaleRatio - 1.0) < 0.005) {
            return "1:1";
        }
        if (scaleRatio < 1.0) {
            return String.format("1:%.2f", 1.0 / scaleRatio);
        }
        return String.format("%.2f:1", scaleRatio);
    }

    public static double mmToPt(double mm) {
        return mm * POINTS_PER_INCH / MM_PER_INCH;
    }
}

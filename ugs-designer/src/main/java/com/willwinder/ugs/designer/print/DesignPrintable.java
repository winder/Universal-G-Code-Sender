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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.util.List;

/**
 * Renders a designer {@link BufferedImage} onto a printer page according to a {@link PrintConfig}.
 * Shared by the preview dialog (for thumbnail rendering) and the real print job so the on-screen
 * preview matches the paper output.
 *
 * @author Damian Nikodem
 */
public class DesignPrintable implements Printable {

    private final BufferedImage sourceImage;
    private final PrintConfig config;
    private final EngineeringTemplateRenderer templateRenderer;

    public DesignPrintable(BufferedImage sourceImage, PrintConfig config,
                           EngineeringTemplateRenderer templateRenderer) {
        this.sourceImage = sourceImage;
        this.config = config;
        this.templateRenderer = templateRenderer;
    }

    public PrintConfig getConfig() {
        return config;
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
        if (sourceImage == null || pageFormat == null) {
            return NO_SUCH_PAGE;
        }

        Graphics2D g2d = (Graphics2D) graphics;

        if (config.isFitToPage()) {
            if (pageIndex != 0) {
                return NO_SUCH_PAGE;
            }
            renderFitToPage(g2d, pageFormat);
            return PAGE_EXISTS;
        }

        List<Integer> pageOrder = config.buildEnabledPageOrder();
        if (pageIndex < 0 || pageIndex >= pageOrder.size()) {
            return NO_SUCH_PAGE;
        }
        int gridIdx = pageOrder.get(pageIndex);
        renderTile(g2d, pageFormat, gridIdx);
        return PAGE_EXISTS;
    }

    /** Render a single tile at grid position {@code gridIdx} for multi-page printing. */
    public void renderTile(Graphics2D g2d, PageFormat pageFormat, int gridIdx) {
        int xPageCount = config.getXPageCount();
        int yPageIndex = gridIdx / xPageCount;
        int xPageIndex = gridIdx % xPageCount;

        AffineTransform saved = g2d.getTransform();
        try {
            double imageableX = pageFormat.getImageableX();
            double imageableY = pageFormat.getImageableY();
            double imageableW = pageFormat.getImageableWidth();
            double imageableH = pageFormat.getImageableHeight();

            double offsetX = PrintConfig.mmToPt(config.getOffsetMmX());
            double offsetY = PrintConfig.mmToPt(config.getOffsetMmY());

            // Center the image within a single page only when it fits — matches prior behaviour.
            int centerX = config.designFitsOnePage() ? centerOffset(imageableW, sourceImage.getWidth()) : 0;
            int centerY = config.designFitsOnePage() ? centerOffset(imageableH, sourceImage.getHeight()) : 0;

            g2d.translate(
                    imageableX - (xPageIndex * imageableW) + centerX + offsetX,
                    imageableY - (yPageIndex * imageableH) + centerY + offsetY);
            g2d.drawImage(sourceImage, null, 0, 0);

            g2d.setTransform(saved);

            if (shouldDrawTemplateNonFit()) {
                g2d.translate(imageableX, imageableY);
                templateRenderer.render(g2d, pageFormat, config);
            }
        } finally {
            g2d.setTransform(saved);
        }
    }

    /** Render the entire design scaled to fit a single page, always with template overlay. */
    public void renderFitToPage(Graphics2D g2d, PageFormat pageFormat) {
        AffineTransform saved = g2d.getTransform();
        try {
            double imageableX = pageFormat.getImageableX();
            double imageableY = pageFormat.getImageableY();
            double imageableW = pageFormat.getImageableWidth();
            double imageableH = pageFormat.getImageableHeight();

            // Reserve space for the template border + title block.
            double borderInset = EngineeringTemplateRenderer.BORDER_INSET_PT;
            double availX = borderInset;
            double availY = borderInset;
            double availW = imageableW - 2 * borderInset;
            double availH = imageableH - 2 * borderInset - EngineeringTemplateRenderer.TITLE_BLOCK_HEIGHT_PT;
            if (availW <= 0 || availH <= 0) {
                // Page too small to fit both template and content — fall back to drawing content full-bleed.
                availX = 0;
                availY = 0;
                availW = imageableW;
                availH = imageableH;
            }

            double scale = Math.min(availW / sourceImage.getWidth(), availH / sourceImage.getHeight());
            if (scale <= 0 || !Double.isFinite(scale)) {
                scale = 1.0;
            }
            config.setScaleRatio(scale);

            double drawW = sourceImage.getWidth() * scale;
            double drawH = sourceImage.getHeight() * scale;
            double drawX = availX + (availW - drawW) / 2.0;
            double drawY = availY + (availH - drawH) / 2.0;

            g2d.translate(imageableX, imageableY);
            AffineTransform imageTransform = AffineTransform.getTranslateInstance(drawX, drawY);
            imageTransform.scale(scale, scale);
            g2d.drawImage(sourceImage, imageTransform, null);

            // Template draws in imageable-area coordinates — we're already translated there.
            templateRenderer.render(g2d, pageFormat, config);
        } finally {
            g2d.setTransform(saved);
        }
    }

    private boolean shouldDrawTemplateNonFit() {
        return config.isIncludeTemplate() && config.designFitsOnePage();
    }

    private int centerOffset(double availableArea, double imageSize) {
        if (imageSize < availableArea) {
            return (int) ((availableArea - imageSize) / 2.0);
        }
        return 0;
    }
}

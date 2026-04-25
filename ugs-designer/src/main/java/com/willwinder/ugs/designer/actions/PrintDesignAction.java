/*
    Copyright 2025-2026 Damian Nikodem

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
package com.willwinder.ugs.designer.actions;

import com.willwinder.ugs.designer.entities.entities.Entity;
import com.willwinder.ugs.designer.entities.entities.controls.GridControl;
import com.willwinder.ugs.designer.gui.Drawing;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.logic.ControllerFactory;
import com.willwinder.ugs.designer.print.DesignPrintable;
import com.willwinder.ugs.designer.print.EngineeringTemplateRenderer;
import com.willwinder.ugs.designer.print.PrintConfig;
import com.willwinder.ugs.designer.print.PrintPreviewDialog;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.services.LookupService;
import com.willwinder.universalgcodesender.utils.SvgIconLoader;

import javax.swing.JOptionPane;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.time.LocalDate;

/**
 * @author Damian Nikodem
 */
public class PrintDesignAction extends AbstractDesignAction {
    public static final String SMALL_ICON_PATH = "img/print.svg";
    public static final String LARGE_ICON_PATH = "img/print24.svg";

    private static final double MM_IN_INCH = 0.0393700787401575; // 5 / 127
    private static final double PRINT_SYSTEM_DPI = 72.0;
    private static final double MM_PER_POINT = 25.4 / 72.0;

    private String filenameHint = "";

    public PrintDesignAction() {
        putValue("iconBase", SMALL_ICON_PATH);
        putValue(SMALL_ICON, SvgIconLoader.loadImageIcon(SMALL_ICON_PATH, SvgIconLoader.SIZE_SMALL).orElse(null));
        putValue(LARGE_ICON_KEY, SvgIconLoader.loadImageIcon(SMALL_ICON_PATH, SvgIconLoader.SIZE_MEDIUM).orElse(null));
        putValue("menuText", "Print");
        putValue(NAME, "Print");
    }

    /**
     * Platform wrappers set this so the printed title block can show the current file's name.
     * The core {@link Controller} has no file-path concept of its own.
     */
    public void setFilenameHint(String filenameHint) {
        this.filenameHint = filenameHint == null ? "" : filenameHint;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Controller controller = ControllerFactory.getController();
        controller.getSelectionManager().clearSelection();

        PrinterJob job = PrinterJob.getPrinterJob();
        PageFormat pageFormat = job.defaultPage();
        pageFormat.setOrientation(PageFormat.LANDSCAPE);

        PrintConfig config = buildConfig(controller, pageFormat);
        BufferedImage bi = renderSourceImage(controller, config);
        config.setSourceImageSize(bi.getWidth(), bi.getHeight());

        EngineeringTemplateRenderer templateRenderer = new EngineeringTemplateRenderer();
        DesignPrintable printable = new DesignPrintable(bi, config, templateRenderer);

        PrintPreviewDialog dialog = new PrintPreviewDialog(controller.getDrawing(), printable, job);
        dialog.setVisible(true);
        if (!dialog.isConfirmed()) {
            return;
        }

        job.setPrintable(printable, config.getPageFormat());
        if (job.printDialog()) {
            try {
                job.print();
            } catch (PrinterException exc) {
                JOptionPane.showMessageDialog(null, "Error Printing: " + exc.getMessage(), "Error Printing!",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private PrintConfig buildConfig(Controller controller, PageFormat pageFormat) {
        PrintConfig config = new PrintConfig();
        config.setPageFormat(pageFormat);
        config.setFilename(filenameHint);
        config.setAuthor(System.getProperty("user.name", ""));
        config.setDate(LocalDate.now());
        config.setIncludeTemplate(true);

        BackendAPI backend = LookupService.lookup(BackendAPI.class);
        if (backend != null && backend.getSettings() != null
                && backend.getSettings().getPreferredUnits() != null) {
            config.setDimensionsUnits(backend.getSettings().getPreferredUnits());
        } else {
            config.setDimensionsUnits(UnitUtils.Units.MM);
        }

        Rectangle2D designBounds = controller.getDrawing().getRootEntity().getBounds();
        config.setDesignWidthMm(designBounds.getWidth());
        config.setDesignHeightMm(designBounds.getHeight());
        return config;
    }

    /**
     * Renders the current design to a {@link BufferedImage} at printer DPI.
     *
     * <p>Bypasses {@link Drawing#getImage()} because that method derives both image size and
     * transform from {@code globalRoot.getBounds()}, which unions with empty controls anchored at
     * the origin and silently clips designs whose minX/minY are non-zero. Here we own the bounds
     * decision: when the design fits on a single page the image becomes page-sized with the
     * design centered; otherwise the image matches the design bounds, so the grid clips and the
     * page count is driven purely by the drawing extent.
     */
    private BufferedImage renderSourceImage(Controller controller, PrintConfig config) {
        Drawing drawing = controller.getDrawing();
        PageFormat pageFormat = config.getPageFormat();
        double oldScale = drawing.getScale();
        GridControl gridControl = drawing.getGridControl();

        double pageWidthMm = pageFormat.getImageableWidth() * MM_PER_POINT;
        double pageHeightMm = pageFormat.getImageableHeight() * MM_PER_POINT;
        Rectangle2D designBounds = drawing.getRootEntity().getBounds();
        Rectangle2D targetBounds = computeTargetBounds(designBounds, pageWidthMm, pageHeightMm);

        double scale = MM_IN_INCH * PRINT_SYSTEM_DPI;
        int imgWidth = Math.max(1, (int) Math.round(targetBounds.getWidth() * scale));
        int imgHeight = Math.max(1, (int) Math.round(targetBounds.getHeight() * scale));

        BufferedImage bi = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bi.createGraphics();
        try {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, imgWidth, imgHeight);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            // GridControl.drawLargeGridAndText reads getClipBounds() to skip off-image grid lines;
            // it crashes if no clip is set. Set the clip in pixel space; once we apply the transform
            // the clip becomes the design-space rectangle that maps to the full image.
            g.setClip(0, 0, imgWidth, imgHeight);

            // Map design (minX, minY) → pixel (0, imgHeight) and (maxX, maxY) → (imgWidth, 0).
            AffineTransform tx = new AffineTransform();
            tx.scale(1, -1);
            tx.translate(0, -imgHeight);
            tx.scale(scale, scale);
            tx.translate(-targetBounds.getMinX(), -targetBounds.getMinY());
            g.setTransform(tx);

            drawing.setScale(scale);
            try {
                gridControl.setPrintBoundsOverride(targetBounds);
                try {
                    gridControl.render(g, drawing);
                } finally {
                    gridControl.setPrintBoundsOverride(null);
                }
                for (Entity entity : drawing.getRootEntity().getChildren()) {
                    entity.render(g, drawing);
                }
            } finally {
                drawing.setScale(oldScale);
                drawing.refresh();
            }
        } finally {
            g.dispose();
        }
        return bi;
    }

    private static Rectangle2D computeTargetBounds(Rectangle2D designBounds, double pageWidthMm, double pageHeightMm) {
        double designW = designBounds.getWidth();
        double designH = designBounds.getHeight();
        if (designW <= pageWidthMm && designH <= pageHeightMm) {
            // Single page — center a page-sized rectangle on the design's center so the design
            // ends up centered within the rendered image.
            double cx = (designBounds.getMinX() + designBounds.getMaxX()) / 2.0;
            double cy = (designBounds.getMinY() + designBounds.getMaxY()) / 2.0;
            return new Rectangle2D.Double(
                    cx - pageWidthMm / 2.0, cy - pageHeightMm / 2.0, pageWidthMm, pageHeightMm);
        }
        // Multi-page — image matches design exactly so the grid clips at design bounds and the
        // page count follows the drawing's actual extent.
        return new Rectangle2D.Double(
                designBounds.getMinX(), designBounds.getMinY(), designW, designH);
    }
}

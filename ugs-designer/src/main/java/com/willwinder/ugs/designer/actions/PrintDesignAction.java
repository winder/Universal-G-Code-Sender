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
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
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
     * Renders the current design to a {@link BufferedImage} at printer DPI. The image always
     * covers at least one full page so the grid extends to the page edges — fixing the long-
     * standing bug where the grid was clipped to design content bounds during printing.
     */
    private BufferedImage renderSourceImage(Controller controller, PrintConfig config) {
        Drawing drawing = controller.getDrawing();
        PageFormat pageFormat = config.getPageFormat();
        double oldScale = drawing.getScale();
        Point2D.Double oldPos = drawing.getPosition();
        GridControl gridControl = drawing.getGridControl();
        Rectangle2D previousOverride = null;

        double pageWidthMm = pageFormat.getImageableWidth() * MM_PER_POINT;
        double pageHeightMm = pageFormat.getImageableHeight() * MM_PER_POINT;

        Rectangle2D designBounds = drawing.getRootEntity().getBounds();
        double minX = Math.min(designBounds.getMinX(), -pageWidthMm / 2.0);
        double minY = Math.min(designBounds.getMinY(), -pageHeightMm / 2.0);
        double maxX = Math.max(designBounds.getMaxX(), pageWidthMm / 2.0);
        double maxY = Math.max(designBounds.getMaxY(), pageHeightMm / 2.0);
        Rectangle2D printBounds = new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);

        try {
            gridControl.setPrintBoundsOverride(printBounds);
            drawing.setScale(MM_IN_INCH * PRINT_SYSTEM_DPI);
            drawing.refresh();
            return drawing.getImage();
        } finally {
            gridControl.setPrintBoundsOverride(previousOverride);
            drawing.setScale(oldScale);
            drawing.setPosition(oldPos.x, oldPos.y);
            drawing.refresh();
        }
    }
}

/*
    Copyright 2025 Damian Nikodem

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
package com.willwinder.ugs.nbp.designer.actions;

import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.ControllerFactory;
import org.openide.util.ImageUtilities;

import javax.swing.JOptionPane;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

/**
 * @author Damian Nikodem
 */
public class PrintDesignAction extends AbstractDesignAction {
    public static final String SMALL_ICON_PATH = "img/print.svg";
    public static final String LARGE_ICON_PATH = "img/print24.svg";

    private final static double MM_IN_INCH = 0.0393700787401575; // 5 / 127
    private final static double PRINT_SYSTEM_DPI = 72.0;

    public PrintDesignAction() {
        putValue("iconBase", SMALL_ICON_PATH);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(SMALL_ICON_PATH, false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(LARGE_ICON_PATH, false));
        putValue("menuText", "Print");
        putValue(NAME, "Print");
    }

    private int centerPos(double pageSize, double imageWidth) {
        int result = 0;
        if ((imageWidth / pageSize) < 1.0) {
            result = (int) ((pageSize - imageWidth) / 2.0);
        }
        return result;
    }

    private Point centerPoint(PageFormat pageFormat, BufferedImage bi) {
        return new Point(
                centerPos(pageFormat.getImageableWidth(), bi.getWidth()),
                centerPos(pageFormat.getImageableHeight(), bi.getHeight())
        );
    }

    private boolean translateGraphicsForPage(PageFormat pageFormat, BufferedImage bi, int pageIndex, Graphics2D g2d) {
        int xPageCount = (int) Math.ceil(bi.getWidth() / pageFormat.getImageableWidth());
        int yPageCount = (int) Math.ceil(bi.getHeight() / pageFormat.getImageableHeight());

        int totalPages = xPageCount * yPageCount;

        if (pageIndex >= totalPages) {
            return false;
        }

        int yPageIndex = (int) Math.floor(pageIndex / xPageCount);
        int xPageIndex = pageIndex % xPageCount;

        g2d.translate(pageFormat.getImageableX() - (xPageIndex * pageFormat.getImageableWidth()),
                pageFormat.getImageableY() - (yPageIndex * pageFormat.getImageableHeight()));
        // Page is valid
        return true;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Render image first. 
        Controller controller = ControllerFactory.getController();
        controller.getSelectionManager().clearSelection();

        PrinterJob job = PrinterJob.getPrinterJob();
        double oldScale = controller.getDrawing().getScale();
        Point2D.Double oldPos = controller.getDrawing().getPosition();

        // Reset Scale so it draws properly. 
        controller.getDrawing().setScale(MM_IN_INCH * PRINT_SYSTEM_DPI);

        BufferedImage bi = controller.getDrawing().getImage();

        controller.getDrawing().setScale(oldScale);
        controller.getDrawing().setPosition(oldPos.x, oldPos.y);

        job.setPrintable((Graphics graphics, PageFormat pageFormat, int pageIndex) -> {
            Graphics2D g2d = (Graphics2D) graphics;

            if (!translateGraphicsForPage(pageFormat, bi, pageIndex, g2d)) {
                return Printable.NO_SUCH_PAGE;
            }

            Point imagePosition = centerPoint(pageFormat, bi);
            g2d.drawImage(bi, null, imagePosition.x, imagePosition.y);

            return Printable.PAGE_EXISTS;
        });

        if (job.printDialog()) {
            try {
                job.print();
            } catch (PrinterException exc) {
                JOptionPane.showMessageDialog(null, "Error Printing: " + exc.getMessage(), "Error Printing!", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

}

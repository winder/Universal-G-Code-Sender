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
import java.awt.Graphics;
import java.awt.Graphics2D;
import org.openide.util.ImageUtilities;

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
    public static final String SMALL_ICON_PATH = "img/export.svg";
    public static final String LARGE_ICON_PATH = "img/export24.svg";
    public static String lastDirectory = "";

    public PrintDesignAction() {
        putValue("iconBase", SMALL_ICON_PATH);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(SMALL_ICON_PATH, false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(LARGE_ICON_PATH, false));
        putValue("menuText", "Print");
        putValue(NAME, "Print");
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
        // 1px == 1mm in UGS Drawing
        //
        // 72px == 1in in Java Printing
        //
        // Therefore Scale = 2.83464576
        controller.getDrawing().setScale(2.83464576); 
        
        
        BufferedImage bi = controller.getDrawing().getImage();
        
        controller.getDrawing().setScale(oldScale);
        controller.getDrawing().setPosition(oldPos.x,oldPos.y);
              
        job.setPrintable((Graphics graphics, PageFormat pageFormat, int pageIndex) -> {
            int xPages = (int)Math.ceil(bi.getWidth() / pageFormat.getImageableWidth());
            int yPages = (int)Math.ceil(bi.getHeight() / pageFormat.getImageableHeight());
            
            int totalPages = xPages * yPages;
            
            if (pageIndex >= totalPages) {
                return Printable.NO_SUCH_PAGE;
            }
            
            int yPageIndex = (int)Math.floor(pageIndex / xPages);
            int xPageIndex = pageIndex % xPages;
            int xPos = 0;
            int yPos = 0;
            Graphics2D g2d = (Graphics2D)graphics;
            
            g2d.translate(pageFormat.getImageableX() - (xPageIndex * pageFormat.getImageableWidth() ),
                    pageFormat.getImageableY() - (yPageIndex * pageFormat.getImageableHeight())  );
            
            if ( (((double)bi.getWidth()) / pageFormat.getImageableWidth()) < 1.0) {
                xPos = (int) ((double)( pageFormat.getImageableWidth() - (double)bi.getWidth() ) / 2.0);
            }
            
            if ( (((double)bi.getHeight()) / pageFormat.getImageableHeight()) < 1.0) {
                yPos = (int) ((double)( pageFormat.getImageableHeight() - (double)bi.getHeight() ) / 2.0);
            }
            
            g2d.drawImage(bi,null, xPos, yPos);
            
            return Printable.PAGE_EXISTS;
        });
        if (job.printDialog()) {
            try {job.print();}
            catch (PrinterException exc) {
                System.out.println(exc);
             }
         }   
    }

}

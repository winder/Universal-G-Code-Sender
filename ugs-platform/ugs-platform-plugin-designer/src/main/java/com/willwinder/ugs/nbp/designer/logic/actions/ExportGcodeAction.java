package com.willwinder.ugs.nbp.designer.logic.actions;

import com.willwinder.ugs.nbp.designer.gcode.SimpleGcodeRouter;
import com.willwinder.ugs.nbp.designer.gcode.path.GcodePath;
import com.willwinder.ugs.nbp.designer.gui.controls.Control;
import com.willwinder.ugs.nbp.designer.io.PngWriter;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import org.openide.util.ImageUtilities;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;

public class ExportGcodeAction extends AbstractAction {

    private static final String ICON_BASE = "img/document-save-as.png";

    public ExportGcodeAction() {
        putValue("iconBase", ICON_BASE);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_BASE, false));
        putValue("menuText", "Export Gcode");
        putValue(NAME, "Export Gcode");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Controller controller = CentralLookup.getDefault().lookup(Controller.class);

        SimpleGcodeRouter gcodeRouter = new SimpleGcodeRouter();

        AffineTransform affineTransform = AffineTransform.getScaleInstance(1, -1);
        affineTransform.translate(0, -controller.getDrawing().getHeight());
        controller.getDrawing().getShapes().forEach(shape -> {
            if (shape instanceof Control) {
                return;
            }

            try {
                GcodePath gcodePath = gcodeRouter.toPath(shape, affineTransform);

                    /*if(shape.getCutSettings().getCutType() == CutType.POCKET) {
                        SimplePocket simplePocket = new SimplePocket(gcodePath);
                        gcodePath = simplePocket.toGcodePath();

                        SimpleOutline simpleOutline = new SimpleOutline(gcodePath);
                        simpleOutline.setDepth(shape.getCutSettings().getDepth());
                        gcodePath = simpleOutline.toGcodePath();
                    }*/

                String gcode = gcodeRouter.toGcode(gcodePath);
                System.out.println(gcode);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }
}

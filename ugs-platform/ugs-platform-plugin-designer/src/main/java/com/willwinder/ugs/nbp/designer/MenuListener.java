package com.willwinder.ugs.nbp.designer;


import com.willwinder.ugs.nbp.designer.controls.Control;
import com.willwinder.ugs.nbp.designer.gcode.SimpleGcodeRouter;
import com.willwinder.ugs.nbp.designer.gcode.path.GcodePath;
import com.willwinder.ugs.nbp.designer.io.DrawIO;
import com.willwinder.ugs.nbp.designer.io.SvgReader;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import org.apache.commons.lang3.StringUtils;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;

/**
 * Listens to actions from the buttons in a menu and modifies the Drawing
 * through a DrawingController
 *
 * @author Alex Lagerstedt
 */
public class MenuListener implements ActionListener {

    Controller controller;
    JFileChooser fileDialog;

    public MenuListener(Controller c) {
        this.controller = c;
    }

    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        DrawIO fio = new DrawIO();

        if (cmd.equals("Quit")) {
            System.exit(0);
        } else if (cmd.equals("Undo")) {
            controller.undo();
        } else if (cmd.equals("Redo")) {
            controller.redo();
        } else if (cmd.equals("Select all")) {
            controller.selectAll();

        } else if (cmd.equals("Clear selection")) {
            controller.getSelectionManager().removeAll();
            controller.getDrawing().repaint();
        } else if (cmd.equals("Rotate")) {
            controller.getDrawing().repaint();
        } else if (cmd.equals("Delete")) {
            controller.deleteSelectedShapes();
        } else if (cmd.equals("Open")) {
            fileDialog = new JFileChooser();
            fileDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
            FileFilter filter = new FileNameExtensionFilter("Draw files",
                    "draw", "svg");
            fileDialog.addChoosableFileFilter(filter);
            fileDialog.setFileFilter(filter);

            fileDialog.showOpenDialog(null);
            File f = fileDialog.getSelectedFile();
            if (f != null) {
                if (StringUtils.endsWithIgnoreCase(f.getName(), ".draw")) {
                    fio.open(f, controller);
                } else if (StringUtils.endsWithIgnoreCase(f.getName(), ".svg")) {
                    SvgReader batikIO = new SvgReader();
                    batikIO.open(f, controller);
                }
            }

        } else if (cmd.equals("Save as")) {
            fileDialog = new JFileChooser();
            fileDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);

            fileDialog.setSelectedFile(new File("new.draw"));
            FileFilter filter = new FileNameExtensionFilter("Draw files",
                    "draw");
            fileDialog.addChoosableFileFilter(filter);
            fileDialog.setFileFilter(filter);

            fileDialog.showSaveDialog(null);

            File f = fileDialog.getSelectedFile();
            if (f != null) {
                fio.save(f, controller);
            }
        } else if (cmd.equals("Export PNG")) {
            fileDialog = new JFileChooser();
            fileDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileDialog.setDialogType(JFileChooser.CUSTOM_DIALOG);
            FileFilter filter = new FileNameExtensionFilter(
                    "Portable Network Graphics", "png");
            fileDialog.addChoosableFileFilter(filter);

            fileDialog.setSelectedFile(new File("out.png"));
            fileDialog.showSaveDialog(null);

            File f = fileDialog.getSelectedFile();
            if (f != null) {
                fio.export(f, controller);
            }

        } else if (cmd.equals("Export GCode")) {
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

        } else if (cmd.equals("New")) {
            controller.newDrawing();
        } else {
            JOptionPane.showMessageDialog(null, "Not implemented.",
                    "Not implemented", JOptionPane.ERROR_MESSAGE);
        }
    }
}

package com.willwinder.ugs.nbp.designer.actions;

import com.willwinder.ugs.nbp.designer.io.PngWriter;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import org.openide.util.ImageUtilities;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.io.File;

public class ExportPngAction extends AbstractAction {

    private static final String SMALL_ICON_PATH = "img/export.svg";
    private static final String LARGE_ICON_PATH = "img/export32.svg";

    public ExportPngAction() {
        putValue("iconBase", SMALL_ICON_PATH);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(SMALL_ICON_PATH, false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(LARGE_ICON_PATH, false));
        putValue("menuText", "Export PNG");
        putValue(NAME, "Export PNG");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Controller controller = CentralLookup.getDefault().lookup(Controller.class);

        JFileChooser fileDialog = new JFileChooser();
        fileDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileDialog.setDialogType(JFileChooser.CUSTOM_DIALOG);
        FileFilter filter = new FileNameExtensionFilter(
                "Portable Network Graphics", "png");
        fileDialog.addChoosableFileFilter(filter);

        fileDialog.setSelectedFile(new File("out.png"));
        fileDialog.showSaveDialog(null);

        File f = fileDialog.getSelectedFile();
        if (f != null) {
            PngWriter pngWriter = new PngWriter();
            pngWriter.write(f, controller);
        }
    }
}

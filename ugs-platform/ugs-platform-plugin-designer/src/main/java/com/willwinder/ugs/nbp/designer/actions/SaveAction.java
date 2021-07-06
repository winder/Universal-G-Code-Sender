package com.willwinder.ugs.nbp.designer.actions;

import com.willwinder.ugs.nbp.designer.io.ugsd.UgsDesignWriter;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import org.openide.util.ImageUtilities;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.io.File;

public class SaveAction extends AbstractAction {
    private static final String ICON_SMALL_PATH = "img/new.svg";
    private static final String ICON_LARGE_PATH = "img/new32.svg";
    private final Controller controller;

    public SaveAction(Controller controller) {
        putValue("iconBase", ICON_SMALL_PATH);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_SMALL_PATH, false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(ICON_LARGE_PATH, false));
        putValue("menuText", "Save");
        putValue(NAME, "Save");
        this.controller = controller;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        controller.getSelectionManager().clearSelection();

        JFileChooser fileDialog = new JFileChooser();
        fileDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileDialog.setDialogType(JFileChooser.CUSTOM_DIALOG);
        FileFilter filter = new FileNameExtensionFilter(
                "Portable Network Graphics", "ugsd");
        fileDialog.addChoosableFileFilter(filter);

        fileDialog.setSelectedFile(new File("out.ugsd"));
        fileDialog.showSaveDialog(null);

        File f = fileDialog.getSelectedFile();
        if (f != null) {
            ThreadHelper.invokeLater(() -> {
                UgsDesignWriter writer = new UgsDesignWriter();
                writer.write(f, controller);
            });
        }
    }
}

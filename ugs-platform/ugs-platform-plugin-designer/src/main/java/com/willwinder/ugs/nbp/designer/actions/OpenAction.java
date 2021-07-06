package com.willwinder.ugs.nbp.designer.actions;

import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.nbp.designer.io.DesignReader;
import com.willwinder.ugs.nbp.designer.io.svg.SvgReader;
import com.willwinder.ugs.nbp.designer.io.ugsd.UgsDesignReader;
import com.willwinder.ugs.nbp.designer.model.Design;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.ImageUtilities;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Optional;

public class OpenAction extends AbstractAction {
    private static final String ICON_SMALL_PATH = "img/open.svg";
    private static final String ICON_LARGE_PATH = "img/open32.svg";
    private static final FileFilter DESIGN_FILE_FILTER = new FileNameExtensionFilter("UGS Design (ugsd)", "ugsd");
    private static final FileFilter SVG_FILE_FILTER = new FileNameExtensionFilter("Scalable Vector Graphics (svg)", "svg");

    public OpenAction() {
        putValue("iconBase", ICON_SMALL_PATH);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_SMALL_PATH, false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(ICON_LARGE_PATH, false));
        putValue("menuText", "Open");
        putValue(NAME, "Open");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        UndoManager undoManager = CentralLookup.getDefault().lookup(UndoManager.class);
        undoManager.clear();

        SelectionManager selectionManager = CentralLookup.getDefault().lookup(SelectionManager.class);
        selectionManager.clearSelection();

        Controller controller = CentralLookup.getDefault().lookup(Controller.class);
        JFileChooser fileDialog = new JFileChooser();
        fileDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileDialog.addChoosableFileFilter(DESIGN_FILE_FILTER);
        fileDialog.addChoosableFileFilter(SVG_FILE_FILTER);
        fileDialog.setFileFilter(DESIGN_FILE_FILTER);

        fileDialog.showOpenDialog(null);

        ThreadHelper.invokeLater(() -> {
            DesignReader designReader = new UgsDesignReader();
            if (fileDialog.getFileFilter() == SVG_FILE_FILTER) {
                designReader = new SvgReader();
            }

            File selectedFile = fileDialog.getSelectedFile();
            Optional<Design> optional = designReader.read(selectedFile);
            if (optional.isPresent()) {
                controller.setDesign(optional.get());
            } else {
                throw new RuntimeException("Could not open svg: " + selectedFile.getName());
            }
        });
    }
}

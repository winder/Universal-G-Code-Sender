package com.willwinder.ugs.nbp.designer.logic.actions;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.io.SvgReader;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.ImageUtilities;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Optional;

public class OpenAction extends AbstractAction {
    private static final String ICON_SMALL_PATH = "img/open.svg";
    private static final String ICON_LARGE_PATH = "img/open32.svg";

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
        FileFilter filter = new FileNameExtensionFilter("Draw files",
                "draw", "svg");
        fileDialog.addChoosableFileFilter(filter);
        fileDialog.setFileFilter(filter);

        fileDialog.showOpenDialog(null);

        ThreadHelper.invokeLater(() -> {
            File f = fileDialog.getSelectedFile();
            if (f != null) {
                if (StringUtils.endsWithIgnoreCase(f.getName(), ".svg")) {
                    SvgReader svgReader = new SvgReader();
                    Optional<Entity> optional = svgReader.read(f);
                    if (optional.isPresent()) {
                        Entity entity = optional.get();
                        controller.newDrawing();
                        controller.getDrawing().insertEntity(entity);
                        controller.getDrawing().repaint();
                    } else {
                        throw new RuntimeException("Could not open svg: " + f.getName());
                    }
                }
            }
        });
    }
}

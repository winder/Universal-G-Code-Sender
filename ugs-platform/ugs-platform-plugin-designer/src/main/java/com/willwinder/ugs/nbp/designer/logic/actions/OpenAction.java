package com.willwinder.ugs.nbp.designer.logic.actions;

import com.willwinder.ugs.nbp.designer.io.SvgReader;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.selection.SelectionManager;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.ImageUtilities;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.io.File;

public class OpenAction extends AbstractAction {
    private static final String ICON_BASE = "img/document-open.png";

    public OpenAction() {
        putValue("iconBase", ICON_BASE);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_BASE, false));
        putValue("menuText", "Open");
        putValue(NAME, "Open");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        UndoManager undoManager = CentralLookup.getDefault().lookup(UndoManager.class);
        undoManager.clear();

        SelectionManager selectionManager = CentralLookup.getDefault().lookup(SelectionManager.class);
        selectionManager.removeAll();

        Controller controller = CentralLookup.getDefault().lookup(Controller.class);
        JFileChooser fileDialog = new JFileChooser();
        fileDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FileFilter filter = new FileNameExtensionFilter("Draw files",
                "draw", "svg");
        fileDialog.addChoosableFileFilter(filter);
        fileDialog.setFileFilter(filter);

        fileDialog.showOpenDialog(null);
        File f = fileDialog.getSelectedFile();
        if (f != null) {
            if (StringUtils.endsWithIgnoreCase(f.getName(), ".svg")) {
                SvgReader batikIO = new SvgReader();
                batikIO.open(f, controller);
            }
        }
    }
}

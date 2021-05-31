package com.willwinder.ugs.nbp.designer.logic.actions;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.io.SvgReader;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.Tool;
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

public final class ToolInsertAction extends AbstractAction {

    public static final String SMALL_ICON_PATH = "img/import.svg";
    public static final String LARGE_ICON_PATH = "img/import32.svg";
    private final Controller controller;

    public ToolInsertAction(Controller controller) {
        putValue("iconBase", SMALL_ICON_PATH);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(SMALL_ICON_PATH, false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(LARGE_ICON_PATH, false));
        putValue("menuText", "Insert");
        putValue(NAME, "Insert");
        this.controller = controller;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
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

                        controller.addEntity(entity);
                        controller.getSelectionManager().addSelection(entity);

                        controller.getDrawing().repaint();
                        controller.setTool(Tool.SELECT);
                    } else {
                        throw new RuntimeException("Could not open svg: " + f.getName());
                    }
                }
            }
        });
    }
}

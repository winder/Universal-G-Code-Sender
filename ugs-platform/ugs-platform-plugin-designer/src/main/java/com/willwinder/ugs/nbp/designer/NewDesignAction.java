package com.willwinder.ugs.nbp.designer;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.model.BackendAPI;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.ImageUtilities;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.io.IOException;

@ActionID(
        category = LocalizingService.OpenCategory,
        id = "NewDesignAction")
@ActionRegistration(
        iconBase = "img/document-new.png",
        displayName = "New",
        lazy = false)
@ActionReferences({
        @ActionReference(
                path = LocalizingService.OpenWindowPath,
                position = 9),
        @ActionReference(
                path = "Toolbars/File",
                position = 9),
        @ActionReference(
                path = "Shortcuts",
                name = "M-O")
})
public final class NewDesignAction extends AbstractAction {

    public static final String ICON_BASE = "img/document-new.png";
    private BackendAPI backend;

    public NewDesignAction() {
        this.backend = CentralLookup.getDefault().lookup(BackendAPI.class);

        putValue("iconBase", ICON_BASE);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_BASE, false));
        putValue("menuText", "New design");
        putValue(NAME, "New design");
    }

    @Override
    public boolean isEnabled() {
        return backend != null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            FileSystem fs = FileUtil.createMemoryFileSystem();
            FileObject fob = fs.getRoot().createData("unnamed", "ugsd");
            DataObject.find(fob).
                    getLookup().lookup(OpenCookie.class).open();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}

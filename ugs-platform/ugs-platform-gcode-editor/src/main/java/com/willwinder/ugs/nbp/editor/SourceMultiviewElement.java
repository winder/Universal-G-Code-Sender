package com.willwinder.ugs.nbp.editor;

import com.willwinder.ugs.nbm.visualizer.Visualizer2TopComponent;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.openide.ErrorManager;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

@MultiViewElement.Registration(
        displayName = "#LBL_Gcode_EDITOR",
        iconBase = "com/willwinder/ugs/nbp/editor/edit.png",
        mimeType = "text/xgcode",
        persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID = "Gcode",
        position = 1000
)
@NbBundle.Messages("LBL_Gcode_EDITOR=Source")
public class SourceMultiviewElement extends MultiViewEditorElement {

    private static final Logger LOGGER = Logger.getLogger(SourceMultiviewElement.class.getName());


    public SourceMultiviewElement(Lookup lookup) {
        super(lookup);


        FileUtil.addFileChangeListener(new FileChangeListener() {
            @Override
            public void fileFolderCreated(FileEvent fe) {

            }

            @Override
            public void fileDataCreated(FileEvent fe) {

            }

            @Override
            public void fileChanged(FileEvent fe) {
                BackendAPI backend = CentralLookup.getDefault().lookup(BackendAPI.class);
                if (backend.getGcodeFile().getPath().equals(fe.getFile().getPath())) {
                    try {
                        backend.setGcodeFile(new File(fe.getFile().getPath()));
                    } catch (Exception e) {
                        ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
                    }
                }
            }

            @Override
            public void fileDeleted(FileEvent fe) {

            }

            @Override
            public void fileRenamed(FileRenameEvent fe) {

            }

            @Override
            public void fileAttributeChanged(FileAttributeEvent fe) {

            }
        });
    }
}

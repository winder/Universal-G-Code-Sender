package com.willwinder.ugs.nbp.editor;

import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

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
    public SourceMultiviewElement(Lookup lookup) {
        super(lookup);
    }
}

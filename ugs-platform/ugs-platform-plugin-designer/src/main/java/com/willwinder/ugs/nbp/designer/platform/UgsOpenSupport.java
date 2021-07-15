package com.willwinder.ugs.nbp.designer.platform;

import com.willwinder.ugs.nbp.lib.EditorUtils;
import com.willwinder.ugs.nbp.lib.lookup.EditorCookie;
import org.openide.cookies.OpenCookie;

public class UgsOpenSupport implements OpenCookie, EditorCookie {

    private final UgsDataObject.Entry entry;

    public UgsOpenSupport(UgsDataObject.Entry entry) {
        this.entry = entry;
    }

    @Override
    public void open() {
        if (EditorUtils.closeOpenEditors()) {
            UgsDataObject dobj = (UgsDataObject) entry.getDataObject();
            DesignerTopComponent designerTopComponent = new DesignerTopComponent(dobj);
            designerTopComponent.open();
            designerTopComponent.requestActive();
        }
    }
}

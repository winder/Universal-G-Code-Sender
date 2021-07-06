package com.willwinder.ugs.nbp.designer.platform;

import com.willwinder.ugs.nbp.designer.DesignerTopComponent;
import org.openide.cookies.OpenCookie;

public class UgsOpenSupport implements OpenCookie {

    private final UgsDataObject.Entry entry;

    public UgsOpenSupport(UgsDataObject.Entry entry) {
        this.entry = entry;
    }

    @Override
    public void open() {
        UgsDataObject dobj = (UgsDataObject) entry.getDataObject();
        DesignerTopComponent designerTopComponent = new DesignerTopComponent(dobj);
        designerTopComponent.open();
        designerTopComponent.requestActive();
    }
}

package com.willwinder.ugs.nbp.designer.platform;

import com.willwinder.ugs.nbp.designer.DesignerTopComponent;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.OpenCookie;
import org.openide.loaders.OpenSupport;
import org.openide.windows.CloneableTopComponent;

public class UgsOpenSupport extends OpenSupport implements OpenCookie, CloseCookie {

    public UgsOpenSupport(UgsDataObject.Entry entry) {
        super(entry);
    }


    @Override
    protected CloneableTopComponent createCloneableTopComponent() {
        UgsDataObject dobj = (UgsDataObject) entry.getDataObject();
        DesignerTopComponent tc = new DesignerTopComponent();
        tc.setDisplayName(dobj.getName());
        return tc;
    }
}

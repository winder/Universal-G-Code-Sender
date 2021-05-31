package com.willwinder.ugs.nbp.designer.platform;

import com.willwinder.ugs.nbp.designer.DesignerTopComponent;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.OpenCookie;
import org.openide.loaders.OpenSupport;
import org.openide.windows.CloneableTopComponent;

public class SvgOpenSupport extends OpenSupport implements OpenCookie, CloseCookie {

    public SvgOpenSupport(SvgDataObject.Entry entry) {
        super(entry);
    }

    @Override
    protected CloneableTopComponent createCloneableTopComponent() {
        SvgDataObject dobj = (SvgDataObject) entry.getDataObject();
        DesignerTopComponent tc = new DesignerTopComponent();
        tc.setDisplayName(dobj.getName());
        return tc;
    }
}

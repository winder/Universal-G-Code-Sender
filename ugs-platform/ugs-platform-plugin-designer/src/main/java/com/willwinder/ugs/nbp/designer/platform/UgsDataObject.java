/*
    Copyright 2020-2021 Will Winder

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.willwinder.ugs.nbp.designer.platform;

import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.CookieSet;

import java.io.IOException;

@MIMEResolver.ExtensionRegistration(
        displayName = "UGS design",
        mimeType = "application/x-ugs",
        extension = {"ugsd", "UGSD"},
        position = 2
)
@DataObject.Registration(
        mimeType = "application/x-ugs",
        iconBase = "com/willwinder/ugs/nbp/designer/platform/edit.png",
        displayName = "UGS design",
        position = 300
)
public class UgsDataObject extends MultiDataObject {

    public UgsDataObject(FileObject pf, MultiFileLoader loader) throws IOException {
        super(pf, loader);

        CookieSet cookies = getCookieSet();
        cookies.add(new UgsCloseCookie(this));
        cookies.add(new UgsOpenSupport(getPrimaryEntry()));
    }

    @Override
    protected int associateLookup() {
        return 1;
    }

    @Override
    public void setModified(boolean isModified) {
        if (isModified) {
            if (getCookie(UgsSaveCookie.class) == null) {
                getCookieSet().add(new UgsSaveCookie(this));
            }
        } else {
            SaveCookie cookie = getCookie(UgsSaveCookie.class);
            if (cookie != null) {
                getCookieSet().remove(cookie);
            }
        }
        super.setModified(isModified);
    }
}

/*
    Copyright 2016-2021 Will Winder

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
package com.willwinder.ugs.nbp.editor;

import com.willwinder.ugs.nbp.lib.lookup.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;

import java.io.IOException;
import java.util.prefs.Preferences;

import static com.willwinder.ugs.nbp.editor.EditorUtils.openFile;

@Messages({
        "LBL_Gcode_LOADER=Files of Gcode"
})
@MIMEResolver.ExtensionRegistration(
        displayName = "#LBL_Gcode_LOADER",
        mimeType = GcodeLanguageConfig.MIME_TYPE,
        extension = {"gcode", "GCODE", "cnc", "CNC", "nc", "NC", "ngc", "NGC", "tap", "TAP", "txt", "TXT", "gc", "GC"},
        position = 1
)
@DataObject.Registration(
        mimeType = GcodeLanguageConfig.MIME_TYPE,
        iconBase = "com/willwinder/ugs/nbp/editor/edit.png",
        displayName = "#LBL_Gcode_LOADER",
        position = 300
)
public class GcodeDataObject extends MultiDataObject {
    public GcodeDataObject(FileObject pf, MultiFileLoader loader) throws IOException {
        super(pf, loader);

        Preferences prefs = NbPreferences.forModule(EditorOptionsPanel.class);
        boolean loadEditor = prefs.getBoolean(EditorOptionsPanel.SHOW_ON_OPEN, true);

        if (loadEditor) {
            registerEditor(GcodeLanguageConfig.MIME_TYPE, true);

            // Add an editor cookie so that EditorUtils can find it
            getCookieSet().add((EditorCookie) () -> {
                OpenCookie cookie = getCookie(OpenCookie.class);
                if (cookie != null) {
                    cookie.open();
                }
            });
        } else {
            openFile(pf);
        }
    }

    @Override
    protected int associateLookup() {
        return 1;
    }
}

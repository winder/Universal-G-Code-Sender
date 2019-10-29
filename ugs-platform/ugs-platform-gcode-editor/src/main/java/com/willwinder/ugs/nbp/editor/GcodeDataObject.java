/*
    Copyright 2016-2019 Will Winder

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

import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.NbBundle.Messages;

import java.io.IOException;

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
@ActionReferences({
        @ActionReference(
                path = "Loaders/text/xgcode/Actions",
                id = @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
                position = 100,
                separatorAfter = 200
        ),
        @ActionReference(
                path = "Loaders/text/xgcode/Actions",
                id = @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
                position = 300
        ),
        @ActionReference(
                path = "Loaders/text/xgcode/Actions",
                id = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
                position = 400,
                separatorAfter = 500
        ),
        @ActionReference(
                path = "Loaders/text/xgcode/Actions",
                id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
                position = 600
        ),
        @ActionReference(
                path = "Loaders/text/xgcode/Actions",
                id = @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
                position = 700,
                separatorAfter = 800
        ),
        @ActionReference(
                path = "Loaders/text/xgcode/Actions",
                id = @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
                position = 900,
                separatorAfter = 1000
        ),
        @ActionReference(
                path = "Loaders/text/xgcode/Actions",
                id = @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
                position = 1100,
                separatorAfter = 1200
        ),
        @ActionReference(
                path = "Loaders/text/xgcode/Actions",
                id = @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
                position = 1300
        ),
        @ActionReference(
                path = "Loaders/text/xgcode/Actions",
                id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
                position = 1400
        )
})
public class GcodeDataObject extends MultiDataObject {

    public GcodeDataObject(FileObject pf, MultiFileLoader loader) throws IOException {
        super(pf, loader);
        registerEditor(GcodeLanguageConfig.MIME_TYPE, true);
    }

    @Override
    protected int associateLookup() {
        return 1;
    }
}

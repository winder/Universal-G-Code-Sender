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
package com.willwinder.ugs.nbp.designer.platform;

import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;

import java.io.IOException;

@MIMEResolver.ExtensionRegistration(
        displayName = "UGS design",
        mimeType = "application/x-ugs",
        extension = {"ugsd", "UGSD"},
        position = 1
)
@DataObject.Registration(
        mimeType = "application/x-ugs",
        iconBase = "com/willwinder/ugs/nbp/designer/edit.png",
        displayName = "UGS design",
        position = 300
)
@ActionReferences({
        @ActionReference(
                path = "Loaders/text/x-ugs/Menu",
                id = @ActionID(category = "Edit", id = "org.openide.actions.UndoAction"),
                position = 100
        ),
        @ActionReference(
                path = "Loaders/text/x-ugs/Menu",
                id = @ActionID(category = "Edit", id = "org.openide.actions.RedoAction"),
                position = 200
        ),
        @ActionReference(
                path = "Loaders/text/x-ugs/Menu",
                id = @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
                position = 300
        ),
        @ActionReference(
                path = "Loaders/text/x-ugs/Menu",
                id = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
                position = 400,
                separatorAfter = 500
        ),
        @ActionReference(
                path = "Loaders/text/x-ugs/Menu",
                id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
                position = 600
        ),
        @ActionReference(
                path = "Loaders/text/x-ugs/Actions",
                id = @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
                position = 100,
                separatorAfter = 200
        ),
        @ActionReference(
                path = "Loaders/text/x-ugs/Actions",
                id = @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
                position = 700,
                separatorAfter = 800
        ),
        @ActionReference(
                path = "Loaders/text/x-ugs/Actions",
                id = @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
                position = 900,
                separatorAfter = 1000
        ),
        @ActionReference(
                path = "Loaders/text/x-ugs/Actions",
                id = @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
                position = 1100,
                separatorAfter = 1200
        ),
        @ActionReference(
                path = "Loaders/text/x-ugs/Actions",
                id = @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
                position = 1300
        ),
        @ActionReference(
                path = "Loaders/text/x-ugs/Actions",
                id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
                position = 1400
        )
})
public class UgsDataObject extends MultiDataObject {

    public UgsDataObject(FileObject pf, MultiFileLoader loader) throws IOException {
        super(pf, loader);
        registerEditor("application/x-ugs", true);
    }

    @Override
    protected int associateLookup() {
        return 1;
    }
}

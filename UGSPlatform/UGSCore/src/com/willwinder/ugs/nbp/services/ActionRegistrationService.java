/*
    Copywrite 2016 Will Winder

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
package com.willwinder.ugs.nbp.services;

import com.google.common.base.Joiner;
import java.io.IOException;
import java.util.Arrays;
import javax.swing.Action;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author wwinder
 */
@ServiceProvider(service=ActionRegistrationService.class) 
public class ActionRegistrationService {
    /**
     * Registers an action with the platform along with optional shortcuts and
     * menu items.
     * @param name Display name of the action.
     * @param category Category in the Keymap tool.
     * @param shortcut Default shortcut, use an empty string or null for none.
     * @param menuPath Menu location starting with "Menu", like "Menu/File"
     * @param action an action object to attach to the action entry.
     * @throws IOException 
     */
    public void registerAction(String name, String category, String shortcut, String menuPath, Action action) throws IOException {
        ///////////////////////
        // Add/Update Action //
        ///////////////////////
        String originalFile = "Actions/" + category + "/" + name + ".instance";
        FileObject in = getFolderAt("Actions/" + category);
        FileObject obj = in.getFileObject(name, "instance");
        if (obj == null) {
            obj = in.createData(name, "instance");
        }
        action.putValue(Action.NAME, name);
        obj.setAttribute("instanceCreate", action);
        obj.setAttribute("instanceClass", action.getClass().getName());

        /////////////////////
        // Add/Update Menu //
        /////////////////////
        in = getFolderAt(menuPath);
        obj = in.getFileObject(name, "shadow");
        // Create if missing.
        if (obj == null) {
            obj = in.createData(name, "shadow");
            obj.setAttribute("originalFile", originalFile);
        }

        /////////////////////////
        // Add/Update Shortcut //
        /////////////////////////
        in = getFolderAt("Shortcuts");
        obj = in.getFileObject(shortcut, "shadow");
        if (obj == null) {
            obj = in.createData(shortcut, "shadow");
            obj.setAttribute("originalFile", originalFile);
        }
    }

    private FileObject getFolderAt(String inputPath) throws IOException {
        FileObject existing = FileUtil.getConfigFile(inputPath);
        if (existing != null)
            return existing;

        String parts[] = inputPath.split("/");
        FileObject base = FileUtil.getConfigFile(parts[0]);
        if (base == null) return null;

        for (int i = 1; i < parts.length; i++) {
            String path = Joiner.on('/').join(Arrays.copyOfRange(parts,0,i+1));
            FileObject next = FileUtil.getConfigFile(path);
            if (next == null) {
                next = base.createFolder(parts[i]);
            }
            base = next;
        }

        return FileUtil.getConfigFile(inputPath);
    }
}

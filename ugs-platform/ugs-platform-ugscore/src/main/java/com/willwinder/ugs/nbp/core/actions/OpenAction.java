/*
    Copywrite 2015-2018 Will Winder

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
package com.willwinder.ugs.nbp.core.actions;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.utils.SwingHelpers;
import org.apache.commons.lang3.ArrayUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.windows.TopComponent;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@ActionID(
        category = LocalizingService.OpenCategory,
        id = LocalizingService.OpenActionId)
@ActionRegistration(
        iconBase = OpenAction.ICON_BASE,
        displayName = "resources.MessagesBundle#" + LocalizingService.OpenTitleKey,
        lazy = false)
@ActionReferences({
        @ActionReference(
                path = LocalizingService.OpenWindowPath,
                position = 10),
        @ActionReference(
                path = "Toolbars/File",
                position = 10),
        @ActionReference(
                path = "Shortcuts",
                name = "M-O")
})
public final class OpenAction extends AbstractAction {

    public static final String ICON_BASE = "resources/icons/open.svg";
    private BackendAPI backend;

    public OpenAction() {
        this.backend = CentralLookup.getDefault().lookup(BackendAPI.class);

        putValue("iconBase", ICON_BASE);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_BASE, false));
        putValue("menuText", LocalizingService.OpenTitle);
        putValue(NAME, LocalizingService.OpenTitle);
    }

    @Override
    public boolean isEnabled() {
        return backend != null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String sourceDir = backend.getSettings().getLastOpenedFilename();
        SwingHelpers
                .openFile(sourceDir)
                .ifPresent(f -> {
                    try {
                        if(closeOpenEditors()) {
                            DataObject.find(FileUtil.toFileObject(f))
                                    .getLookup()
                                    .lookup(OpenCookie.class)
                                    .open();
                        }
                    } catch (DataObjectNotFoundException ex) {
                        ex.printStackTrace();
                    }
                });
    }

    /**
     * Close all open editors
     *
     * @return true if all editors could be closed
     */
    private boolean closeOpenEditors() {
        List<TopComponent> editorCookies = TopComponent.getRegistry().getOpened()
                .stream()
                .filter(topComponent ->
                        Arrays.stream(ArrayUtils.nullToEmpty(topComponent.getActivatedNodes(), Node[].class))
                            .anyMatch(node -> node.getCookie(EditorCookie.class) != null))
                .collect(Collectors.toList());

        boolean closed = true;
        for(TopComponent editorCookie : editorCookies) {
            if (!editorCookie.close()) {
               closed = false;
            }
        }
        return closed;
    }
}

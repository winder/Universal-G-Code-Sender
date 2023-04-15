/*
    Copyright 2023 Will Winder

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
package com.willwinder.ugs.nbp.lib.services;

import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataShadow;
import org.openide.loaders.InstanceDataObject;
import org.openide.util.lookup.ServiceProvider;

import javax.swing.KeyStroke;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * A service that can be used to find actions using their shortcut key mapping
 * in the format defined in {@link org.openide.util.Utilities#keyToString(KeyStroke, boolean)}
 * (remember to use portable format)
 *
 * @author Joacim Breiler
 */
@ServiceProvider(service = ShortcutService.class)
public class ShortcutService implements FileChangeListener {

    private static final Logger LOGGER = Logger.getLogger(ShortcutService.class.getSimpleName());

    /**
     * Cache the found shortcuts to speed things up using the shortcut as key and the actionId as value.
     */
    private final Map<String, String> shortcutMap = new ConcurrentHashMap<>();

    /**
     * The current keymap used
     */
    private String currentKeymap = "NetBeans";

    public ShortcutService() {
        reloadShortcuts();
        FileUtil.getSystemConfigRoot().addRecursiveListener(this);
    }

    /**
     * Finds a shortcut in format defined in {@link org.openide.util.Utilities#stringToKey(String)}
     *
     * @param keyAsString the keymap shortcut
     * @return a shortcut reference
     */
    public Optional<String> getActionIdForShortcut(String keyAsString) {
        return Optional.ofNullable(shortcutMap.get(keyAsString));
    }


    @Override
    public void fileFolderCreated(FileEvent fileEvent) {
        // Not used
    }

    @Override
    public void fileDataCreated(FileEvent fileEvent) {
        if (!fileEvent.getFile().getPath().startsWith(getKeymapConfigRoot())) {
            return;
        }

        DataFolder folder = DataFolder.findFolder(FileUtil.getSystemConfigFile(getKeymapConfigRoot()));
        String keyAsString = fileEvent.getFile().getName();
        Collections.list(folder.children()).stream()
                .filter(DataShadow.class::isInstance).map(DataShadow.class::cast)
                .filter(f -> f.getName().equals(keyAsString))
                .findFirst()
                .ifPresent(ShortcutService.this::setShortcut);
    }

    @Override
    public void fileChanged(FileEvent fileEvent) {
        fileDataCreated(fileEvent);
    }

    @Override
    public void fileDeleted(FileEvent fileEvent) {
        if (!fileEvent.getFile().getPath().startsWith("Keymaps/" + currentKeymap + "/")) {
            return;
        }

        shortcutMap.remove(fileEvent.getFile().getName());
        LOGGER.fine(() -> String.format("Removed shortcut: %s", fileEvent.getFile().getName()));
    }

    @Override
    public void fileRenamed(FileRenameEvent fileRenameEvent) {
        // Not used
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fileAttributeEvent) {
        if (!fileAttributeEvent.getFile().getPath().equals("Keymaps") && fileAttributeEvent.getName().equals("currentKeymap")) {
            return;
        }

        reloadShortcuts();
    }

    private String getKeymapConfigRoot() {
        return "Keymaps/" + currentKeymap;
    }

    private void setShortcut(DataShadow file) {
        InstanceDataObject cookie = file.getCookie(InstanceDataObject.class);
        String actionId = cookie.getPrimaryFile().getPath();
        shortcutMap.put(file.getName(), actionId);
        LOGGER.fine(() -> String.format("Set shortcut: %s -> %s", file.getName(), actionId));
    }

    private void reloadShortcuts() {
        FileObject keymaps = FileUtil.getConfigFile("Keymaps");
        if (keymaps == null || keymaps.getAttribute("currentKeymap") == null) {
            return;
        }

        currentKeymap = keymaps.getAttribute("currentKeymap").toString();
        LOGGER.fine(() -> String.format("Reloading shortcuts using keymap %s", currentKeymap));

        shortcutMap.clear();
        DataFolder folder = DataFolder.findFolder(FileUtil.getSystemConfigFile(getKeymapConfigRoot()));
        Collections.list(folder.children()).stream()
                .filter(DataShadow.class::isInstance).map(DataShadow.class::cast)
                .forEach(this::setShortcut);
    }
}

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

import org.apache.commons.io.IOUtils;
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
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
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
    public static final Map<String, String> shortcutMap = new ConcurrentHashMap<>();

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
        String currentKeymap = getCurrentKeymap();
        String keymapConfigRoot = "Keymaps/" + currentKeymap;
        if (!fileEvent.getFile().getPath().startsWith(keymapConfigRoot)) {
            return;
        }

        DataFolder folder = DataFolder.findFolder(FileUtil.getSystemConfigFile(keymapConfigRoot));
        String keyAsString = fileEvent.getFile().getName();
        Collections.list(folder.children()).stream()
                .filter(DataShadow.class::isInstance).map(DataShadow.class::cast)
                .filter(f -> f.getName().equals(keyAsString))
                .findFirst()
                .ifPresent(ShortcutService::setShortcut);
    }

    @Override
    public void fileChanged(FileEvent fileEvent) {
        fileDataCreated(fileEvent);
    }

    @Override
    public void fileDeleted(FileEvent fileEvent) {
        String currentKeymap = getCurrentKeymap();
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


    private static void setShortcut(DataShadow file) {
        InstanceDataObject cookie = file.getCookie(InstanceDataObject.class);
        String actionId = cookie.getPrimaryFile().getPath();
        shortcutMap.put(file.getName(), actionId);
        LOGGER.fine(() -> String.format("Set shortcut: %s -> %s", file.getName(), actionId));
    }

    private static void reloadShortcuts() {
        String currentKeymap = getCurrentKeymap();
        LOGGER.fine(() -> String.format("Reloading shortcuts using keymap %s", currentKeymap));

        shortcutMap.clear();
        DataFolder folder = DataFolder.findFolder(FileUtil.getSystemConfigFile("Keymaps/" + currentKeymap));
        Collections.list(folder.children()).stream()
                .filter(DataShadow.class::isInstance).map(DataShadow.class::cast)
                .forEach(ShortcutService::setShortcut);
    }

    public static void createShortcut(String id, String category, String shortcut) throws IOException {
        String currentKeymap = getCurrentKeymap();
        FileObject root = FileUtil.getConfigRoot();
        FileObject keyMapsFolder = root.getFileObject("Keymaps");
        if (keyMapsFolder == null) {
            keyMapsFolder = FileUtil.createFolder(root, "Keymaps");
        }

        FileObject keyMapsNetBeansFolder = keyMapsFolder.getFileObject(currentKeymap);
        if (keyMapsNetBeansFolder == null) {
            keyMapsNetBeansFolder = keyMapsFolder.createFolder(currentKeymap);
        }

        FileObject shortcutFile = keyMapsNetBeansFolder.getFileObject(shortcut, "shadow");
        if (shortcutFile == null) {
            shortcutFile = keyMapsNetBeansFolder.createData(shortcut, "shadow");
            OutputStream outputStream = shortcutFile.getOutputStream();
            IOUtils.write("nbfs://nbhost/SystemFileSystem/Actions/" + category + "/" + id + ".instance", outputStream, Charset.defaultCharset());
            outputStream.close();
        }
    }

    private static String getCurrentKeymap() {
        try {
            FileObject keymaps = FileUtil.getConfigFile("Keymaps");
            if (keymaps == null) {
                FileObject root = FileUtil.getConfigRoot();
                keymaps = FileUtil.createFolder(root, "Keymaps");
            }

            if (keymaps.getAttribute("currentKeymap") == null) {
                keymaps.setAttribute("currentKeymap", "NetBeans");
            }

            return keymaps.getAttribute("currentKeymap").toString();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could get keymaps folder", e);
            return "NetBeans";
        }
    }
}

/*
    Copyright 2016-2020 Will Winder

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

import org.apache.commons.lang3.StringUtils;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

/**
 * A service for dynamically registering actions.
 *
 * @author wwinder
 */
@ServiceProvider(service = ActionRegistrationService.class)
public class ActionRegistrationService {

    private static final String SHADOW = "shadow";
    private static final Logger LOGGER = Logger.getLogger(ActionRegistrationService.class.getSimpleName());

    private Map<String, List<ActionReference>> actionCache = new HashMap<>();

    /**
     * Extract action ID from the instance file path. For example 'com-willwinder-ugs-nbp-editor-actions-RunFromHere' from 'Action/Machine/com-willwinder-ugs-nbp-editor-actions-RunFromHere.instance'.
     *
     * @param path full path to the action instance file.
     */
    private String extractId(String path) {
        return path.substring(
                path.lastIndexOf('/') + 1,
                path.lastIndexOf('.')
        );
    }

    /**
     * Extract the action path from the instance file path. For example 'Machine/Jog' from 'Action/Machine/Jog/com-willwinder-ugs-nbp-editor-actions-RunFromHere.instance'.
     *
     * @param path full path to the action instance file.
     */
    private String extractCategory(String path) {
        return path.substring(
                path.indexOf('/') + 1,
                path.lastIndexOf('/')
        );
    }

    /**
     * Update an {@link ActionReference} already registered with the platform along with optional shortcuts and
     * menu items.
     *
     * @param reference    Name and ID of the action.
     * @param category     Category in the Keymap tool.
     * @param shortcut     Default shortcut, use an empty string or null for none.
     * @param menuPath     Menu location starting with "Menu", like "Menu/Head/Hats"
     * @param menuPosition Defines how the menu item should be ordered
     * @param localMenu    Localized menu location starting with "Menu", like "Menu/Cabeza/Sombreros"
     * @throws IOException if the action couldn't be registered
     */
    public void updateAction(ActionReference reference, String category, String shortcut, String menuPath, int menuPosition, String localMenu) throws IOException {
        registerAction(reference.getId(), reference.getName(), category, shortcut, menuPath, menuPosition, localMenu, reference.getAction());
    }

    /**
     * Registers an action with the platform along with optional shortcuts and
     * menu items.
     *
     * @param id           The unique id of the action
     * @param name         Display name of the action.
     * @param category     Category in the Keymap tool.
     * @param shortcut     Default shortcut, use an empty string or null for none.
     * @param menuPath     Menu location starting with "Menu", like "Menu/Head/Hats"
     * @param menuPosition Defines how the menu item should be ordered
     * @param localMenu    Localized menu location starting with "Menu", like "Menu/Cabeza/Sombreros"
     * @param action       an action object to attach to the action entry.
     * @throws IOException if the action couldn't be registered
     */
    public void registerAction(String id, String name, String category, String shortcut, String menuPath, int menuPosition, String localMenu, Action action) throws IOException {
        ///////////////////////
        // Add/Update Action //
        ///////////////////////
        String originalFile = "Actions/" + category + "/" + id + ".instance";
        FileObject root = FileUtil.getConfigRoot();
        FileObject in = FileUtil.createFolder(root, "Actions/" + category);
        in.refresh();

        FileObject obj = in.getFileObject(id, "instance");
        if (obj == null) {
            obj = in.createData(id, "instance");
        }
        action.putValue(Action.NAME, name);
        obj.setAttribute("instanceCreate", action);
        obj.setAttribute("instanceClass", action.getClass().getName());

        /////////////////////
        // Add/Update Menu //
        /////////////////////
        if (StringUtils.isNotEmpty(menuPath) && StringUtils.isNotEmpty(id)) {
            in = createAndLocalizeFullMenu(menuPath, localMenu);

            obj = in.getFileObject(id, SHADOW);
            // Create if missing.
            if (obj == null) {
                obj = in.createData(id, SHADOW);
                obj.setAttribute("originalFile", originalFile);
            }
            obj.setAttribute("position", menuPosition);
        }

        /////////////////////////
        // Add/Update Shortcut //
        /////////////////////////
        if (shortcut != null && shortcut.length() > 0) {
            in = FileUtil.createFolder(root, "Shortcuts");
            obj = in.getFileObject(shortcut, SHADOW);
            if (obj == null) {
                obj = in.createData(shortcut, SHADOW);
                obj.setAttribute("originalFile", originalFile);
            }
        }

        invalidateCache();
    }

    /**
     * Creates a folder path in the netbeans filesystem and sets a localized.
     * display name or each level of the path.
     */
    public FileObject createAndLocalizeFullMenu(String path, String localizedPath) throws IOException {
        FileObject root = FileUtil.getConfigRoot();
        String[] paths = path.split("/");
        String[] names = localizedPath.split("/");
        if (paths.length != names.length) {
            throw new IllegalArgumentException("Path length must equal localized path length: " + path + ", " + localizedPath);
        }
        if (!paths[0].equals(names[0])) {
            throw new IllegalArgumentException(
                    "Path and localized path must be in the same top level directory. Found: "
                            + paths[0] + " and " + names[0]);
        }

        String fullPath = paths[0];
        FileObject in = FileUtil.createFolder(root, fullPath);
        for (int i = 1; i < paths.length; i++) {
            fullPath = fullPath + "/" + paths[i];
            in = FileUtil.createFolder(root, fullPath);
            in.setAttribute("displayName", names[i]);
            in.refresh();
        }

        invalidateCache();
        return in;
    }

    /**
     * Set the display name of an action for a given action reference.
     *
     * @param ref  the action reference to update.
     * @param name display name to set.
     */
    public void overrideActionName(ActionReference ref, String name) {
        overrideActionName(extractCategory(ref.getId()), extractId(ref.getId()), name);
    }

    /**
     * Set the display name of an action for a given category.
     *
     * @param category which category the key is in.
     * @param key      identifier for the action.
     * @param name     display name to set.
     */
    public void overrideActionName(String category, String key, String name) {
        try {
            FileObject root = FileUtil.getConfigRoot();
            FileObject in = FileUtil.createFolder(root, "Actions/" + category);

            FileObject obj = in.getFileObject(key.replaceAll("\\.", "-"), "instance");
            if (obj != null) {
                obj.setAttribute("displayName", name);
            }

            in.refresh();
            invalidateCache();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public Optional<ActionReference> lookupAction(String category, String name) {
        Map<String, List<ActionReference>> categoryActions = getCategoryActions();

        String key = "Actions/" + category;
        if (key != null) {
            if (categoryActions.containsKey(key)) {
                return categoryActions.get(key).stream()
                        .filter(action -> action.getId().contains(name))
                        .findFirst();
            }
            return Optional.empty();
        }

        return categoryActions.values().stream().flatMap(Collection::stream)
                .filter(action -> action.getId().contains(name))
                .findFirst();
    }


    /**
     * Get actions organized by category.
     */
    public Map<String, List<ActionReference>> getCategoryActions() {
        if(actionCache.isEmpty()) {
            FileObject rootFileObject = FileUtil.getConfigFile("Actions/");
            actionCache = getCategoryActions(rootFileObject);
        }

        return actionCache;
    }

    private Map<String, List<ActionReference>> getCategoryActions(final FileObject rootFileObject) {
        return recursiveAddCategoryActions(rootFileObject, new HashMap<>());
    }

    /**
     * Returns a list of all categories
     *
     * @return a set with all categories
     */
    public Set<String> getCategories() {
        return new HashSet<>(getCategoryActions().keySet());
    }

    /**
     * A recursive method that walks through all children of a file object and tries to find all actions.
     *
     * @param category        the file object for a category or directory
     * @param categoryActions the result map with all categories and their actions to add all children to
     * @return a map with all categories and their lists of actions
     */
    private Map<String, List<ActionReference>> recursiveAddCategoryActions(final FileObject category, Map<String, List<ActionReference>> categoryActions) {
        List<ActionReference> actionList = categoryActions.getOrDefault(category.getPath(), new ArrayList<>());

        FileObject[] children = category.getChildren();
        for (FileObject child : children) {
            if (child.getPath().endsWith(".instance")) {
                getActionFromFileObject(child).ifPresent(actionList::add);
            }
            recursiveAddCategoryActions(child, categoryActions);
        }

        if (!actionList.isEmpty()) {
            actionList.sort(Comparator.comparing(action -> {
                String name = action.getClass().getSimpleName();
                if (action.getAction().getValue(Action.NAME) != null) {
                    name = action.getAction().getValue(Action.NAME).toString();
                }
                return name.replaceAll("&", "");
            }));
            categoryActions.put(category.getPath(), actionList);
        }

        return categoryActions;
    }

    /**
     * Tries to get an action from a file object. If the file object doesn't have an action instance attached to it
     * or if the action class couldn't be loaded this will return an empty optional.
     *
     * @param actionFileObject the file object to try and retrieve the action instance from.
     * @return an optional with an action if found or else an empty optional.
     */
    private Optional<ActionReference> getActionFromFileObject(FileObject actionFileObject) {
        try {
            DataObject dob = DataObject.find(actionFileObject);
            InstanceCookie cookie = dob.getLookup().lookup(InstanceCookie.class);
            if (cookie != null) {
                Object o = cookie.instanceCreate();
                if (o instanceof Action) {
                    ActionReference actionReference = new ActionReference();
                    actionReference.setAction((Action) o);
                    actionReference.setId(actionFileObject.getPath());
                    return Optional.of(actionReference);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.warning(String.format("Could not load class for action %s", actionFileObject.getPath()));
        }
        return Optional.empty();
    }

    /**
     * Returns a action reference by a given file id.
     *
     * @param id a id for a action (ie. Action/UndoAction.instance).
     * @return an optional with an action if found or else an empty optional.
     */
    public Optional<ActionReference> getActionById(String id) {
        FileObject configFile = FileUtil.getConfigFile(id);
        if (configFile == null) {
            return Optional.empty();
        }

        return getActionFromFileObject(configFile);
    }

    /**
     * Invalidates the action cache and forces to reread all actions
     */
    private void invalidateCache() {
        actionCache = new HashMap<>();
    }

    /**
     * Returns all actions by the given category
     * @param category the name of the category to fetch
     * @return a list of actions
     */
    public List<ActionReference> getActionsByCategory(String category) {
        return new ArrayList<>(getCategoryActions().getOrDefault(category, Collections.emptyList()));
    }
}

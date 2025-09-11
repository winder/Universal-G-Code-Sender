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
package com.willwinder.ugs.nbp.designer.platform;

import com.google.common.io.Files;
import com.willwinder.ugs.nbp.designer.actions.CopyAction;
import com.willwinder.ugs.nbp.designer.actions.DeleteAction;
import com.willwinder.ugs.nbp.designer.actions.PasteAction;
import com.willwinder.ugs.nbp.designer.actions.RedoAction;
import com.willwinder.ugs.nbp.designer.actions.SelectAllAction;
import com.willwinder.ugs.nbp.designer.actions.UndoAction;
import com.willwinder.ugs.nbp.designer.io.DesignWriter;
import com.willwinder.ugs.nbp.designer.io.gcode.GcodeDesignWriter;
import com.willwinder.ugs.nbp.designer.logic.ControllerFactory;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;
import org.openide.util.Utilities;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.text.DefaultEditorKit;
import java.io.File;

public class PlatformUtils {
    public static final String UNDO_KEY = "undo";
    public static final String REDO_KEY = "redo";
    public static final String DELETE_KEY = "delete";
    public static final String ERASE_KEY = "erase";


    private static final DeleteAction DELETE_ACTION = new DeleteAction();
    private static final SelectAllAction SELECT_ALL_ACTION = new SelectAllAction();
    private static final CopyAction COPY_ACTION = new CopyAction();
    private static final PasteAction PASTE_ACTION = new PasteAction();
    private static final UndoAction UNDO_ACTION = new UndoAction();
    private static final RedoAction REDO_ACTION = new RedoAction();

    private PlatformUtils() {
    }

    public static void registerActions(ActionMap actionMap, TopComponent component) {
        actionMap.put(DELETE_KEY, DELETE_ACTION);
        actionMap.put(ERASE_KEY, DELETE_ACTION);
        actionMap.put(DefaultEditorKit.selectAllAction, SELECT_ALL_ACTION);
        actionMap.put(DefaultEditorKit.copyAction, COPY_ACTION);
        actionMap.put(DefaultEditorKit.pasteAction, PASTE_ACTION);
        actionMap.put(UNDO_KEY, UNDO_ACTION);
        actionMap.put(REDO_KEY, REDO_ACTION);

        // Need to make special input maps as this normally is handled by the texteditor
        InputMap inputMap = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(Utilities.stringToKey("BACK_SPACE"), ERASE_KEY);
        inputMap.put(Utilities.stringToKey("DELETE"), DELETE_KEY);
        inputMap.put(Utilities.stringToKey("D-C"), DefaultEditorKit.copyAction);
        inputMap.put(Utilities.stringToKey("D-V"), DefaultEditorKit.pasteAction);
        inputMap.put(Utilities.stringToKey("D-A"), DefaultEditorKit.selectAllAction);
        inputMap.put(Utilities.stringToKey("D-Z"), UNDO_KEY);
        inputMap.put(Utilities.stringToKey("SD-Z"), REDO_KEY);
    }

    public static void exportAndLoadGcode(String name) {
        try {
            DesignWriter designWriter = new GcodeDesignWriter();
            File file = new File(Files.createTempDir(), name + ".gcode");
            designWriter.write(file, ControllerFactory.getController());
            CentralLookup.getDefault().lookup(BackendAPI.class).setGcodeFile(file);
        } catch (Exception e) {
            throw new RuntimeException("Could not generate gcode", e);
        }
    }

    public static void openSettings() {
        SettingsTopComponent settingsTopComponent = TopComponent.getRegistry().getOpened().stream().filter(SettingsTopComponent.class::isInstance)
                .map(SettingsTopComponent.class::cast)
                .findFirst().orElseGet(() -> {
                    SettingsTopComponent topComponent = SettingsTopComponent.findInstance();
                    dockComponentToPreferredMode(topComponent, com.willwinder.ugs.nbp.lib.Mode.LEFT_TOP);
                    return topComponent;
                });
        settingsTopComponent.requestActive();
    }

    private static void dockComponentToPreferredMode(TopComponent topComponent, String mode) {
        if (!topComponent.isOpened()) {
            Mode preferredMode = WindowManager.getDefault()
                    .findMode(mode);
            if (preferredMode != null) {
                preferredMode.dockInto(topComponent);
            }
            topComponent.open();
        }
    }

    public static void openEntitesTree() {
        EntitiesTreeTopComponent entitiesTreeTopComponent = TopComponent.getRegistry().getOpened().stream().filter(EntitiesTreeTopComponent.class::isInstance)
                .map(EntitiesTreeTopComponent.class::cast)
                .findFirst().orElseGet(() -> {
                    EntitiesTreeTopComponent topComponent = EntitiesTreeTopComponent.findInstance();
                    dockComponentToPreferredMode(topComponent, com.willwinder.ugs.nbp.lib.Mode.LEFT_BOTTOM);
                    return topComponent;
                });
        entitiesTreeTopComponent.requestActive();
    }
}

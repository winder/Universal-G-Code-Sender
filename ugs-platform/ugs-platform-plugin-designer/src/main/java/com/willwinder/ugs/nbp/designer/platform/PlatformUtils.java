package com.willwinder.ugs.nbp.designer.platform;

import com.willwinder.ugs.nbp.designer.actions.*;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import org.openide.util.Utilities;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

import javax.swing.*;
import javax.swing.text.DefaultEditorKit;

public class PlatformUtils {
    public static final String UNDO_KEY = "undo";
    public static final String REDO_KEY = "redo";
    public static final String DELETE_KEY = "delete";

    public static void registerActions(ActionMap actionMap, Controller controller, TopComponent component) {
        actionMap.put(DELETE_KEY, new DeleteAction(controller));
        actionMap.put(DefaultEditorKit.selectAllAction, new SelectAllAction(controller));
        actionMap.put(DefaultEditorKit.copyAction, new CopyAction(controller));
        actionMap.put(DefaultEditorKit.pasteAction, new PasteAction(controller));
        actionMap.put(UNDO_KEY, new UndoAction());
        actionMap.put(REDO_KEY, new RedoAction());

        // Need to make special input maps as this normally is handled by the a texteditor
        InputMap inputMap = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(Utilities.stringToKey("BACK_SPACE"), DELETE_KEY);
        inputMap.put(Utilities.stringToKey("D-C"), DefaultEditorKit.copyAction);
        inputMap.put(Utilities.stringToKey("D-V"), DefaultEditorKit.pasteAction);
        inputMap.put(Utilities.stringToKey("D-A"), DefaultEditorKit.selectAllAction);
        inputMap.put(Utilities.stringToKey("D-Z"), UNDO_KEY);
        inputMap.put(Utilities.stringToKey("SD-Z"), REDO_KEY);
    }

    public static SettingsTopComponent openSettings(Controller controller) {
        SettingsTopComponent component = TopComponent.getRegistry().getOpened().stream().filter(SettingsTopComponent.class::isInstance)
                .map(SettingsTopComponent.class::cast)
                .findFirst().orElseGet(() -> {
                    SettingsTopComponent topComponent = new SettingsTopComponent();
                    topComponent.open();

                    Mode editorMode = WindowManager.getDefault().findMode("top_left");
                    editorMode.dockInto(topComponent);
                    return topComponent;
                });

        if (!controller.getSelectionManager().getSelection().isEmpty()) {
            component.requestActive();
        }
        return component;
    }

    public static EntitiesTreeTopComponent openEntitesTree(Controller controller) {
        EntitiesTreeTopComponent component = TopComponent.getRegistry().getOpened().stream().filter(EntitiesTreeTopComponent.class::isInstance)
                .map(EntitiesTreeTopComponent.class::cast)
                .findFirst().orElseGet(() -> {
                    EntitiesTreeTopComponent topComponent = new EntitiesTreeTopComponent();
                    topComponent.open();

                    Mode editorMode = WindowManager.getDefault().findMode("bottom_left");
                    editorMode.dockInto(topComponent);
                    return topComponent;
                });

        if (!controller.getSelectionManager().getSelection().isEmpty()) {
            component.requestActive();
        }
        return component;
    }
}

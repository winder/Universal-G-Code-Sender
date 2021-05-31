package com.willwinder.ugs.nbp.designer;

import com.willwinder.ugs.nbp.designer.logic.Controller;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

public class Utils {
    public static SettingsTopComponent openSettings(Controller controller) {
        SettingsTopComponent component = TopComponent.getRegistry().getOpened().stream().filter(t -> t instanceof SettingsTopComponent)
                .map(t -> (SettingsTopComponent) t)
                .findFirst().orElseGet(() -> {
                    SettingsTopComponent topComponent = new SettingsTopComponent();
                    topComponent.open();

                    Mode editorMode = WindowManager.getDefault().findMode("top_left");
                    editorMode.dockInto(topComponent);
                    return topComponent;
                });

        component.updateController(controller);
        if (!controller.getSelectionManager().getSelection().isEmpty()) {
            component.requestActive();
        }
        return component;
    }

    public static EntitiesTreeTopComponent openEntitesTree(Controller controller) {
        EntitiesTreeTopComponent component = TopComponent.getRegistry().getOpened().stream().filter(t -> t instanceof EntitiesTreeTopComponent)
                .map(t -> (EntitiesTreeTopComponent) t)
                .findFirst().orElseGet(() -> {
                    EntitiesTreeTopComponent topComponent = new EntitiesTreeTopComponent();
                    topComponent.open();

                    Mode editorMode = WindowManager.getDefault().findMode("bottom_left");
                    editorMode.dockInto(topComponent);
                    return topComponent;
                });

        component.updateController(controller);
        if (!controller.getSelectionManager().getSelection().isEmpty()) {
            component.requestActive();
        }
        return component;
    }
}

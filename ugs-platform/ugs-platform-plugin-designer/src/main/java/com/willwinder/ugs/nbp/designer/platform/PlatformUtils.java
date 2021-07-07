package com.willwinder.ugs.nbp.designer.platform;

import com.willwinder.ugs.nbp.designer.logic.Controller;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

public class PlatformUtils {
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

        component.updateController(controller);
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

        component.updateController(controller);
        if (!controller.getSelectionManager().getSelection().isEmpty()) {
            component.requestActive();
        }
        return component;
    }
}

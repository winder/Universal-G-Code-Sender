package com.willwinder.ugs.nbp.designer;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.nbp.designer.gcode.SimpleGcodeRouter;
import com.willwinder.ugs.nbp.designer.gcode.path.GcodePath;
import com.willwinder.ugs.nbp.designer.gcode.toolpaths.SimpleOnPath;
import com.willwinder.ugs.nbp.designer.gcode.toolpaths.SimplePocket;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.Settings;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

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

    public static String toGcode(Controller controller, List<Entity> entities) {
        Settings settings = controller.getSettings();

        SimpleGcodeRouter gcodeRouter = new SimpleGcodeRouter();
        gcodeRouter.setSafeHeight(settings.getSafeHeight());

        List<String> collect = entities.stream()
                .filter(Cuttable.class::isInstance)
                .map(Cuttable.class::cast)
                .map(cuttable -> {
                    GcodePath gcodePath = gcodeRouter.toPath(cuttable, new AffineTransform());

                    switch (cuttable.getCutType()) {
                        case POCKET:
                            SimplePocket simplePocket = new SimplePocket(gcodePath);
                            simplePocket.setTargetDepth(cuttable.getCutDepth());
                            simplePocket.setToolDiameter(settings.getToolDiameter());
                            simplePocket.setStepOver(settings.getToolStepOver());
                            gcodePath = simplePocket.toGcodePath();

                            /*SimpleOutline simpleOutline = new SimpleOutline(gcodePath);
                            simpleOutline.setDepth(cuttable.get);
                            gcodePath = simpleOutline.toGcodePath();*/
                            break;
                        case ON_PATH:
                            SimpleOnPath simpleOnPath = new SimpleOnPath(gcodePath);
                            //simpleOutline.setDepth(shape.getCutSettings().getDepth());
                            gcodePath = simpleOnPath.toGcodePath();
                            break;
                        default:
                            return "";
                    }

                    try {
                        return gcodeRouter.toGcode(gcodePath);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return "";
                }).filter(line -> !line.isEmpty())
                .collect(Collectors.toList());

        return String.join("\n", collect);
    }
}

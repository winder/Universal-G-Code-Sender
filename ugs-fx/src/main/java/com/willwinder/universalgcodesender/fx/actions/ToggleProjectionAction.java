package com.willwinder.universalgcodesender.fx.actions;

import com.willwinder.universalgcodesender.fx.settings.VisualizerSettings;
import com.willwinder.universalgcodesender.i18n.Localization;
import javafx.event.ActionEvent;

public class ToggleProjectionAction extends BaseAction {

    public static final String ICON_PARALELL = "icons/paralell.svg";
    public static final String ICON_PERSPECTIVE = "icons/perspective.svg";

    public ToggleProjectionAction() {
        super(null, Localization.getString("platform.visualizer.toggleProjection"), Localization.getString("actions.category.visualizer"), ICON_PARALELL);
        VisualizerSettings.getInstance().useParallelCameraProperty().addListener((observable, oldValue, newValue) -> updateIcon(newValue));
        updateIcon(VisualizerSettings.getInstance().useParallelCameraProperty().get());
    }


    private void updateIcon(boolean useParalell) {
        iconProperty().set(useParalell ? ICON_PARALELL : ICON_PERSPECTIVE);
    }

    @Override
    public void handleAction(ActionEvent event) {
        VisualizerSettings.getInstance().useParallelCameraProperty().set(!VisualizerSettings.getInstance().useParallelCameraProperty().get());
    }
}
package com.willwinder.universalgcodesender.fx.actions;

import com.willwinder.universalgcodesender.fx.component.visualizer.VisualizerSettings;
import com.willwinder.universalgcodesender.i18n.Localization;
import javafx.event.ActionEvent;

public class ToggleMachineVisualizationAction extends BaseAction {

    private static final String ICON_BASE = "icons/cnc.svg";


    public ToggleMachineVisualizationAction() {
        super(Localization.getString("settings.visializer.toggleMachine"), ICON_BASE);
        selectedProperty().set(VisualizerSettings.getInstance().showMachineProperty().get());
    }

    @Override
    public void handleAction(ActionEvent event) {
        VisualizerSettings.getInstance().showMachineProperty().set(!VisualizerSettings.getInstance().showMachineProperty().get());
    }
}
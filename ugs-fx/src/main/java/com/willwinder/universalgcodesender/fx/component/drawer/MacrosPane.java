package com.willwinder.universalgcodesender.fx.component.drawer;

import com.willwinder.universalgcodesender.fx.actions.MacroAction;
import com.willwinder.universalgcodesender.fx.control.ActionButton;
import com.willwinder.universalgcodesender.fx.model.MacroAdapter;
import com.willwinder.universalgcodesender.fx.service.ActionRegistry;
import com.willwinder.universalgcodesender.fx.service.MacroRegistry;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;

public class MacrosPane extends FlowPane {
    public MacrosPane() {
        setHgap(10);
        setVgap(10);
        setPadding(new Insets(10));

        MacroRegistry.getInstance().getMacros().addListener((ListChangeListener<MacroAdapter>) c -> onUpdatedMacros());
        onUpdatedMacros();
    }

    private void onUpdatedMacros() {
        getChildren().clear();
        ActionRegistry.getInstance().getAllActionsOfClass(MacroAction.class)
                .forEach(macroAction -> {
                    Button button = new ActionButton(macroAction, 24);
                    button.setPrefWidth(120);
                    button.setPrefHeight(38);
                    getChildren().add(button);
                });
    }
}

package com.willwinder.universalgcodesender.fx.component.drawer;

import com.willwinder.universalgcodesender.fx.service.ActionRegistry;
import com.willwinder.universalgcodesender.fx.actions.MacroAction;
import com.willwinder.universalgcodesender.fx.control.ActionButton;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;

public class MacrosPane extends FlowPane {
    public MacrosPane() {
        setHgap(10);
        setVgap(10);
        setPadding(new Insets(10));


        ActionRegistry.getInstance().getAllActionsOfClass(MacroAction.class)
                .forEach(macroAction -> {
                    Button button = new ActionButton(macroAction, 24);
                    button.setPrefWidth(120);
                    button.setPrefHeight(38);
                    getChildren().add(button);
                });
    }
}

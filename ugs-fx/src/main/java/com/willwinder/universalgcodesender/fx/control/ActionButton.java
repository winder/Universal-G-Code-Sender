package com.willwinder.universalgcodesender.fx.control;

import com.willwinder.universalgcodesender.fx.actions.Action;
import static com.willwinder.universalgcodesender.fx.helper.SvgLoader.loadIcon;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;

public class ActionButton extends Button {
    public ActionButton(Action action) {
        // Register listeners
        setOnAction(action);
        registerPropertyListeners(action);

        setText(action.getTitle());
        setDisable(!action.isEnabled());
        setIcon(action.getIcon());
    }

    private void registerPropertyListeners(Action action) {
        action.titleProperty().addListener((v, o, n) -> setText(n));
        action.enabledProperty().addListener((v, o, n) -> setDisable(!n));
        action.iconProperty().addListener((v, o, n) -> setIcon(n));
    }

    private void setIcon(String iconBase) {
        loadIcon(iconBase, 24).ifPresent(image -> setGraphic(new ImageView(image)));
    }
}

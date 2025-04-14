package com.willwinder.universalgcodesender.fx.control;

import com.willwinder.universalgcodesender.fx.actions.Action;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;

import static com.willwinder.universalgcodesender.fx.helper.SvgLoader.loadIcon;

public class ActionButton extends Button {

    public static final int SIZE_NORMAL = 24;
    public static final int SIZE_LARGE = 32;
    private final Action action;
    private final BooleanProperty showText = new SimpleBooleanProperty(true);
    private final IntegerProperty iconSize = new SimpleIntegerProperty(SIZE_NORMAL);

    public ActionButton(Action action) {
        this(action, SIZE_NORMAL);
    }

    public ActionButton(Action action, int size) {
        this.action = action;
        this.iconSize.setValue(size);

        setOnAction(action);
        registerPropertyListeners(action);

        setText(action.getTitle());
        setTooltip(new Tooltip(action.getTitle()));
        setDisable(!action.isEnabled());
        setIcon(action.getIcon());
    }

    public void setShowText(boolean show) {
        showText.set(show);
        setText(show ? action.getTitle() : null);
    }

    private void registerPropertyListeners(Action action) {
        action.titleProperty().addListener((v, o, n) -> {
            if (showText.get()) {
                setText(n);
            }
        });
        action.enabledProperty().addListener((v, o, n) -> setDisable(!n));
        action.iconProperty().addListener((v, o, n) -> setIcon(n));
        iconSize.addListener((v, o, n) -> setIcon(action.getIcon()));
    }

    private void setIcon(String iconBase) {
        loadIcon(iconBase, iconSize.get()).ifPresent(image -> setGraphic(new ImageView(image)));
    }
}

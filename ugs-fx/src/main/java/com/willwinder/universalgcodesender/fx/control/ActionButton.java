package com.willwinder.universalgcodesender.fx.control;

import com.willwinder.universalgcodesender.fx.actions.Action;
import com.willwinder.universalgcodesender.fx.helper.Colors;
import static com.willwinder.universalgcodesender.fx.helper.SvgLoader.loadImageIcon;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class ActionButton extends Button {

    public static final int SIZE_NORMAL = 24;
    private final Action action;
    private final BooleanProperty showText = new SimpleBooleanProperty(true);
    private final IntegerProperty iconSize = new SimpleIntegerProperty(SIZE_NORMAL);
    private final StringProperty iconColor = new SimpleStringProperty(Colors.BLACKISH.toString());

    public ActionButton(Action action, int size) {
        this(action, size, true);
    }

    public ActionButton(Action action, int size, boolean showText) {
        this.action = action;
        this.iconSize.setValue(size);

        setOnAction(event -> {
            if (event != null) {
                action.handle(event);
            }
        });
        addEventHandler(MouseEvent.MOUSE_CLICKED, action);
        addEventHandler(MouseEvent.MOUSE_PRESSED, action);
        addEventHandler(MouseEvent.MOUSE_RELEASED, action);
        registerPropertyListeners(action);

        setText(action.getTitle());
        setDisable(!action.isEnabled());
        setIcon(action.getIcon());
        setShowText(showText);

        Tooltip tooltip = new Tooltip(action.getTitle());
        tooltip.setShowDelay(Duration.millis(100));
        setTooltip(tooltip);
    }

    public void setShowText(boolean show) {
        showText.set(show);
        setText(show ? action.getTitle() : null);
    }

    public void setIconColor(Color color) {
        this.iconColor.set(color.toString());
        setIcon(action.getIcon());
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
        loadImageIcon(iconBase, iconSize.get(), Color.web(iconColor.get())).ifPresent(this::setGraphic);
    }
}

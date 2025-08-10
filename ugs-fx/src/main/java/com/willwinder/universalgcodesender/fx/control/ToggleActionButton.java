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
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class ToggleActionButton extends ToggleButton {
    public static final int SIZE_NORMAL = 24;
    private final Action action;
    private final BooleanProperty showText = new SimpleBooleanProperty(true);
    private final IntegerProperty iconSize = new SimpleIntegerProperty(SIZE_NORMAL);
    private final StringProperty iconColor = new SimpleStringProperty(Colors.BLACKISH.toString());

    public ToggleActionButton(Action action, int size, boolean showText) {
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
        setSelected(action.selectedProperty().get());

        Tooltip tooltip = new Tooltip(action.getTitle());
        tooltip.setShowDelay(Duration.millis(100));
        setTooltip(tooltip);
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
        action.selectedProperty().addListener((v, o, n) -> setSelected(n));
        iconSize.addListener((v, o, n) -> setIcon(action.getIcon()));
    }

    private void setIcon(String iconBase) {
        loadImageIcon(iconBase, iconSize.get(), Color.web(iconColor.get())).ifPresent(this::setGraphic);
    }
}

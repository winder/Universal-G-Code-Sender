package com.willwinder.universalgcodesender.fx.actions;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.input.MouseEvent;

public abstract class BaseAction implements Action {
    private final BooleanProperty enabled = new SimpleBooleanProperty(true);
    private final BooleanProperty selected = new SimpleBooleanProperty(false);
    private final StringProperty title = new SimpleStringProperty("");
    private final StringProperty icon = new SimpleStringProperty("");
    private final LongPressMouseEventProxy mouseEventHandler = new LongPressMouseEventProxy(300, this::handleMouseEvent);

    public BaseAction() {
    }

    public BaseAction(String title, String icon) {
        this.title.set(title);
        this.icon.set(icon);
    }

    @Override
    public String getId() {
        return this.getClass().getCanonicalName();
    }

    @Override
    public boolean isEnabled() {
        return enabled.get();
    }

    @Override
    public String getTitle() {
        return title.get();
    }

    @Override
    public String getIcon() {
        return icon.get();
    }

    public BooleanProperty enabledProperty() {
        return enabled;
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    public StringProperty titleProperty() {
        return title;
    }

    public StringProperty iconProperty() {
        return icon;
    }

    @Override
    public void handle(Event event) {
        if (event instanceof ActionEvent actionEvent) {
            handleAction(actionEvent);
        } else if (event instanceof MouseEvent mouseEvent) {
            mouseEventHandler.handle(mouseEvent);
        }
    }

    public void handleMouseEvent(MouseEvent mouseEvent) {
    }

    public abstract void handleAction(ActionEvent event);
}

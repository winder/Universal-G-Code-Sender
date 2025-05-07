package com.willwinder.universalgcodesender.fx.actions;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public interface Action extends EventHandler<ActionEvent> {

    String getId();

    boolean isEnabled();

    String getTitle();

    String getIcon();

    BooleanProperty enabledProperty();

    StringProperty titleProperty();

    StringProperty iconProperty();

    BooleanProperty selectedProperty();
}

package com.willwinder.universalgcodesender.fx.actions;

import javafx.application.Platform;
import javafx.event.ActionEvent;

import javax.swing.Action;
import java.beans.PropertyChangeEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;


public class ActionAdapter extends BaseAction {
    private final Action action;

    public static final String MENU_TEXT = "menuText";
    public static final String ICON_BASE = "iconBase";
    public static final String ENABLED = "enabled";

    public ActionAdapter(Class<? extends Action> actionClass) {
        try {
            action = actionClass.getDeclaredConstructor().newInstance();
            action.addPropertyChangeListener(this::onPropertyChanged);
            updatePropertiesFromAction();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private void onPropertyChanged(PropertyChangeEvent propertyChangeEvent) {
        Platform.runLater(this::updatePropertiesFromAction);
    }

    private void updatePropertiesFromAction() {
        titleProperty().set(action.getValue(MENU_TEXT).toString());
        iconProperty().set(action.getValue(ICON_BASE).toString());
        enabledProperty().set((Boolean) action.getValue(ENABLED));
    }

    @Override
    public String toString() {
        return action.toString();
    }

    @Override
    public void handleAction(ActionEvent event) {
        Objects.requireNonNull(action).actionPerformed(new java.awt.event.ActionEvent(event.getSource(), 0, event.getEventType().getName()));
    }


    @Override
    public String getId() {
        return action.getClass().getCanonicalName();
    }
}

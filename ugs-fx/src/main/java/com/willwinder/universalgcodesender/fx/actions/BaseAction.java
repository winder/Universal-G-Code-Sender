/*
    Copyright 2025 Joacim Breiler

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.universalgcodesender.fx.actions;

import com.willwinder.universalgcodesender.fx.model.ShortcutEvent;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.input.MouseEvent;

import java.util.logging.Logger;

public abstract class BaseAction implements Action {
    private static final Logger LOGGER = Logger.getLogger(BaseAction.class.getName());
    private final BooleanProperty enabled = new SimpleBooleanProperty(true);
    private final BooleanProperty selected = new SimpleBooleanProperty(false);
    private final StringProperty title = new SimpleStringProperty("");
    private final StringProperty label = new SimpleStringProperty("");
    private final StringProperty category = new SimpleStringProperty("");
    private final StringProperty icon = new SimpleStringProperty("");
    private final LongPressMouseEventProxy mouseEventHandler = new LongPressMouseEventProxy(300, this::handleMouseEvent);

    public BaseAction() {
    }

    public BaseAction(String label, String title, String icon) {
        this(label, title, "", icon);
    }

    public BaseAction(String label, String title, String category, String icon) {
        this.label.set(label);
        this.title.set(title);
        this.icon.set(icon);
        this.category.set(category);
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
    public String getLabel() {
        return label.get();
    }

    @Override
    public String getIcon() {
        return icon.get();
    }

    @Override
    public BooleanProperty enabledProperty() {
        return enabled;
    }

    @Override
    public BooleanProperty selectedProperty() {
        return selected;
    }

    @Override
    public StringProperty titleProperty() {
        return title;
    }

    @Override
    public StringProperty labelProperty() {
        return label;
    }

    @Override
    public StringProperty iconProperty() {
        return icon;
    }

    @Override
    public String getCategory() {
        return category.get();
    }

    @Override
    public StringProperty categoryProperty() {
        return category;
    }

    @Override
    public void handle(Event event) {
        if (event instanceof ActionEvent actionEvent) {
            handleAction(actionEvent);
        } else if (event instanceof MouseEvent mouseEvent) {
            mouseEventHandler.handle(mouseEvent);
        } else if (event instanceof ShortcutEvent shortcutEvent) {
            if (shortcutEvent.getEventType() == ShortcutEvent.SHORTCUT_PRESSED) {
                // Only listen for shortcut presses for now, possible to add continuous worker later
                handleAction(new ActionEvent());
            }
        } else {
            LOGGER.info("Unknown event type for triggering action: " + event.getClass().getName());
        }
    }

    public void handleMouseEvent(MouseEvent mouseEvent) {
    }

    public abstract void handleAction(ActionEvent event);
}

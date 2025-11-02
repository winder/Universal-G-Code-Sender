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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.event.Event;
import javafx.event.EventHandler;

public interface Action extends EventHandler<Event> {

    String getId();

    boolean isEnabled();

    /**
     * A title for the action that can be shown in menus
     *
     * @return the title for the action
     */
    String getTitle();

    /**
     * A label property with the text that should be visible on a button
     *
     * @return the label
     */
    String getLabel();


    /**
     * A readable category for the action
     *
     * @return the action category
     */
    String getCategory();

    String getIcon();

    BooleanProperty enabledProperty();

    /**
     * A title for the action that can be shown in menus
     *
     * @return the title for the action
     */
    StringProperty titleProperty();

    /**
     * Get the label property which can be used for observing
     * the label text
     *
     * @return the label property
     */
    StringProperty labelProperty();

    /**
     * A readable category for the action which can be used for observing
     *
     * @return the action category property
     */
    StringProperty categoryProperty();

    StringProperty iconProperty();

    BooleanProperty selectedProperty();
}

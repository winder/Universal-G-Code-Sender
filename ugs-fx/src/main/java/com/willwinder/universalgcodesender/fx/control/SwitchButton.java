/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.universalgcodesender.fx.control;

import javafx.scene.layout.Region;

import java.util.Objects;

public class SwitchButton extends javafx.scene.control.ToggleButton {
    public SwitchButton() {
        super();
        getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/switch-button.css")).toExternalForm());
        getStyleClass().add("switch-button");

        Region thumb = new Region();
        thumb.getStyleClass().add("thumb");
        setGraphic(thumb);
    }

    public SwitchButton(boolean selected) {
        this();
        setSelected(selected);
    }
}

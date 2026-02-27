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
package com.willwinder.universalgcodesender.fx.component;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.util.Objects;

public class BorderedTitledPane extends StackPane {
    public BorderedTitledPane(String title, Node... content) {
        getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/bordered-title-pane.css")).toExternalForm());
        getStyleClass().add("bordered-titled-pane");

        Label label = new Label(title);
        label.getStyleClass().add("title");
        StackPane.setAlignment(label, Pos.TOP_LEFT);

        StackPane contentPane = new StackPane();
        contentPane.getStyleClass().add("content");
        contentPane.getChildren().addAll(content);
        getChildren().addAll(label, contentPane);
    }
}
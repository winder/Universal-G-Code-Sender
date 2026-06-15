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

import com.willwinder.universalgcodesender.fx.helper.SvgLoader;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Objects;

/**
 * A lightweight, borderless section: a title followed by a horizontal rule and a collapse
 * caret to the right. Clicking the header toggles the visibility of the content. Intended
 * as a flatter replacement for {@link BorderedTitledPane}.
 */
public class CollapsibleTitledPane extends VBox {
    private final BooleanProperty expanded = new SimpleBooleanProperty(true);

    public CollapsibleTitledPane(String title, Node content) {
        getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/collapsible-titled-pane.css")).toExternalForm());
        getStyleClass().add("collapsible-titled-pane");

        Label label = new Label(title);
        label.getStyleClass().add("title");

        Region rule = new Region();
        rule.getStyleClass().add("rule");
        HBox.setHgrow(rule, Priority.ALWAYS);

        HBox header = new HBox(label, rule);
        header.getStyleClass().add("header");
        header.setAlignment(Pos.CENTER);
        header.setOnMouseClicked(event -> expanded.set(!expanded.get()));

        SvgLoader.loadImageIcon("icons/caret-down.svg", 14).ifPresent(caret -> {
            caret.getStyleClass().add("caret");
            // Point down when expanded, right when collapsed.
            caret.rotateProperty().bind(expanded.map(e -> e ? 0.0 : -90.0));
            header.getChildren().add(caret);
        });

        content.managedProperty().bind(content.visibleProperty());
        content.visibleProperty().bind(expanded);

        getChildren().addAll(header, content);
    }

    public BooleanProperty expandedProperty() {
        return expanded;
    }
}

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
package com.willwinder.universalgcodesender.fx.component.designer;

import com.willwinder.ugs.designer.entities.Anchor;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;

/**
 * A 3x3 grid of toggle buttons for choosing the anchor (origin) point of an entity, laid out to
 * mirror the physical position of each anchor. Selecting a cell updates {@link #anchorProperty()}.
 * Inspired by the Swing {@code AnchorSelectorPanel}.
 */
public class AnchorSelector extends GridPane {
    private static final double CELL_SIZE = 16;
    private static final Anchor[][] LAYOUT = {
            {Anchor.TOP_LEFT, Anchor.TOP_CENTER, Anchor.TOP_RIGHT},
            {Anchor.LEFT_CENTER, Anchor.CENTER, Anchor.RIGHT_CENTER},
            {Anchor.BOTTOM_LEFT, Anchor.BOTTOM_CENTER, Anchor.BOTTOM_RIGHT}
    };

    private final ObjectProperty<Anchor> anchor = new SimpleObjectProperty<>(Anchor.BOTTOM_LEFT);
    private final ToggleGroup group = new ToggleGroup();

    public AnchorSelector() {
        getStyleClass().add("anchor-selector");
        setHgap(2);
        setVgap(2);
        setAlignment(Pos.CENTER);

        for (int row = 0; row < LAYOUT.length; row++) {
            for (int col = 0; col < LAYOUT[row].length; col++) {
                add(createCell(LAYOUT[row][col]), col, row);
            }
        }

        // Keep exactly one cell selected: reject attempts to deselect the current one.
        group.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) {
                group.selectToggle(oldToggle);
            } else {
                anchor.set((Anchor) newToggle.getUserData());
            }
        });

        anchor.addListener((obs, oldVal, newVal) -> selectToggleFor(newVal));
        selectToggleFor(anchor.get());
    }

    private ToggleButton createCell(Anchor cellAnchor) {
        ToggleButton button = new ToggleButton();
        button.getStyleClass().add("anchor-cell");
        button.setToggleGroup(group);
        button.setUserData(cellAnchor);
        button.setFocusTraversable(false);
        button.setMinSize(CELL_SIZE, CELL_SIZE);
        button.setPrefSize(CELL_SIZE, CELL_SIZE);
        button.setMaxSize(CELL_SIZE, CELL_SIZE);

        Tooltip tooltip = new Tooltip(cellAnchor.name());
        tooltip.setShowDelay(Duration.millis(100));
        button.setTooltip(tooltip);
        return button;
    }

    private void selectToggleFor(Anchor value) {
        group.getToggles().stream()
                .filter(toggle -> toggle.getUserData() == value)
                .findFirst()
                .ifPresent(group::selectToggle);
    }

    public ObjectProperty<Anchor> anchorProperty() {
        return anchor;
    }

    public Anchor getAnchor() {
        return anchor.get();
    }

    public void setAnchor(Anchor anchor) {
        this.anchor.set(anchor);
    }
}

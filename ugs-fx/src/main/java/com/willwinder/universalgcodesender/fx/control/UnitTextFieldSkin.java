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

import com.willwinder.universalgcodesender.model.Unit;
import javafx.css.PseudoClass;
import javafx.scene.control.Label;
import javafx.scene.control.skin.TextFieldSkin;

final class UnitTextFieldSkin extends TextFieldSkin {
    private static final PseudoClass SHOWING_UNIT = PseudoClass.getPseudoClass("showing-unit");
    private final Label unitLabel;

    UnitTextFieldSkin(UnitTextField control) {
        super(control);

        unitLabel = new Label();
        unitLabel.getStyleClass().add("unit-label");
        unitLabel.setMouseTransparent(true);
        unitLabel.textProperty().bind(control.unitProperty().map(Unit::getAbbreviation));

        // Only show unit when not focused AND unit text is non-empty.
        unitLabel.visibleProperty().bind(
                control.focusedProperty().not()
        );
        unitLabel.managedProperty().bind(unitLabel.visibleProperty());

        // Trigger CSS pseudoclass for easy styling.
        unitLabel.visibleProperty().addListener((obs, oldV, newV) ->
                control.pseudoClassStateChanged(SHOWING_UNIT, Boolean.TRUE.equals(newV))
        );
        control.pseudoClassStateChanged(SHOWING_UNIT, unitLabel.isVisible());

        getChildren().add(unitLabel);
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        // Reserve space on the right for the unit label when it's visible.
        // This avoids the typed/displayed text overlapping the unit.
        double reservedRight = unitLabel.isVisible() ? snapSizeX(unitLabel.prefWidth(-1)) : 0.0;

        // Let the normal TextFieldSkin layout, but with less width (space reserved for unit).
        super.layoutChildren(x, y, Math.max(0.0, w - reservedRight), h);

        if (unitLabel.isVisible()) {
            double labelH = snapSizeY(unitLabel.prefHeight(-1));
            double labelX = snapPositionX(x + w - reservedRight);
            double labelY = snapPositionY(y + (h - labelH) / 2.0);

            unitLabel.resizeRelocate(labelX, labelY, reservedRight, labelH);
        }
    }
}

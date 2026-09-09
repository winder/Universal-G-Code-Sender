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
package com.willwinder.universalgcodesender.fx.component.visualizer;

import com.willwinder.universalgcodesender.Utils;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Bounds3;
import com.willwinder.universalgcodesender.fx.helper.SvgLoader;
import com.willwinder.universalgcodesender.fx.service.StockService;
import com.willwinder.universalgcodesender.fx.service.StockSpecs;
import com.willwinder.universalgcodesender.fx.settings.VisualizerSettings;
import com.willwinder.universalgcodesender.fx.stage.StockSettingsStage;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.services.LookupService;
import javafx.beans.InvalidationListener;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

import java.util.Optional;

/**
 * Sits next to the tool button and summarizes the block of material the stock simulation cuts
 * into, as width by length by thickness in the preferred units. Clicking opens the stock settings.
 * Hidden while the stock is turned off, since there is nothing to summarize; the stock is turned
 * on from the visualizer toolbar or the settings.
 */
public class StockButton extends Button {
    private static final int ICON_SIZE = 32;
    private final InvalidationListener updateListener = observable -> updateText();

    public StockButton() {
        getStyleClass().add("tool-button");
        Tooltip tooltip = new Tooltip("Stock settings");
        tooltip.setShowDelay(Duration.millis(100));
        setTooltip(tooltip);
        setGraphic(SvgLoader.loadImageIcon("icons/cube.svg", ICON_SIZE).orElse(null));
        setOnAction(event -> new StockSettingsStage(getScene() == null ? null : getScene().getWindow()).showAndWait());

        StockService.getInstance().boundsProperty().addListener(updateListener);
        VisualizerSettings settings = VisualizerSettings.getInstance();
        visibleProperty().bind(settings.showStockProperty());
        managedProperty().bind(visibleProperty());
        settings.showStockProperty().addListener(updateListener);
        settings.stockProperties().forEach(property -> property.addListener(updateListener));
        updateText();
    }

    private void updateText() {
        VisualizerSettings settings = VisualizerSettings.getInstance();
        if (!settings.showStockProperty().get()) {
            return;
        }
        Optional<Bounds3> bounds = StockService.getInstance().getBounds();
        String mode = StockSpecs.parseMode(settings.stockModeProperty().get()).displayName();
        if (bounds.isEmpty()) {
            setText("No stock");
            getTooltip().setText("Stock size: " + mode + ". Nothing to cut in the loaded program yet.");
            return;
        }
        setText(describe(bounds.get()));
        getTooltip().setText("Stock size: " + mode + ". Width × length × thickness. Click to change.");
    }

    private static String describe(Bounds3 bounds) {
        UnitUtils.Units units = preferredUnits();
        double scale = UnitUtils.scaleUnits(UnitUtils.Units.MM, units);
        return Utils.formatter.format(bounds.width() * scale)
                + " × " + Utils.formatter.format(bounds.height() * scale)
                + " × " + Utils.formatter.format(bounds.depth() * scale)
                + " " + units.abbreviation;
    }

    private static UnitUtils.Units preferredUnits() {
        try {
            UnitUtils.Units units = LookupService.lookup(BackendAPI.class).getSettings().getPreferredUnits();
            return units == null || units == UnitUtils.Units.UNKNOWN ? UnitUtils.Units.MM : units;
        } catch (RuntimeException e) {
            return UnitUtils.Units.MM;
        }
    }
}

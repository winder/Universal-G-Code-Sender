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
package com.willwinder.universalgcodesender.fx.component.clipart;

import com.willwinder.ugs.designer.gui.clipart.Clipart;
import com.willwinder.ugs.designer.gui.clipart.ClipartSource;
import com.willwinder.universalgcodesender.fx.helper.BrowserHelper;
import com.willwinder.universalgcodesender.i18n.Localization;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;

/**
 * Shows the full attribution for the currently selected clipart: where it comes from, who made it,
 * under which terms it may be used and a link to the source website.
 */
public class ClipartDetailsPane extends StackPane {
    private final Label placeholder = new Label(Localization.getString("designer.clipart.noSelection"));
    private final GridPane details = new GridPane();
    private final Label nameLabel = new Label();
    private final Label sourceLabel = new Label();
    private final Label creditsLabel = new Label();
    private final Label licenseLabel = new Label();
    private final Hyperlink urlLink = new Hyperlink();

    public ClipartDetailsPane() {
        getStyleClass().add("clipart-details");

        placeholder.getStyleClass().add("clipart-details-placeholder");
        StackPane.setAlignment(placeholder, Pos.CENTER);

        nameLabel.getStyleClass().add("clipart-details-name");
        licenseLabel.getStyleClass().add("clipart-license");
        urlLink.setOnAction(event -> BrowserHelper.open(urlLink.getText()));

        details.setHgap(12);
        details.setVgap(4);
        ColumnConstraints labelColumn = new ColumnConstraints();
        labelColumn.setHalignment(HPos.RIGHT);
        ColumnConstraints valueColumn = new ColumnConstraints();
        valueColumn.setHgrow(Priority.ALWAYS);
        details.getColumnConstraints().addAll(labelColumn, valueColumn);

        details.add(nameLabel, 0, 0, 2, 1);
        GridPane.setHalignment(nameLabel, HPos.LEFT);
        addRow(1, "designer.clipart.source", sourceLabel);
        addRow(2, "designer.clipart.credits", creditsLabel);
        addRow(3, "designer.clipart.license", licenseLabel);
        addRow(4, "designer.clipart.website", urlLink);

        // Both children stay managed so the pane keeps the height of the details grid,
        // and the layout does not jump when a clipart is selected or deselected
        getChildren().addAll(placeholder, details);
        setClipart(null);
    }

    private void addRow(int row, String localizationKey, javafx.scene.Node value) {
        Label label = new Label(Localization.getString(localizationKey));
        label.getStyleClass().add("clipart-details-label");
        details.add(label, 0, row);
        details.add(value, 1, row);
    }

    public void setClipart(Clipart clipart) {
        boolean hasClipart = clipart != null;
        placeholder.setVisible(!hasClipart);
        details.setVisible(hasClipart);
        if (!hasClipart) {
            return;
        }

        ClipartSource source = clipart.getSource();
        nameLabel.setText(clipart.getName());
        sourceLabel.setText(source.getName());
        creditsLabel.setText(source.getCredits());
        licenseLabel.setText(source.getLicense());
        urlLink.setText(source.getUrl());
        urlLink.setVisited(false);
    }
}

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
import com.willwinder.universalgcodesender.i18n.Localization;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * A selectable card that shows a clipart preview together with its name, the terms it may be
 * used under and who to credit for it.
 */
public class ClipartCard extends VBox {
    public static final double CARD_WIDTH = 150;
    private static final double PREVIEW_SIZE = 96;
    private static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");

    private final transient Clipart clipart;

    public ClipartCard(Clipart clipart) {
        this.clipart = clipart;
        getStyleClass().add("clipart-card");
        setAlignment(Pos.TOP_CENTER);
        setPrefWidth(CARD_WIDTH);
        setMinWidth(CARD_WIDTH);
        setMaxWidth(CARD_WIDTH);

        StackPane preview = new StackPane(ClipartPreviewFactory.createPreview(clipart.getCuttable().getShape(), PREVIEW_SIZE));
        preview.getStyleClass().add("clipart-preview");
        preview.setMinSize(PREVIEW_SIZE, PREVIEW_SIZE);
        preview.setPrefSize(PREVIEW_SIZE, PREVIEW_SIZE);
        preview.setMaxSize(PREVIEW_SIZE, PREVIEW_SIZE);

        getChildren().addAll(preview, createLabel(clipart.getName(), "clipart-name"));

        Tooltip tooltip = new Tooltip(createTooltipText(clipart));
        tooltip.setShowDelay(Duration.millis(400));
        Tooltip.install(this, tooltip);
    }

    private static Label createLabel(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        label.setTextOverrun(OverrunStyle.ELLIPSIS);
        label.setMaxWidth(CARD_WIDTH);
        label.setAlignment(Pos.CENTER);
        return label;
    }

    private static String createTooltipText(Clipart clipart) {
        ClipartSource source = clipart.getSource();
        return clipart.getName() + "\n" +
                Localization.getString("designer.clipart.source") + ": " + source.getName() + "\n" +
                Localization.getString("designer.clipart.credits") + ": " + source.getCredits() + "\n" +
                Localization.getString("designer.clipart.license") + ": " + source.getLicense();
    }

    public Clipart getClipart() {
        return clipart;
    }

    public void setSelected(boolean selected) {
        pseudoClassStateChanged(SELECTED, selected);
    }
}

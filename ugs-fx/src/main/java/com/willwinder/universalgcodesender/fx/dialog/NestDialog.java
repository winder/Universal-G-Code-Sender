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
package com.willwinder.universalgcodesender.fx.dialog;

import com.willwinder.ugs.designer.actions.NestAction;
import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.entities.cuttable.Group;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.universalgcodesender.fx.component.ButtonBox;
import com.willwinder.universalgcodesender.fx.component.SettingsRow;
import com.willwinder.universalgcodesender.fx.control.SwitchButton;
import com.willwinder.universalgcodesender.fx.control.UnitTextField;
import com.willwinder.universalgcodesender.fx.helper.Colors;
import com.willwinder.universalgcodesender.fx.helper.SvgLoader;
import com.willwinder.universalgcodesender.fx.helper.WindowHelper;
import com.willwinder.universalgcodesender.fx.settings.NestSettings;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.Unit;
import com.willwinder.universalgcodesender.model.UnitValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.List;
import java.util.Objects;

/**
 * Packs the selected objects within an area. The objects are rearranged in the design while the
 * dialog is open so the result can be judged, put back if the dialog is cancelled, and recorded as
 * a single undo step when it is accepted. The area and spacing are remembered in the application
 * settings.
 */
public class NestDialog extends Stage {
    private static final int WARNING_ICON_SIZE = 16;

    private final Controller controller;
    private final List<Entity> targets;
    private final UnitTextField maxWidth = new UnitTextField(new UnitValue(Unit.MM, NestSettings.getMaxWidth()), Unit.MM);
    private final UnitTextField maxHeight = new UnitTextField(new UnitValue(Unit.MM, NestSettings.getMaxHeight()), Unit.MM);
    private final UnitTextField spacing = new UnitTextField(new UnitValue(Unit.MM, NestSettings.getSpacing()), Unit.MM);
    private final SwitchButton allowRotation = new SwitchButton();
    private final HBox overflowWarning = createOverflowWarning();
    private NestAction preview;

    public NestDialog(Window owner, Controller controller) {
        this.controller = controller;
        this.targets = targets(controller);
        if (owner != null) {
            initOwner(owner);
        }
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Nest selection");

        allowRotation.selectedProperty().set(NestSettings.isRotationAllowed());
        overflowWarning.managedProperty().bind(overflowWarning.visibleProperty());

        maxWidth.unitValueProperty().addListener((_, _, _) -> updatePreview());
        maxHeight.unitValueProperty().addListener((_, _, _) -> updatePreview());
        spacing.unitValueProperty().addListener((_, _, _) -> updatePreview());
        allowRotation.selectedProperty().addListener((_, _, _) -> updatePreview());
        updatePreview();

        setScene(createScene());
        setWidth(380);
        setHeight(360);
        setResizable(false);
        setOnShowing(_ -> WindowHelper.centerOnOwner(this));
        setOnHidden(_ -> removePreview());
    }

    /**
     * The objects to pack: the selected objects, or the objects inside a group when that group
     * is the only thing selected.
     */
    public static List<Entity> targets(Controller controller) {
        List<Entity> selected = controller.getSelectionManager().getChildren();
        if (selected.size() == 1 && selected.getFirst() instanceof Group group) {
            return group.getChildren();
        }
        return selected;
    }

    private static HBox createOverflowWarning() {
        Label text = new Label(Localization.getString("platform.designer.nest.overflow"));
        text.setWrapText(true);
        HBox warning = new HBox(8, text);
        warning.setAlignment(Pos.CENTER_LEFT);
        warning.setPadding(new Insets(12, 0, 0, 0));
        warning.getStyleClass().add("warning-label");
        SvgLoader.loadImageIcon("icons/warning-circle.svg", WARNING_ICON_SIZE, Colors.ORANGE)
                .ifPresent(icon -> warning.getChildren().addFirst(icon));
        return warning;
    }

    private Scene createScene() {
        VBox form = new VBox(4,
                new SettingsRow("Max width", "The width of the area to pack the objects within.", maxWidth),
                new SettingsRow("Max height", "The height of the area the objects should fit within.", maxHeight),
                new SettingsRow("Spacing", "The distance kept between the objects.", spacing),
                new Separator(),
                new SettingsRow("Rotate", "Turn the objects in steps of 45 degrees when that makes them fit better.", allowRotation),
                overflowWarning);
        form.setPadding(new Insets(16));

        Button cancel = new Button("Cancel");
        cancel.setCancelButton(true);
        cancel.setOnAction(_ -> close());
        Button ok = new Button("OK");
        ok.setDefaultButton(true);
        ok.setOnAction(_ -> onOk());
        ButtonBox buttons = new ButtonBox();
        ButtonBox.setButtonData(cancel, ButtonBox.ButtonData.CANCEL_CLOSE);
        ButtonBox.setButtonData(ok, ButtonBox.ButtonData.OK_DONE);
        buttons.getButtons().addAll(cancel, ok);

        BorderPane root = new BorderPane(form);
        root.setBottom(buttons);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/root.css")).toExternalForm());
        return scene;
    }

    private void updatePreview() {
        removePreview();
        preview = new NestAction(targets, maxWidth.getValue(), spacing.getValue(), allowRotation.selectedProperty().get());
        preview.redo();
        overflowWarning.setVisible(!preview.fitsWithin(maxWidth.getValue(), maxHeight.getValue()));
    }

    private void onOk() {
        NestSettings.setMaxWidth(maxWidth.getValue());
        NestSettings.setMaxHeight(maxHeight.getValue());
        NestSettings.setSpacing(spacing.getValue());
        NestSettings.setRotationAllowed(allowRotation.selectedProperty().get());

        // The preview has already been applied, so only the undo step is left to record.
        controller.getUndoManager().addAction(preview);
        preview = null;
        close();
    }

    private void removePreview() {
        if (preview != null) {
            preview.undo();
            preview = null;
        }
    }
}

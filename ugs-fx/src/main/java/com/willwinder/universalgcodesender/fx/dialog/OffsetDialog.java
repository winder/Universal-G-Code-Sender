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

import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.entities.EntityGroup;
import com.willwinder.ugs.designer.entities.cuttable.Path;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.model.OffsetDirection;
import com.willwinder.ugs.designer.model.OffsetJoinStyle;
import com.willwinder.ugs.designer.utils.OffsetUtils;
import com.willwinder.universalgcodesender.fx.component.ButtonBox;
import com.willwinder.universalgcodesender.fx.component.SettingsRow;
import com.willwinder.universalgcodesender.fx.control.SwitchButton;
import com.willwinder.universalgcodesender.fx.control.UnitTextField;
import com.willwinder.universalgcodesender.model.Unit;
import com.willwinder.universalgcodesender.model.UnitValue;
import com.willwinder.universalgcodesender.fx.helper.WindowHelper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Draws offset outlines around the selected entities. The result is previewed in the design
 * through a temporary group while the dialog is open, and committed as new entities (with a single
 * undo step) when the dialog is accepted.
 */
public class OffsetDialog extends Stage {
    private static final double DEFAULT_DISTANCE = 1;

    private final Controller controller;
    private final EntityGroup previewGroup = new EntityGroup();
    private final List<Entity> selection;
    private final UnitTextField distance = new UnitTextField(new UnitValue(Unit.MM, DEFAULT_DISTANCE), Unit.MM);
    private final ComboBox<OffsetDirection> direction = new ComboBox<>(FXCollections.observableArrayList(OffsetDirection.values()));
    private final ComboBox<OffsetJoinStyle> joinStyle = new ComboBox<>(FXCollections.observableArrayList(OffsetJoinStyle.values()));
    private final SwitchButton unionResults = new SwitchButton();

    public OffsetDialog(Window owner, Controller controller) {
        this.controller = controller;
        this.selection = new ArrayList<>(controller.getSelectionManager().getSelection());
        if (owner != null) {
            initOwner(owner);
        }
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Offset selection");

        previewGroup.setName("Offset preview");
        controller.getModel().insertEntity(previewGroup);

        direction.setValue(OffsetDirection.OUTWARD);
        direction.setMaxWidth(Double.MAX_VALUE);
        joinStyle.setValue(OffsetJoinStyle.ROUND);
        joinStyle.setMaxWidth(Double.MAX_VALUE);

        distance.unitValueProperty().addListener((_, _, _) -> updatePreview());
        direction.valueProperty().addListener((_, _, _) -> updatePreview());
        joinStyle.valueProperty().addListener((_, _, _) -> updatePreview());
        unionResults.selectedProperty().addListener((_, _, _) -> updatePreview());
        updatePreview();

        setScene(createScene());
        setWidth(380);
        setResizable(false);
        setOnShowing(_ -> WindowHelper.centerOnOwner(this));
        setOnHidden(_ -> removePreview());
    }

    private Scene createScene() {
        VBox form = new VBox(4,
                new SettingsRow("Distance", distance),
                new SettingsRow("Direction", "On which side of the shape the offset is drawn.", direction),
                new Separator(),
                new SettingsRow("Join style", "How the corners of the offset are joined.", joinStyle),
                new SettingsRow("Union all", "Merge all offset outlines into a single path.", unionResults));
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
        previewGroup.removeAll();
        List<Path> offsetPaths = OffsetUtils.offset(selection, distance.getValue(), direction.getValue(), joinStyle.getValue(), unionResults.selectedProperty().get());
        offsetPaths.forEach(previewGroup::addChild);
    }

    private void onOk() {
        List<Entity> offsetPaths = new ArrayList<>(previewGroup.getChildren());
        removePreview();
        if (!offsetPaths.isEmpty()) {
            controller.addEntities(offsetPaths);
            controller.getSelectionManager().clearSelection();
            controller.getSelectionManager().addSelection(offsetPaths);
            controller.getSelectionManager().addSelection(selection);
        }
        close();
    }

    private void removePreview() {
        previewGroup.removeAll();
        controller.getModel().removeEntity(previewGroup);
    }
}

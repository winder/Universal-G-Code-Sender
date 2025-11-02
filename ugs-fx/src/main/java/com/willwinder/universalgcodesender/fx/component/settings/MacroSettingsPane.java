package com.willwinder.universalgcodesender.fx.component.settings;

import com.willwinder.universalgcodesender.fx.helper.SvgLoader;
import com.willwinder.universalgcodesender.fx.model.MacroAdapter;
import com.willwinder.universalgcodesender.fx.service.MacroRegistry;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.types.Macro;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;

import java.util.UUID;

public class MacroSettingsPane extends BorderPane {


    public MacroSettingsPane() {
        addTitle();
        addMacrosTable();
        addButtons();
    }

    private void addTitle() {
        Label title = new Label(Localization.getString("platform.window.macros"));
        title.setPadding(new Insets(0, 0, 15, 0));
        title.setFont(Font.font(20));
        setTop(title);
    }

    private void addButtons() {
        HBox buttons = new HBox();
        buttons.setPadding(new Insets(15, 0, 0, 0));
        buttons.setAlignment(Pos.CENTER_RIGHT);

        Button add = new Button(Localization.getString("add"), SvgLoader.loadImageIcon("icons/add.svg", 16).orElse(null));
        add.setOnAction((event) -> {
            String macroName = MacroRegistry.findUniqueMacroName();
            MacroRegistry.getInstance().getMacros().add(new MacroAdapter(new Macro(UUID.randomUUID().toString(), macroName, null, "")));
        });
        buttons.getChildren().add(add);
        setBottom(buttons);
    }

    private void addMacrosTable() {

        TableView<MacroAdapter> table = new TableView<>();
        table.setEditable(true);

        TableColumn<MacroAdapter, String> titleColumn = new TableColumn<>(Localization.getString("macroPanel.name"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        titleColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        titleColumn.setOnEditCommit(event -> {
            MacroAdapter macro = event.getRowValue();
            macro.nameProperty().set(event.getNewValue());
        });

        TableColumn<MacroAdapter, String> gcodeColumn = new TableColumn<>(Localization.getString("macroPanel.text"));
        gcodeColumn.setCellValueFactory(new PropertyValueFactory<>("gcode"));
        gcodeColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        gcodeColumn.setOnEditCommit(event -> {
            MacroAdapter macro = event.getRowValue();
            macro.gcodeProperty().set(event.getNewValue());
        });
        gcodeColumn.setEditable(true);

        TableColumn<MacroAdapter, Void> actionsColumn = new TableColumn<>();
        actionsColumn.setPrefWidth(40);
        actionsColumn.setCellFactory(col -> new MacroActionsTableCell());

        table.getColumns().addAll(titleColumn, gcodeColumn, actionsColumn);
        table.setItems(MacroRegistry.getInstance().getMacros());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        setCenter(table);
    }
}

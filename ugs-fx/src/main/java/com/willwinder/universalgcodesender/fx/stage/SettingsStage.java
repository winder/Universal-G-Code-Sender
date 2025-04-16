package com.willwinder.universalgcodesender.fx.stage;

import com.willwinder.universalgcodesender.fx.component.FirmwareSettingsPane;
import com.willwinder.universalgcodesender.fx.component.GeneralSettingsPane;
import com.willwinder.universalgcodesender.fx.component.SettingsListCell;
import com.willwinder.universalgcodesender.fx.model.SettingsListItem;
import com.willwinder.universalgcodesender.i18n.Localization;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class SettingsStage extends Stage {

    private BorderPane root;

    public SettingsStage(Window owner) {
        initModality(Modality.APPLICATION_MODAL);
        initOwner(owner);
        setTitle(Localization.getString("mainWindow.swing.settingsMenu"));

        createLayout();

        setOnShowing(event -> {
            double centerX = getOwner().getX() + getOwner().getWidth() / 2 - getWidth() / 2;
            double centerY = getOwner().getY() + getOwner().getHeight() / 2 - getHeight() / 2;
            setX(centerX);
            setY(centerY);
        });

        Scene scene = new Scene(root);
        setScene(scene);
        setWidth(800);
        setHeight(600);
        setResizable(true);
    }


    private void createLayout() {
        root = new BorderPane();

        ListView<SettingsListItem> sectionList = new ListView<>();
        sectionList.getItems().add(new SettingsListItem(Localization.getString("settings.general"), "icons/sliders.svg", new GeneralSettingsPane()));
        sectionList.getItems().add(new SettingsListItem(Localization.getString("settings.machineStatus"), "icons/position.svg", new Label(Localization.getString("settings.machineStatus"))));
        sectionList.getItems().add(new SettingsListItem(Localization.getString("platform.menu.macros"), "icons/robot.svg", new Label(Localization.getString("platform.menu.macros"))));
        sectionList.getItems().add(new SettingsListItem(Localization.getString("settings.probe"), "icons/probe.svg", new Label(Localization.getString("settings.probe"))));
        sectionList.getItems().add(new SettingsListItem(Localization.getString("settings.firmware"), "icons/microchip.svg", new FirmwareSettingsPane()));
        sectionList.getItems().add(new SettingsListItem(Localization.getString("platform.window.visualizer"), "icons/cube.svg", new Label(Localization.getString("platform.window.visualizer"))));
        sectionList.getItems().add(new SettingsListItem(Localization.getString("settings.gamepad"), "icons/gamepad.svg", new Label(Localization.getString("platform.window.visualizer"))));
        sectionList.getItems().add(new SettingsListItem(Localization.getString("mainWindow.swing.pendant"), "resources/icons/pendant.svg", new Label(Localization.getString("mainWindow.swing.pendant"))));
        sectionList.setCellFactory(listView -> new SettingsListCell());
        sectionList.getSelectionModel().select(0);

        // Right content area (StackPane to easily swap content)
        StackPane contentPane = new StackPane();
        contentPane.setPadding(new Insets(20, 20, 20, 20));
        contentPane.getChildren().add(sectionList.getItems().get(0).settingsPane());

        // Update right pane based on selection
        sectionList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            contentPane.getChildren().clear();
            contentPane.getChildren().add(newVal.settingsPane());
        });

        ScrollPane leftScrollPane = new ScrollPane(sectionList);
        leftScrollPane.setFitToWidth(true);
        leftScrollPane.setFitToHeight(true);
        leftScrollPane.setPrefWidth(220);

        HBox layout = new HBox(leftScrollPane, contentPane);
        HBox.setHgrow(contentPane, Priority.ALWAYS);

        root.setCenter(layout);
    }
}

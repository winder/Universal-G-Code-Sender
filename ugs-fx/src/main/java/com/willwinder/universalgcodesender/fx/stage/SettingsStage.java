package com.willwinder.universalgcodesender.fx.stage;

import com.willwinder.universalgcodesender.fx.component.SettingsListCell;
import com.willwinder.universalgcodesender.fx.component.settings.FirmwareSettingsPane;
import com.willwinder.universalgcodesender.fx.component.settings.GeneralSettingsPane;
import com.willwinder.universalgcodesender.fx.component.settings.KeyboardSettingPane;
import com.willwinder.universalgcodesender.fx.component.settings.MachineStatusSettingsPane;
import com.willwinder.universalgcodesender.fx.component.settings.MacroSettingsPane;
import com.willwinder.universalgcodesender.fx.component.settings.PendantSettingsPane;
import com.willwinder.universalgcodesender.fx.component.settings.TerminalSettingsPane;
import com.willwinder.universalgcodesender.fx.component.settings.VisualizerSettingsPane;
import com.willwinder.universalgcodesender.fx.model.SettingsListItem;
import com.willwinder.universalgcodesender.i18n.Localization;
import javafx.geometry.Insets;
import javafx.scene.Scene;
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
        scene.getStylesheets().add(getClass().getResource("/styles/settings.css").toExternalForm());
        setScene(scene);
        setWidth(800);
        setHeight(600);
        setResizable(true);
    }


    private void createLayout() {
        root = new BorderPane();

        ListView<SettingsListItem> sectionList = new ListView<>();
        sectionList.getItems().add(new SettingsListItem(Localization.getString("settings.general"), "icons/sliders.svg", new GeneralSettingsPane()));
        sectionList.getItems().add(new SettingsListItem(Localization.getString("settings.firmware"), "icons/microchip.svg", new FirmwareSettingsPane()));
        sectionList.getItems().add(new SettingsListItem(Localization.getString("settings.machineStatus"), "icons/position.svg", new MachineStatusSettingsPane()));
        sectionList.getItems().add(new SettingsListItem(Localization.getString("platform.menu.macros"), "icons/robot.svg", new MacroSettingsPane()));
        //sectionList.getItems().add(new SettingsListItem(Localization.getString("settings.probe"), "icons/probe.svg", new Label(Localization.getString("settings.probe"))));
        //sectionList.getItems().add(new SettingsListItem(Localization.getString("settings.gamepad"), "icons/gamepad.svg", new Label(Localization.getString("platform.gamepad"))));
        sectionList.getItems().add(new SettingsListItem(Localization.getString("settings.terminal"), "icons/terminal.svg", new TerminalSettingsPane()));
        sectionList.getItems().add(new SettingsListItem(Localization.getString("mainWindow.swing.pendant"), "resources/icons/pendant.svg", new PendantSettingsPane()));
        sectionList.getItems().add(new SettingsListItem(Localization.getString("settings.keyboard"), "icons/shortcut.svg", new KeyboardSettingPane()));
        sectionList.getItems().add(new SettingsListItem(Localization.getString("platform.window.visualizer"), "icons/cube.svg", new VisualizerSettingsPane()));
        sectionList.setCellFactory(listView -> new SettingsListCell());
        sectionList.getSelectionModel().select(0);
        sectionList.setPrefWidth(200);

        // Right content area
        StackPane contentPane = new StackPane();
        contentPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        contentPane.setPadding(new Insets(20, 20, 40, 20));
        contentPane.getChildren().add(sectionList.getItems().get(0).settingsPane());
        ScrollPane contentScrollPane = new ScrollPane(contentPane);
        contentScrollPane.setFitToWidth(true);
        contentScrollPane.setFitToHeight(true);

        // Update right pane based on selection
        sectionList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            contentPane.getChildren().clear();
            contentPane.getChildren().add(newVal.settingsPane());
        });

        HBox layout = new HBox(sectionList, contentScrollPane);
        HBox.setHgrow(contentScrollPane, Priority.ALWAYS);

        root.setCenter(layout);
    }
}

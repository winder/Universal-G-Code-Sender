/*
    Copyright 2025 Joacim Breiler

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
package com.willwinder.universalgcodesender.fx;

import com.formdev.flatlaf.FlatLightLaf;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.fx.actions.StartAction;
import com.willwinder.universalgcodesender.fx.component.MachineStatusPane;
import com.willwinder.universalgcodesender.fx.component.ToolBarMenu;
import com.willwinder.universalgcodesender.fx.component.drawer.DrawerPane;
import com.willwinder.universalgcodesender.fx.component.jog.JogPane;
import com.willwinder.universalgcodesender.fx.component.visualizer.Visualizer;
import com.willwinder.universalgcodesender.fx.helper.SvgLoader;
import com.willwinder.universalgcodesender.fx.service.ActionRegistry;
import com.willwinder.universalgcodesender.fx.service.JogActionRegistry;
import com.willwinder.universalgcodesender.fx.service.MacroActionService;
import com.willwinder.universalgcodesender.fx.service.ShortcutService;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.pendantui.PendantUI;
import com.willwinder.universalgcodesender.utils.SettingsFactory;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import com.willwinder.universalgcodesender.utils.Version;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.swing.UIManager;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends Application {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private SplitPane leftSplitPane;
    private SplitPane contentSplitPane;
    private StackPane contentPanel;
    private SplitPane.Divider contentPaneDivider;
    private SplitPane.Divider leftPaneDivider;

    @Override
    public void init() throws Exception {
        try {
            FlatLightLaf.setup();
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Could not load the look and feel", e);
        }

        if (Settings.getInstance().pendantAutostartProperty().get()) {
            ThreadHelper.invokeLater(() -> {
                BackendAPI backend = CentralLookup.getDefault().lookup(BackendAPI.class);
                PendantUI pendantUI = new PendantUI(backend);
                pendantUI.start();
            }, 4000);
        }

        MacroActionService.registerMacros();

        JogActionRegistry.registerActions();
    }

    @Override
    public void start(Stage primaryStage) {
        registerListeners(primaryStage);

        ToolBarMenu toolBarMenu = new ToolBarMenu();
        createLeftPane();
        createContentPanel();
        createContentPane();
        VBox.setVgrow(contentSplitPane, Priority.ALWAYS);

        VBox root = new VBox();
        Scene scene = new Scene(root);

        ShortcutService.registerListener(scene);

        scene.getStylesheets().add(Main.class.getResource("/styles/root.css").toExternalForm());
        root.getChildren().addAll(toolBarMenu, contentSplitPane);

        primaryStage.setTitle("Universal G-code Sender - " + Version.getVersion());
        SvgLoader.loadIcon("icons/ugs.svg", 128).ifPresent(icon -> primaryStage.getIcons().add(icon));
        primaryStage.setScene(scene);
        primaryStage.show();
        registerShortCuts(scene);

        Parameters params = getParameters();
        if (!params.getUnnamed().isEmpty()) {
            BackendAPI backendAPI = CentralLookup.getDefault().lookup(BackendAPI.class);
            try {
                File file = new File(params.getUnnamed().get(0));
                backendAPI.setGcodeFile(file);
                backendAPI.getSettings().setLastWorkingDirectory(file.getParent());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void registerLayoutListeners(Stage primaryStage) {
        primaryStage.widthProperty().addListener((observable, oldValue, newValue) -> Settings.getInstance().windowWidthProperty().set(newValue.doubleValue()));
        primaryStage.heightProperty().addListener((observable, oldValue, newValue) -> Settings.getInstance().windowHeightProperty().set(newValue.doubleValue()));
        primaryStage.xProperty().addListener((observable, oldValue, newValue) -> Settings.getInstance().windowPositionXProperty().set(newValue.doubleValue()));
        primaryStage.yProperty().addListener((observable, oldValue, newValue) -> Settings.getInstance().windowPositionYProperty().set(newValue.doubleValue()));
        leftPaneDivider.positionProperty().addListener((obs, oldVal, newVal) -> Settings.getInstance().windowDividerLeftProperty().set(newVal.doubleValue()));
        contentPaneDivider.positionProperty().addListener((obs, oldVal, newVal) -> Settings.getInstance().windowDividerContentProperty().set(newVal.doubleValue()));
    }

    private void registerListeners(Stage primaryStage) {
        primaryStage.setOnShown(event -> {
            primaryStage.setX(Settings.getInstance().windowPositionXProperty().get());
            primaryStage.setY(Settings.getInstance().windowPositionYProperty().get());
            primaryStage.setWidth(Settings.getInstance().windowWidthProperty().get());
            primaryStage.setHeight(Settings.getInstance().windowHeightProperty().get());
            leftPaneDivider.setPosition(Settings.getInstance().windowDividerLeftProperty().get());
            contentPaneDivider.setPosition(Settings.getInstance().windowDividerContentProperty().get());


            // Hack to make sure that the window is shown with correct size before setting the dividers
            ThreadHelper.invokeLater(() -> {
                leftPaneDivider.setPosition(Settings.getInstance().windowDividerLeftProperty().get());
                contentPaneDivider.setPosition(Settings.getInstance().windowDividerContentProperty().get());
                registerLayoutListeners(primaryStage);
            }, 200);
        });

        primaryStage.setOnCloseRequest(event -> {
            SettingsFactory.saveSettings();
            Platform.exit();
            System.exit(0);
        });
    }

    private void createContentPanel() {
        contentPanel = new StackPane();
        contentPanel.getChildren().add(new Visualizer());

        DrawerPane drawerPane = new DrawerPane();
        contentPanel.getChildren().add(drawerPane);
        StackPane.setAlignment(drawerPane, Pos.BOTTOM_RIGHT);
    }


    private void createContentPane() {
        contentSplitPane = new SplitPane();
        contentSplitPane.setMinWidth(200);
        contentSplitPane.setOrientation(Orientation.HORIZONTAL);
        contentSplitPane.getItems().addAll(leftSplitPane, contentPanel);
        SplitPane.setResizableWithParent(contentSplitPane, false);

        contentPaneDivider = contentSplitPane.getDividers().get(0);
        contentPaneDivider.setPosition(Settings.getInstance().windowDividerContentProperty().get());
    }

    private void createLeftPane() {
        leftSplitPane = new SplitPane();
        leftSplitPane.setStyle("-fx-border-color: transparent;");

        leftSplitPane.setMinWidth(200);
        leftSplitPane.setOrientation(Orientation.VERTICAL);
        leftSplitPane.getItems().addAll(new MachineStatusPane(), new JogPane());
        SplitPane.setResizableWithParent(leftSplitPane, false);

        leftPaneDivider = leftSplitPane.getDividers().get(0);
        leftPaneDivider.setPosition(Settings.getInstance().windowDividerLeftProperty().get());
    }

    private void registerShortCuts(Scene scene) {
        KeyCombination kc = new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN);
        scene.getAccelerators().put(kc, () -> ActionRegistry.getInstance()
                .getAction(StartAction.class.getCanonicalName())
                .ifPresent(a -> a.handle(null)));
    }

    public static void main(String[] args) {
        launch(Main.class, args);
    }
}

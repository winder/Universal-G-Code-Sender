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
import com.willwinder.universalgcodesender.fx.actions.ToggleRightPaneAction;
import com.willwinder.universalgcodesender.fx.actions.ToggleLeftPaneAction;
import com.willwinder.universalgcodesender.fx.component.MachinePane;
import com.willwinder.universalgcodesender.fx.component.MainMenuBar;
import com.willwinder.universalgcodesender.fx.component.ToolBarMenu;
import com.willwinder.universalgcodesender.fx.component.WorkspaceTools;
import com.willwinder.universalgcodesender.fx.component.drawer.DrawerPane;
import com.willwinder.universalgcodesender.fx.component.sidepane.CollapsibleSidePane;
import com.willwinder.universalgcodesender.fx.component.sidepane.SidePane;
import com.willwinder.universalgcodesender.fx.component.sidepane.SidePaneAlignment;
import com.willwinder.universalgcodesender.fx.component.visualizer.VisualizerPane;
import com.willwinder.universalgcodesender.fx.model.UgsdWorkspaceContext;
import com.willwinder.universalgcodesender.fx.service.FxBackend;
import com.willwinder.universalgcodesender.fx.service.FxEventDispatcher;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.services.LookupService;
import com.willwinder.universalgcodesender.fx.helper.BrowserHelper;
import com.willwinder.universalgcodesender.fx.helper.FontRegistry;
import com.willwinder.universalgcodesender.fx.helper.SplitPaneDividerPersistence;
import com.willwinder.universalgcodesender.fx.helper.SvgLoader;
import com.willwinder.universalgcodesender.fx.service.JogActionRegistry;
import com.willwinder.universalgcodesender.fx.interceptor.InterceptorDialogService;
import com.willwinder.universalgcodesender.fx.service.MacroActionService;
import com.willwinder.universalgcodesender.fx.service.ShortcutService;
import com.willwinder.universalgcodesender.fx.service.WorkspaceFileLoader;
import com.willwinder.universalgcodesender.fx.service.WorkspaceManager;
import com.willwinder.universalgcodesender.fx.settings.Settings;
import com.willwinder.universalgcodesender.i18n.Localization;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.swing.UIManager;
import java.io.File;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends Application {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private SidePane leftPane;
    private SidePane rightPane;
    private SplitPane contentSplitPane;
    private StackPane contentPanel;
    private VisualizerPane visualizerPane;
    private CollapsibleSidePane leftSidePane;
    private CollapsibleSidePane rightSidePane;

    @Override
    public void init() throws Exception {
        LookupService.initialize(new FxBackend(new FxEventDispatcher()));
        LookupService.register(new WorkspaceFileLoader());

        BackendAPI backend = LookupService.lookup(BackendAPI.class);
        Localization.initialize(backend.getSettings().getLanguage());

        try {
            FlatLightLaf.setup();
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Could not load the look and feel", e);
        }

        if (Settings.getInstance().pendantAutostartProperty().get()) {
            ThreadHelper.invokeLater(() -> {
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

        MainMenuBar mainMenuBar = new MainMenuBar();
        ToolBarMenu toolBarMenu = new ToolBarMenu();
        createLeftPane();
        createRightPane();
        createContentPanel();
        createContentPane();

        // The collapsed rails sit outside the split pane so a collapsed side leaves no divider behind
        HBox workArea = new HBox(leftSidePane.getRail(), contentSplitPane, rightSidePane.getRail());
        HBox.setHgrow(contentSplitPane, Priority.ALWAYS);
        VBox.setVgrow(workArea, Priority.ALWAYS);

        VBox root = new VBox();
        Scene scene = new Scene(root);

        ShortcutService.registerListener(scene);
        FontRegistry.registerFonts();
        BrowserHelper.setHostServices(getHostServices());

        scene.getStylesheets().add(Objects.requireNonNull(Main.class.getResource("/styles/root.css")).toExternalForm());
        scene.getStylesheets().add(Objects.requireNonNull(Main.class.getResource("/styles/menu-bar.css")).toExternalForm());
        root.getChildren().addAll(mainMenuBar, toolBarMenu, workArea);

        primaryStage.setTitle("Universal G-code Sender - " + Version.getVersion());
        SvgLoader.loadIcon("icons/ugs.svg", 128).ifPresent(icon -> primaryStage.getIcons().add(icon));
        primaryStage.setScene(scene);
        restoreWindowSize(primaryStage);
        primaryStage.show();
        registerInterceptorDialogs(primaryStage);

        Parameters params = getParameters();
        if (!params.getUnnamed().isEmpty()) {
            try {
                File file = new File(params.getUnnamed().get(0));
                WorkspaceManager.getInstance().openWorkspace(file);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            openDefaultWorkspace();
        }
    }

    /**
     * Opens an empty UGSD design workspace when the application is started without a file argument,
     * so the designer is ready to use straight away.
     */
    private void openDefaultWorkspace() {
        try {
            UgsdWorkspaceContext workspace = new UgsdWorkspaceContext(null);
            WorkspaceManager.getInstance().setWorkspace(workspace);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Could not open the default design workspace", e);
        }
    }

    private void registerInterceptorDialogs(Stage primaryStage) {
        BackendAPI backend = LookupService.lookup(BackendAPI.class);
        backend.addUGSEventListener(new InterceptorDialogService(backend, primaryStage));
    }

    private void registerWindowBoundsListeners(Stage primaryStage) {
        primaryStage.widthProperty().addListener((observable, oldValue, newValue) -> Settings.getInstance().windowWidthProperty().set(newValue.doubleValue()));
        primaryStage.heightProperty().addListener((observable, oldValue, newValue) -> Settings.getInstance().windowHeightProperty().set(newValue.doubleValue()));
        primaryStage.xProperty().addListener((observable, oldValue, newValue) -> Settings.getInstance().windowPositionXProperty().set(newValue.doubleValue()));
        primaryStage.yProperty().addListener((observable, oldValue, newValue) -> Settings.getInstance().windowPositionYProperty().set(newValue.doubleValue()));
    }

    // Applied before the window is shown so the scene is laid out at its final size from the
    // start. Restoring the dividers against a scene that is resized right afterwards would apply
    // the fractions to the wrong width and then save the resized positions back over the settings.
    // The position is applied after showing instead, since window managers may ignore a position
    // requested before the window exists.
    private static void restoreWindowSize(Stage primaryStage) {
        primaryStage.setWidth(Settings.getInstance().windowWidthProperty().get());
        primaryStage.setHeight(Settings.getInstance().windowHeightProperty().get());
    }

    private void registerListeners(Stage primaryStage) {
        primaryStage.setOnShown(event -> {
            primaryStage.setX(Settings.getInstance().windowPositionXProperty().get());
            primaryStage.setY(Settings.getInstance().windowPositionYProperty().get());
            registerWindowBoundsListeners(primaryStage);

            Platform.runLater(() -> {
                SplitPaneDividerPersistence.install(contentSplitPane, leftPane, Settings.getInstance().windowDividerContentProperty());
                SplitPaneDividerPersistence.install(contentSplitPane, contentPanel, Settings.getInstance().windowDividerInspectorProperty());
            });
        });

        primaryStage.setOnCloseRequest(event -> {
            if (visualizerPane != null) {
                visualizerPane.dispose();
            }
            SettingsFactory.saveSettings();
            Platform.exit();
            System.exit(0);
        });
    }

    private void createContentPanel() {
        contentPanel = new StackPane();
        visualizerPane = new VisualizerPane();
        contentPanel.getChildren().add(visualizerPane);

        DrawerPane drawerPane = new DrawerPane();
        contentPanel.getChildren().add(drawerPane);
        StackPane.setAlignment(drawerPane, Pos.BOTTOM_RIGHT);
    }


    private void createContentPane() {
        contentSplitPane = new SplitPane();
        contentSplitPane.setMinWidth(200);
        contentSplitPane.setOrientation(Orientation.HORIZONTAL);
        contentSplitPane.getItems().add(contentPanel);
        SplitPane.setResizableWithParent(contentSplitPane, false);

        // The side panes add themselves to the split pane around the content while expanded
        leftSidePane = new CollapsibleSidePane(contentSplitPane, leftPane,
                Settings.getInstance().windowLeftPaneCollapsedProperty(), ToggleLeftPaneAction.class);
        rightSidePane = new CollapsibleSidePane(contentSplitPane, rightPane,
                Settings.getInstance().windowRightPaneCollapsedProperty(), ToggleRightPaneAction.class);
    }

    private void createLeftPane() {
        leftPane = new SidePane(SidePaneAlignment.LEFT, ToggleLeftPaneAction.class);
        leftPane.titleProperty().set(Localization.getString("actions.category.machine"));
        leftPane.setContent(new MachinePane());
    }

    private void createRightPane() {
        WorkspaceTools workspaceTools = new WorkspaceTools();
        rightPane = new SidePane(SidePaneAlignment.RIGHT, ToggleRightPaneAction.class);
        rightPane.titleProperty().bind(workspaceTools.titleProperty());
        rightPane.contentProperty().bind(workspaceTools.contentProperty());
    }

    public static void main(String[] args) {
        launch(Main.class, args);
    }
}

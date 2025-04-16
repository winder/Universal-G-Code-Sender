package com.willwinder.universalgcodesender.fx;

import com.formdev.flatlaf.FlatLightLaf;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.fx.actions.ActionRegistry;
import com.willwinder.universalgcodesender.fx.actions.StartAction;
import com.willwinder.universalgcodesender.fx.component.JogPane;
import com.willwinder.universalgcodesender.fx.component.MachineStatusPane;
import com.willwinder.universalgcodesender.fx.component.ToolBarMenu;
import com.willwinder.universalgcodesender.fx.component.overlay.OverlayPane;
import com.willwinder.universalgcodesender.fx.helper.SvgLoader;
import com.willwinder.universalgcodesender.fx.visualizer.Visualizer;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.pendantui.PendantUI;
import com.willwinder.universalgcodesender.utils.Settings;
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
    private static Settings settings;
    private SplitPane leftSplitPane;
    private SplitPane contentSplitPane;
    private StackPane contentPanel;
    private SplitPane.Divider contentPaneDivider;
    private SplitPane.Divider leftPaneDivider;

    @Override
    public void start(Stage primaryStage) {
        initialize();
        registerListeners(primaryStage);

        ToolBarMenu toolBarMenu = new ToolBarMenu();
        createLeftPane();
        createContentPanel();
        createContentPane();
        VBox.setVgrow(contentSplitPane, Priority.ALWAYS);

        VBox root = new VBox();
        Scene scene = new Scene(root);
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
        primaryStage.widthProperty().addListener((observable, oldValue, newValue) -> settings.getFxSettings().setWindowWidth((int) newValue.doubleValue()));
        primaryStage.heightProperty().addListener((observable, oldValue, newValue) -> settings.getFxSettings().setWindowHeight((int) newValue.doubleValue()));
        primaryStage.xProperty().addListener((observable, oldValue, newValue) -> settings.getFxSettings().setWindowPositionX((int) newValue.doubleValue()));
        primaryStage.yProperty().addListener((observable, oldValue, newValue) -> settings.getFxSettings().setWindowPositionY((int) newValue.doubleValue()));
        leftPaneDivider.positionProperty().addListener((obs, oldVal, newVal) -> settings.getFxSettings().setDividerLeftPercent(newVal.doubleValue()));
        contentPaneDivider.positionProperty().addListener((obs, oldVal, newVal) -> settings.getFxSettings().setDividerContentPercent(newVal.doubleValue()));
    }

    private void registerListeners(Stage primaryStage) {
        primaryStage.setOnShown(event -> {
            primaryStage.setX(settings.getFxSettings().getWindowPositionX());
            primaryStage.setY(settings.getFxSettings().getWindowPositionY());
            primaryStage.setWidth(settings.getFxSettings().getWindowWidth());
            primaryStage.setHeight(settings.getFxSettings().getWindowHeight());
            leftPaneDivider.setPosition(settings.getFxSettings().getDividerLeftPercent());
            contentPaneDivider.setPosition(settings.getFxSettings().getDividerContentPercent());


            // Hack to make sure that the window is shown with correct size before setting the dividers
            ThreadHelper.invokeLater(() -> {
                leftPaneDivider.setPosition(settings.getFxSettings().getDividerLeftPercent());
                contentPaneDivider.setPosition(settings.getFxSettings().getDividerContentPercent());
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

        OverlayPane overlayPane = new OverlayPane();
        contentPanel.getChildren().add(overlayPane);
        StackPane.setAlignment(overlayPane, Pos.BOTTOM_RIGHT);
    }


    private void createContentPane() {
        contentSplitPane = new SplitPane();
        contentSplitPane.setMinWidth(200);
        contentSplitPane.setOrientation(Orientation.HORIZONTAL);
        contentSplitPane.getItems().addAll(leftSplitPane, contentPanel);
        SplitPane.setResizableWithParent(contentSplitPane, false);

        contentPaneDivider = contentSplitPane.getDividers().get(0);
        contentPaneDivider.setPosition(settings.getFxSettings().getDividerContentPercent());
    }

    private void createLeftPane() {
        leftSplitPane = new SplitPane();
        leftSplitPane.setMinWidth(200);
        leftSplitPane.setOrientation(Orientation.VERTICAL);
        leftSplitPane.getItems().addAll(new MachineStatusPane(), new JogPane());
        SplitPane.setResizableWithParent(leftSplitPane, false);

        leftPaneDivider = leftSplitPane.getDividers().get(0);
        leftPaneDivider.setPosition(settings.getFxSettings().getDividerLeftPercent());
    }

    private void registerShortCuts(Scene scene) {
        KeyCombination kc = new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN);
        scene.getAccelerators().put(kc, () -> ActionRegistry.getInstance()
                .getAction(StartAction.class.getCanonicalName())
                .ifPresent(a -> a.handle(null)));
    }

    private void initialize() {
        try {
            BackendAPI backendAPI = CentralLookup.getDefault().lookup(BackendAPI.class);
            settings = backendAPI.getSettings();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Could not load settings, resetting to default", e);
            settings = new Settings();
        }

        try {
            FlatLightLaf.setup();
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Could not load the look and feel", e);
        }


        if (settings.isAutoStartPendant()) {
            BackendAPI backend = CentralLookup.getDefault().lookup(BackendAPI.class);
            PendantUI pendantUI = new PendantUI(backend);
            pendantUI.start();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

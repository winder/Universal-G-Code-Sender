package com.willwinder.universalgcodesender.fx;

import com.formdev.flatlaf.FlatLightLaf;
import com.willwinder.ugs.nbp.dro.MachineStatusTopComponent;
import com.willwinder.ugs.nbp.jog.JogTopComponent;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.fx.actions.ActionRegistry;
import com.willwinder.universalgcodesender.fx.actions.StartAction;
import com.willwinder.universalgcodesender.fx.component.ToolBarMenu;
import com.willwinder.universalgcodesender.fx.helper.SvgLoader;
import com.willwinder.universalgcodesender.fx.visualizer.Visualizer;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.pendantui.PendantUI;
import com.willwinder.universalgcodesender.utils.Settings;
import com.willwinder.universalgcodesender.utils.SettingsFactory;
import com.willwinder.universalgcodesender.utils.Version;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.geometry.Orientation;
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
    private final VBox root = new VBox();

    @Override
    public void start(Stage primaryStage) {
        initialize(primaryStage);

        ToolBarMenu toolBarMenu = new ToolBarMenu();

        SwingNode machineStatusPanel = new SwingNode();
        machineStatusPanel.setContent(new MachineStatusTopComponent());

        SwingNode jogPanel = new SwingNode();
        jogPanel.setContent(new JogTopComponent());


        SplitPane settingsSplitPane = new SplitPane();
        settingsSplitPane.setMinWidth(200);
        settingsSplitPane.setDividerPositions(0.5);
        settingsSplitPane.setOrientation(Orientation.VERTICAL);
        settingsSplitPane.getItems().addAll(machineStatusPanel, jogPanel);


        // ===== Content Panel (Right) =====
        StackPane contentPanel = new StackPane();
        contentPanel.getChildren().add(new Visualizer());

        // ===== SplitPane =====
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);
        splitPane.getItems().addAll(settingsSplitPane, contentPanel);
        SplitPane.setResizableWithParent(splitPane, false);
        SplitPane.Divider splitPaneDivider = splitPane.getDividers().get(0);


        // ===== Top-level Layout =====
        root.getChildren().addAll(toolBarMenu, splitPane);
        VBox.setVgrow(splitPane, Priority.ALWAYS); // Make splitPane expand


        // ===== Scene and Stage =====
        Scene scene = new Scene(root);
        primaryStage.setTitle("Universal G-code Sender - " + Version.getVersion());
        SvgLoader.loadIcon("icons/ugs.svg", 128).ifPresent(icon -> primaryStage.getIcons().add(icon));
        primaryStage.setScene(scene);
        primaryStage.show();

        registerShortCuts(scene);


        splitPaneDivider.setPosition(settings.getFxSettings().getDividerContentPercent());
        splitPaneDivider.positionProperty().addListener((obs, oldVal, newVal) -> settings.getFxSettings().setDividerContentPercent(newVal.doubleValue()));

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

    private void registerShortCuts(Scene scene) {
        KeyCombination kc = new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN);
        scene.getAccelerators().put(kc, () -> ActionRegistry.getInstance()
                .getAction(StartAction.class.getCanonicalName())
                .ifPresent(a -> a.handle(null)));
    }

    private void initialize(Stage primaryStage) {
        try {
            BackendAPI backendAPI = CentralLookup.getDefault().lookup(BackendAPI.class);
            settings = backendAPI.getSettings();
            primaryStage.setWidth(settings.getFxSettings().getWindowWidth());
            primaryStage.setHeight(settings.getFxSettings().getWindowHeight());
            primaryStage.setX(settings.getFxSettings().getWindowPositionX());
            primaryStage.setY(settings.getFxSettings().getWindowPositionY());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Could not load settings, resetting to default", e);
            settings = new Settings();
        }

        try {
            FlatLightLaf.setup();
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        primaryStage.setOnShown(e -> root.requestFocus());

        // Exit when the window is closed
        primaryStage.setOnCloseRequest(event -> {
            settings.getFxSettings().setWindowWidth((int) primaryStage.getWidth());
            settings.getFxSettings().setWindowHeight((int) primaryStage.getHeight());
            settings.getFxSettings().setWindowPositionX((int) primaryStage.getX());
            settings.getFxSettings().setWindowPositionY((int) primaryStage.getY());
            SettingsFactory.saveSettings();

            Platform.exit(); // Clean shutdown of the application thread
            System.exit(0);  // Optional: force the JVM to exit
        });


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

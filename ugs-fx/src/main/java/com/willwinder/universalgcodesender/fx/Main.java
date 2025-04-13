package com.willwinder.universalgcodesender.fx;

import com.formdev.flatlaf.FlatLightLaf;
import com.willwinder.ugs.nbp.dro.MachineStatusTopComponent;
import com.willwinder.ugs.nbp.jog.JogTopComponent;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.fx.component.ToolBarMenu;
import com.willwinder.universalgcodesender.fx.visualizer.Visualizer;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.utils.Settings;
import com.willwinder.universalgcodesender.utils.SettingsFactory;
import com.willwinder.universalgcodesender.utils.Version;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
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
        VBox root = new VBox();
        root.getChildren().addAll(toolBarMenu, splitPane);
        VBox.setVgrow(splitPane, Priority.ALWAYS); // Make splitPane expand


        // ===== Scene and Stage =====
        Scene scene = new Scene(root);
        primaryStage.setTitle("Universal G-code Sender - " + Version.getVersion());
        primaryStage.getIcons().add(new Image("icons/icon.png"));
        primaryStage.setScene(scene);
        primaryStage.show();

        splitPaneDivider.setPosition(settings.getFxSettings().getDividerContentPercent());
        splitPaneDivider.positionProperty().addListener((obs, oldVal, newVal) -> settings.getFxSettings().setDividerContentPercent(newVal.doubleValue()));

        Parameters params = getParameters();
        if (!params.getUnnamed().isEmpty()) {
            BackendAPI backendAPI = CentralLookup.getDefault().lookup(BackendAPI.class);
            try {
                backendAPI.setGcodeFile(new File(params.getUnnamed().get(0)));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void initialize(Stage primaryStage) {
        try {
            settings = SettingsFactory.loadSettings();
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
    }

    public static void main(String[] args) {
        launch(args);
    }
}

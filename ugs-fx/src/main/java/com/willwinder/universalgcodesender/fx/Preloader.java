package com.willwinder.universalgcodesender.fx;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Preloader extends javafx.application.Preloader {

    private Stage preloaderStage;

    @Override
    public void start(Stage primaryStage) {
        this.preloaderStage = primaryStage;

        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setPrefSize(50, 50);

        VBox box = new VBox(10, new Label("Starting..."), spinner);
        box.setAlignment(Pos.CENTER);
        Scene scene = new Scene(box, 300, 150);

        primaryStage.setScene(scene);
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.show();
    }

    @Override
    public void handleStateChangeNotification(StateChangeNotification info) {
        if (info.getType() == StateChangeNotification.Type.BEFORE_START) {
            preloaderStage.hide();
        }
    }
}
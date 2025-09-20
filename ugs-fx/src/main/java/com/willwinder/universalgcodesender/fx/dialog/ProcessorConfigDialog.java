package com.willwinder.universalgcodesender.fx.dialog;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.willwinder.universalgcodesender.fx.component.ButtonBox;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.utils.ControllerSettings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.Objects;

public class ProcessorConfigDialog extends Stage {
    private final TextArea textArea = new TextArea();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private JsonObject result = null;

    public ProcessorConfigDialog(Window parent, ControllerSettings.ProcessorConfig config) {
        initOwner(parent);
        initModality(Modality.APPLICATION_MODAL);
        setAlwaysOnTop(true);
        setTitle(Localization.getString("settings.processor.settings"));
        result = config.args;

        textArea.setText(GSON.toJson(config.args));

        VBox content = new VBox(10, textArea);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(10));

        ButtonBox buttonBar = new ButtonBox();
        Button acceptButton = new Button(Localization.getString("mainWindow.swing.okButton"));
        ButtonBox.setButtonData(acceptButton, ButtonBox.ButtonData.OK_DONE);
        acceptButton.setOnAction(event -> {
            result = JsonParser.parseString(textArea.getText()).getAsJsonObject();
            close();
        });

        Button cancelButton = new Button(Localization.getString("mainWindow.swing.cancelButton"));
        cancelButton.setOnAction(event -> close());
        ButtonBox.setButtonData(cancelButton, ButtonBox.ButtonData.LEFT);
        buttonBar.getButtons().addAll(cancelButton, acceptButton);

        BorderPane root = new BorderPane();
        root.setCenter(content);
        root.setBottom(buttonBar);

        Scene scene = new Scene(root, 300, 150);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/root.css")).toExternalForm());

        setScene(scene);

        setOnShown(e -> {
            Window owner = getOwner();
            if (owner != null) {
                double centerX = owner.getX() + owner.getWidth() / 2;
                double centerY = owner.getY() + owner.getHeight() / 2;

                setX(centerX - getWidth() / 2);
                setY(centerY - getHeight() / 2);
            }
        });
    }


    public JsonObject getConfig() {
        return result;
    }
}

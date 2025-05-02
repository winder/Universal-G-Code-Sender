package com.willwinder.universalgcodesender.fx.component.settings;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.fx.helper.Colors;
import com.willwinder.universalgcodesender.fx.helper.SvgLoader;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.function.UnaryOperator;

public class PendantSettingsPane extends VBox {

    public static final int TEXT_FIELD_WIDTH = 200;
    private final BackendAPI backend;

    public PendantSettingsPane() {
        setSpacing(20);

        backend = CentralLookup.getDefault().lookup(BackendAPI.class);

        addTitleSection();
        addAutoStartPendant();
        addWorkspaceSection();
        addPortSection();
    }

    private void addPortSection() {
        Label label = new Label(Localization.getString("settings.pendantPort"));
        TextField pendantPort = new TextField(String.valueOf(backend.getSettings().getPendantPort()));

        UnaryOperator<TextFormatter.Change> filter = change -> {
            String text = change.getControlNewText();
            if (text.matches("\\d*") && text.length() <= 5) {
                return change;
            }

            return null;
        };

        pendantPort.setTextFormatter(new TextFormatter<>(filter));
        pendantPort.setPrefWidth(60);
        getChildren().add(new VBox(5, label, new HBox(pendantPort)));
    }

    private void addWorkspaceSection() {
        Label autoStartPendantLabel = new Label(Localization.getString("settings.workspaceDirectory"));
        TextField workspaceDirectory = new TextField(backend.getSettings().getWorkspaceDirectory());
        workspaceDirectory.setPrefWidth(TEXT_FIELD_WIDTH);
        workspaceDirectory.setFocusTraversable(false);
        workspaceDirectory.textProperty().addListener((observable, oldValue, newValue) -> backend.getSettings().setWorkspaceDirectory(newValue));

        Button browseButton = new Button(Localization.getString("mainWindow.swing.browseButton"), SvgLoader.loadImageIcon("icons/open.svg", 16, Colors.BLACKISH).orElse(null));
        browseButton.setOnAction(e -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            if (StringUtils.isNotEmpty(backend.getSettings().getWorkspaceDirectory())) {
                File directory = new File(backend.getSettings().getWorkspaceDirectory());
                if (directory.exists()) {
                    directoryChooser.setInitialDirectory(directory);
                }
            }
            directoryChooser.setTitle("Select Directory");
            File selectedDirectory = directoryChooser.showDialog(getScene().getWindow());

            if (selectedDirectory != null) {
                backend.getSettings().setWorkspaceDirectory(selectedDirectory.getAbsolutePath());
                workspaceDirectory.setText(selectedDirectory.getAbsolutePath());
            }
        });

        workspaceDirectory.setStyle("-fx-border-radius: 3 0 0 3; -fx-background-radius: 3 0 0 3;");
        browseButton.setStyle("-fx-border-radius: 0 3 3 0; -fx-background-radius: 0 3 3 0; -fx-border-width: 1 1 1 0;");


        HBox hbox = new HBox(workspaceDirectory, browseButton);
        hbox.setAlignment(Pos.CENTER_LEFT);

        getChildren().add(new VBox(5, autoStartPendantLabel, hbox));

    }

    private void addTitleSection() {
        Label title = new Label(Localization.getString("mainWindow.swing.pendant"));
        title.setFont(Font.font(20));
        getChildren().add(title);
    }

    private void addAutoStartPendant() {
        CheckBox autoStartPendant = new CheckBox(Localization.getString("sender.autostartpendant"));
        autoStartPendant.setSelected(backend.getSettings().isAutoStartPendant());
        autoStartPendant.setOnAction((event) -> ThreadHelper.invokeLater(() -> backend.getSettings().setAutoStartPendant(autoStartPendant.isSelected())));

        getChildren().add(new VBox(5, autoStartPendant));
    }


}

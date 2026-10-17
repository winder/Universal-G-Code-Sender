/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.universalgcodesender.fx.stage;

import com.willwinder.universalgcodesender.fx.component.ButtonBox;
import com.willwinder.universalgcodesender.fx.helper.BrowserHelper;
import com.willwinder.universalgcodesender.fx.helper.SvgLoader;
import com.willwinder.universalgcodesender.fx.service.InMemoryLogHandler;
import com.willwinder.universalgcodesender.fx.service.VisualizerService;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.utils.Version;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Shows information about the application together with the application log captured by the
 * {@link InMemoryLogHandler}. The log view follows new entries while the stage is showing.
 */
public class AboutStage extends Stage {
    private static final String WEBSITE_URL = "https://universalgcodesender.com";
    private static final String APPLICATION_NAME = "Universal G-code Sender";
    private static final int LOGO_SIZE = 64;

    private final InMemoryLogHandler logHandler = InMemoryLogHandler.getInstance();
    private final TextArea logTextArea = new TextArea();
    private final Consumer<String> logListener = entry -> Platform.runLater(() -> logTextArea.appendText(entry));

    public AboutStage(Window owner) {
        initModality(Modality.NONE);
        if (owner != null) {
            initOwner(owner);
        }
        setTitle(Localization.getString("actions.openAbout"));

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getTabs().add(new Tab(Localization.getString("actions.openAbout"), createAboutPane()));
        tabPane.getTabs().add(new Tab(Localization.getString("about.log"), createLogPane()));

        Button closeButton = new Button(Localization.getString("close"));
        closeButton.setCancelButton(true);
        closeButton.setOnAction(_ -> close());
        ButtonBox buttonBox = new ButtonBox();
        ButtonBox.setButtonData(closeButton, ButtonBox.ButtonData.CANCEL_CLOSE);
        buttonBox.getButtons().add(closeButton);

        BorderPane root = new BorderPane();
        root.setCenter(tabPane);
        root.setBottom(buttonBox);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/root.css")).toExternalForm());
        setScene(scene);
        setWidth(700);
        setHeight(500);
        setResizable(true);
        SvgLoader.loadIcon("icons/ugs.svg", 128).ifPresent(icon -> getIcons().add(icon));

        registerListeners();
    }

    private void registerListeners() {
        setOnShowing(_ -> {
            centerOnOwner();
            logTextArea.setText(logHandler.getLogText());
            logTextArea.positionCaret(logTextArea.getLength());
            logHandler.addListener(logListener);
        });
        setOnHidden(_ -> logHandler.removeListener(logListener));
    }

    private void centerOnOwner() {
        Window owner = getOwner();
        if (owner == null) {
            centerOnScreen();
            return;
        }
        setX(owner.getX() + owner.getWidth() / 2 - getWidth() / 2);
        setY(owner.getY() + owner.getHeight() / 2 - getHeight() / 2);
    }

    private VBox createAboutPane() {
        Label nameLabel = new Label(APPLICATION_NAME);
        nameLabel.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");
        Label versionLabel = new Label(Localization.getString("version") + " " + Version.getVersionString());

        VBox titleBox = new VBox(4, nameLabel, versionLabel);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        HBox header = new HBox(16, titleBox);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().addFirst(createLogo());

        GridPane details = new GridPane();
        details.setHgap(16);
        details.setVgap(6);
        addDetailRow(details, 0, Localization.getString("about.buildDate"), Version.getBuildDate());
        addDetailRow(details, 1, Localization.getString("about.javaVersion"), System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")");
        addDetailRow(details, 2, Localization.getString("about.javafxVersion"), System.getProperty("javafx.version", ""));
        addDetailRow(details, 3, Localization.getString("about.operatingSystem"), System.getProperty("os.name") + " " + System.getProperty("os.version") + " (" + System.getProperty("os.arch") + ")");
        addDetailRow(details, 4, Localization.getString("about.graphics"), "").textProperty().bind(graphicsDescription());

        Hyperlink websiteLink = new Hyperlink(WEBSITE_URL);
        websiteLink.setPadding(Insets.EMPTY);
        websiteLink.setOnAction(_ -> BrowserHelper.open(WEBSITE_URL));
        HBox websiteBox = new HBox(16, new Label(Localization.getString("about.website")), websiteLink);
        websiteBox.setAlignment(Pos.CENTER_LEFT);

        Label licenseLabel = new Label(Localization.getString("about.license"));
        licenseLabel.setWrapText(true);

        VBox aboutPane = new VBox(20, header, details, websiteBox, licenseLabel);
        aboutPane.setPadding(new Insets(20));
        return aboutPane;
    }

    private ImageView createLogo() {
        Image logo = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/ugs.png")));
        ImageView logoView = new ImageView(logo);
        logoView.setFitWidth(LOGO_SIZE);
        logoView.setFitHeight(LOGO_SIZE);
        logoView.setPreserveRatio(true);
        logoView.setSmooth(true);
        return logoView;
    }

    private Label addDetailRow(GridPane grid, int row, String title, String value) {
        Label valueLabel = new Label(value);
        valueLabel.setWrapText(true);
        grid.add(new Label(title), 0, row);
        grid.add(valueLabel, 1, row);
        return valueLabel;
    }

    private StringBinding graphicsDescription() {
        ReadOnlyStringProperty description = VisualizerService.getInstance().rendererDescriptionProperty();
        return Bindings.createStringBinding(
                () -> description.get().isBlank() ? Localization.getString("about.graphicsUnavailable") : description.get(),
                description);
    }

    private BorderPane createLogPane() {
        logTextArea.setEditable(false);
        logTextArea.setWrapText(false);
        logTextArea.setStyle("-fx-font-family: monospace; -fx-font-size: 11;");

        Button copyButton = new Button(Localization.getString("about.copyLog"));
        copyButton.setOnAction(_ -> copyLogToClipboard());
        Button clearButton = new Button(Localization.getString("mainWindow.swing.clearOutput"));
        clearButton.setOnAction(_ -> {
            logHandler.clear();
            logTextArea.clear();
        });

        HBox buttons = new HBox(8, copyButton, clearButton);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.setPadding(new Insets(8, 0, 0, 0));

        BorderPane logPane = new BorderPane();
        logPane.setPadding(new Insets(10));
        logPane.setCenter(logTextArea);
        logPane.setBottom(buttons);
        return logPane;
    }

    private void copyLogToClipboard() {
        ClipboardContent content = new ClipboardContent();
        content.putString(logTextArea.getText());
        Clipboard.getSystemClipboard().setContent(content);
    }
}

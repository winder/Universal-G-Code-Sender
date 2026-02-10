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
package com.willwinder.universalgcodesender.fx.component.drawer;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.MessageType;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.StyleClassedTextArea;

import java.util.logging.Level;
import java.util.logging.Logger;

public class TerminalDrawer extends Drawer {
    private static final Logger LOGGER = Logger.getLogger(TerminalDrawer.class.getCanonicalName());
    private final StyleClassedTextArea logArea = new StyleClassedTextArea();

    private final TextField inputField = new TextField();
    private final BackendAPI backend;

    public TerminalDrawer() {
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        BorderPane borderPane = new BorderPane();

        getStylesheets().add(getClass().getResource("/styles/terminal-pane.css").toExternalForm());
        setupLogArea();
        setupInputField();


        borderPane.setCenter(new VirtualizedScrollPane<>(logArea));
        borderPane.setBottom(inputField);
        BorderPane.setMargin(inputField, new Insets(0, 0, 10, 0));
        getChildren().add(borderPane);
        backend.addUGSEventListener(this::onEvent);
        backend.addMessageListener(this::onMessage);
    }

    private void onMessage(MessageType messageType, String text) {
        Platform.runLater(() -> {
            if ((!messageType.equals(MessageType.VERBOSE) || backend.getSettings().isVerboseOutputEnabled())) {
                String styleClass = switch (messageType) {
                    case VERBOSE -> "verbose";
                    case INFO -> "info";
                    case ERROR -> "error";
                };

                logArea.append(text, styleClass);
                logArea.moveTo(logArea.getLength());
                logArea.requestFollowCaret();
            }
        });
    }

    private void onEvent(UGSEvent event) {

    }

    private void setupLogArea() {
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setFocusTraversable(false);
        logArea.getStyleClass().add("terminal");
        logArea.setPadding(new Insets(5));

        ContextMenu contextMenu = new ContextMenu();
        CheckMenuItem verboseLoggingItem = new CheckMenuItem(Localization.getString("mainWindow.swing.showVerboseOutputCheckBox"));
        verboseLoggingItem.setSelected(backend.getSettings().isVerboseOutputEnabled());
        verboseLoggingItem.selectedProperty().addListener((observable, oldValue, newValue) -> backend.getSettings().setVerboseOutputEnabled(newValue));
        contextMenu.getItems().add(verboseLoggingItem);
        logArea.setContextMenu(contextMenu);
    }

    private void setupInputField() {
        inputField.setPromptText("Enter command...");
        inputField.setFont(Font.font("Monospaced", 12));
        inputField.setPadding(new Insets(10));
        inputField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String command = inputField.getText().trim();
                if (command.isEmpty()) {
                    command = "\n";
                }

                try {
                    backend.sendGcodeCommand(command);
                    inputField.clear();
                } catch (Exception e) {
                    String message = "Could not send command: " + e.getMessage();
                    LOGGER.log(Level.SEVERE, message);
                    logArea.append(message + "\n", "error");
                }
            }
        });
    }

    @Override
    public void setActive(boolean active) {

    }
}

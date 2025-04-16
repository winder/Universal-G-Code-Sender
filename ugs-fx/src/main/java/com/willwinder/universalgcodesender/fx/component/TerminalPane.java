package com.willwinder.universalgcodesender.fx.component;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.listeners.MessageType;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;

import java.util.logging.Level;
import java.util.logging.Logger;

public class TerminalPane extends BorderPane {
    private static final Logger LOGGER = Logger.getLogger(TerminalPane.class.getCanonicalName());
    private final TextArea logArea = new TextArea();
    private final TextField inputField = new TextField();
    private final BackendAPI backend;

    public TerminalPane() {
        getStylesheets().add(getClass().getResource("/styles/terminal-pane.css").toExternalForm());

        setupLogArea();
        setupInputField();


        setCenter(logArea);
        setBottom(inputField);

        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        backend.addUGSEventListener(this::onEvent);
        backend.addMessageListener(this::onMessage);
    }

    private void onMessage(MessageType messageType, String text) {
        Platform.runLater(() -> {
            if (!messageType.equals(MessageType.VERBOSE)) {
                logArea.appendText(text);
                logArea.setScrollTop(Double.MAX_VALUE);
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
    }

    private void setupInputField() {
        inputField.setPromptText("Enter command...");
        inputField.setFont(Font.font("Monospaced", 12));
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
                    LOGGER.log(Level.SEVERE, "Could not send command: " + e.getMessage());
                }
            }
        });
    }

    public void append(String message) {
        logArea.appendText(message + "\n");
        logArea.setScrollTop(Double.MAX_VALUE); // Scroll to bottom
    }
}

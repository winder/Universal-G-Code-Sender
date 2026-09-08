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

import com.willwinder.ugs.designer.logic.ToolLibraryListener;
import com.willwinder.ugs.designer.logic.ToolLibraryService;
import com.willwinder.ugs.designer.model.toollibrary.EndmillShape;
import com.willwinder.ugs.designer.model.toollibrary.ToolDefinition;
import com.willwinder.universalgcodesender.fx.component.ButtonBox;
import com.willwinder.universalgcodesender.fx.component.toollibrary.ToolEditorPane;
import com.willwinder.universalgcodesender.fx.component.toollibrary.ToolListCell;
import com.willwinder.universalgcodesender.fx.service.ToolLibraryProvider;
import com.willwinder.universalgcodesender.model.UnitUtils;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The tool library: a list of the tools with an editor for the selected one. Edits are written to
 * the library as they are made. Opened with {@link #show} to manage the library, or with
 * {@link #pick} to let the user choose a tool for a design.
 */
public class ToolLibraryStage extends Stage {
    private static final double WIDTH = 780;
    private static final double HEIGHT = 560;

    enum Mode {
        MANAGE("Tool library"),
        PICK("Select tool");

        private final String title;

        Mode(String title) {
            this.title = title;
        }
    }

    private final ToolLibraryService service;
    private final Mode mode;
    private final ListView<ToolDefinition> toolList = new ListView<>();
    private final ToolEditorPane editor;
    private final Button duplicateButton = new Button("Duplicate");
    private final Button deleteButton = new Button("Delete");
    private final Button revertButton = new Button("Revert");
    private final ToolLibraryListener libraryListener = this::onLibraryChangedExternally;
    /**
     * Edits made here notify the library listeners too. Counting them lets those notifications be
     * told apart from changes made elsewhere, which do warrant reloading the list.
     */
    private int pendingSelfTriggeredEvents;
    private ToolDefinition result;

    ToolLibraryStage(Window owner, ToolLibraryService service, UnitUtils.Units preferredUnits, Mode mode, String selectedToolId) {
        this.service = service;
        this.mode = mode;
        initModality(Modality.APPLICATION_MODAL);
        if (owner != null) {
            initOwner(owner);
        }
        setTitle(mode.title);
        editor = new ToolEditorPane(preferredUnits);
        editor.setChangeListener(this::onEditorChanged);
        setScene(createScene());
        setWidth(WIDTH);
        setHeight(HEIGHT);
        setMinWidth(560);
        setMinHeight(400);
        refreshList(selectedToolId);
        service.addListener(libraryListener);
        setOnHidden(event -> service.removeListener(libraryListener));
        setOnShowing(event -> centerOnOwner());
    }

    private Scene createScene() {
        toolList.setCellFactory(list -> new ToolListCell());
        toolList.getSelectionModel().selectedItemProperty().addListener((observable, was, selected) -> onSelectionChanged());
        if (mode == Mode.PICK) {
            toolList.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 && selectedTool() != null) {
                    acceptAndClose();
                }
            });
        }
        VBox.setVgrow(toolList, Priority.ALWAYS);

        Button addButton = new Button("Add");
        addButton.setOnAction(event -> onAdd());
        duplicateButton.setOnAction(event -> onDuplicate());
        deleteButton.setOnAction(event -> onDelete());
        revertButton.setOnAction(event -> onRevert());
        HBox listButtons = new HBox(6, addButton, duplicateButton, deleteButton, revertButton);
        listButtons.getChildren().forEach(button -> {
            HBox.setHgrow(button, Priority.ALWAYS);
            ((Button) button).setMaxWidth(Double.MAX_VALUE);
        });

        VBox left = new VBox(6, toolList, listButtons);
        left.setPadding(new Insets(8));

        ScrollPane editorScroll = new ScrollPane(editor);
        editorScroll.setFitToWidth(true);

        SplitPane split = new SplitPane(left, editorScroll);
        split.setDividerPositions(0.42);

        BorderPane root = new BorderPane();
        root.setCenter(split);
        root.setBottom(createBottomBar());

        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/root.css")).toExternalForm());
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                cancelAndClose();
            }
        });
        return scene;
    }

    private ButtonBox createBottomBar() {
        ButtonBox buttons = new ButtonBox();
        if (mode == Mode.PICK) {
            Button select = new Button("Select");
            select.setDefaultButton(true);
            select.setOnAction(event -> acceptAndClose());
            select.disableProperty().bind(toolList.getSelectionModel().selectedItemProperty().isNull());
            Button cancel = new Button("Cancel");
            cancel.setOnAction(event -> cancelAndClose());
            ButtonBox.setButtonData(cancel, ButtonBox.ButtonData.CANCEL_CLOSE);
            ButtonBox.setButtonData(select, ButtonBox.ButtonData.OK_DONE);
            buttons.getButtons().addAll(cancel, select);
        } else {
            Button close = new Button("Close");
            close.setOnAction(event -> cancelAndClose());
            ButtonBox.setButtonData(close, ButtonBox.ButtonData.OK_DONE);
            buttons.getButtons().add(close);
        }
        return buttons;
    }

    private void acceptAndClose() {
        ToolDefinition selected = selectedTool();
        // Read back through the service so a just committed edit is part of the returned tool
        result = selected == null ? null : service.getById(selected.getId()).orElse(selected);
        close();
    }

    private void cancelAndClose() {
        result = null;
        close();
    }

    public Optional<ToolDefinition> getResult() {
        return Optional.ofNullable(result);
    }

    private ToolDefinition selectedTool() {
        return toolList.getSelectionModel().getSelectedItem();
    }

    private void refreshList(String preferredSelectionId) {
        List<ToolDefinition> tools = service.getTools();
        toolList.getItems().setAll(tools);
        if (tools.isEmpty()) {
            editor.setTool(null, true);
            updateButtonState();
            return;
        }
        int index = 0;
        if (preferredSelectionId != null) {
            for (int i = 0; i < tools.size(); i++) {
                if (preferredSelectionId.equals(tools.get(i).getId())) {
                    index = i;
                    break;
                }
            }
        }
        toolList.getSelectionModel().clearAndSelect(index);
        toolList.scrollTo(index);
        onSelectionChanged();
    }

    private void onSelectionChanged() {
        editor.setTool(selectedTool(), false);
        updateButtonState();
    }

    private void updateButtonState() {
        ToolDefinition selected = selectedTool();
        boolean hasSelection = selected != null;
        boolean isCustom = hasSelection && selected.isCustomSentinel();
        duplicateButton.setDisable(!hasSelection || isCustom);
        deleteButton.setDisable(!hasSelection || isCustom);
        revertButton.setDisable(!hasSelection || !selected.isBuiltIn() || isCustom);
    }

    private void onEditorChanged(ToolDefinition edited) {
        if (edited == null) {
            return;
        }
        pendingSelfTriggeredEvents++;
        try {
            ToolDefinition stored = service.updateTool(edited);
            replaceInList(stored);
        } catch (RuntimeException e) {
            pendingSelfTriggeredEvents--;
            Alert alert = new Alert(Alert.AlertType.WARNING, e.getMessage(), ButtonType.OK);
            alert.setHeaderText(null);
            alert.setTitle("Tool library");
            alert.initOwner(this);
            alert.showAndWait();
            restoreEditorFromLibrary(edited.getId());
        }
    }

    /**
     * Updates the list row without disturbing the selection.
     */
    private void replaceInList(ToolDefinition tool) {
        List<ToolDefinition> items = toolList.getItems();
        for (int i = 0; i < items.size(); i++) {
            if (tool.getId().equals(items.get(i).getId())) {
                boolean selected = toolList.getSelectionModel().getSelectedIndex() == i;
                items.set(i, tool);
                if (selected) {
                    toolList.getSelectionModel().select(i);
                }
                return;
            }
        }
    }

    private void restoreEditorFromLibrary(String id) {
        service.getById(id).ifPresent(stored -> {
            replaceInList(stored);
            ToolDefinition selected = selectedTool();
            if (selected != null && id.equals(selected.getId())) {
                editor.setTool(stored, false);
            }
        });
    }

    private void onAdd() {
        ToolDefinition tool = new ToolDefinition();
        tool.setName("New tool");
        tool.setShape(EndmillShape.UPCUT);
        tool.setDiameter(3.0);
        tool.setDiameterUnit(UnitUtils.Units.MM);
        tool.setFeedSpeed(900);
        tool.setPlungeSpeed(300);
        tool.setDepthPerPass(1.0);
        tool.setStepOverPercent(0.4);
        tool.setMaxSpindleSpeed(18000);
        tool.setToolNumber(service.nextAvailableToolNumber());
        ToolDefinition added = withSelfTriggeredEvent(() -> service.addTool(tool));
        refreshList(added.getId());
    }

    private void onDuplicate() {
        ToolDefinition selected = selectedTool();
        if (selected == null) {
            return;
        }
        ToolDefinition copy = withSelfTriggeredEvent(() -> service.duplicate(selected.getId()));
        refreshList(copy.getId());
    }

    private void onDelete() {
        ToolDefinition selected = selectedTool();
        if (selected == null || !confirm("Delete tool", "Delete tool \"" + selected.getName() + "\"?")) {
            return;
        }
        withSelfTriggeredEvent(() -> {
            service.deleteTool(selected.getId());
            return null;
        });
        refreshList(null);
    }

    private void onRevert() {
        ToolDefinition selected = selectedTool();
        if (selected == null || !selected.isBuiltIn()) {
            return;
        }
        if (!confirm("Revert tool", "Restore default values for \"" + selected.getName() + "\"?\nThe name will be kept.")) {
            return;
        }
        ToolDefinition reset = withSelfTriggeredEvent(() -> service.revertToDefault(selected.getId()));
        refreshList(reset.getId());
    }

    private boolean confirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        alert.setHeaderText(null);
        alert.setTitle(title);
        alert.initOwner(this);
        return alert.showAndWait().filter(ButtonType.YES::equals).isPresent();
    }

    private <T> T withSelfTriggeredEvent(java.util.function.Supplier<T> mutation) {
        pendingSelfTriggeredEvents++;
        try {
            return mutation.get();
        } catch (RuntimeException e) {
            pendingSelfTriggeredEvents--;
            throw e;
        }
    }

    /**
     * The library notifies on the Swing event thread, and the count of pending self triggered
     * events is only touched on the JavaFX thread, so the whole handler hops over first.
     */
    private void onLibraryChangedExternally() {
        Platform.runLater(() -> {
            if (pendingSelfTriggeredEvents > 0) {
                pendingSelfTriggeredEvents--;
                return;
            }
            ToolDefinition selected = selectedTool();
            refreshList(selected == null ? null : selected.getId());
        });
    }

    private void centerOnOwner() {
        Window owner = getOwner();
        if (owner != null) {
            setX(owner.getX() + owner.getWidth() / 2 - getWidth() / 2);
            setY(owner.getY() + owner.getHeight() / 2 - getHeight() / 2);
        }
    }

    /**
     * Opens the library for editing and returns when it is closed.
     */
    public static void show(Window owner, UnitUtils.Units preferredUnits, String selectedToolId) {
        new ToolLibraryStage(owner, ToolLibraryProvider.getInstance(), preferredUnits, Mode.MANAGE, selectedToolId).showAndWait();
    }

    /**
     * Lets the user pick a tool from the library. Empty when the dialog was cancelled.
     */
    public static Optional<ToolDefinition> pick(Window owner, UnitUtils.Units preferredUnits, String selectedToolId) {
        ToolLibraryStage stage = new ToolLibraryStage(owner, ToolLibraryProvider.getInstance(), preferredUnits, Mode.PICK, selectedToolId);
        stage.showAndWait();
        return stage.getResult();
    }
}

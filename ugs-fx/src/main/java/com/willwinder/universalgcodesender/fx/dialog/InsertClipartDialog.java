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
package com.willwinder.universalgcodesender.fx.dialog;

import com.willwinder.ugs.designer.gui.clipart.Category;
import com.willwinder.ugs.designer.gui.clipart.Clipart;
import com.willwinder.ugs.designer.gui.clipart.ClipartSources;
import com.willwinder.universalgcodesender.fx.component.ButtonBox;
import com.willwinder.universalgcodesender.fx.component.clipart.ClipartCard;
import com.willwinder.universalgcodesender.fx.component.clipart.ClipartDetailsPane;
import com.willwinder.universalgcodesender.i18n.Localization;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A modal dialog that presents all bundled cliparts grouped by category. Each clipart is shown as a
 * card with a preview, its license and credits. Selecting a card shows the full attribution and
 * double-clicking, or pressing insert, closes the dialog with the clipart in {@link #getSelectedClipart()}.
 */
public class InsertClipartDialog extends Stage {
    private static Category defaultCategory = Category.ANIMALS;
    private final ListView<Category> categoryList = new ListView<>();
    private final TextField searchField = new TextField();
    private final Label resultCountLabel = new Label();
    private final FlowPane cardPane = new FlowPane(10, 10);
    private final ScrollPane cardScrollPane = new ScrollPane(cardPane);
    private final Label noResultsLabel = new Label(Localization.getString("designer.clipart.noResults"));
    private final ClipartDetailsPane detailsPane = new ClipartDetailsPane();
    private final Button insertButton = new Button(Localization.getString("designer.clipart.insert"));

    private ClipartCard selectedCard;
    private Clipart result;

    public InsertClipartDialog(Window owner) {
        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setTitle(Localization.getString("platform.designer.clipart"));

        BorderPane root = new BorderPane();
        root.setLeft(createCategoryList());
        root.setCenter(createClipartPane());
        root.setBottom(createButtons());

        Scene scene = new Scene(root, 1024, 800);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/root.css")).toExternalForm());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/clipart.css")).toExternalForm());
        setScene(scene);
        setMinWidth(640);
        setMinHeight(480);

        setOnShown(event -> centerOnOwner());
        categoryList.getSelectionModel().select(defaultCategory);
    }

    private ListView<Category> createCategoryList() {
        categoryList.getItems().setAll(Category.values());
        categoryList.getStyleClass().add("clipart-category-list");
        categoryList.setPrefWidth(200);
        categoryList.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getTitle());
            }
        });
        categoryList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            refreshCards();
            defaultCategory = newValue;
        });
        return categoryList;
    }

    private VBox createClipartPane() {
        searchField.setPromptText(Localization.getString("designer.clipart.search"));
        searchField.textProperty().addListener((observable, oldValue, newValue) -> refreshCards());
        HBox.setHgrow(searchField, Priority.ALWAYS);
        resultCountLabel.getStyleClass().add("clipart-result-count");
        HBox searchBar = new HBox(10, searchField, resultCountLabel);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        searchBar.setPadding(new Insets(10, 10, 0, 10));

        cardPane.setPadding(new Insets(10));
        cardPane.setPrefWrapLength(0);
        cardScrollPane.setFitToWidth(true);
        cardScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        cardScrollPane.getStyleClass().add("clipart-scroll");

        noResultsLabel.getStyleClass().add("clipart-no-results");
        StackPane cardStack = new StackPane(cardScrollPane, noResultsLabel);
        VBox.setVgrow(cardStack, Priority.ALWAYS);

        return new VBox(10, searchBar, cardStack, detailsPane);
    }

    private ButtonBox createButtons() {
        Button cancelButton = new Button(Localization.getString("mainWindow.swing.cancelButton"));
        ButtonBox.setButtonData(cancelButton, ButtonBox.ButtonData.LEFT);
        cancelButton.setOnAction(event -> close());

        ButtonBox.setButtonData(insertButton, ButtonBox.ButtonData.OK_DONE);
        insertButton.setDefaultButton(true);
        insertButton.setDisable(true);
        insertButton.setOnAction(event -> confirmSelection());

        ButtonBox buttonBox = new ButtonBox();
        buttonBox.getButtons().addAll(cancelButton, insertButton);
        return buttonBox;
    }

    private void refreshCards() {
        Category category = Optional.ofNullable(categoryList.getSelectionModel().getSelectedItem()).orElse(Category.ALL);
        String filter = searchField.getText().trim().toLowerCase();

        List<ClipartCard> cards = ClipartSources.getCliparts(category).stream()
                .filter(clipart -> matchesFilter(clipart, filter))
                .map(this::createCard)
                .toList();

        select(null);
        cardPane.getChildren().setAll(cards);
        cardScrollPane.setVvalue(0);
        noResultsLabel.setVisible(cards.isEmpty());
        resultCountLabel.setText(String.valueOf(cards.size()));
    }

    private static boolean matchesFilter(Clipart clipart, String filter) {
        return filter.isEmpty()
                || clipart.getName().toLowerCase().contains(filter)
                || clipart.getSource().getName().toLowerCase().contains(filter);
    }

    private ClipartCard createCard(Clipart clipart) {
        ClipartCard card = new ClipartCard(clipart);
        card.setOnMouseClicked(event -> {
            if (event.getButton() != MouseButton.PRIMARY) {
                return;
            }
            select(card);
            if (event.getClickCount() == 2) {
                confirmSelection();
            }
        });
        return card;
    }

    private void select(ClipartCard card) {
        if (selectedCard != null) {
            selectedCard.setSelected(false);
        }
        selectedCard = card;
        if (selectedCard != null) {
            selectedCard.setSelected(true);
        }
        detailsPane.setClipart(card == null ? null : card.getClipart());
        insertButton.setDisable(card == null);
    }

    private void confirmSelection() {
        if (selectedCard == null) {
            return;
        }
        result = selectedCard.getClipart();
        close();
    }

    private void centerOnOwner() {
        Window owner = getOwner();
        if (owner != null) {
            setX(owner.getX() + owner.getWidth() / 2 - getWidth() / 2);
            setY(owner.getY() + owner.getHeight() / 2 - getHeight() / 2);
        }
    }

    /**
     * The clipart chosen by the user, or empty if the dialog was cancelled.
     */
    public Optional<Clipart> getSelectedClipart() {
        return Optional.ofNullable(result);
    }
}

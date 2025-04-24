package com.willwinder.universalgcodesender.fx.component.overlay;

import com.willwinder.universalgcodesender.fx.component.JobControlsPane;
import com.willwinder.universalgcodesender.fx.component.MacrosPane;
import com.willwinder.universalgcodesender.fx.component.TerminalPane;
import com.willwinder.universalgcodesender.fx.helper.SvgLoader;
import com.willwinder.universalgcodesender.i18n.Localization;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class OverlayPane extends BorderPane {

    private final ToggleGroup toggleGroup;
    private final VBox buttonBox;

    public OverlayPane() {
        getStylesheets().add(getClass().getResource("/styles/overlay-pane.css").toExternalForm());
        toggleGroup = new ToggleGroup();
        getStyleClass().add("overlay-pane");

        JobControlsPane jobControlsPane = new JobControlsPane();

        buttonBox = new VBox();
        buttonBox.getStyleClass().add("toggle-group");
        createAndAddButton(jobControlsPane, "icons/run.svg", Localization.getString("platform.window.sendstatus"));
        createAndAddButton(new MacrosPane(), "icons/robot.svg", Localization.getString("platform.menu.macros"));
        createAndAddButton(new Label("Probing"), "icons/probe.svg",  Localization.getString("settings.probe"));
        createAndAddButton(new TerminalPane(), "icons/terminal.svg",  Localization.getString("platform.window.serialconsole"));
        setRight(buttonBox);

        toggleGroup.selectToggle(toggleGroup.getToggles().get(0));
        setCenter(jobControlsPane);

        toggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) {
                oldToggle.setSelected(true);
            }
        });

        setMaxWidth(500);
        setMaxHeight(100);
    }

    private void createAndAddButton(Node node, String icon, String tooltipText) {
        ToggleButton button = new ToggleButton();
        button.getStyleClass().add("toggle-button");
        if (buttonBox.getChildren().isEmpty()) {
            button.getStyleClass().add("toggle-button-first");
        }

        SvgLoader.loadImageIcon(icon, 32).ifPresent(button::setGraphic);
        button.setOnAction(event -> {
            setCenter(node);
        });
        Tooltip tooltip = new Tooltip(tooltipText);
        tooltip.setShowDelay(Duration.millis(100));
        button.setTooltip(tooltip);
        button.setToggleGroup(toggleGroup);
        buttonBox.getChildren().add(button);
    }
}

package com.willwinder.universalgcodesender.fx.component.overlay;

import com.willwinder.universalgcodesender.fx.component.JobControlsPane;
import com.willwinder.universalgcodesender.fx.component.MacrosPane;
import com.willwinder.universalgcodesender.fx.component.TerminalPane;
import com.willwinder.universalgcodesender.fx.helper.Colors;
import com.willwinder.universalgcodesender.fx.helper.SvgLoader;
import com.willwinder.universalgcodesender.i18n.Localization;
import javafx.animation.TranslateTransition;
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
        getStyleClass().add("overlay-pane");

        toggleGroup = new ToggleGroup();

        JobControlsPane jobControlsPane = new JobControlsPane();
        buttonBox = new VBox(5);
        buttonBox.getStyleClass().add("toggle-group");
        createAndAddButton(jobControlsPane, "icons/run.svg", Localization.getString("platform.window.sendstatus"));
        createAndAddButton(new MacrosPane(), "icons/robot.svg", Localization.getString("platform.menu.macros"));
        createAndAddButton(new Label("Probing"), "icons/probe.svg",  Localization.getString("settings.probe"));
        createAndAddButton(new TerminalPane(), "icons/terminal.svg",  Localization.getString("platform.window.serialconsole"));

        toggleGroup.selectToggle(toggleGroup.getToggles().get(0));

        setCenter(jobControlsPane);
        setLeft(buttonBox);

        toggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            toggleDrawer(newToggle != null);
        });

        setMaxWidth(500);
        setMaxHeight(100);
    }

    private void toggleDrawer(boolean drawerVisible) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(300), this);
        if (drawerVisible) {
            tt.setToX(0);
        } else {
            tt.setToX(getWidth() - this.buttonBox.getWidth());
        }
        tt.play();
    }

    private void createAndAddButton(Node node, String icon, String tooltipText) {
        ToggleButton button = new ToggleButton();
        button.getStyleClass().add("toggle-button");
        SvgLoader.loadImageIcon(icon, 32, Colors.BLACKISH).ifPresent(button::setGraphic);
        button.setOnAction(event -> setCenter(node));

        Tooltip tooltip = new Tooltip(tooltipText);
        tooltip.setShowDelay(Duration.millis(100));
        button.setTooltip(tooltip);

        button.setToggleGroup(toggleGroup);
        buttonBox.getChildren().add(button);
    }
}

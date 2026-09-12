package com.willwinder.universalgcodesender.fx.component.designer;

import com.willwinder.universalgcodesender.fx.helper.SplitPaneDividerPersistence;
import com.willwinder.universalgcodesender.fx.settings.Settings;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * The designer side panel with the drawing toolbars, the selected entity's settings and the
 * entity tree. Shown in the right pane while a design workspace is active.
 */
public class InspectorPane extends VBox {
    private final SplitPane inspectorSplit;
    private boolean sectionsDividerPersisted;

    public InspectorPane() {
        ScrollPane entityScroll = new ScrollPane(new EntitySettingsPanel());
        entityScroll.getStyleClass().add("inspector-scroll");
        entityScroll.setFitToWidth(true);

        VBox settingsSection = section("Object properties", entityScroll);
        VBox treeSection = section("Objects", new EntityTreeView());

        // The entity tree is scrollable on its own; a vertical split lets the user resize the
        // space between the settings above and the design tree at the bottom.
        inspectorSplit = new SplitPane(settingsSection, treeSection);
        inspectorSplit.setOrientation(Orientation.VERTICAL);
        VBox.setVgrow(inspectorSplit, Priority.ALWAYS);

        getChildren().addAll(new DesignToolbar(), new DesignAlignToolbar(), new DesignOperationToolbar(), inspectorSplit);

        // The sections divider can only be positioned once the pane is in a scene, and the
        // persistence keeps following the divider afterwards even while the pane is collapsed.
        sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null && !sectionsDividerPersisted) {
                sectionsDividerPersisted = true;
                Platform.runLater(() -> SplitPaneDividerPersistence.install(
                        inspectorSplit, settingsSection, Settings.getInstance().windowDividerInspectorSectionsProperty()));
            }
        });
    }

    /**
     * Wraps a section's content under a labeled header bar so the stacked inspector
     * sections are visually separated. The content fills the remaining height.
     */
    private static VBox section(String title, Region content) {
        Label header = new Label(title);
        header.getStyleClass().add("inspector-section-header");
        header.setMaxWidth(Double.MAX_VALUE);

        VBox.setVgrow(content, Priority.ALWAYS);
        VBox box = new VBox(header, content);
        box.setMinHeight(0);
        return box;
    }
}

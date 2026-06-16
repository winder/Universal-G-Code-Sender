package com.willwinder.universalgcodesender.fx.component.visualizer.designer;

import com.willwinder.universalgcodesender.fx.model.UgsdWorkspaceContext;
import com.willwinder.universalgcodesender.fx.model.WorkspaceContext;
import com.willwinder.universalgcodesender.fx.service.WorkspaceManager;
import com.willwinder.universalgcodesender.fx.settings.Settings;
import javafx.application.Platform;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class InspectorPane extends VBox {
    private final SplitPane parent;
    private SplitPane.Divider divider;

    public InspectorPane(SplitPane parent) {
        this.parent = parent;

        ScrollPane entityScroll = new ScrollPane(new EntitySettingsPanel());
        entityScroll.getStyleClass().add("inspector-scroll");
        entityScroll.setFitToWidth(true);
        VBox.setVgrow(entityScroll, Priority.ALWAYS);

        getChildren().addAll(new DesignToolbar(), entityScroll);
        setMinWidth(200);
        SplitPane.setResizableWithParent(this, false);

        WorkspaceManager.getInstance().addListener(new WorkspaceManager.WorkspaceListener() {
            @Override
            public void onWorkspaceOpened(WorkspaceContext workspace) {
                setDocked(workspace instanceof UgsdWorkspaceContext);
            }

            @Override
            public void onWorkspaceClosed() {
                setDocked(false);
            }

            @Override
            public void onWorkspaceDirtyStateChanged(WorkspaceContext workspace, boolean dirty) {
            }
        });
    }

    private void setDocked(boolean docked) {
        Platform.runLater(() -> {
            boolean shown = parent.getItems().contains(this);
            if (docked && !shown) {
                parent.getItems().add(this);
                divider = parent.getDividers().get(parent.getDividers().size() - 1);
                divider.setPosition(Settings.getInstance().windowDividerInspectorProperty().get());
                divider.positionProperty().addListener((obs, oldVal, newVal) ->
                        Settings.getInstance().windowDividerInspectorProperty().set(newVal.doubleValue()));
            } else if (!docked && shown) {
                parent.getItems().remove(this);
                divider = null;
            }
        });
    }
}

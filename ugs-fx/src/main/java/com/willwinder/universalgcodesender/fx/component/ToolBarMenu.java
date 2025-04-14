package com.willwinder.universalgcodesender.fx.component;

import com.willwinder.ugs.nbp.core.actions.ConnectDisconnectAction;
import com.willwinder.ugs.nbp.core.actions.PauseAction;
import com.willwinder.ugs.nbp.core.actions.SoftResetAction;
import com.willwinder.ugs.nbp.core.actions.StopAction;
import com.willwinder.ugs.nbp.core.actions.UnlockAction;
import com.willwinder.universalgcodesender.fx.actions.ActionRegistry;
import com.willwinder.universalgcodesender.fx.actions.OpenFileAction;
import com.willwinder.universalgcodesender.fx.actions.StartAction;
import com.willwinder.universalgcodesender.fx.control.ActionButton;
import javafx.scene.Node;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;

public class ToolBarMenu extends VBox {
    public ToolBarMenu() {
        List<Node> children = getChildren();
        createButton(ConnectDisconnectAction.class).ifPresent(children::add);
        children.add(new Separator());
        createButton(OpenFileAction.class).ifPresent(children::add);
        children.add(new Separator());
        createButton(StartAction.class).ifPresent(children::add);
        createButton(PauseAction.class).ifPresent(children::add);
        createButton(StopAction.class).ifPresent(children::add);
        children.add(new Separator());
        createButton(UnlockAction.class).ifPresent(children::add);
        createButton(SoftResetAction.class).ifPresent(children::add);

        ToolBar toolBar = new ToolBar();
        toolBar.getItems().addAll(children);

        getChildren().add(toolBar);
    }

    private Optional<Node> createButton(Class<?> actionClass) {
        return ActionRegistry
                .getInstance()
                .getAction(actionClass.getCanonicalName())
                .map(action -> {
                    ActionButton actionButton = new ActionButton(action, ActionButton.SIZE_LARGE);
                    actionButton.setShowText(false);
                    return actionButton;
                });
    }
}

package com.willwinder.universalgcodesender.fx.component.settings;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.fx.actions.Action;
import com.willwinder.universalgcodesender.fx.actions.ActionRegistry;
import com.willwinder.universalgcodesender.fx.helper.Colors;
import static com.willwinder.universalgcodesender.fx.helper.SvgLoader.loadImageIcon;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.Collection;

public class KeyboardSettingPane extends VBox {
    private final BackendAPI backend;

    public KeyboardSettingPane() {
        setSpacing(20);

        backend = CentralLookup.getDefault().lookup(BackendAPI.class);

        addTitleSection();

        Collection<Action> actions = ActionRegistry.getInstance().getActions();
        actions.forEach(action -> {
            addAction(action);
        });
    }

    private void addTitleSection() {
        Label title = new Label(Localization.getString("settings.machineStatus"));
        title.setFont(Font.font(20));
        getChildren().add(title);
    }

    private void addAction(Action action) {
        getChildren().add(new Label(action.getTitle(), loadImageIcon(action.getIcon(), 16, Color.web(Colors.BLACKISH.toString())).orElse(null)));
    }
}

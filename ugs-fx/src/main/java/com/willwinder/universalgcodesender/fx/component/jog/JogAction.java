package com.willwinder.universalgcodesender.fx.component.jog;

import com.willwinder.universalgcodesender.fx.actions.BaseAction;
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;

public class JogAction extends BaseAction {
    private final JogButtonEnum jogButtonEnum;

    public JogAction(JogButtonEnum jogButtonEnum) {
        super(jogButtonEnum.getText(), jogButtonEnum.getIconUrl());
        this.jogButtonEnum = jogButtonEnum;
    }

    @Override
    public String getId() {
        return super.getId() + jogButtonEnum.name();
    }

    @Override
    public void handleAction(ActionEvent event) {
        System.out.println(event);
    }

    @Override
    public void handleMouseEvent(MouseEvent mouseEvent) {
        System.out.println(mouseEvent);
    }
}

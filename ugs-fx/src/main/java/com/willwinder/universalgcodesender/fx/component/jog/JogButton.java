package com.willwinder.universalgcodesender.fx.component.jog;

import com.willwinder.universalgcodesender.fx.helper.Colors;
import com.willwinder.universalgcodesender.fx.helper.SvgLoader;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;

public class JogButton extends Button {
    public static final int ICON_SIZE = 24;
    private final JogButtonEnum button;

    public JogButton(JogButtonEnum button) {
        super(button.getText(), SvgLoader.loadImageIcon(button.getIconUrl(), ICON_SIZE, Colors.BLACKISH).orElse(null));
        JogActionRegistry.getInstance().getAction(button).ifPresent(action -> {
           action
        });
        setContentDisplay(button.getContentDisplay());
        this.button = button;
    }



    public JogButtonEnum getButton() {
        return button;
    }

    private void onAction(Event event) {
        if (event instanceof ActionEvent) {
            this.onAction(event);
        } else if (event instanceof MouseEvent) {
            this.fireEvent(event);
        }
    }
}

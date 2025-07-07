package com.willwinder.universalgcodesender.fx.component.jog;

import com.willwinder.universalgcodesender.fx.helper.Colors;
import com.willwinder.universalgcodesender.fx.helper.SvgLoader;
import javafx.scene.control.Button;

public class JogButton extends Button {
    public static final int ICON_SIZE = 24;
    private final JogButtonEnum button;

    public JogButton(JogButtonEnum button) {
        super(button.getText(), SvgLoader.loadImageIcon(button.getIconUrl(), ICON_SIZE, Colors.BLACKISH).orElse(null));
        setContentDisplay(button.getContentDisplay());
        this.button = button;
    }

    public JogButtonEnum getButton() {
        return button;
    }
}

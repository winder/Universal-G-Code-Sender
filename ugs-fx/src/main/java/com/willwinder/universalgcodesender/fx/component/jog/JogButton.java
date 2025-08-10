/*
    Copyright 2025 Joacim Breiler

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
package com.willwinder.universalgcodesender.fx.component.jog;

import com.willwinder.universalgcodesender.fx.actions.Action;
import com.willwinder.universalgcodesender.fx.helper.Colors;
import com.willwinder.universalgcodesender.fx.helper.SvgLoader;
import com.willwinder.universalgcodesender.fx.service.JogActionRegistry;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;

public class JogButton extends Button {
    public static final int ICON_SIZE = 24;
    private final JogButtonEnum button;
    private Action action;

    public JogButton(JogButtonEnum button) {
        super(button.getLabel(), SvgLoader.loadImageIcon(button.getIconUrl(), ICON_SIZE, Colors.BLACKISH).orElse(null));
        JogActionRegistry.getInstance().getAction(button).ifPresent(action -> this.action = action);
        setOnMousePressed(this::onAction);
        setOnMouseReleased(this::onAction);
        setContentDisplay(button.getContentDisplay());
        this.button = button;
    }



    public JogButtonEnum getButton() {
        return button;
    }

    private void onAction(Event event) {
        if (event instanceof ActionEvent) {
            action.handle(event);
        } else if (event instanceof MouseEvent) {
            action.handle(event);
        }
    }
}

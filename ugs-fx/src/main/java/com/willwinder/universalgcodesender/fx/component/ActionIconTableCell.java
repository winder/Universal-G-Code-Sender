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
package com.willwinder.universalgcodesender.fx.component;

import com.willwinder.universalgcodesender.fx.actions.Action;
import com.willwinder.universalgcodesender.fx.helper.Colors;
import static com.willwinder.universalgcodesender.fx.helper.SvgLoader.loadImageIcon;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class ActionIconTableCell extends TableCell<Action, Void> {
    @Override
    protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);

        if (getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
            return;
        }

        Action action = getTableView().getItems().get(getIndex());
        ImageView imageView = loadImageIcon(action.getIcon(), 16, Colors.DARK_BLUE_GREY)
                .orElse(null);


        if (imageView != null) {
            HBox wrapper = new HBox(imageView);
            wrapper.setAlignment(Pos.CENTER);
            wrapper.setPadding(new Insets(4));
            setGraphic(wrapper);
        } else {
            setGraphic(null);
        }

        setAlignment(Pos.CENTER);
    }
}

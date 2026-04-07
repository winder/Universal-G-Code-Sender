/*
    Copyright 2023 Will Winder

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
package com.willwinder.ugs.designer.actions;

import com.willwinder.ugs.designer.logic.ControllerFactory;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.services.JogService;
import com.willwinder.universalgcodesender.services.LookupService;
import com.willwinder.universalgcodesender.utils.SvgIconLoader;
import com.willwinder.universalgcodesender.utils.ThreadHelper;

import java.awt.event.ActionEvent;
import java.awt.geom.Rectangle2D;

import static com.willwinder.universalgcodesender.model.UnitUtils.Units.MM;

/**
 * @author Joacim Breiler
 */
public class JogMachineToTopRightCornerAction extends JogMachineAbstractAction {
    public static final String SMALL_ICON_PATH = "img/jog-to-top-right.svg";
    public static final String LARGE_ICON_PATH = "img/jog-to-top-right24.svg";

    public JogMachineToTopRightCornerAction() {
        super();
        putValue("menuText", "Jog to top right corner");
        putValue(NAME, "Jog to top right corner");
        putValue(SHORT_DESCRIPTION, "Jog to top right corner");
        putValue("iconBase", SMALL_ICON_PATH);
        putValue(SMALL_ICON, SvgIconLoader.loadImageIcon(SMALL_ICON_PATH, SvgIconLoader.SIZE_SMALL).orElse(null));
        putValue(LARGE_ICON_KEY, SvgIconLoader.loadImageIcon(SMALL_ICON_PATH, SvgIconLoader.SIZE_MEDIUM).orElse(null));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ThreadHelper.invokeLater(() -> {
            Rectangle2D bounds = ControllerFactory.getController().getSelectionManager().getBounds();
            PartialPosition centerPosition = new PartialPosition(bounds.getMaxX(), bounds.getMaxY(), MM);

            JogService jogService = LookupService.lookup(JogService.class);
            jogService.jogTo(centerPosition);
        });
    }
}

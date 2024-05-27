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
package com.willwinder.ugs.nbp.designer.actions;

import com.willwinder.ugs.nbp.designer.logic.ControllerFactory;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.model.PartialPosition;
import static com.willwinder.universalgcodesender.model.UnitUtils.Units.MM;
import com.willwinder.universalgcodesender.services.JogService;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;

import java.awt.event.ActionEvent;
import java.awt.geom.Rectangle2D;

/**
 * @author Joacim Breiler
 */
@ActionID(
        category = LocalizingService.CATEGORY_DESIGNER,
        id = "JogMachineToLowerLeftCornerAction")
@ActionRegistration(
        iconBase = JogMachineToLowerLeftCornerAction.SMALL_ICON_PATH,
        displayName = "Jog machine to lower left",
        lazy = false)
public class JogMachineToLowerLeftCornerAction extends JogMachineAbstractAction {
    public static final String SMALL_ICON_PATH = "img/jog-to-lower-left.svg";
    public static final String LARGE_ICON_PATH = "img/jog-to-lower-left24.svg";

    public JogMachineToLowerLeftCornerAction() {
        super();
        putValue("menuText", "Jog to lower left corner");
        putValue(NAME, "Jog to lower left corner");
        putValue("iconBase", SMALL_ICON_PATH);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(SMALL_ICON_PATH, false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(LARGE_ICON_PATH, false));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ThreadHelper.invokeLater(() -> {
            Rectangle2D bounds = ControllerFactory.getController().getSelectionManager().getBounds();
            PartialPosition centerPosition = new PartialPosition(bounds.getMinX(), bounds.getMinY(), MM);

            JogService jogService = CentralLookup.getDefault().lookup(JogService.class);
            jogService.jogTo(centerPosition);
        });
    }
}

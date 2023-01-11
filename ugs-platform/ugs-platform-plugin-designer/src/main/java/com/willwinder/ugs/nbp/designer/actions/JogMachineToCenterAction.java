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

import com.willwinder.ugs.nbp.designer.entities.selection.SelectionEvent;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionListener;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.nbp.designer.logic.ControllerFactory;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.services.JogService;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;

import javax.swing.SwingUtilities;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;

import static com.willwinder.universalgcodesender.model.UnitUtils.Units.MM;

/**
 * An action that will jog the machine to the center of the selected objects
 *
 * @author Joacim Breiler
 */
@ActionID(
        category = LocalizingService.CATEGORY_DESIGNER,
        id = "JogMachineToCenterAction")
@ActionRegistration(
        iconBase = JogMachineToCenterAction.SMALL_ICON_PATH,
        displayName = "Jog machine to center",
        lazy = false)
public class JogMachineToCenterAction extends AbstractDesignAction implements SelectionListener, UGSEventListener {
    public static final String SMALL_ICON_PATH = "img/jog-to.svg";
    public static final String LARGE_ICON_PATH = "img/jog-to24.svg";
    private final transient BackendAPI backend;

    public JogMachineToCenterAction() {
        putValue("menuText", "Jog machine to center");
        putValue(NAME, "Jog machine to center");
        putValue("iconBase", SMALL_ICON_PATH);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(SMALL_ICON_PATH, false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(LARGE_ICON_PATH, false));

        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        backend.addUGSEventListener(this);
        registerControllerListener();
        setEnabled(isEnabled());
    }

    private void registerControllerListener() {
        SelectionManager selectionManager = ControllerFactory.getController().getSelectionManager();
        selectionManager.addSelectionListener(this);
        setEnabled(isEnabled());
    }

    @Override
    public boolean isEnabled() {
        SelectionManager selectionManager = ControllerFactory.getController().getSelectionManager();
        boolean hasSelection = !selectionManager.getSelection().isEmpty();
        boolean isIdle = backend.getControllerState() == ControllerState.IDLE;
        return hasSelection && isIdle;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ThreadHelper.invokeLater(() -> {
            Point2D center = ControllerFactory.getController().getSelectionManager().getCenter();
            PartialPosition centerPosition = new PartialPosition(center.getX(), center.getY(), MM);

            JogService jogService = CentralLookup.getDefault().lookup(JogService.class);
            jogService.jogTo(centerPosition);
        });
    }

    @Override
    public void onSelectionEvent(SelectionEvent selectionEvent) {
        setEnabled(isEnabled());
    }

    @Override
    public void UGSEvent(UGSEvent event) {
        if (event instanceof ControllerStateEvent) {
            SwingUtilities.invokeLater(() -> setEnabled(isEnabled()));
        }
    }
}

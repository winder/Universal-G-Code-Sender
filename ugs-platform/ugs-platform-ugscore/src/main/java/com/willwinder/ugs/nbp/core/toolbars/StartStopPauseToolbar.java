/*
    Copywrite 2016 Will Winder

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
package com.willwinder.ugs.nbp.core.toolbars;

import com.willwinder.ugs.nbp.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.uielements.toolbars.StartStopPausePanel;
import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.actions.Presenter;

/**
 *
 * @author wwinder
 */
@ActionID(
        category = "Edit",
        id = "com.willwinder.ugs.nbp.core.toolbars.StartStopPause"
)
@ActionRegistration(
        displayName = "Start Stop Pause",
        lazy = false
)
@ActionReference(path = "Toolbars/StartPauseStop", position = 0)
public class StartStopPauseToolbar extends AbstractAction implements Presenter.Toolbar {
    @Override
    public Component getToolbarPresenter() {
        BackendAPI backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        return new StartStopPausePanel(backend);
    }

    // Need this to get the toolbar to show up.
    @Override
    public void actionPerformed(ActionEvent e) {
    }
}

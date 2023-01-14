/*
    Copyright 2022 Will Winder

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
package com.willwinder.ugs.nbp.designer.platform;

import com.willwinder.ugs.nbp.designer.gui.SelectionSettingsPanel;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.ControllerEventType;
import com.willwinder.ugs.nbp.designer.logic.ControllerFactory;
import com.willwinder.ugs.nbp.designer.logic.ControllerListener;
import org.openide.windows.TopComponent;

/**
 * @author Joacim Breiler
 */
@TopComponent.Description(
        preferredID = "SettingsTopComponent",
        persistenceType = TopComponent.PERSISTENCE_NEVER
)
@TopComponent.Registration(mode = "top_left", openAtStartup = false)
public class SettingsTopComponent extends TopComponent implements ControllerListener {
    private static final long serialVersionUID = 324234398723987873L;

    private transient SelectionSettingsPanel selectionSettingsPanel;

    public SettingsTopComponent() {
        setMinimumSize(new java.awt.Dimension(50, 50));
        setPreferredSize(new java.awt.Dimension(200, 200));
        setLayout(new java.awt.BorderLayout());
        setDisplayName("Cut settings");
    }

    @Override
    protected void componentClosed() {
        super.componentClosed();
        selectionSettingsPanel.release();
        selectionSettingsPanel = null;

        Controller controller = ControllerFactory.getController();
        controller.removeListener(this);
    }

    @Override
    protected void componentOpened() {
        super.componentOpened();
        removeAll();
        Controller controller = ControllerFactory.getController();
        selectionSettingsPanel = new SelectionSettingsPanel(controller);
        add(selectionSettingsPanel);
        controller.addListener(this);
    }

    @Override
    public void onControllerEvent(ControllerEventType event) {
        if (event == ControllerEventType.RELEASE) {
            close();
        }
    }
}

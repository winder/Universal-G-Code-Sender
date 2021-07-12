/*
    Copyright 2021 Will Winder

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

import com.willwinder.ugs.nbp.designer.gui.EntitiesTree;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import org.openide.windows.TopComponent;

/**
 * @author Joacim Breiler
 */
@TopComponent.Description(
        preferredID = "EntitiesTreeTopComponent",
        persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED
)
@TopComponent.Registration(mode = "bottom_left", openAtStartup = false)
public class EntitiesTreeTopComponent extends TopComponent {
    private static final long serialVersionUID = 432423498723987873L;

    private EntitiesTree entitiesTree;

    public EntitiesTreeTopComponent() {
        setMinimumSize(new java.awt.Dimension(50, 50));
        setPreferredSize(new java.awt.Dimension(200, 200));
        setLayout(new java.awt.BorderLayout());
        setDisplayName("Design objects");
    }

    @Override
    protected void componentOpened() {
        super.componentOpened();
        entitiesTree = new EntitiesTree();
        add(entitiesTree);
        validate();
    }

    public void updateController(Controller controller) {
        entitiesTree.updateController(controller);
    }
}

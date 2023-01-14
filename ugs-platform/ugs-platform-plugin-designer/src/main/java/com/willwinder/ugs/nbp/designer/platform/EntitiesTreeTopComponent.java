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

import com.willwinder.ugs.nbp.designer.gui.tree.EntitiesTree;
import com.willwinder.ugs.nbp.designer.gui.tree.EntityTreeModel;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.ControllerEventType;
import com.willwinder.ugs.nbp.designer.logic.ControllerFactory;
import com.willwinder.ugs.nbp.designer.logic.ControllerListener;
import org.openide.windows.TopComponent;

import javax.swing.JScrollPane;
import java.awt.BorderLayout;

/**
 * @author Joacim Breiler
 */
@TopComponent.Description(
        preferredID = "EntitiesTreeTopComponent",
        persistenceType = TopComponent.PERSISTENCE_NEVER
)
@TopComponent.Registration(mode = "bottom_left", openAtStartup = false)
public class EntitiesTreeTopComponent extends TopComponent implements ControllerListener {
    private static final long serialVersionUID = 432423498723987873L;
    private transient EntitiesTree entitesTree;
    private transient EntityTreeModel entitiesTreeModel;

    public EntitiesTreeTopComponent() {
        setMinimumSize(new java.awt.Dimension(50, 50));
        setPreferredSize(new java.awt.Dimension(200, 200));
        setLayout(new java.awt.BorderLayout());
        setDisplayName("Design objects");
    }

    @Override
    protected void componentClosed() {
        entitesTree.release();
        entitesTree = null;

        entitiesTreeModel.release();
        entitiesTreeModel = null;
    }

    @Override
    protected void componentOpened() {
        super.componentOpened();
        Controller controller = ControllerFactory.getController();
        controller.addListener(this);
        entitiesTreeModel = new EntityTreeModel(controller);
        entitesTree = new EntitiesTree(controller, entitiesTreeModel);

        removeAll();
        add(new JScrollPane(entitesTree), BorderLayout.CENTER);
    }

    @Override
    public void onControllerEvent(ControllerEventType event) {
        if (event == ControllerEventType.RELEASE) {
            close();
        }
    }
}

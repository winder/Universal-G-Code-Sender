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
import com.willwinder.ugs.nbp.designer.logic.ControllerFactory;
import com.willwinder.ugs.nbp.lib.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.io.Serial;

/**
 * @author Joacim Breiler
 */
@TopComponent.Description(
        preferredID = EntitiesTreeTopComponent.PREFERRED_ID
)
@TopComponent.Registration(mode = Mode.LEFT_BOTTOM, openAtStartup = false)
public class EntitiesTreeTopComponent extends TopComponent {
    @Serial
    private static final long serialVersionUID = 432423498723987873L;
    public static final String PREFERRED_ID = "EntitiesTreeTopComponent";

    private transient EntitiesTree entitesTree;
    private transient EntityTreeModel entitiesTreeModel;

    public EntitiesTreeTopComponent() {
        setMinimumSize(new java.awt.Dimension(50, 50));
        setPreferredSize(new java.awt.Dimension(200, 200));
        setLayout(new java.awt.BorderLayout());
        setDisplayName("Design objects");
    }

    public static EntitiesTreeTopComponent findInstance() {
        TopComponent tc = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (tc instanceof EntitiesTreeTopComponent) {
            return (EntitiesTreeTopComponent) tc;
        }

        return TopComponent.getRegistry().getOpened().stream()
                .filter(EntitiesTreeTopComponent.class::isInstance)
                .map(EntitiesTreeTopComponent.class::cast)
                .findFirst().orElseGet(EntitiesTreeTopComponent::new);
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
        entitiesTreeModel = new EntityTreeModel(controller);
        entitesTree = new EntitiesTree(controller, entitiesTreeModel);

        removeAll();
        add(new JScrollPane(entitesTree), BorderLayout.CENTER);
    }
}

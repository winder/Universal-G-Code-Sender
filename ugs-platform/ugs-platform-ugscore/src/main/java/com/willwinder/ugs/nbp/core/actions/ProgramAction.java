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
package com.willwinder.ugs.nbp.core.actions;

import com.willwinder.universalgcodesender.utils.ThreadHelper;
import org.openide.cookies.SaveCookie;
import org.openide.nodes.Node;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;
import org.openide.windows.TopComponent;

import javax.swing.AbstractAction;
import java.beans.PropertyChangeEvent;

import static org.openide.nodes.Node.PROP_COOKIE;
import static org.openide.windows.TopComponent.Registry.PROP_ACTIVATED_NODES;

/**
 * An abstract action that will be active only if the current program contains unsaved
 * changes.
 *
 * @author Joacim Breiler
 */
public abstract class ProgramAction extends AbstractAction implements NodeListener {
    /**
     * If the current document contains unsaved changes this will become false.
     */
    protected boolean isSaved = true;

    protected ProgramAction() {
        TopComponent.getRegistry().addPropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(PROP_COOKIE)) {
            isSaved = ((Node) evt.getSource()).getCookie(SaveCookie.class) == null;
            ThreadHelper.invokeLater(() -> setEnabled(isEnabled()));
        } else if (evt.getPropertyName().equals(PROP_ACTIVATED_NODES)) {
            registerNodeListener(evt);
        }
    }

    /**
     * Register/unregister ourselves as a node listener for listening to changes in the
     * currently opened document
     *
     * @param event the property event.
     */
    private void registerNodeListener(PropertyChangeEvent event) {
        if (event.getOldValue() != null && ((Node[]) event.getOldValue()).length > 0) {
            Node oldNode = ((Node[]) event.getOldValue())[0];
            oldNode.removeNodeListener(this);
        }

        if (event.getNewValue() != null && ((Node[]) event.getNewValue()).length > 0) {
            Node newNode = ((Node[]) event.getNewValue())[0];
            newNode.addNodeListener(this);
        }
    }

    @Override
    public boolean isEnabled() {
        return isSaved;
    }

    @Override
    public void childrenAdded(NodeMemberEvent ev) {
        // Not used
    }

    @Override
    public void childrenRemoved(NodeMemberEvent ev) {
        // Not used
    }

    @Override
    public void childrenReordered(NodeReorderEvent ev) {
        // Not used
    }

    @Override
    public void nodeDestroyed(NodeEvent ev) {
        // Not used
    }
}

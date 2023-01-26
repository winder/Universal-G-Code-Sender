/*
    Copyright 2022-2023 Will Winder

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
package com.willwinder.ugs.nbp.console;

import org.openide.windows.IOContainer;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.text.JTextComponent;
import java.awt.BorderLayout;

/**
 * A hack to be able to get access to the JTextComponent in the
 *
 * @author Joacim Breiler
 */
public class ConsoleProvider implements IOContainer.Provider {
    private final JComponent component;

    public ConsoleProvider(JComponent component) {
        this.component = component;
    }

    @Override
    public void open() {
    }

    @Override
    public void requestActive() {
        getTextComponent().requestFocus();
    }

    @Override
    public void requestVisible() {
        component.requestFocusInWindow();
    }

    @Override
    public boolean isActivated() {
        return component.hasFocus();
    }

    @Override
    public void add(JComponent comp, IOContainer.CallBacks cb) {
        component.add(comp, BorderLayout.CENTER);
    }

    @Override
    public void remove(JComponent comp) {
        component.remove(comp);
    }

    @Override
    public void select(JComponent comp) {
    }

    @Override
    public JComponent getSelected() {
        return (JComponent) component.getComponent(0);
    }

    @Override
    public void setTitle(JComponent comp, String name) {
    }

    @Override
    public void setToolTipText(JComponent comp, String text) {
    }

    @Override
    public void setIcon(JComponent comp, Icon icon) {
    }

    @Override
    public void setToolbarActions(JComponent comp, Action[] toolbarActions) {
    }

    @Override
    public boolean isCloseable(JComponent comp) {
        return false;
    }

    public JTextComponent getTextComponent() {
        // A workaround to get the output pane text component
        JComponent outputTab = getSelected();
        JScrollPane outputPane = (JScrollPane) outputTab.getComponent(0);
        return (JTextComponent) outputPane.getViewport().getComponent(0);
    }
}

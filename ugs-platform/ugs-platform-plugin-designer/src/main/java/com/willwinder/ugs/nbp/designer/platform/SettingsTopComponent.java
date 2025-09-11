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
package com.willwinder.ugs.nbp.designer.platform;

import com.willwinder.ugs.nbp.designer.gui.selectionsettings.SelectionSettingsPanel;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.ControllerFactory;
import com.willwinder.ugs.nbp.lib.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import java.awt.BorderLayout;
import java.io.Serial;

/**
 * @author Joacim Breiler
 */
@TopComponent.Description(
        preferredID = SettingsTopComponent.PREFERRED_ID
)
@TopComponent.Registration(mode = Mode.LEFT_TOP, openAtStartup = false)
public class SettingsTopComponent extends TopComponent {
    @Serial
    private static final long serialVersionUID = 324234398723987873L;
    public static final String PREFERRED_ID = "SettingsTopComponent";

    private transient SelectionSettingsPanel selectionSettingsPanel;

    public SettingsTopComponent() {
        setMinimumSize(new java.awt.Dimension(50, 50));
        setPreferredSize(new java.awt.Dimension(200, 200));
        setLayout(new BorderLayout());
        setDisplayName("Cut settings");
    }

    public static SettingsTopComponent findInstance() {
        TopComponent tc = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (tc instanceof SettingsTopComponent) {
            return (SettingsTopComponent) tc;
        }

        return TopComponent.getRegistry().getOpened().stream()
                .filter(SettingsTopComponent.class::isInstance)
                .map(SettingsTopComponent.class::cast)
                .findFirst().orElseGet(SettingsTopComponent::new);
    }

    @Override
    protected void componentClosed() {
        super.componentClosed();
        selectionSettingsPanel.release();
        selectionSettingsPanel = null;
    }

    @Override
    protected void componentOpened() {
        super.componentOpened();
        removeAll();
        Controller controller = ControllerFactory.getController();
        selectionSettingsPanel = new SelectionSettingsPanel(controller);
        JScrollPane scrollPane = new JScrollPane(selectionSettingsPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(10);
        add(scrollPane);
    }
}

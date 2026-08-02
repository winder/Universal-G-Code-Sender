/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.ugs.designer.actions;

import com.willwinder.ugs.designer.gui.ToolSettingsPanel;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.universalgcodesender.uielements.DialogUtils;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class OpenToolSettingsAction implements ActionListener {
    /**
     * The settings do not fit on a short screen, so the dialog opens at no more than this share of
     * the available height and scrolls the rest.
     */
    private static final double MAX_HEIGHT_FRACTION = 0.7;
    private static final int FALLBACK_MAX_HEIGHT = 700;
    private static final int SCROLL_UNIT = 16;

    private final Controller controller;

    public OpenToolSettingsAction(Controller controller) {
        this.controller = controller;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ToolSettingsPanel toolSettingsPanel = new ToolSettingsPanel(controller);
        Window parent = DialogUtils.getParentWindow(controller.getDrawing());

        if (DialogUtils.showModalDialog(parent, "Tool settings", scrollable(toolSettingsPanel))) {
            ChangeToolSettingsAction changeToolSettingsAction =
                    new ChangeToolSettingsAction(controller, toolSettingsPanel.getSettings());
            changeToolSettingsAction.actionPerformed(null);
            controller.getUndoManager().addAction(changeToolSettingsAction);
        }
    }

    /**
     * Wraps the content in a vertically scrolling pane. The dialog sizes itself from the preferred
     * size of what it is given, so the pane also caps its preferred height — without that the
     * dialog would simply open tall enough to show everything and never scroll.
     */
    static JPanel scrollable(JComponent content) {
        JScrollPane scrollPane = new JScrollPane(content,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(SCROLL_UNIT);

        Dimension preferred = content.getPreferredSize();
        boolean scrolls = preferred.height > maxHeight();
        int width = preferred.width + (scrolls ? scrollPane.getVerticalScrollBar().getPreferredSize().width : 0);
        scrollPane.setPreferredSize(new Dimension(width, Math.min(preferred.height, maxHeight())));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(scrollPane, BorderLayout.CENTER);
        return wrapper;
    }

    private static int maxHeight() {
        if (GraphicsEnvironment.isHeadless()) {
            return FALLBACK_MAX_HEIGHT;
        }
        return (int) (GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getMaximumWindowBounds().height * MAX_HEIGHT_FRACTION);
    }
}

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
package com.willwinder.ugs.nbp.designer.gui;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A drop down button factory that creates a drop down button with a popup menu.
 * <p/>
 * Usage:
 * JToggleButton button = DropDownButtonFactory.createDropDownToggleButton(icon, popupMenu);
 */
public final class DropDownButtonFactory {

    private DropDownButtonFactory() {
    }

    public static JToggleButton createDropDownToggleButton(Icon icon, JPopupMenu popupMenu) {
        return createDropDownToggleButton(icon, popupMenu, null);
    }

    public static JToggleButton createDropDownToggleButton(Icon icon, JPopupMenu popupMenu, Action initialAction) {
        DropDownToggleButton button = new DropDownToggleButton(popupMenu);

        if (icon != null) {
            button.setIcon(icon);
        }

        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setContentAreaFilled(true);
        button.setHorizontalTextPosition(AbstractButton.CENTER);
        button.setVerticalTextPosition(AbstractButton.BOTTOM);
        button.setRolloverEnabled(true);

        if (initialAction != null) {
            button.setAction(initialAction);
            button.setIcon(icon);
        }

        return button;
    }

    private static final class DropDownToggleButton extends JToggleButton {
        private final JPopupMenu popupMenu;
        private boolean showingPopup;

        private DropDownToggleButton(JPopupMenu popupMenu) {
            this.popupMenu = popupMenu;

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    maybeShowPopup(e);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    maybeShowPopup(e);
                }
            });
        }

        @Override
        public void setAction(Action a) {
            super.setAction(a);

            if (a == null) {
                return;
            }

            Object shortDesc = a.getValue(Action.SHORT_DESCRIPTION);
            if (shortDesc instanceof String s) {
                setToolTipText(s);
            }

            Object largeIcon = a.getValue(Action.LARGE_ICON_KEY);
            if (largeIcon instanceof Icon icon) {
                setIcon(icon);
            }

            setText("");
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension size = super.getPreferredSize();
            size.width += 14; // small room for the dropdown affordance
            return size;
        }

        private void maybeShowPopup(MouseEvent e) {
            if (popupMenu == null || !isEnabled()) {
                return;
            }

            if (!e.isPopupTrigger() && e.getSource() == this) {
                if (showingPopup) {
                    return;
                }
                showingPopup = true;
                try {
                    popupMenu.show(this, 0, getHeight());
                } finally {
                    showingPopup = false;
                }
            }
        }
    }
}
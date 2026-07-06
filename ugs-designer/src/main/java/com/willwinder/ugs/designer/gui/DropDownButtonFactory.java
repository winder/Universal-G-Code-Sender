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
package com.willwinder.ugs.designer.gui;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
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
        private static final int ARROW_WIDTH = 8;
        private static final int ARROW_HEIGHT = 4;
        private static final int ARROW_MARGIN = 4;
        private final JPopupMenu popupMenu;
        private boolean showingPopup;

        private DropDownToggleButton(JPopupMenu popupMenu) {
            this.popupMenu = popupMenu;
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
        public Insets getInsets() {
            return withArrowInset(super.getInsets());
        }

        @Override
        public Insets getInsets(Insets insets) {
            return withArrowInset(super.getInsets(insets));
        }

        private Insets withArrowInset(Insets insets) {
            insets.right += ARROW_WIDTH + ARROW_MARGIN; // room for the dropdown affordance
            return insets;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color arrowColor = isEnabled()
                        ? getForeground()
                        : UIManager.getColor("Button.disabledText");
                if (arrowColor == null) {
                    arrowColor = getForeground().darker();
                }
                g2.setColor(arrowColor);

                int x = getWidth() - ARROW_WIDTH - ARROW_MARGIN;
                int y = (getHeight() - ARROW_HEIGHT) / 2;
                int[] xPoints = {x, x + ARROW_WIDTH, x + (ARROW_WIDTH / 2)};
                int[] yPoints = {y, y, y + ARROW_HEIGHT};
                g2.fillPolygon(xPoints, yPoints, 3);
            } finally {
                g2.dispose();
            }
        }

        @Override
        protected void processMouseEvent(MouseEvent e) {
            if (popupMenu != null && isEnabled() && isInArrowZone(e.getX())) {
                if (e.getID() == MouseEvent.MOUSE_PRESSED) {
                    showPopup();
                }
                // Consume so the button model isn't armed and the action does not fire
                e.consume();
                return;
            }
            super.processMouseEvent(e);
        }

        private boolean isInArrowZone(int x) {
            return x >= getWidth() - (ARROW_WIDTH + ARROW_MARGIN * 2);
        }

        private void showPopup() {
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
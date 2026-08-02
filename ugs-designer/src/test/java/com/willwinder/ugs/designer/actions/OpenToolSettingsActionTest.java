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

import org.junit.Test;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.Component;
import java.awt.Dimension;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class OpenToolSettingsActionTest {

    @Test
    public void scrollable_ShouldScrollVerticallyOnly() {
        JScrollPane scrollPane = scrollPaneOf(panelOfHeight(200));

        assertEquals(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, scrollPane.getVerticalScrollBarPolicy());
        assertEquals(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER, scrollPane.getHorizontalScrollBarPolicy());
    }

    @Test
    public void scrollable_ShouldKeepTheContentAsTheViewportView() {
        JPanel content = panelOfHeight(200);

        JScrollPane scrollPane = scrollPaneOf(content);

        assertSame(content, scrollPane.getViewport().getView());
    }

    @Test
    public void scrollable_ShouldCapThePreferredHeightOfTallContent() {
        JPanel tall = panelOfHeight(10_000);

        JScrollPane scrollPane = scrollPaneOf(tall);

        assertTrue("A tall panel must not dictate the dialog height",
                scrollPane.getPreferredSize().height < 10_000);
    }

    @Test
    public void scrollable_ShouldLeaveShortContentUnchanged() {
        JPanel shortPanel = panelOfHeight(120);

        JScrollPane scrollPane = scrollPaneOf(shortPanel);

        assertEquals(120, scrollPane.getPreferredSize().height);
        assertEquals("No scrollbar is shown, so no width is reserved for one",
                300, scrollPane.getPreferredSize().width);
    }

    @Test
    public void scrollable_ShouldReserveWidthForTheScrollbarOnTallContent() {
        JScrollPane scrollPane = scrollPaneOf(panelOfHeight(10_000));

        assertTrue(scrollPane.getPreferredSize().width > 300);
    }

    private static JScrollPane scrollPaneOf(JPanel content) {
        JPanel wrapper = OpenToolSettingsAction.scrollable(content);
        for (Component child : wrapper.getComponents()) {
            if (child instanceof JScrollPane scrollPane) {
                return scrollPane;
            }
        }
        throw new AssertionError("The content was not wrapped in a scroll pane");
    }

    private static JPanel panelOfHeight(int height) {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(300, height));
        return panel;
    }
}

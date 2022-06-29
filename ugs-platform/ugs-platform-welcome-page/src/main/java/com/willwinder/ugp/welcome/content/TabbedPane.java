/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.willwinder.ugp.welcome.content;

import com.willwinder.ugp.welcome.Constants;
import com.willwinder.ugp.welcome.WelcomePageOptions;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import net.miginfocom.swing.MigLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;

/**
 * @author S. Aubrecht
 */
public class TabbedPane extends JPanel {
    private final List<JComponent> tabs;
    private final TabButton[] buttons;
    private final JScrollPane scroll;
    private int selTabIndex = -1;

    public TabbedPane(List<JComponent> tabs) {
        super(new BorderLayout());
        setOpaque(false);
        this.tabs = tabs;

        ActionListener al = (ActionEvent e) -> {
            TabButton btn = (TabButton) e.getSource();
            switchTab(btn.getTabIndex());
            WelcomePageOptions.getDefault().setLastActiveTab(btn.getTabIndex());
        };

        buttons = new TabButton[tabs.size()];
        for (int i = 0; i < buttons.length; i++) {
            buttons[i] = new TabButton(tabs.get(i).getName(), i);
            buttons[i].addActionListener(al);
        }

        JComponent tabHeader = new TabHeader(buttons);
        add(tabHeader, BorderLayout.NORTH);

        scroll = new JScrollPane(new TabContentPanel(tabs));
        scroll.getVerticalScrollBar().setUnitIncrement(10);
        scroll.getHorizontalScrollBar().setUnitIncrement(10);
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        add(scroll, BorderLayout.CENTER);

        int activeTabIndex = WelcomePageOptions.getDefault().getLastActiveTab();
        if (WelcomePageOptions.getDefault().isSecondStart() && activeTabIndex < 0) {
            activeTabIndex = 1;
            WelcomePageOptions.getDefault().setLastActiveTab(1);
        }
        activeTabIndex = Math.max(0, activeTabIndex);
        activeTabIndex = Math.min(activeTabIndex, tabs.size() - 1);
        switchTab(activeTabIndex);

        // We need to reset the scroll position after the component has been initialized
        ThreadHelper.invokeLater(() -> scroll.getVerticalScrollBar().setValue(0), 3000);
    }

    private void switchTab(int tabIndex) {
        if (selTabIndex >= 0) {
            buttons[selTabIndex].setSelected(false);
        }
        JComponent compToHide = selTabIndex >= 0 ? tabs.get(selTabIndex) : null;
        JComponent compToShow = tabs.get(tabIndex);
        selTabIndex = tabIndex;
        buttons[selTabIndex].setSelected(true);

        if (null != compToHide) {
            compToHide.setVisible(false);
        }

        compToShow.setVisible(true);
        compToShow.requestFocusInWindow();
        compToShow.revalidate();
        revalidate();
        ThreadHelper.invokeLater(() -> scroll.getVerticalScrollBar().setValue(0));
    }
}

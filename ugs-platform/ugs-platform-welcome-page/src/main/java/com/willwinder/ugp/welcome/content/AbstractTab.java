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
import com.willwinder.universalgcodesender.uielements.helpers.ThemeColors;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.UIManager;
import java.awt.BorderLayout;

/**
 * Base class for inner tabs in the Welcome Page
 *
 * @author S. Aubrecht
 */
public abstract class AbstractTab extends JPanel {

    private boolean initialized = false;

    public AbstractTab(String title) {
        super(new BorderLayout());
        setName(title);
        setOpaque(false);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (!initialized) {
            JComponent component = buildContent();

            if (isDarkLaF()) {
                component.setForeground(Constants.COLOR_TEXT_DARK_LAF);
                component.setBackground(Constants.COLOR_CONTENT_BACKGROUND_DARK_LAF);
            } else {
                component.setForeground(Constants.COLOR_TEXT);
                component.setBackground(Constants.COLOR_CONTENT_BACKGROUND);
            }

            add(component, BorderLayout.CENTER);
            initialized = true;
        }
    }

    protected boolean isDarkLaF() {
        return UIManager.getBoolean("nb.dark.theme");
    }

    protected abstract JComponent buildContent();
}

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

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author S. Aubrecht
 */
class ShowNextTime extends JPanel
        implements ActionListener, PropertyChangeListener {

    private final JCheckBox button;

    /**
     * Creates a new instance of RecentProjects
     */
    public ShowNextTime() {
        super(new BorderLayout());
        setOpaque(false);

        button = new JCheckBox("Show Next Time"); // NOI18N
        button.setSelected(WelcomePageOptions.getDefault().isShowOnStartup());
        button.setOpaque(false);
        button.setForeground(Color.white);
        button.setHorizontalTextPosition(SwingConstants.LEFT);
        button.setFont(Constants.TAB_FONT);
        // TODO: tooltips?
        //BundleSupport.setAccessibilityProperties( button, "ShowOnStartup" ); //NOI18N
        add(button, BorderLayout.CENTER);
        button.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        WelcomePageOptions.getDefault().setShowOnStartup(button.isSelected());
    }

    @Override
    public void addNotify() {
        super.addNotify();
        WelcomePageOptions.getDefault().addPropertyChangeListener(this);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        WelcomePageOptions.getDefault().removePropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        button.setSelected(WelcomePageOptions.getDefault().isShowOnStartup());
    }
}

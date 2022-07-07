/*
    Copyright 2018-2022 Will Winder

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
package com.willwinder.ugp.welcome;

import com.google.common.collect.ImmutableList;
import com.willwinder.ugp.welcome.content.TabbedPane;
import com.willwinder.ugs.nbp.lib.services.TopComponentLocalizer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.modules.OnStart;
import org.openide.windows.TopComponent;

import javax.swing.JComponent;
import java.awt.BorderLayout;
import java.util.List;

/**
 * Top component which displays something.
 */
@TopComponent.Description(
        preferredID = "WelcomePageTopComponent",
        persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED
)
@TopComponent.Registration(mode = "editor", openAtStartup = true)
@ActionID(category = WelcomePageTopComponent.Category, id = WelcomePageTopComponent.ActionId)
@ActionReference(path = WelcomePageTopComponent.Path)
@TopComponent.OpenActionRegistration(
        displayName = "<Not localized:WelcomePageTopComponent>",
        preferredID = "WelcomePageTopComponent"
)
public final class WelcomePageTopComponent extends TopComponent {
    protected final static String Category = "Help";
    protected final static String ActionId = "com.willwinder.ugp.welcome.WelcomePageTopComponent";
    protected final static String Path = "Menu/Help";
    protected final static String Title = "Welcome Page";
    protected final static String Tooltip = "Welcome Page";
    // Flag to prevent opening the start page once, allowing it to be opened manually.
    private static boolean firstTimeOpen = true;

    public WelcomePageTopComponent() {
        setName(Title);
        setToolTipText(Tooltip);

        setLayout(new BorderLayout());
        add(new TabbedPane(getTabs()));
    }

    private List<JComponent> getTabs() {
        return ImmutableList.of(
                new GettingStartedTab(),
                new RecentWorkTab(),
                new FeaturesTab()
                // TODO: Populate a features tab with data from github.
                //new FeaturesTab("What's New", downloadedFeatureList)
        );
    }

    @Override
    public void componentOpened() {
        if (firstTimeOpen) {
            firstTimeOpen = false;
            if (!WelcomePageOptions.getDefault().isShowOnStartup()) {
                close();
            }
        }
    }

    @Override
    public void componentClosed() {
        // No special component close logic needed.
    }

    public void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    public void readProperties(java.util.Properties p) {
        //String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @OnStart
    public static class Localizer extends TopComponentLocalizer {
        public Localizer() {
            super(Category, ActionId, Title);
        }
    }
}

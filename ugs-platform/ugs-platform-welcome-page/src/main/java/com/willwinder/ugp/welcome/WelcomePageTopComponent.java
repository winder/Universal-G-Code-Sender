/*
    Copyright 2018-2020 Will Winder

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

import com.willwinder.ugp.welcome.content.WelcomePagePanel;
import com.google.common.collect.ImmutableList;
import com.willwinder.ugp.welcome.FeaturesTab.Feature;
import com.willwinder.ugs.nbp.lib.services.TopComponentLocalizer;
import java.awt.BorderLayout;
import java.util.Collection;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.modules.OnStart;
import org.openide.windows.TopComponent;

/**
 * Top component which displays something.
 */
@TopComponent.Description(
        preferredID = "WelcomePageTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE",
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
  // Flag to prevent opening the start page once, allowing it to be opened manually.
  private static boolean firstTimeOpen = true;

  protected final static String Category = "Help";
  protected final static String ActionId = "com.willwinder.ugp.welcome.WelcomePageTopComponent";
  protected final static String Path = "Menu/Help";
  protected final static String Title = "Welcome Page";
  protected final static String Tooltip = "Welcome Page";

  private static final Collection<Feature> features = ImmutableList.of(
            new Feature(null, "Plugin Support!", "Most plugins can be found in the 'Window > Plugins' menu. New plugins are added all the time, see the what's new section for the latest features!", "com/willwinder/ugp/welcome/resources/features/new_plugins.png"),
            new Feature(null, "Setup wizard", "Configure and calibrate your hardware with the Setup wizard. It will help you to setup and test limit switches, step length, homing and soft limits. Start the wizard from the machine menu.", "com/willwinder/ugp/welcome/resources/features/setup_wizard.png"),
            new Feature(null, "Digital Read-Out (DRO)", "The DRO is your first stop for the current machine status. It tells you the work/machine coordinates, machine/spindle speeds, gcode state and more! It can be used to reset individual axes by clicking on the axis label, and has dynamic work position controls by clicking on the coordinate numbers.", "com/willwinder/ugp/welcome/resources/features/dro.png"),
            new Feature(null, "Jog Controller", "The Jog Controller is your primary tool for manually controlling your machines location. It has the option of using separate step sizes for XY and Z axes in addition to a click-and-hold mode for continuous jogging.", "com/willwinder/ugp/welcome/resources/features/jog_controller.png"),
            new Feature(null, "Custom Macros", "User defined macros can be configured in the preferences menu. A variety of convenient substitutions are available to help create perfectly tailored time savers for your workflow. See the help menu for more details.", "com/willwinder/ugp/welcome/resources/features/custom_macros.png"),
            new Feature(null, "Configurable Keybindings", "Nearly every feature in UGS can have a configurable keybinding. From the preferences menu open the keybinding section for a complete list of actions which can be configured (including your custom macros!).", "com/willwinder/ugp/welcome/resources/features/keybinding.png"),
            new Feature(null, "Probe Module", "The probe module can be opened from the plugins menu, it provides first class support for common probe devices. Some of the supported routines include standard Z-Depth probing and 3-Axis corner probing.", "com/willwinder/ugp/welcome/resources/features/probe_module.png"),
            new Feature(null, "Workflow helper", "Manage complex projects with multiple files using the Workflow helper. It will keep track of which files that has been run and remind you which tool to use for the next file.", "com/willwinder/ugp/welcome/resources/features/workflow.png")
            );

  @OnStart
  public static class Localizer extends TopComponentLocalizer {
    public Localizer() {
      super(Category, ActionId, Title);
    }
  }

  public WelcomePageTopComponent() {
    setName(Title);
    setToolTipText(Tooltip);

    setLayout(new BorderLayout());
    JScrollPane scroll = new JScrollPane(new WelcomePagePanel(getTabs()));
    scroll.getViewport().setOpaque(false);
    scroll.setOpaque(false);
    setFocusable( false );
    add(scroll);
  }

  private List<JComponent> getTabs() {
    return ImmutableList.of(
            new GettingStartedTab(),
            new RecentWorkTab(),
            new FeaturesTab("Features", features)
            // TODO: Populate a features tab with data from github.
            //new FeaturesTab("What's New", downloadedFeatureList)
    );
  }

  @Override
  public void componentOpened() {
    if( firstTimeOpen ) {
      firstTimeOpen = false;
      if( !WelcomePageOptions.getDefault().isShowOnStartup() ) {
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
}

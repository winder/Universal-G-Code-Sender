/*
    Copyright 2018 Will Winder

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
package com.willwinder.ugp;

import com.willwinder.ugp.content.StartPagePanel;
import com.google.common.collect.ImmutableList;
import com.willwinder.ugp.FeaturesTab.Feature;
import com.willwinder.ugp.content.AbstractTab;
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
        preferredID = "StartPageTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED
)
@TopComponent.Registration(mode = "editor", openAtStartup = true)
@ActionID(category = StartPageTopComponent.Category, id = StartPageTopComponent.ActionId)
@ActionReference(path = StartPageTopComponent.Path)
@TopComponent.OpenActionRegistration(
        displayName = "<Not localized:StartPageTopComponent>",
        preferredID = "StartPageTopComponent"
)
public final class StartPageTopComponent extends TopComponent {
  protected final static String Category = "Help";
  protected final static String ActionId = "com.willwinder.ugp.StartPageTopComponent";
  protected final static String Path = "Menu/Help";
  protected final static String Title = "Start Page";
  protected final static String Tooltip = "Start Page";

  private static final Collection<Feature> features = ImmutableList.of(
            new Feature(null, "Plugin Support!", "Most plugins can be found in the 'Window > Plugins' menu. New plugins are added all the time, see the what's new section for the latest features!", "com/willwinder/ugp/resources/features/new_plugins.png"),
            new Feature(null, "Digital Read-Out (DRO)", "The DRO is your first stop for the current machine status. It tells you the work/machine coordinates, machine/spindle speeds, gcode state and more! It can be used to reset individual axes by clicking on the axis label, and has dynamic work position controls by clicking on the coordinate numbers.", "com/willwinder/ugp/resources/features/dro.png"),
            new Feature(null, "Jog Controller", "The Jog Controller is your primary tool for manually controlling your machines location. It has the option of using separate step sizes for XY and Z axes in addition to a click-and-hold mode for continuous jogging.", "com/willwinder/ugp/resources/features/jog_controller.png"),
            new Feature(null, "Custom Macros", "User defined macros can be configured in the preferences menu. A variety of convenient substitutions are available to help create perfectly tailored time savers for your workflow. See the help menu for more details.", "com/willwinder/ugp/resources/features/custom_macros.png"),
            new Feature(null, "Configurable Keybindings", "Nearly every feature in UGS can have a configurable keybinding. From the preferences menu open the keybinding section for a complete list of actions which can be configured (including your custom macros!).", "com/willwinder/ugp/resources/features/keybinding.png"),
            new Feature(null, "Probe Module", "The probe module can be opened from the plugins menu, it provides first class support for common probe devices. Some of the supported routines include standard Z-Depth probing and 3-Axis corner probing.", "com/willwinder/ugp/resources/features/probe_module.png")
            );

  @OnStart
  public static class Localizer extends TopComponentLocalizer {
    public Localizer() {
      super(Category, ActionId, Title);
    }
  }

  public StartPageTopComponent() {
    setName(Title);
    setToolTipText(Tooltip);

    setLayout(new BorderLayout());
    JScrollPane scroll = new JScrollPane(new StartPagePanel(getTabs()));
    scroll.getViewport().setOpaque(false);
    scroll.setOpaque(false);
    setFocusable( false );
    add(scroll);
  }

  private List<JComponent> getTabs() {
    AbstractTab tab1 = new GettingStartedTab();
    AbstractTab tab2 = new TestTab("Recent", "List of recent files/directories");
    AbstractTab tab3 = new FeaturesTab("Features", features);
    AbstractTab tab4 = new TestTab("What's New", "New features");

    return ImmutableList.of(tab1, tab2, tab3, tab4);
  }

  @Override
  public void componentOpened() {
  }

  @Override
  public void componentClosed() {
  }

  void writeProperties(java.util.Properties p) {
    // better to version settings since initial version as advocated at
    // http://wiki.apidesign.org/wiki/PropertyFiles
    p.setProperty("version", "1.0");
    // TODO store your settings
  }

  void readProperties(java.util.Properties p) {
    String version = p.getProperty("version");
    // TODO read your settings according to their version
  }
}

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
import com.willwinder.ugs.nbp.lib.services.TopComponentLocalizer;
import java.awt.BorderLayout;
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
        persistenceType = TopComponent.PERSISTENCE_NEVER
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
  //public final static String Title = Localization.getString("platform.window.visualizer", lang);
  //public final static String VisualizerTooltip = Localization.getString("platform.window.visualizer.tooltip", lang);

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
    JScrollPane scroll = new JScrollPane(new StartPagePanel());
    scroll.getViewport().setOpaque(false);
    scroll.setOpaque(false);
    setFocusable( false );
    add(scroll);
  }

  @Override
  public void componentOpened() {
    // TODO add custom code on component opening
  }

  @Override
  public void componentClosed() {
    // TODO add custom code on component closing
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

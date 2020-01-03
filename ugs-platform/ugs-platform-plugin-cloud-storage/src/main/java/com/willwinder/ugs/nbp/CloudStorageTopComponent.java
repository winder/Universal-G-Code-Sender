/*
    Copyright 2020 Will Winder

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
package com.willwinder.ugs.nbp;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import static com.willwinder.ugs.nbp.lib.services.LocalizingService.lang;
import com.willwinder.ugs.nbp.lib.services.TopComponentLocalizer;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;

import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;

import javax.swing.*;
import java.awt.BorderLayout;
import org.openide.modules.OnStart;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//com.willwinder//CloudStorage//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "CloudStorageTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "output", openAtStartup = false)
@ActionID(
        category = CloudStorageTopComponent.CloudStorageCategory,
        id = CloudStorageTopComponent.CloudStorageActionId)
@ActionReference(path = LocalizingService.MENU_WINDOW_PLUGIN)
@TopComponent.OpenActionRegistration(
        displayName = "CloudStorage",
        preferredID = "CloudStorageTopComponent"
)
public final class CloudStorageTopComponent extends TopComponent {
  public final static String CloudStorageTitle = "CloudStorage"; //Localization.getString("platform.window.template-module", lang);
  public final static String CloudStorageTooltip = "CloudStorage tooltip"; //Localization.getString("platform.window.template-module.tooltip", lang);
  public final static String CloudStorageActionId = "com.willwinder.CloudStorageTopComponent";
  public final static String CloudStorageCategory = LocalizingService.CATEGORY_WINDOW;

  @OnStart
  public static class Localizer extends TopComponentLocalizer {
    public Localizer() {
      super(CloudStorageCategory, CloudStorageActionId, CloudStorageTitle);
    }
  }

  private final BackendAPI backend;

  public CloudStorageTopComponent() {
    setName(CloudStorageTitle);
    setToolTipText(CloudStorageTooltip);

    backend = CentralLookup.getDefault().lookup(BackendAPI.class);

    setLayout(new BorderLayout());
    add(new JLabel("Hello CloudStorage!"), BorderLayout.CENTER);
  }

  @Override
  public void componentOpened() {
  }

  @Override
  public void componentClosed() {
  }

  public void writeProperties(java.util.Properties p) {
    // better to version settings since initial version as advocated at
    // http://wiki.apidesign.org/wiki/PropertyFiles
    p.setProperty("version", "1.0");
  }

  public void readProperties(java.util.Properties p) {
    String version = p.getProperty("version");
  }
}

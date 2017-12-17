/*
    Copyright 2017 Will Winder

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
package com.willwinder.ugs.nbp.core.windows;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import java.awt.BorderLayout;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import net.miginfocom.swing.MigLayout;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//com.willwinder.ugs.nbp.core.windows//Diagnostics//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "DiagnosticsTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "top_left", openAtStartup = false)
@ActionID(
        category = LocalizingService.DiagnosticsCategory,
        id = LocalizingService.DiagnosticsActionId)
@ActionReference(path = LocalizingService.DiagnosticsWindowPath, position = 20100)
@TopComponent.OpenActionRegistration(
        displayName = "<Not localized:DiagnosticsAction>",
        preferredID = "DiagnosticsTopComponent")
public final class DiagnosticsTopComponent extends TopComponent implements UGSEventListener {
  private final BackendAPI backend;

  private Map<String, JLabel> labels = new LinkedHashMap<>();

  public DiagnosticsTopComponent() {
    setName(LocalizingService.DiagnosticsTitle);
    setToolTipText(LocalizingService.DiagnosticsTooltip);
    initComponents();

    backend = CentralLookup.getDefault().lookup(BackendAPI.class);
    backend.addUGSEventListener(this);
  }

  private void initComponents() {
    this.labels.put("backend:isConnected", new JLabel("-----"));
    this.labels.put("backend:isActive", new JLabel("-----"));
    this.labels.put("backend:isSendingFile", new JLabel("-----"));
    this.labels.put("backend:isIdle", new JLabel("-----"));
    this.labels.put("backend:isPaused", new JLabel("-----"));
    this.labels.put("backend:canPause", new JLabel("-----"));
    this.labels.put("backend:canCancel", new JLabel("-----"));
    this.labels.put("backend:canSend", new JLabel("-----"));
    this.labels.put("backend:getControlState", new JLabel("-----"));

    this.labels.put("controller:isPaused", new JLabel("-----"));
    this.labels.put("controller:isIdle", new JLabel("-----"));
    this.labels.put("controller:isCommOpen", new JLabel("-----"));
    this.labels.put("controller:isStreaming", new JLabel("-----"));
    this.labels.put("controller:rowsInSend", new JLabel("-----"));
    this.labels.put("controller:rowsSent", new JLabel("-----"));
    this.labels.put("controller:rowsRemaining", new JLabel("-----"));
    this.labels.put("controller:getSingleStepMode", new JLabel("-----"));
    this.labels.put("controller:getStatusUpdatesEnabled", new JLabel("-----"));
    this.labels.put("controller:getStatusUpdateRate", new JLabel("-----"));

    setLayout(new BorderLayout());


    JPanel labelPanel = new JPanel();
    labelPanel.setLayout(new MigLayout("wrap2,fillx"));
    for (Map.Entry<String,JLabel> dp : labels.entrySet()) {
      labelPanel.add(new JLabel(dp.getKey()));
      labelPanel.add(dp.getValue());
    }
    JScrollPane scrollPane = new JScrollPane(labelPanel);
    add(scrollPane, BorderLayout.CENTER);
  }

  private void refreshValues() {
    try {
      labels.get("backend:isConnected").setText(String.valueOf(backend.isConnected()));
      labels.get("backend:isActive").setText(String.valueOf(backend.isActive()));
      labels.get("backend:isSendingFile").setText(String.valueOf(backend.isSendingFile()));
      labels.get("backend:isIdle").setText(String.valueOf(backend.isIdle()));
      labels.get("backend:isPaused").setText(String.valueOf(backend.isPaused()));
      labels.get("backend:canPause").setText(String.valueOf(backend.canPause()));
      labels.get("backend:canCancel").setText(String.valueOf(backend.canCancel()));
      labels.get("backend:canSend").setText(String.valueOf(backend.canSend()));
      labels.get("backend:getControlState").setText(String.valueOf(backend.getControlState().toString()));
    } catch (Exception e) {
      e.printStackTrace();
    }

    try {
      IController controller = backend.getController();
      if (controller != null) {
        labels.get("controller:isPaused").setText(String.valueOf(controller.isPaused()));
        labels.get("controller:isIdle").setText(String.valueOf(controller.isIdle()));
        labels.get("controller:isCommOpen").setText(String.valueOf(controller.isCommOpen()));
        labels.get("controller:isStreaming").setText(String.valueOf(controller.isStreaming()));
        labels.get("controller:rowsInSend").setText(String.valueOf(controller.rowsInSend()));
        labels.get("controller:rowsSent").setText(String.valueOf(controller.rowsSent()));
        labels.get("controller:rowsRemaining").setText(String.valueOf(controller.rowsRemaining()));
        labels.get("controller:getSingleStepMode").setText(String.valueOf(controller.getSingleStepMode()));
        labels.get("controller:getStatusUpdatesEnabled").setText(String.valueOf(controller.getStatusUpdatesEnabled()));
        labels.get("controller:getStatusUpdateRate").setText(String.valueOf(controller.getStatusUpdateRate()));
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void UGSEvent(UGSEvent evt) {
    refreshValues();
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
    //String version = p.getProperty("version");
    // TODO read your settings according to their version
  }
}

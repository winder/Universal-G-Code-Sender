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
import com.willwinder.universalgcodesender.communicator.ICommunicator;
import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.firmware.IFirmwareSettings;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import net.miginfocom.swing.MigLayout;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.LinkedHashMap;
import java.util.Map;

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
    public static final String EMPTY_VALUE = "-----";
    private final BackendAPI backend;

    private Map<String, JLabel> labels = new LinkedHashMap<>();

    public DiagnosticsTopComponent() {
        setName(LocalizingService.DiagnosticsTitle);
        setToolTipText(LocalizingService.DiagnosticsTooltip);
        initComponents();

        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
    }

    private void initComponents() {
        this.labels.put("backend:isConnected", new JLabel(EMPTY_VALUE));
        this.labels.put("backend:isSendingFile", new JLabel(EMPTY_VALUE));
        this.labels.put("backend:isIdle", new JLabel(EMPTY_VALUE));
        this.labels.put("backend:isPaused", new JLabel(EMPTY_VALUE));
        this.labels.put("backend:canPause", new JLabel(EMPTY_VALUE));
        this.labels.put("backend:canCancel", new JLabel(EMPTY_VALUE));
        this.labels.put("backend:canSend", new JLabel(EMPTY_VALUE));
        this.labels.put("backend:getControllerState", new JLabel(EMPTY_VALUE));

        this.labels.put("controller:isPaused", new JLabel(EMPTY_VALUE));
        this.labels.put("controller:isIdle", new JLabel(EMPTY_VALUE));
        this.labels.put("controller:isCommOpen", new JLabel(EMPTY_VALUE));
        this.labels.put("controller:isStreaming", new JLabel(EMPTY_VALUE));
        this.labels.put("controller:rowsInSend", new JLabel(EMPTY_VALUE));
        this.labels.put("controller:rowsSent", new JLabel(EMPTY_VALUE));
        this.labels.put("controller:rowsRemaining", new JLabel(EMPTY_VALUE));
        this.labels.put("controller:getSingleStepMode", new JLabel(EMPTY_VALUE));
        this.labels.put("controller:getStatusUpdatesEnabled", new JLabel(EMPTY_VALUE));
        this.labels.put("controller:getStatusUpdateRate", new JLabel(EMPTY_VALUE));
        this.labels.put("controller:getControlState", new JLabel(EMPTY_VALUE));
        this.labels.put("controller:getCommunicatorState", new JLabel(EMPTY_VALUE));

        this.labels.put("communicator:numActiveCommands", new JLabel(EMPTY_VALUE));
        this.labels.put("communicator:isPaused", new JLabel(EMPTY_VALUE));
        this.labels.put("communicator:getSingleStepMode", new JLabel(EMPTY_VALUE));

        this.labels.put("settings:isHomingEnabled", new JLabel(EMPTY_VALUE));
        this.labels.put("settings:getReportingUnits", new JLabel(EMPTY_VALUE));
        setLayout(new BorderLayout());


        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new MigLayout("wrap2,fillx"));
        for (Map.Entry<String, JLabel> dp : labels.entrySet()) {
            labelPanel.add(new JLabel(dp.getKey()));
            labelPanel.add(dp.getValue());
        }

        JButton refresh = new JButton("Refresh");
        refresh.addActionListener((event) -> refreshValues());
        labelPanel.add(refresh, "spanx 2");

        JScrollPane scrollPane = new JScrollPane(labelPanel);
        add(scrollPane, BorderLayout.CENTER);
        setMinimumSize(new Dimension(100, 200));
    }

    private void refreshValues() {
        try {
            labels.get("backend:isConnected").setText(String.valueOf(backend.isConnected()));
            labels.get("backend:isSendingFile").setText(String.valueOf(backend.isSendingFile()));
            labels.get("backend:isIdle").setText(String.valueOf(backend.isIdle()));
            labels.get("backend:isPaused").setText(String.valueOf(backend.isPaused()));
            labels.get("backend:canPause").setText(String.valueOf(backend.canPause()));
            labels.get("backend:canCancel").setText(String.valueOf(backend.canCancel()));
            labels.get("backend:canSend").setText(String.valueOf(backend.canSend()));
            labels.get("backend:getControllerState").setText(String.valueOf(backend.getControllerState().toString()));
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
                labels.get("controller:getCommunicatorState").setText(String.valueOf(controller.getCommunicatorState()));

                IFirmwareSettings firmwareSettings = controller.getFirmwareSettings();
                if (firmwareSettings != null) {
                    labels.get("settings:isHomingEnabled").setText(String.valueOf(firmwareSettings.isHomingEnabled()));

                    if (firmwareSettings.getReportingUnits() != null) {
                        labels.get("settings:getReportingUnits").setText(controller.getFirmwareSettings().getReportingUnits().toString());
                    } else {
                        labels.get("settings:getReportingUnits").setText("?");
                    }
                }

                ICommunicator communicator = controller.getCommunicator();
                if (communicator != null) {
                    labels.get("communicator:numActiveCommands").setText(String.valueOf(communicator.numActiveCommands()));
                    labels.get("communicator:isPaused").setText(String.valueOf(communicator.isPaused()));
                    labels.get("communicator:getSingleStepMode").setText(String.valueOf(communicator.getSingleStepMode()));
                }
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
        backend.addUGSEventListener(this);
    }

    @Override
    public void componentClosed() {
        backend.removeUGSEventListener(this);
    }

    public void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
    }

    public void readProperties(java.util.Properties p) {
    }
}

/*
    Copywrite 2016 Will Winder

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
package com.willwinder.ugs.nbp.lib.services;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import java.io.IOException;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author wwinder
 */
@ServiceProvider(service=LocalizingService.class) 
public class LocalizingService {
    // Initialize backend (locale setting) before we load localized strings.
    static private final String lang = CentralLookup.getDefault().lookup(BackendAPI.class).getSettings().getLanguage();
    public final static String VisualizerTitle = Localization.getString("platform.window.visualizer", lang);
    public final static String VisualizerTooltip = Localization.getString("platform.window.visualizer.tooltip", lang);
    public final static String VisualizerWindowPath = "Menu/Window";
    public final static String VisualizerActionId = "com.willwinder.ugs.nbm.visualizer.Visualizer2TopComponent";
    public final static String VisualizerCategory = "Window";

    public final static String SerialConsoleTitle = Localization.getString("platform.window.serialconsole", lang);
    public final static String SerialConsoleTooltip = Localization.getString("platform.window.serialconsole.tooltip", lang);
    public final static String SerialConsoleWindowPath = "Menu/Window";
    public final static String SerialConsoleActionId = "com.willwinder.universalgcodesender.nbp.console.SerialConsoleTopComponent";
    public final static String SerialConsoleCategory = "Window";

    public final static String SendStatusTitle = Localization.getString("platform.window.sendstatus", lang);
    public final static String SendStatusTooltip = Localization.getString("platform.window.sendstatus.tooltip", lang);
    public final static String SendStatusWindowPath = "Menu/Window/Classic";
    public final static String SendStatusActionId = "com.willwinder.ugs.nbp.core.control.SendStatusTopComponent";
    public final static String SendStatusCategory = "Window";

    public final static String OverridesTitle = Localization.getString("platform.window.overrides", lang);
    public final static String OverridesTooltip = Localization.getString("platform.window.overrides.tooltip", lang);
    public final static String OverridesWindowPath = "Menu/Window";
    public final static String OverridesActionId = "com.willwinder.universalgcodesender.nbp.control.OverridesTopComponent";
    public final static String OverridesCategory = "Window";
    
    public final static String MacrosTitle = Localization.getString("platform.window.macros", lang);
    public final static String MacrosTooltip = Localization.getString("platform.window.macros.tooltip", lang);
    public final static String MacrosWindowPath = "Menu/Window";
    public final static String MacrosActionId = "com.willwinder.ugs.nbp.core.control.MacrosTopComponent";
    public final static String MacrosCategory = "Window";

    public final static String JogControlTitle = Localization.getString("platform.window.jogcontrol", lang);
    public final static String JogControlTooltip = Localization.getString("platform.window.jogcontrol.tooltip", lang);
    public final static String JogControlWindowPath = "Menu/Window";
    public final static String JogControlActionId = "com.willwinder.ugs.nbp.core.control.JogControlTopComponent";
    public final static String JogControlCategory = "Window";

    public final static String FileBrowserTitle = Localization.getString("platform.window.filebrowser", lang);
    public final static String FileBrowserTooltip = Localization.getString("platform.window.filebrowser.tooltip", lang);
    public final static String FileBrowserWindowPath = "Menu/Window/Classic";
    public final static String FileBrowserActionId = "com.willwinder.ugs.nbp.core.filebrowser.FileBrowserTopComponentTopComponent";
    public final static String FileBrowserCategory = "Window";

    public final static String LocationStatusTitle = Localization.getString("platform.window.dro", lang);
    public final static String LocationStatusTooltip = Localization.getString("platform.window.dro.tooltip", lang);
    public final static String LocationStatusWindowPath = "Menu/Window";
    public final static String LocationStatusActionId = "com.willwinder.ugs.nbp.control.StatusTopComponent";
    public final static String LocationStatusCategory = "Window";

    public final static String ActionsTitle = Localization.getString("platform.window.actions", lang);
    public final static String ActionsTooltip = Localization.getString("platform.window.actions.tooltip", lang);
    public final static String ActionsWindowPath = "Menu/Window/Classic";
    public final static String ActionsActionId = "com.willwinder.ugs.nbp.control.ActionsTopComponent";
    public final static String ActionsCategory = "Window";

    public final static String WorkflowWindowTitle = Localization.getString("platform.window.workflow", lang);
    public final static String WorkflowWindowTooltip = Localization.getString("platform.window.workflow.tooltip", lang);
    public final static String WorkflowWindowWindowPath = "Menu/Window/Plugins";
    public final static String WorkflowWindowActionId = "com.willwinder.ugs.nbm.workflow.WorkflowWindowTopComponent";
    public final static String WorkflowWindowCategory = "Window";

    public final static String ProbeTitle = Localization.getString("platform.window.probe", lang);
    public final static String ProbeTooltip = Localization.getString("platform.window.probe.tooltip", lang);
    public final static String ProbeWindowPath = "Menu/Window/Plugins";
    public final static String ProbeActionId = "com.willwinder.ugs.nbp.control.ProbeTopComponent";
    public final static String ProbeCategory = "Window";

    public final static String AutoLevelerTitle = Localization.getString("platform.window.autoleveler", lang);
    public final static String AutoLevelerTooltip = Localization.getString("platform.window.autoleveler.tooltip", lang);
    public final static String AutoLevelerWindowPath = "Menu/Window/Plugins";
    public final static String AutoLevelerActionId = "com.willwinder.ugs.platform.surfacescanner.AutoLevelerTopComponent";
    public final static String AutoLevelerCategory = "Window";

    public final static String StartTitleKey = "mainWindow.swing.sendButton";
    public final static String StartTitle = Localization.getString(StartTitleKey, lang);
    public final static String StartWindowPath = "Menu/Machine";
    public final static String StartActionId = "com.willwinder.ugs.nbp.core.actions.StartAction";
    public final static String StartCategory = "Machine";

    public final static String PauseTitleKey = "mainWindow.swing.pauseButton";
    public final static String PauseTitle = Localization.getString(PauseTitleKey, lang);
    public final static String PauseWindowPath = "Menu/Machine";
    public final static String PauseActionId = "com.willwinder.ugs.nbp.core.actions.PauseAction";
    public final static String PauseCategory = "Machine";

    public final static String StopTitleKey = "mainWindow.swing.stopButton";
    public final static String StopTitle = Localization.getString(StopTitleKey, lang);
    public final static String StopWindowPath = "Menu/Machine";
    public final static String StopActionId = "com.willwinder.ugs.nbp.core.actions.StopAction";
    public final static String StopCategory = "Machine";

    public final static String PendantTitleKey = "mainWindow.swing.pendantButton";
    public final static String PendantTitle = Localization.getString(PendantTitleKey, lang);
    public final static String PendantWindowPath = "Menu/Machine";
    public final static String PendantActionId = "com.willwinder.ugs.nbp.core.actions.PendantAction";
    public final static String PendantCategory = "Machine";

    public final static String ConnectDisconnectTitleConnect = Localization.getString("mainWindow.ui.connect", lang);
    public final static String ConnectDisconnectTitleDisconnect = Localization.getString("mainWindow.ui.disconnect", lang);
    public final static String ConnectDisconnectActionTitleKey = "mainWindow.ui.connectDisconnect";
    public final static String ConnectWindowPath = "Menu/Machine";
    public final static String ConnectDisconnectActionId = "com.willwinder.ugs.nbp.core.actions.ConnectDisconnectAction";
    public final static String ConnectDisconnectCategory = "Machine";

    public final static String ConnectionBaudRateToolbarTitle = Localization.getString("mainWindow.swing.baudrate.toolbarTitle", lang);
    public final static String ConnectionBaudRateToolbarTitleKey = "mainWindow.swing.baudrate.toolbarTitle";
    public final static String ConnectionBaudRateToolbarActionId = "com.willwinder.ugs.nbp.core.toolbars.ConnectionBaudRateToolbar";
    public final static String ConnectionBaudRateToolbarCategory = "Machine";

    public final static String ConnectionFirmwareToolbarTitle = Localization.getString("mainWindow.swing.firmware.toolbarTitle", lang);
    public final static String ConnectionFirmwareToolbarTitleKey = "mainWindow.swing.firmware.toolbarTitle";
    public final static String ConnectionFirmwareToolbarActionId = "com.willwinder.ugs.nbp.core.toolbars.ConnectionFirmwareToolbar";
    public final static String ConnectionFirmwareToolbarCategory = "Machine";

    public final static String ConnectionSerialPortToolbarTitle = Localization.getString("mainWindow.swing.serialport.toolbarTitle", lang);
    public final static String ConnectionSerialPortToolbarTitleKey = "mainWindow.swing.firmware.toolbarTitle";
    public final static String ConnectionSerialPortToolbarActionId = "com.willwinder.ugs.nbp.core.toolbars.ConnectionSerialPortToolbar";
    public final static String ConnectionSerialPortToolbarCategory = "Machine";

    public final static String FileBrowserToolbarTitle = Localization.getString("mainWindow.swing.filebrowser.toolbarTitle", lang);
    public final static String FileBrowserToolbarTitleKey = "mainWindow.swing.filebrowser.toolbarTitle";
    public final static String FileBrowserToolbarActionId = "com.willwinder.ugs.nbp.core.toolbars.FileBrowserToolbar";
    public final static String FileBrowserToolbarCategory = "Machine";


    public LocalizingService() throws IOException {
        ActionRegistrationService ars =  Lookup.getDefault().lookup(ActionRegistrationService.class);

        // Update menu's with localized names.

        // Menu Folders
        ars.createAndLocalizeFullMenu("Menu/Window/Classic",
                String.format("Menu/%s/%s",
                        Localization.getString("platform.menu.window"),
                        Localization.getString("platform.menu.classic")));
        ars.createAndLocalizeFullMenu("Menu/Window/Plugins",
                String.format("Menu/%s/%s",
                        Localization.getString("platform.menu.window"),
                        Localization.getString("platform.menu.plugins")));

        // Menu Items
        ars.overrideActionName(WorkflowWindowCategory, WorkflowWindowActionId, WorkflowWindowTitle);
        ars.overrideActionName(ActionsCategory, ActionsActionId, ActionsTitle);
        ars.overrideActionName(LocationStatusCategory, LocationStatusActionId, LocationStatusTitle);
        ars.overrideActionName(FileBrowserCategory, FileBrowserActionId, FileBrowserTitle);
        ars.overrideActionName(JogControlCategory, JogControlActionId, JogControlTitle);
        ars.overrideActionName(MacrosCategory, MacrosActionId, MacrosTitle);
        ars.overrideActionName(OverridesCategory, OverridesActionId, OverridesTitle);
        ars.overrideActionName(SendStatusCategory, SendStatusActionId, SendStatusTitle);
        ars.overrideActionName(SerialConsoleCategory, SerialConsoleActionId, SerialConsoleTitle);
        ars.overrideActionName(VisualizerCategory, VisualizerActionId, VisualizerTitle);
        ars.overrideActionName(ProbeCategory, ProbeActionId, ProbeTitle);
        ars.overrideActionName(AutoLevelerCategory, AutoLevelerActionId, AutoLevelerTitle);
    }
}

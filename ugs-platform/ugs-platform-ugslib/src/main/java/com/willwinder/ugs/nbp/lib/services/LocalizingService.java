/**
 * LocalizingService for core module top components.
 * Modules should use TopComponentLocalizer and @OnStart instead of this.
 */
/*
    Copyright 2016-2017 Will Winder

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
    public static final String MENU_WINDOW = "Menu/Window";
    public static final String MENU_WINDOW_PLUGIN = MENU_WINDOW +"/Plugins";
    public static final String MENU_WINDOW_CLASSIC = MENU_WINDOW + "/Classic";
    public static final String MENU_MACHINE = "Menu/Machine";

    public static final String CATEGORY_WINDOW = "Window";
    public static final String CATEGORY_MACHINE = "Machine";

    // Initialize backend (locale setting) before we load localized strings.
    public static final String lang = CentralLookup.getDefault().lookup(BackendAPI.class).getSettings().getLanguage();

    public final static String SerialConsoleTitle = Localization.getString("platform.window.serialconsole", lang);
    public final static String SerialConsoleTooltip = Localization.getString("platform.window.serialconsole.tooltip", lang);
    public final static String SerialConsoleWindowPath = MENU_WINDOW;
    public final static String SerialConsoleActionId = "com.willwinder.universalgcodesender.nbp.console.SerialConsoleTopComponent";
    public final static String SerialConsoleCategory = CATEGORY_WINDOW;

    public final static String SendStatusTitle = Localization.getString("platform.window.sendstatus", lang);
    public final static String SendStatusTooltip = Localization.getString("platform.window.sendstatus.tooltip", lang);
    public final static String SendStatusWindowPath = MENU_WINDOW_CLASSIC;
    public final static String SendStatusActionId = "com.willwinder.ugs.nbp.core.control.SendStatusTopComponent";
    public final static String SendStatusCategory = CATEGORY_WINDOW;

    public final static String OverridesTitle = Localization.getString("platform.window.overrides", lang);
    public final static String OverridesTooltip = Localization.getString("platform.window.overrides.tooltip", lang);
    public final static String OverridesWindowPath = MENU_WINDOW;
    public final static String OverridesActionId = "com.willwinder.universalgcodesender.nbp.control.OverridesTopComponent";
    public final static String OverridesCategory = CATEGORY_WINDOW;
    
    public final static String MacrosTitle = Localization.getString("platform.window.macros", lang);
    public final static String MacrosTooltip = Localization.getString("platform.window.macros.tooltip", lang);
    public final static String MacrosWindowPath = MENU_WINDOW;
    public final static String MacrosActionId = "com.willwinder.ugs.nbp.core.control.MacrosTopComponent";
    public final static String MacrosCategory = CATEGORY_WINDOW;

    public final static String JogControlTitle = Localization.getString("platform.window.jogcontrol", lang);
    public final static String JogControlTooltip = Localization.getString("platform.window.jogcontrol.tooltip", lang);
    public final static String JogControlWindowPath = MENU_WINDOW;
    public final static String JogControlActionId = "com.willwinder.ugs.nbp.core.control.JogControlTopComponent";
    public final static String JogControlCategory = CATEGORY_WINDOW;

    public final static String FileBrowserTitle = Localization.getString("platform.window.filebrowser", lang);
    public final static String FileBrowserTooltip = Localization.getString("platform.window.filebrowser.tooltip", lang);
    public final static String FileBrowserWindowPath = MENU_WINDOW_CLASSIC;
    public final static String FileBrowserActionId = "com.willwinder.ugs.nbp.core.filebrowser.FileBrowserTopComponentTopComponent";
    public final static String FileBrowserCategory = CATEGORY_WINDOW;

    public final static String LocationStatusTitle = Localization.getString("platform.window.dro", lang);
    public final static String LocationStatusTooltip = Localization.getString("platform.window.dro.tooltip", lang);
    public final static String LocationStatusWindowPath = MENU_WINDOW;
    public final static String LocationStatusActionId = "com.willwinder.ugs.nbp.control.StatusTopComponent";
    public final static String LocationStatusCategory = CATEGORY_WINDOW;

    public final static String ActionsTitle = Localization.getString("platform.window.actions", lang);
    public final static String ActionsTooltip = Localization.getString("platform.window.actions.tooltip", lang);
    public final static String ActionsWindowPath = MENU_WINDOW_CLASSIC;
    public final static String ActionsActionId = "com.willwinder.ugs.nbp.control.ActionsTopComponent";
    public final static String ActionsCategory = CATEGORY_WINDOW;

    public final static String StartTitleKey = "mainWindow.swing.sendButton";
    public final static String StartTitle = Localization.getString(StartTitleKey, lang);
    public final static String StartWindowPath = MENU_MACHINE;
    public final static String StartActionId = "com.willwinder.ugs.nbp.core.actions.StartAction";
    public final static String StartCategory = CATEGORY_MACHINE;

    public final static String PauseTitleKey = "mainWindow.swing.pauseButton";
    public final static String PauseTitle = Localization.getString(PauseTitleKey, lang);
    public final static String PauseWindowPath = MENU_MACHINE;
    public final static String PauseActionId = "com.willwinder.ugs.nbp.core.actions.PauseAction";
    public final static String PauseCategory = CATEGORY_MACHINE;

    public final static String StopTitleKey = "mainWindow.swing.stopButton";
    public final static String StopTitle = Localization.getString(StopTitleKey, lang);
    public final static String StopWindowPath = MENU_MACHINE;
    public final static String StopActionId = "com.willwinder.ugs.nbp.core.actions.StopAction";
    public final static String StopCategory = CATEGORY_MACHINE;

    public final static String PendantTitleKey = "mainWindow.swing.pendant";
    public final static String PendantTitle = Localization.getString(PendantTitleKey, lang);
    public final static String PendantWindowPath = MENU_MACHINE;
    public final static String PendantActionId = "com.willwinder.ugs.nbp.core.actions.PendantAction";
    public final static String PendantCategory = CATEGORY_MACHINE;

    public final static String ConnectDisconnectTitleConnect = Localization.getString("mainWindow.ui.connect", lang);
    public final static String ConnectDisconnectTitleDisconnect = Localization.getString("mainWindow.ui.disconnect", lang);
    public final static String ConnectDisconnectActionTitleKey = "mainWindow.ui.connectDisconnect";
    public final static String ConnectWindowPath = MENU_MACHINE;
    public final static String ConnectDisconnectActionId = "com.willwinder.ugs.nbp.core.actions.ConnectDisconnectAction";
    public final static String ConnectDisconnectCategory = CATEGORY_MACHINE;

    public final static String ConnectionBaudRateToolbarTitle = Localization.getString("mainWindow.swing.baudrate.toolbarTitle", lang);
    public final static String ConnectionBaudRateToolbarTitleKey = "mainWindow.swing.baudrate.toolbarTitle";
    public final static String ConnectionBaudRateToolbarActionId = "com.willwinder.ugs.nbp.core.actions.BaudRateAction";
    public final static String ConnectionBaudRateToolbarCategory = CATEGORY_MACHINE;

    public final static String ConnectionFirmwareToolbarTitle = Localization.getString("mainWindow.swing.firmware.toolbarTitle", lang);
    public final static String ConnectionFirmwareToolbarTitleKey = "mainWindow.swing.firmware.toolbarTitle";
    public final static String ConnectionFirmwareToolbarActionId = "com.willwinder.ugs.nbp.core.actions.FirmwareAction";
    public final static String ConnectionFirmwareToolbarCategory = CATEGORY_MACHINE;

    public final static String ConnectionSerialPortToolbarTitle = Localization.getString("mainWindow.swing.serialport.toolbarTitle", lang);
    public final static String ConnectionSerialPortToolbarTitleKey = "mainWindow.swing.firmware.toolbarTitle";
    public final static String ConnectionSerialPortToolbarActionId = "com.willwinder.ugs.nbp.core.actions.PortAction";
    public final static String ConnectionSerialPortToolbarCategory = CATEGORY_MACHINE;

    public final static String FileBrowserToolbarTitle = Localization.getString("mainWindow.swing.filebrowser.toolbarTitle", lang);
    public final static String FileBrowserToolbarTitleKey = "mainWindow.swing.filebrowser.toolbarTitle";
    public final static String FileBrowserToolbarActionId = "com.willwinder.ugs.nbp.core.toolbars.FileBrowserToolbar";
    public final static String FileBrowserToolbarCategory = CATEGORY_MACHINE;

    public final static String StateTitle = Localization.getString("platform.window.states", lang);
    public final static String StateTooltip = Localization.getString("platform.window.states.tooltip", lang);
    public final static String StateWindowPath = MENU_WINDOW;
    public final static String StateActionId = "com.willwinder.ugs.nbp.core.windows.StateTopComponent";
    public final static String StateCategory = CATEGORY_WINDOW;

    public LocalizingService() throws IOException {
        ActionRegistrationService ars =  Lookup.getDefault().lookup(ActionRegistrationService.class);

        // Update menu's with localized names.

        // Menu Folders
        ars.createAndLocalizeFullMenu(MENU_WINDOW_CLASSIC,
                String.format("Menu/%s/%s",
                        Localization.getString("platform.menu.window"),
                        Localization.getString("platform.menu.classic")));
        ars.createAndLocalizeFullMenu(MENU_WINDOW_PLUGIN,
                String.format("Menu/%s/%s",
                        Localization.getString("platform.menu.window"),
                        Localization.getString("platform.menu.plugins")));

        // Localize Menu Items
        ars.overrideActionName(ActionsCategory, ActionsActionId, ActionsTitle);
        ars.overrideActionName(LocationStatusCategory, LocationStatusActionId, LocationStatusTitle);
        ars.overrideActionName(FileBrowserCategory, FileBrowserActionId, FileBrowserTitle);
        ars.overrideActionName(JogControlCategory, JogControlActionId, JogControlTitle);
        ars.overrideActionName(MacrosCategory, MacrosActionId, MacrosTitle);
        ars.overrideActionName(OverridesCategory, OverridesActionId, OverridesTitle);
        ars.overrideActionName(SendStatusCategory, SendStatusActionId, SendStatusTitle);
        ars.overrideActionName(SerialConsoleCategory, SerialConsoleActionId, SerialConsoleTitle);
        ars.overrideActionName(StateCategory, StateActionId, StateTitle);
    }
}

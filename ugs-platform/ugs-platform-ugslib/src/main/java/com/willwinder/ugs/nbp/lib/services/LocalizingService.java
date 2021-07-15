/*
    Copyright 2016-2021 Will Winder

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
 * LocalizingService for core module top components.
 *
 * @author wwinder
 */
@ServiceProvider(service=LocalizingService.class)
public class LocalizingService {
    public static final String MENU_WINDOW = "Menu/Window";
    public static final String MENU_WINDOW_PLUGIN = MENU_WINDOW +"/Plugins";
    public static final String MENU_WINDOW_CLASSIC = MENU_WINDOW + "/Classic";
    public static final String MENU_FILE = "Menu/File";
    public static final String MENU_EDIT = "Menu/Edit";
    public static final String MENU_MACHINE = "Menu/Machine";
    public static final String MENU_PROGRAM = "Menu/Program";
    public static final String MENU_MACHINE_JOG = "Menu/Machine/Jog";
    public static final String MENU_MACHINE_JOG_STEP_SIZE = "Menu/Machine/Jog/Step Size";
    public static final String MENU_MACHINE_ACTIONS = "Menu/Machine/Actions";
    public static final String MENU_VISUALIZER = "Menu/Visualizer";
    public static final String MENU_MACROS = "Menu/Machine/Macros";

    public static final String CATEGORY_WINDOW = "Window";
    public static final String CATEGORY_MACHINE = "Machine";
    public static final String CATEGORY_PROGRAM = "Program";
    public static final String CATEGORY_FILE = "File";
    public static final String CATEGORY_VISUALIZER = "Visualizer";
    public static final String CATEGORY_EDIT = "Edit";

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

    public final static String EditMacrosTitleKey = "platform.window.edit.macros";
    public final static String EditMacrosTitle = Localization.getString("platform.window.edit.macros", lang);
    public final static String EditMacrosTooltip = Localization.getString("platform.window.edit.macros.tooltip", lang);
    public final static String EditMacrosWindowPath = LocalizingService.MENU_MACHINE;
    public final static String EditMacrosActionId = "com.willwinder.ugs.nbp.core.control.EditMacrosAction";
    public final static String EditMacrosActionCategory = CATEGORY_MACHINE;

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
    public final static String LocationStatusCategory = MENU_WINDOW_PLUGIN;

    public final static String ActionsTitle = Localization.getString("platform.window.actions", lang);
    public final static String ActionsTooltip = Localization.getString("platform.window.actions.tooltip", lang);
    public final static String ActionsWindowPath = MENU_WINDOW_CLASSIC;
    public final static String ActionsActionId = "com.willwinder.ugs.nbp.control.ActionsTopComponent";
    public final static String ActionsCategory = CATEGORY_WINDOW;

    public final static String OpenTitleKey = "platform.menu.open";
    public final static String OpenTitle = Localization.getString(OpenTitleKey, lang);
    public final static String OpenWindowPath = MENU_FILE;
    public final static String OpenActionId = "com.willwinder.ugs.nbp.core.actions.OpenAction";
    public final static String OpenCategory = CATEGORY_FILE;

    public final static String ReloadGcodeTitleKey = "platform.menu.reload";
    public final static String ReloadGcodeTitle = Localization.getString(ReloadGcodeTitleKey, lang);
    public final static String ReloadGcodeWindowPath = MENU_FILE;
    public final static String ReloadGcodeActionId = "com.willwinder.ugs.nbp.core.actions.ReloadGcodeAction";
    public final static String ReloadGcodeCategory = CATEGORY_FILE;

    public final static String StartTitleKey = "mainWindow.swing.sendButton";
    public final static String StartTitle = Localization.getString(StartTitleKey, lang);
    public final static String StartWindowPath = MENU_PROGRAM;
    public final static String StartActionId = "com.willwinder.ugs.nbp.core.actions.StartAction";
    public final static String StartCategory = CATEGORY_MACHINE;

    public final static String PauseTitleKey = "mainWindow.swing.pauseButton";
    public final static String PauseTitle = Localization.getString(PauseTitleKey, lang);
    public final static String PauseWindowPath = MENU_PROGRAM;
    public final static String PauseActionId = "com.willwinder.ugs.nbp.core.actions.PauseAction";
    public final static String PauseCategory = CATEGORY_MACHINE;

    public final static String StopTitleKey = "mainWindow.swing.stopButton";
    public final static String StopTitle = Localization.getString(StopTitleKey, lang);
    public final static String StopWindowPath = MENU_PROGRAM;
    public final static String StopActionId = "com.willwinder.ugs.nbp.core.actions.StopAction";
    public final static String StopCategory = CATEGORY_MACHINE;

    public final static String PendantTitleKey = "mainWindow.swing.pendant";
    public final static String PendantTitle = Localization.getString(PendantTitleKey, lang);
    public final static String PendantWindowPath = MENU_MACHINE;
    public final static String PendantActionId = "com.willwinder.ugs.nbp.core.actions.PendantAction";
    public final static String PendantCategory = CATEGORY_MACHINE;

    public final static String HomeTitleKey = "mainWindow.swing.homeMachine";
    public final static String HomeTitle = Localization.getString(HomeTitleKey, lang);
    public final static String HomeWindowPath = MENU_MACHINE_ACTIONS;
    public final static String HomeActionId = "com.willwinder.ugs.nbp.core.actions.HomeAction";
    public final static String HomeCategory = CATEGORY_MACHINE;

    public final static String ReturnToZeroTitleKey = "mainWindow.swing.returnToZeroButton";
    public final static String ReturnToZeroTitle = Localization.getString(ReturnToZeroTitleKey, lang);
    public final static String ReturnToZeroWindowPath = MENU_MACHINE_ACTIONS;
    public final static String ReturnToZeroActionId = "com.willwinder.ugs.nbp.core.actions.ReturnToZeroAction";
    public final static String ReturnToZeroCategory = CATEGORY_MACHINE;

    public final static String UnlockTitleKey = "mainWindow.swing.alarmLock";
    public final static String UnlockTitle = Localization.getString(UnlockTitleKey, lang);
    public final static String UnlockWindowPath = MENU_MACHINE_ACTIONS;
    public final static String UnlockActionId = "com.willwinder.ugs.nbp.core.actions.UnlockAction";
    public final static String UnlockCategory = CATEGORY_MACHINE;

    public final static String SoftResetTitleKey = "mainWindow.swing.softResetMachineControl";
    public final static String SoftResetTitle = Localization.getString(SoftResetTitleKey, lang);
    public final static String SoftResetWindowPath = MENU_MACHINE_ACTIONS;
    public final static String SoftResetActionId = "com.willwinder.ugs.nbp.core.actions.SoftResetAction";
    public final static String SoftResetCategory = CATEGORY_MACHINE;

    public final static String OpenDoorTitleKey = "mainWindow.swing.openDoor";
    public final static String OpenDoorTitle = Localization.getString(OpenDoorTitleKey, lang);
    public final static String OpenDoorWindowPath = MENU_MACHINE_ACTIONS;
    public final static String OpenDoorActionId = "com.willwinder.ugs.nbp.core.actions.OpenDoorAction";
    public final static String OpenDoorCategory = CATEGORY_MACHINE;

    public final static String ResetZeroTitleKey = "mainWindow.swing.resetCoordinatesButton";
    public final static String ResetZeroTitle = Localization.getString(ResetZeroTitleKey, lang);
    public final static String ResetZeroWindowPath = MENU_MACHINE_ACTIONS;
    public final static String ResetZeroActionId = "com.willwinder.ugs.nbp.core.actions.ResetCoordinatesToZeroAction";
    public final static String ResetZeroCategory = CATEGORY_MACHINE;

    public final static String ResetXZeroTitleKey = "action.resetXCoordinatesButton";
    public final static String ResetXZeroTitle = Localization.getString(ResetXZeroTitleKey, lang);
    public final static String ResetXZeroWindowPath = MENU_MACHINE_ACTIONS;
    public final static String ResetXZeroActionId = "com.willwinder.ugs.nbp.core.actions.ResetXCoordinatesToZeroAction";
    public final static String ResetXZeroCategory = CATEGORY_MACHINE;

    public final static String ResetYZeroTitleKey = "action.resetYCoordinatesButton";
    public final static String ResetYZeroTitle = Localization.getString(ResetYZeroTitleKey, lang);
    public final static String ResetYZeroWindowPath = MENU_MACHINE_ACTIONS;
    public final static String ResetYZeroActionId = "com.willwinder.ugs.nbp.core.actions.ResetYCoordinatesToZeroAction";
    public final static String ResetYZeroCategory = CATEGORY_MACHINE;

    public final static String ResetZZeroTitleKey = "action.resetZCoordinatesButton";
    public final static String ResetZZeroTitle = Localization.getString(ResetZZeroTitleKey, lang);
    public final static String ResetZZeroWindowPath = MENU_MACHINE_ACTIONS;
    public final static String ResetZZeroActionId = "com.willwinder.ugs.nbp.core.actions.ResetZCoordinatesToZeroAction";
    public final static String ResetZZeroCategory = CATEGORY_MACHINE;

    public final static String ResetAZeroTitleKey = "action.resetACoordinatesButton";
    public final static String ResetAZeroTitle = Localization.getString(ResetAZeroTitleKey, lang);
    public final static String ResetAZeroWindowPath = MENU_MACHINE_ACTIONS;
    public final static String ResetAZeroActionId = "com.willwinder.ugs.nbp.core.actions.ResetACoordinatesToZeroAction";
    public final static String ResetAZeroCategory = CATEGORY_MACHINE;

    public final static String ResetBZeroTitleKey = "action.resetBCoordinatesButton";
    public final static String ResetBZeroTitle = Localization.getString(ResetBZeroTitleKey, lang);
    public final static String ResetBZeroWindowPath = MENU_MACHINE_ACTIONS;
    public final static String ResetBZeroActionId = "com.willwinder.ugs.nbp.core.actions.ResetBCoordinatesToZeroAction";
    public final static String ResetBZeroCategory = CATEGORY_MACHINE;

    public final static String ResetCZeroTitleKey = "action.resetCCoordinatesButton";
    public final static String ResetCZeroTitle = Localization.getString(ResetCZeroTitleKey, lang);
    public final static String ResetCZeroWindowPath = MENU_MACHINE_ACTIONS;
    public final static String ResetCZeroActionId = "com.willwinder.ugs.nbp.core.actions.ResetCCoordinatesToZeroAction";
    public final static String ResetCZeroCategory = CATEGORY_MACHINE;


    public final static String CheckModeTitleKey = "mainWindow.swing.checkMode";
    public final static String CheckModeTitle = Localization.getString(CheckModeTitleKey, lang);
    public final static String CheckModeWindowPath = MENU_MACHINE_ACTIONS;
    public final static String CheckModeActionId = "com.willwinder.ugs.nbp.core.actions.CheckModeAction";
    public final static String CheckModeCategory = CATEGORY_MACHINE;

    public final static String GetStateTitleKey = "mainWindow.swing.getState";
    public final static String GetStateTitle = Localization.getString(GetStateTitleKey, lang);
    public final static String GetStateWindowPath = MENU_MACHINE_ACTIONS;
    public final static String GetStateActionId = "com.willwinder.ugs.nbp.core.actions.GetStateAction";
    public final static String GetStateCategory = CATEGORY_MACHINE;

    public final static String OutlineTitleKey = "platform.action.outline";
    public final static String OutlineTitle = Localization.getString(OutlineTitleKey, lang);
    public final static String OutlineWindowPath = MENU_MACHINE_ACTIONS;
    public final static String OutlineActionId = "com.willwinder.ugs.nbp.core.actions.OutlineAction";
    public final static String OutlineCategory = CATEGORY_PROGRAM;

    public final static String ConnectDisconnectTitleConnect = Localization.getString("mainWindow.ui.connect", lang);
    public final static String ConnectDisconnectTitleDisconnect = Localization.getString("mainWindow.ui.disconnect", lang);
    public final static String ConnectDisconnectActionTitleKey = "mainWindow.ui.connectDisconnect";
    public final static String ConnectWindowPath = MENU_MACHINE;
    public final static String ConnectDisconnectActionId = "com.willwinder.ugs.nbp.core.actions.ConnectDisconnectAction";
    public final static String ConnectDisconnectCategory = CATEGORY_MACHINE;

    public final static String ConfigureFirmwareTitle = Localization.getString("mainWindow.swing.firmwareSettingsMenu", lang);
    public final static String ConfigureFirmwareActionTitleKey = "mainWindow.swing.firmwareSettingsMenu";
    public final static String ConfigureFirmwareWindowPath = MENU_MACHINE;
    public final static String ConfigureFirmwareActionId = "com.willwinder.ugs.nbp.core.actions.ConfigureFirmwareAction";
    public final static String ConfigureFirmwareCategory = CATEGORY_MACHINE;

    public final static String ConnectionBaudRateToolbarTitle = Localization.getString("mainWindow.swing.baudrate.toolbarTitle", lang);
    public final static String ConnectionBaudRateToolbarTitleKey = "mainWindow.swing.baudrate.toolbarTitle";
    public final static String ConnectionBaudRateToolbarActionId = "com.willwinder.ugs.nbp.core.actions.BaudRateAction";
    public final static String ConnectionBaudRateToolbarCategory = CATEGORY_MACHINE;

    public final static String ConnectionFirmwareToolbarTitle = Localization.getString("mainWindow.swing.firmware.toolbarTitle", lang);
    public final static String ConnectionFirmwareToolbarTitleKey = "mainWindow.swing.firmware.toolbarTitle";
    public final static String ConnectionFirmwareToolbarActionId = "com.willwinder.ugs.nbp.core.actions.FirmwareAction";
    public final static String ConnectionFirmwareToolbarCategory = CATEGORY_MACHINE;

    public final static String ConnectionSerialPortToolbarTitle = Localization.getString("mainWindow.swing.serialport.toolbarTitle", lang);
    public final static String ConnectionSerialPortToolbarTitleKey = "mainWindow.swing.serialport.toolbarTitle";
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

    public final static String DiagnosticsTitle = Localization.getString("platform.window.diagnostics", lang);
    public final static String DiagnosticsTooltip = Localization.getString("platform.window.diagnostics.tooltip", lang);
    public final static String DiagnosticsWindowPath = MENU_WINDOW;
    public final static String DiagnosticsActionId = "com.willwinder.ugs.nbp.core.windows.DiagnosticsTopComponent";
    public final static String DiagnosticsCategory = CATEGORY_WINDOW;

    public final static String RunFromTitleKey = "platform.menu.runFrom";
    public final static String RunFromTitle = Localization.getString(RunFromTitleKey, lang);
    public final static String RunFromWindowPath = MENU_PROGRAM;
    public final static String RunFromActionId = "com.willwinder.ugs.nbp.core.actions.RunFromAction";
    public final static String RunFromCategory = CATEGORY_MACHINE;

    public final static String ToolboxTitle = Localization.getString("platform.plugin.toolbox.title", lang);
    public final static String ToolboxTooltip = Localization.getString("platform.plugin.toolbox.tooltip", lang);


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
        ars.createAndLocalizeFullMenu(MENU_VISUALIZER,
                String.format("Menu/%s",
                        Localization.getString("platform.window.visualizer")));

        // Localize TopComponent Menu Items
        ars.overrideActionName(ActionsCategory, ActionsActionId, ActionsTitle);
        ars.overrideActionName(LocationStatusCategory, LocationStatusActionId, LocationStatusTitle);
        ars.overrideActionName(FileBrowserCategory, FileBrowserActionId, FileBrowserTitle);
        ars.overrideActionName(JogControlCategory, JogControlActionId, JogControlTitle);
        ars.overrideActionName(MacrosCategory, MacrosActionId, MacrosTitle);
        ars.overrideActionName(OverridesCategory, OverridesActionId, OverridesTitle);
        ars.overrideActionName(SendStatusCategory, SendStatusActionId, SendStatusTitle);
        ars.overrideActionName(SerialConsoleCategory, SerialConsoleActionId, SerialConsoleTitle);
        ars.overrideActionName(StateCategory, StateActionId, StateTitle);
        ars.overrideActionName(DiagnosticsCategory, DiagnosticsActionId, DiagnosticsTitle);
    }
}

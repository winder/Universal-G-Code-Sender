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

import com.willwinder.universalgcodesender.i18n.Localization;
import java.io.IOException;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author wwinder
 */
@ServiceProvider(service=LocalizingService.class) 
public class LocalizingService {
    public final static String VisualizerTitle = Localization.getString("platform.window.visualizer");
    public final static String VisualizerTooltip = Localization.getString("platform.window.visualizer.tooltip");
    public final static String VisualizerWindowPath = "Menu/Window";
    public final static String VisualizerActionId = "com.willwinder.ugs.nbm.visualizer.Visualizer2TopComponent";
    public final static String VisualizerCategory = "Window";

    public final static String SerialConsoleTitle = Localization.getString("platform.window.serialconsole");
    public final static String SerialConsoleTooltip = Localization.getString("platform.window.serialconsole.tooltip");
    public final static String SerialConsoleWindowPath = "Menu/Window";
    public final static String SerialConsoleActionId = "com.willwinder.universalgcodesender.nbp.console.SerialConsoleTopComponent";
    public final static String SerialConsoleCategory = "Window";

    public final static String SendStatusTitle = Localization.getString("platform.window.sendstatus");
    public final static String SendStatusTooltip = Localization.getString("platform.window.sendstatus.tooltip");
    public final static String SendStatusWindowPath = "Menu/Window/Classic";
    public final static String SendStatusActionId = "com.willwinder.ugs.nbp.core.control.SendStatusTopComponent";
    public final static String SendStatusCategory = "Window";

    public final static String OverridesTitle = Localization.getString("platform.window.overrides");
    public final static String OverridesTooltip = Localization.getString("platform.window.overrides.tooltip");
    public final static String OverridesWindowPath = "Menu/Window";
    public final static String OverridesActionId = "com.willwinder.universalgcodesender.nbp.control.OverridesTopComponent";
    public final static String OverridesCategory = "Window";
    
    public final static String MacrosTitle = Localization.getString("platform.window.macros");
    public final static String MacrosTooltip = Localization.getString("platform.window.macros.tooltip");
    public final static String MacrosWindowPath = "Menu/Window";
    public final static String MacrosActionId = "com.willwinder.ugs.nbp.core.control.MacrosTopComponent";
    public final static String MacrosCategory = "Window";

    public final static String JogControlTitle = Localization.getString("platform.window.jogcontrol");
    public final static String JogControlTooltip = Localization.getString("platform.window.jogcontrol.tooltip");
    public final static String JogControlWindowPath = "Menu/Window";
    public final static String JogControlActionId = "com.willwinder.ugs.nbp.core.control.JogControlTopComponent";
    public final static String JogControlCategory = "Window";

    public final static String FileBrowserTitle = Localization.getString("platform.window.filebrowser");
    public final static String FileBrowserTooltip = Localization.getString("platform.window.filebrowser.tooltip");
    public final static String FileBrowserWindowPath = "Menu/Window/Classic";
    public final static String FileBrowserActionId = "com.willwinder.ugs.nbp.core.filebrowser.FileBrowserTopComponentTopComponent";
    public final static String FileBrowserCategory = "Window";

    public final static String LocationStatusTitle = Localization.getString("platform.window.dro");
    public final static String LocationStatusTooltip = Localization.getString("platform.window.dro.tooltip");
    public final static String LocationStatusWindowPath = "Menu/Window";
    public final static String LocationStatusActionId = "com.willwinder.ugs.nbp.control.StatusTopComponent";
    public final static String LocationStatusCategory = "Window";

    public final static String ActionsTitle = Localization.getString("platform.window.actions");
    public final static String ActionsTooltip = Localization.getString("platform.window.actions.tooltip");
    public final static String ActionsWindowPath = "Menu/Window/Classic";
    public final static String ActionsActionId = "com.willwinder.ugs.nbp.control.ActionsTopComponent";
    public final static String ActionsCategory = "Window";

    public final static String WorkflowWindowTitle = Localization.getString("platform.window.workflow");
    public final static String WorkflowWindowTooltip = Localization.getString("platform.window.workflow.tooltip");
    public final static String WorkflowWindowWindowPath = "Menu/Window/Plugins";
    public final static String WorkflowWindowActionId = "com.willwinder.ugs.nbm.workflow.WorkflowWindowTopComponent";
    public final static String WorkflowWindowCategory = "Window";

    public LocalizingService() throws IOException {
        ActionRegistrationService ars =  Lookup.getDefault().lookup(ActionRegistrationService.class);
        // Menus
        ars.createAndLocalizeFullMenu("Menu/Window/Classic",
                String.format("Menu/%s/%s",
                        Localization.getString("platform.menu.window"),
                        Localization.getString("platform.menu.classic")));
        ars.createAndLocalizeFullMenu("Menu/Window/Plugins",
                String.format("Menu/%s/%s",
                        Localization.getString("platform.menu.window"),
                        Localization.getString("platform.menu.plugins")));

        // Actions
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
    }
}

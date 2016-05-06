/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
    }
}

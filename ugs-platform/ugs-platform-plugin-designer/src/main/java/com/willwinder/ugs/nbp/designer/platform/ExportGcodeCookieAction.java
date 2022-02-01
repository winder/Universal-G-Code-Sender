/*
    Copyright 2021 Will Winder

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
package com.willwinder.ugs.nbp.designer.platform;

import com.willwinder.ugs.nbp.designer.actions.ExportGcodeAction;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import org.openide.awt.*;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * An action for exporting a design as gcode. This type of action will make sure
 * this action will only be enabled when an designer file is loaded.
 * 
 * @author Joacim Breiler
 */
@ActionID(
        id = "com.willwinder.ugs.nbp.designer.actions.ExportGcodeAction",
        category = "File")
@ActionRegistration(
        iconBase = ExportGcodeAction.SMALL_ICON_PATH,
        displayName = "Export Gcode",
        lazy = false)
@ActionReferences({
        @ActionReference(
                path = LocalizingService.MENU_FILE,
                position = 330)
})
public class ExportGcodeCookieAction extends CookieAction {

    private final ExportGcodeAction action;

    /**
     * for layer registration
     */
    public ExportGcodeCookieAction() {
        setEnabled(false);
        putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        action = new ExportGcodeAction();
    }

    @Override
    public String getName() {
        return (String) action.getValue(Action.NAME);
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    protected String iconResource() {
        return ExportGcodeAction.SMALL_ICON_PATH;
    }

    @Override
    protected void performAction(Node[] nodes) {
        action.actionPerformed(new ActionEvent(this, 0, ""));
    }

    @Override
    protected int mode() {
        return CookieAction.MODE_ANY;
    }

    @Override
    protected Class<?>[] cookieClasses() {
        return new Class[]{UgsDataObject.class};
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}
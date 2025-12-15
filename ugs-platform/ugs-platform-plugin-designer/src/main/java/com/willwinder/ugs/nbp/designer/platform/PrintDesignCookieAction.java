/*
    Copyright 2025 Damian Nikodem

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

import com.willwinder.ugs.nbp.designer.actions.PrintDesignAction;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import org.openide.awt.*;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author Damian Nikodem
 */
@ActionID(
        id = "com.willwinder.ugs.nbp.designer.actions.PrintDesignAction",
        category = "File")
@ActionRegistration(
        iconBase = PrintDesignAction.SMALL_ICON_PATH,
        displayName = "Print",
        lazy = false)
@ActionReferences({
        @ActionReference(
                path = LocalizingService.MENU_FILE,
                position = 330)
})
public class PrintDesignCookieAction extends CookieAction {

    private final PrintDesignAction action;

    /**
     * for layer registration
     */
    public PrintDesignCookieAction() {
        setEnabled(false);
        putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        action = new PrintDesignAction();
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
        return PrintDesignAction.SMALL_ICON_PATH;
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
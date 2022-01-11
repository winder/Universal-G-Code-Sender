/*
 * Copyright (C) 2020 Will Winder
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.ugs.nbp.editor.actions;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.ActionReferenceLocalizer;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.services.RunFromService;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import org.netbeans.api.editor.EditorRegistry;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.modules.OnStart;

import javax.swing.text.Element;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@ActionID(
        category = LocalizingService.CATEGORY_MACHINE,
        id = "com.willwinder.ugs.nbp.editor.actions.RunFromHere"
)
@ActionRegistration(
        displayName = "Start program here."
)
@ActionReference(path = "Editors/text/xgcode/Popup", position = 400)
public final class RunFromHere implements ActionListener {

    public static final String NAME = Localization.getString("platform.menu.runFrom");
    private final RunFromService runFromService;

    public RunFromHere() {
        this.runFromService = CentralLookup.getDefault().lookup(RunFromService.class);
    }

    @OnStart
    public static class ActionLocalizer extends ActionReferenceLocalizer {
        public ActionLocalizer() {
            super(LocalizingService.CATEGORY_MACHINE, "RunFromHere", NAME);
        }
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        Element root = EditorRegistry.lastFocusedComponent().getDocument().getDefaultRootElement();
        int caretPosition = EditorRegistry.lastFocusedComponent().getCaretPosition();
        int line = root.getElementIndex(caretPosition) - 1;

        try {
            runFromService.runFromLine(line);
        } catch (Exception e) {
            GUIHelpers.displayErrorDialog(e.getLocalizedMessage());
        }
    }
}

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
import com.willwinder.universalgcodesender.gcode.GcodeParser;
import com.willwinder.universalgcodesender.gcode.processors.RunFromProcessor;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.cookies.EditorCookie;
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

    private final EditorCookie context;
    public static final String NAME = Localization.getString("platform.menu.runFrom");

    public RunFromHere(EditorCookie context) {
        this.context = context;
    }

    @OnStart
    public static class ActionLocalizer extends ActionReferenceLocalizer {
        public ActionLocalizer() {
            super(LocalizingService.CATEGORY_MACHINE, "RunFromHere", NAME);
        }
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        BackendAPI backend = CentralLookup.getDefault().lookup(BackendAPI.class);

        Element root = context.getDocument().getDefaultRootElement();
        int caretPosition = context.getOpenedPanes()[0].getCaretPosition();
        int line = root.getElementIndex(caretPosition) + 1;

        GcodeParser gcp = new GcodeParser();
        gcp.addCommandProcessor(new RunFromProcessor(line));

        try {
            backend.applyGcodeParser(gcp);
        } catch (Exception e) {
            GUIHelpers.displayErrorDialog(e.getLocalizedMessage());
        }
    }
}

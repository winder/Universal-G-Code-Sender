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
package com.willwinder.ugs.nbp.editor;

import com.willwinder.ugs.nbp.lib.options.AbstractOptionsPanelController;
import com.willwinder.universalgcodesender.uielements.helpers.AbstractUGSSettings;
import org.netbeans.spi.options.OptionsPanelController;

@OptionsPanelController.SubRegistration(
        id = "EditorOptions",
        location = "UGS/EditorOptions",
        displayName = "#platform.window.editor.settings",
        keywords = "#platform.window.editor.settings.keywords",
        keywordsCategory = "Advanced/EditorOptions",
        position = 10000
)
@org.openide.util.NbBundle.Messages({
        "platform.window.editor.settings=Editor",
        "platform.window.editor.settings.keywords=editor,gcode"})
public final class EditorOptionsPanelController extends AbstractOptionsPanelController {
    @Override
    public AbstractUGSSettings initPanel() {
        return new EditorOptionsPanel(settings, this);
    }
}
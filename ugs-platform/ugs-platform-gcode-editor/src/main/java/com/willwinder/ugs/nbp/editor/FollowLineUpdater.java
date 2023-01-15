/*
    Copyright 2023 Will Winder

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

import com.willwinder.ugs.nbp.editor.actions.FollowAction;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.openide.cookies.EditorCookie;
import org.openide.text.Line;

import java.util.prefs.Preferences;

/**
 * Updates the cursor in the editor to the current line of the completed gcode command.
 * The updater is activated by the {@link FollowAction}.
 *
 * @author Joacim Breiler
 */
public class FollowLineUpdater {
    private final Preferences preferences;

    public FollowLineUpdater() {
        preferences = MimeLookup.getLookup(MimePath.get(GcodeLanguageConfig.MIME_TYPE)).lookup(Preferences.class);
    }

    /**
     * Sets the current line and tries to update the editor cursor to that position.
     * If the line number is less than zero or if the follow l
     *
     * @param dataObject the current data object for
     * @param lineNumber the line number of the command that was completed
     */
    public void updateCurrentLine(GcodeDataObject dataObject, int lineNumber) {
        if (lineNumber < 0) {
            return;
        }

        if (!preferences.getBoolean(FollowAction.PREFERENCE_KEY, false)) {
            return;
        }

        EditorCookie ec = dataObject.getCookie(EditorCookie.class);
        if (ec == null) {
            return;
        }

        ec.getLineSet().getCurrent(lineNumber).show(Line.ShowOpenType.NONE, Line.ShowVisibilityType.NONE);
    }
}

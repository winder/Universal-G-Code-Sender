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
import org.netbeans.modules.editor.NbEditorKit;

import javax.swing.Action;
import javax.swing.text.TextAction;

/**
 * A special editor kit for gcode mime type. This is needed as a workaround to
 * load certain actions in the editor which can't be loaded using
 * {@link org.netbeans.api.editor.EditorActionRegistration}
 *
 * @author Joacim Breiler
 */
public class GcodeEditorKit extends NbEditorKit {

    @Override
    public String getContentType() {
        return GcodeLanguageConfig.MIME_TYPE;
    }

    @Override
    protected Action[] createActions() {
        Action[] editorActions = new Action[]{
                new FollowAction()
        };
        return TextAction.augmentList(super.createActions(), editorActions);
    }
}

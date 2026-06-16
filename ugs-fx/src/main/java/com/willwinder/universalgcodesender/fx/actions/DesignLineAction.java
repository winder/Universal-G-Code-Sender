/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.universalgcodesender.fx.actions;

import com.willwinder.ugs.designer.logic.Tool;
import com.willwinder.universalgcodesender.i18n.Localization;

public class DesignLineAction extends AbstractDesignToolAction {
    public static final String ICON_BASE = "icons/line.svg";

    public DesignLineAction() {
        super(Tool.LINE, Localization.getString("platform.designer.line"), ICON_BASE);
    }
}

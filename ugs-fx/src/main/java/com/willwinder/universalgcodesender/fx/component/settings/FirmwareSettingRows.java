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
package com.willwinder.universalgcodesender.fx.component.settings;

import com.willwinder.universalgcodesender.firmware.FirmwareSetting;
import javafx.scene.control.TreeItem;

/**
 * The firmware settings are presented in a tree where the group headings are the only rows that have
 * children.
 */
final class FirmwareSettingRows {

    private FirmwareSettingRows() {
        // Can not be instanced
    }

    static boolean isGroupHeading(TreeItem<FirmwareSetting> treeItem) {
        return treeItem != null && !treeItem.getChildren().isEmpty();
    }
}

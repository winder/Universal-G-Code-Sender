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
package com.willwinder.universalgcodesender.fx.service;

import com.willwinder.ugs.designer.logic.ToolLibraryService;

/**
 * Holds the one tool library the UGS FX application reads, so that the visualizer and the designer
 * do not each open and watch the library file.
 */
public final class ToolLibraryProvider {
    private static volatile ToolLibraryService instance;

    private ToolLibraryProvider() {
    }

    public static ToolLibraryService getInstance() {
        ToolLibraryService result = instance;
        if (result == null) {
            synchronized (ToolLibraryProvider.class) {
                result = instance;
                if (result == null) {
                    result = new ToolLibraryService();
                    instance = result;
                }
            }
        }
        return result;
    }
}

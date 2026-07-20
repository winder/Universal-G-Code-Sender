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
package com.willwinder.universalgcodesender.fx.component.designer;

/**
 * Guards against feedback loops while the settings panel writes values back into its own controls.
 * While a guarded write is running, field listeners and entity-change listeners can check
 * {@link #isActive()} and skip reacting to a change the panel itself just made.
 */
class EditGuard {
    private boolean active;

    boolean isActive() {
        return active;
    }

    void run(Runnable body) {
        boolean previous = active;
        active = true;
        try {
            body.run();
        } finally {
            active = previous;
        }
    }
}

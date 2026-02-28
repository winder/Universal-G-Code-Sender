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
package com.willwinder.universalgcodesender.fx;

import com.willwinder.ugs.cli.TerminalClient;

import java.io.IOException;

/**
 * This starter class is needed to start the application from the IDE
 */
public class Launcher {
    public static void main(String[] args) throws IOException {
        if (args.length == 0 || !args[0].startsWith("-")) {
            Main.main(args);
            return;
        }

        TerminalClient.main(args);
    }
}

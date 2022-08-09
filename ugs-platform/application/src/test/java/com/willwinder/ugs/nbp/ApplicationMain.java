/*
    Copyright 2022 Will Winder

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
package com.willwinder.ugs.nbp;

import org.netbeans.Main;

import java.io.File;

/**
 * A test main for starting the application
 *
 * @author Joacim Breiler
 */
public class ApplicationMain {
    public static void main(String[] args) throws Exception {
        File userDirectory = new File("ugs-platform/application/target/userdir");
        if (!userDirectory.exists()) {
            userDirectory.mkdir();
        }

        System.setProperty("netbeans.logger.console", "true");
        System.setProperty("netbeans.user", userDirectory.getAbsolutePath());
        System.setProperty("netbeans.home", new File("ugs-platform/application/target/ugsplatform/platform").getAbsolutePath());
        System.setProperty("netbeans.dirs",
                new File("ugs-platform/application/target/ugsplatform/platform").getAbsolutePath() + File.pathSeparator +
                        new File("ugs-platform/application/target/ugsplatform/java").getAbsolutePath() + File.pathSeparator +
                        new File("ugs-platform/application/target/ugsplatform/testplatform").getAbsolutePath() + File.pathSeparator +
                        new File("ugs-platform/application/target/ugsplatform/extra").getAbsolutePath());
        Main.main(new String[]{"--branding", "ugsplatform"});
    }
}

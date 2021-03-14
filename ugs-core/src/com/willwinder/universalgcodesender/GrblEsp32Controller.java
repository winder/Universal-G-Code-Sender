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
package com.willwinder.universalgcodesender;

import java.util.Optional;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.willwinder.universalgcodesender.CapabilitiesConstants.*;

public class GrblEsp32Controller extends GrblController {
    private final Capabilities capabilities = new Capabilities();
    private static final Logger logger = Logger.getLogger(GrblEsp32Controller.class.getName());
    static Pattern axisCountPattern = Pattern.compile("\\[MSG:Axis count (\\d*)]");

    public GrblEsp32Controller() {
        super();
        this.capabilities.addCapability(GrblCapabilitiesConstants.V1_FORMAT);
    }

    Optional<Integer> getAxisCount(String response) {
        Matcher m = axisCountPattern.matcher(response);
        if (!m.find()) {
            return Optional.empty();
        }
        return Optional.of(Integer.parseInt(m.group(1)));
    }

    @Override
    public Capabilities getCapabilities() {
        return capabilities.merge(super.getCapabilities());
    }

    @Override
    protected void openCommAfterEvent() {
        // This doesn't seem to be required, but it's an option.
        //this.comm.queueCommand(new GcodeCommand("[ESP444]RESTART"));
        //this.comm.streamCommands();
    }

    @Override
    protected void rawResponseHandler(String response) {
        /*
        [MSG:Grbl_ESP32 Ver 1.3a Date 20210203]
        [MSG:Compiled with ESP32 SDK:v3.2.3-14-gd3e562907]
        [MSG:Using machine:Test Drive - Demo Only No I/O!]
        [MSG:Axis count 6]
        [MSG:Timed Steps]
        [MSG:Init Motors]
        [MSG:No spindle]

        [MSG:Local access point GRBL_ESP started, 192.168.4.1]
        [MSG:Captive Portal Started]
        [MSG:HTTP Started]
        [MSG:TELNET Started 23]
         */

        Optional<Integer> axes = getAxisCount(response);
        if (axes.isPresent()) {
            logger.info("Axis Count: " + axes.get());
            this.capabilities.removeCapability(X_AXIS);
            this.capabilities.removeCapability(Y_AXIS);
            this.capabilities.removeCapability(Z_AXIS);
            this.capabilities.removeCapability(A_AXIS);
            this.capabilities.removeCapability(B_AXIS);
            this.capabilities.removeCapability(C_AXIS);

            switch(axes.get()) {
                case 6:
                    this.capabilities.addCapability(C_AXIS);
                case 5:
                    this.capabilities.addCapability(B_AXIS);
                case 4:
                    this.capabilities.addCapability(A_AXIS);
                case 3:
                    this.capabilities.addCapability(Z_AXIS);
                case 2:
                    this.capabilities.addCapability(Y_AXIS);
                case 1:
                    this.capabilities.addCapability(X_AXIS);
            }
        }

        super.rawResponseHandler(response);
    }
}

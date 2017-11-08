/**
 * Handles incoming commands from the pendant client.
 */

/*
    Copywrite 2016 Will Winder

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
package com.willwinder.ugs.platform.plugin.pendant;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.services.JogService;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PendantCommandHandler {

    private static final Logger logger = Logger.getLogger(PendantCommandHandler.class.getName());

    private final BackendAPI backend;
    private final JogService jogService;

    public PendantCommandHandler() {
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        jogService = CentralLookup.getDefault().lookup(JogService.class);
    }

    public void handleCommand(String command) {
        logger.log(Level.INFO, "handleCommand({0})", command);
        try {
            PendantCommand pc = PendantCommand.valueOf(command);
            switch (pc) {
                case INCREMENT_Y:
                    jogXY(0, 1);
                    break;
                case DECREMENT_Y:
                    jogXY(0, -1);
                    break;
                case INCREMENT_X:
                    jogXY(1, 0);
                    break;
                case DECREMENT_X:
                    jogXY(-1, 0);
                    break;
                case DECREMENT_Z:
                    jogZ(-1);
                    break;
                case INCREMENT_Z:
                    jogZ(1);
                    break;
                case TOGGLE_UNITS:
                    toggleUnits();
                    break;
                case RESET_X:
                    backend.resetCoordinateToZero('X');
                    break;
                case RESET_Y:
                    backend.resetCoordinateToZero('Y');
                    break;
                case RESET_Z:
                    backend.resetCoordinateToZero('Z');
                    break;
                default:
                    logger.info("Unrecognized command: " + command);
                    break;

            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Exception in pendant server.", e);
            logger.warning(Localization.getString("SendGcodeHandler"));
        }
    }

    private void jogZ(int z) {
        jogService.adjustManualLocationZ(z);
    }

    private void jogXY(int x, int y) {
        jogService.adjustManualLocationXY(x, y);
    }

    private void toggleUnits() {
        if (jogService.getUnits() == UnitUtils.Units.MM) {
            jogService.setUnits(UnitUtils.Units.INCH);
        } else {
            jogService.setUnits(UnitUtils.Units.MM);
        }
    }

    private boolean isManualControlEnabled() {
        switch (backend.getControlState()) {
            case COMM_DISCONNECTED:
                return false;
            case COMM_IDLE:
                return true;
            case COMM_SENDING:
                return false;
            default:
                return true;
        }
    }
}

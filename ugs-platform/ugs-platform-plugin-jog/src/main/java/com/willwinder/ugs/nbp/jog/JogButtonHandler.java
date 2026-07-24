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
package com.willwinder.ugs.nbp.jog;

import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.services.JogService;
import com.willwinder.universalgcodesender.utils.ContinuousJogWorker;

/**
 * Handles jog button events by translating them into single jog steps or continuous jogging. A short click
 * performs a single jog step while a long press starts a continuous jog that stops when the button is
 * released.
 *
 * @author Joacim Breiler
 */
public class JogButtonHandler {
    private final BackendAPI backend;
    private final JogService jogService;
    private final ContinuousJogWorker continuousJogWorker;

    private boolean ignoreLongClick = false;

    public JogButtonHandler(BackendAPI backend, JogService jogService) {
        this.backend = backend;
        this.jogService = jogService;
        this.continuousJogWorker = new ContinuousJogWorker(backend, jogService);
    }

    /**
     * Registers the handler so that it can send continuous jog commands.
     */
    public void init() {
        continuousJogWorker.init();
    }

    /**
     * Stops any ongoing continuous jog and unregisters the handler.
     */
    public void destroy() {
        continuousJogWorker.destroy();
    }

    public void onJogButtonClicked(JogPanelButtonEnum button) {
        // Ignore the "click" event when a long button press is released
        if (ignoreLongClick) {
            ignoreLongClick = false;
            return;
        }

        switch (button) {
            case BUTTON_XNEG:
                jogService.adjustManualLocationXY(-1, 0);
                break;
            case BUTTON_XPOS:
                jogService.adjustManualLocationXY(1, 0);
                break;
            case BUTTON_YNEG:
                jogService.adjustManualLocationXY(0, -1);
                break;
            case BUTTON_YPOS:
                jogService.adjustManualLocationXY(0, 1);
                break;
            case BUTTON_DIAG_XNEG_YNEG:
                jogService.adjustManualLocationXY(-1, -1);
                break;
            case BUTTON_DIAG_XNEG_YPOS:
                jogService.adjustManualLocationXY(-1, 1);
                break;
            case BUTTON_DIAG_XPOS_YNEG:
                jogService.adjustManualLocationXY(1, -1);
                break;
            case BUTTON_DIAG_XPOS_YPOS:
                jogService.adjustManualLocationXY(1, 1);
                break;
            case BUTTON_ZNEG:
                jogService.adjustManualLocationZ(-1);
                break;
            case BUTTON_ZPOS:
                jogService.adjustManualLocationZ(1);
                break;
            case BUTTON_ANEG:
                jogService.adjustManualLocationABC(-1, 0, 0);
                break;
            case BUTTON_APOS:
                jogService.adjustManualLocationABC(1, 0, 0);
                break;
            case BUTTON_BNEG:
                jogService.adjustManualLocationABC(0, -1, 0);
                break;
            case BUTTON_BPOS:
                jogService.adjustManualLocationABC(0, 1, 0);
                break;
            case BUTTON_CNEG:
                jogService.adjustManualLocationABC(0, 0, -1);
                break;
            case BUTTON_CPOS:
                jogService.adjustManualLocationABC(0, 0, 1);
                break;
            default:
        }
    }

    public void onJogButtonLongPressed(JogPanelButtonEnum button) {
        if (backend.getController().getCapabilities().hasContinuousJogging()) {
            // set flag so when we release the long press we don't add
            // an extra jog step through the click event
            ignoreLongClick = true;

            continuousJogWorker.setDirection(button.getX(), button.getY(), button.getZ(), button.getA(), button.getB(), button.getC());
            continuousJogWorker.start();
        }
    }

    public void onJogButtonLongReleased() {
        continuousJogWorker.stop();
    }
}

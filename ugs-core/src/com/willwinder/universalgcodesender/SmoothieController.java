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
package com.willwinder.universalgcodesender;

import com.willwinder.universalgcodesender.i18n.Localization;

/**
 *
 * @author wwinder
 */
final public class SmoothieController extends GrblController {

    public SmoothieController() {
        super(new SmoothieCommunicator());
        setSingleStepMode(true);
    }

    @Override
    public Boolean handlesAllStateChangeEvents() {
        return false;
    }

    @Override
    public void setSingleStepMode(boolean ignored) {
        super.setSingleStepMode(true);
    }

    @Override
    protected void openCommAfterEvent() throws Exception {
        //this.sendCommandImmediately(createCommand("version"));
    }

    @Override
    protected void isReadyToSendCommandsEvent() throws Exception {
        if (this.isReady == false) {
            throw new Exception(Localization.getString("controller.smoothie.exception.booting"));
        }
    }

    @Override
    protected void rawResponseHandler(String response) {
        if (response.contains("Smoothie")){
              this.isReady = true;
        }
        else {
            super.rawResponseHandler(response);
        }
    }

    @Override
    protected Boolean isIdleEvent() {
        // Let abstract controller decide
        return true;
    }
}
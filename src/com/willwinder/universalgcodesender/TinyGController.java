/*
 * TinyG Control layer, coordinates all aspects of control.
 */

/*
    Copywrite 2013 Will Winder

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

import com.willwinder.universalgcodesender.gcode.TinyGGcodeCommandCreator;
import com.willwinder.universalgcodesender.types.TinyGGcodeCommand;
import java.io.IOException;
import java.util.Collection;

/**
 *
 * @author wwinder
 */
public class TinyGController extends AbstractController {

    protected TinyGController(TinyGCommunicator comm) {
        super(comm);
        
        this.commandCreator = new TinyGGcodeCommandCreator();
        //this.positionPollTimer = createPositionPollTimer();
    }
    
    public TinyGController() {
        this(new TinyGCommunicator()); //f4grx: connection created at opencomm() time
    }

    @Override
    public long getJobLengthEstimate(Collection<String> jobLines) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void closeCommBeforeEvent() {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void closeCommAfterEvent() {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void cancelSendBeforeEvent() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void cancelSendAfterEvent() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void pauseStreamingEvent() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void resumeStreamingEvent() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void rawResponseHandler(String response) {
        if (TinyGGcodeCommand.isOkErrorResponse(response)) {
            this.messageForConsole(response + "\n");
        }
        /*
        // boot information check?
        else if (GrblUtils.isGrblVersionString(response)) {

        }
        // position / status info?
        else if (GrblUtils.isGrblStatusString(response)) {

        }
        */
        else {
            // Display any unhandled messages
            this.messageForConsole(response + "\n");
        }
    }

    @Override
    public void performHomingCycle() throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public void resetCoordinatesToZero() throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public void returnToHome() throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void killAlarmLock() throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void toggleCheckMode() throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void viewParserState() throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void softReset() throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void isReadyToStreamFileEvent() throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void statusUpdatesEnabledValueChanged(boolean enabled) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void statusUpdatesRateValueChanged(int rate) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

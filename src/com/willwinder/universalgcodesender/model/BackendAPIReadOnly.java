/**
 * Read only API used by front ends to interface with the model.
 */

/*
    Copywrite 2015 Will Winder

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
package com.willwinder.universalgcodesender.model;

import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.listeners.ControlStateListener;
import com.willwinder.universalgcodesender.utils.Settings;
import com.willwinder.universalgcodesender.Utils;
import com.willwinder.universalgcodesender.model.Utils.ControlState;
import com.willwinder.universalgcodesender.pendantui.SystemStateBean;
import java.io.File;

/**
 *
 * @author wwinder
 */
public interface BackendAPIReadOnly {
    public void addControlStateListener(ControlStateListener listener);
    public void addControllerListener(ControllerListener listener);
    
    // Config options
    public File getFile();

    // Controller status
    public boolean isConnected();
    public boolean isSending();
    public boolean isPaused();
    public boolean canPause();
    public boolean canCancel();
    public boolean canSend();
    public ControlState getControlState();

    // Send status
    public long getNumRows();
    public long getNumSentRows();
    public long getNumRemainingRows();

    public long getSendDuration();
    public long getSendRemainingDuration();
    public String getPauseResumeText();

    // Bulk status getter.
    public void updateSystemState(SystemStateBean systemStateBean);	

    // Shouldn't be needed often.
    public Settings getSettings();
}

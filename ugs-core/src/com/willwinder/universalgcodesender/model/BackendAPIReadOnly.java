/*
    Copyright 2015-2018 Will Winder

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

import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.listeners.MessageListener;
import com.willwinder.universalgcodesender.utils.Settings;

import java.io.File;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;

/**
 * Read only API used by front ends to interface with the model.
 *
 * @author wwinder
 */
public interface BackendAPIReadOnly {

    /**
     * Contains all common GUI events: state changes, probe events,
     * settings changes and file changes.
     *
     * @param listener the listener to be added
     */
    void addUGSEventListener(UGSEventListener listener);

    /**
     * Removes a UGSEvent listener
     *
     * @param listener the listener to be removed
     */
    void removeUGSEventListener(UGSEventListener listener);

    /**
     * Adds a listener that will receive all messages that should be written to the console
     *
     * @param listener the listener to be added
     */
    void addMessageListener(MessageListener listener);

    /**
     * Removes a listener for console messages
     *
     * @parame listener the listener to be removed
     */
    void removeMessageListener(MessageListener listener);

    // Config options
    File getGcodeFile();
    File getProcessedGcodeFile();

    // Controller status
    boolean isConnected();
    boolean isSendingFile();
    boolean isIdle();
    boolean isPaused();
    boolean canPause();
    boolean canCancel();
    boolean canSend();
    CommunicatorState getControlState();
    Position getWorkPosition();
    Position getMachinePosition();
    GcodeState getGcodeState();

    // Send status
    long getNumRows();
    long getNumSentRows();
    long getNumRemainingRows();
    long getNumCompletedRows();

    long getSendDuration();
    long getSendRemainingDuration();
    String getPauseResumeText();

    // Shouldn't be needed often.
    Settings getSettings();
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.universalgcodesender;

import com.willwinder.universalgcodesender.gcode.GcodeCommandCreator;
import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.model.Utils.Units;
import com.willwinder.universalgcodesender.types.GcodeCommand;

import java.util.Collection;

/**
 *
 * @author will
 */
public interface IController {
    /*
    Observable
    */
    public void addListener(ControllerListener cl);

    /*
    State updates.
    */
    public void currentUnits(Units units);
    
    /*
    Actions
    */
    public void performHomingCycle() throws Exception;
    public void returnToHome() throws Exception;
    public void resetCoordinatesToZero() throws Exception;
    public void resetCoordinateToZero(final char coord) throws Exception;
    public void killAlarmLock() throws Exception;
    public void toggleCheckMode() throws Exception;
    public void viewParserState() throws Exception;
    public void issueSoftReset() throws Exception;

    /*
    Behavior
    */
    public void setSingleStepMode(boolean enabled);
    public boolean getSingleStepMode();

    public void setStatusUpdatesEnabled(boolean enabled);
    public boolean getStatusUpdatesEnabled();
    
    public void setStatusUpdateRate(int rate);
    public int getStatusUpdateRate();
    
    public GcodeCommandCreator getCommandCreator();
    public long getJobLengthEstimate(Collection<String> jobLines);
    
    /*
    Serial
    */
    public Boolean openCommPort(String port, int portRate) throws Exception;
    public Boolean closeCommPort() throws Exception;
    public Boolean isCommOpen();
    
    /*
    Stream information
    */
    public Boolean isReadyToStreamFile() throws Exception;
    public Boolean isStreamingFile();
    public long getSendDuration();
    public int rowsInQueue();
    public int rowsInSend();
    public int rowsSent();
    public int rowsRemaining();
    
    /*
    Stream control
    */
    public void beginStreaming() throws Exception;
    public void pauseStreaming() throws Exception;
    public void resumeStreaming() throws Exception;
    public void cancelSend();

    /*
    Stream content
    */
    public GcodeCommand createCommand(String gcode) throws Exception;
    public void sendCommandImmediately(GcodeCommand cmd) throws Exception;
    public void queueCommand(GcodeCommand cmd) throws Exception;
    public void queueCommands(Iterable<String> commandStrings) throws Exception;
//
//    public String getDistanceModeCode();
//    public String getUnitsCode();
    public void restoreParserModalState();
    public void updateParserModalState(GcodeCommand command);
}

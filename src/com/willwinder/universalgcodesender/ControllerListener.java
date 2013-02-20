/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.universalgcodesender;

import javax.vecmath.Point3d;

/**
 *
 * @author Owen
 */
public interface ControllerListener {
    void fileStreamComplete(String filename, boolean success);
    void commandQueued(GcodeCommand command);
    void commandSent(GcodeCommand command);
    void commandComplete(GcodeCommand command);
    void commandComment(String comment);
    void messageForConsole(String msg, Boolean verbose);
    void statusStringListener(String state, Point3d machineCoord, Point3d workCoord);
    
    //void verboseMessageForConsole(String msg);
    //void capabilitiesListener(CommUtils.Capabilities capability);
    //String preprocessCommand(String command);
}
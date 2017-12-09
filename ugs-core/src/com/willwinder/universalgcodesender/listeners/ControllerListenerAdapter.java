package com.willwinder.universalgcodesender.listeners;

import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.types.GcodeCommand;

public class ControllerListenerAdapter implements ControllerListener {

    @Override
    public void controlStateChange(UGSEvent.ControlState state) {}

    @Override
    public void fileStreamComplete(String filename, boolean success) {}

    @Override
    public void commandSkipped(GcodeCommand command) {}

    @Override
    public void commandSent(GcodeCommand command) {}

    @Override
    public void commandComplete(GcodeCommand command) {}

    @Override
    public void commandComment(String comment) {}

    @Override
    public void probeCoordinates(Position p) {}

    @Override
    public void messageForConsole(MessageType type, String msg) {}

    @Override
    public void statusStringListener(ControllerStatus status) {}

    @Override
    public void postProcessData(int numRows) {}
}

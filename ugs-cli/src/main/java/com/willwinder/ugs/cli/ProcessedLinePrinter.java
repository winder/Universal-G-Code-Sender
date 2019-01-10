package com.willwinder.ugs.cli;

import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.model.Alarm;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import org.apache.commons.lang3.StringUtils;

public class ProcessedLinePrinter implements ControllerListener {

    @Override
    public void controlStateChange(UGSEvent.ControlState state) {
        System.out.println(state);
    }

    @Override
    public void fileStreamComplete(String filename, boolean success) {

    }

    @Override
    public void receivedAlarm(Alarm alarm) {
        System.err.println("Alarm: " + alarm.name());

    }

    @Override
    public void commandSkipped(GcodeCommand command) {
        System.out.println("#" + command.getCommandNumber() + " - " + command.getOriginalCommandString() + " [skipped]");
    }

    @Override
    public void commandSent(GcodeCommand command) {
        if(command.getCommandNumber() > 0 ) {
            System.out.println("#" + command.getCommandNumber() + " - " + command.getOriginalCommandString());
        }
    }

    @Override
    public void commandComplete(GcodeCommand command) {
        if(command.getCommandNumber() > 0 && !StringUtils.equalsIgnoreCase(command.getResponse(), "ok")) {
            System.err.println("#" + command.getCommandNumber() + " - " + command.getOriginalCommandString() + " [" + command.getResponse() + "]");
        }
    }

    @Override
    public void commandComment(String comment) {

    }

    @Override
    public void probeCoordinates(Position p) {

    }

    @Override
    public void statusStringListener(ControllerStatus status) {
    }
}

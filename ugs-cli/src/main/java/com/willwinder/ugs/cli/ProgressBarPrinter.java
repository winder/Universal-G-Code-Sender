package com.willwinder.ugs.cli;

import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.Alarm;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;

/**
 * Displays the file send progress as a progress bar
 */
public class ProgressBarPrinter implements ControllerListener, UGSEventListener {
    private ProgressBar pb;
    private final BackendAPI backend;

    public ProgressBarPrinter(BackendAPI backend) {
        this.backend = backend;
    }

    @Override
    public void controlStateChange(UGSEvent.ControlState state) {

    }

    @Override
    public void fileStreamComplete(String filename, boolean success) {
        if (pb != null) {
            pb.maxHint(backend.getNumRows());
            pb.stepTo(backend.getNumCompletedRows());
            pb.close();
            pb = null;
        }
    }

    @Override
    public void receivedAlarm(Alarm alarm) {

    }

    @Override
    public void commandSkipped(GcodeCommand command) {
        commandComplete(command);
    }

    @Override
    public void commandSent(GcodeCommand command) {
        commandComplete(command);
    }

    @Override
    public void commandComplete(GcodeCommand command) {
        if (pb != null) {
            pb.maxHint(backend.getNumRows());
            pb.stepTo(backend.getNumCompletedRows());
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

    @Override
    public void UGSEvent(UGSEvent event) {
        if (event.isFileChangeEvent() && event.getFileState() == UGSEvent.FileState.FILE_LOADED) {
            pb = new ProgressBarBuilder()
                    .setStyle(ProgressBarStyle.UNICODE_BLOCK)
                    .setInitialMax(100)
                    .setTaskName(backend.getGcodeFile().getName())
                    .setPrintStream(System.out)
                    .build();

        }
    }
}

package com.willwinder.universalgcodesender.services;

import com.willwinder.universalgcodesender.gcode.processors.TranslateProcessor;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.UnitUtils;

public class TranslateModelService implements UGSEventListener {
    private final BackendAPI backend;
    private TranslateProcessor translateProcessor = new TranslateProcessor();

    public TranslateModelService(BackendAPI backend) {
        this.backend = backend;
        try {
            this.backend.applyCommandProcessor(translateProcessor);
        } catch (Exception e) {
            // Never mind this
        }
        this.backend.addUGSEventListener(this);
    }

    public void translate(Position offset) {
        translateProcessor.setOffset(offset);

        try {
            this.backend.applyCommandProcessor(translateProcessor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Position getOffset() {
        return translateProcessor.getOffset();
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt.isFileChangeEvent() && evt.getFileState() == UGSEvent.FileState.OPENING_FILE) {
            translateProcessor.setOffset(new Position(0, 0, 0, UnitUtils.Units.MM));
        }
    }
}

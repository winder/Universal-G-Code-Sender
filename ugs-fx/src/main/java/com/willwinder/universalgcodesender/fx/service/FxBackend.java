/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.universalgcodesender.fx.service;

import com.willwinder.universalgcodesender.gcode.processors.CommandProcessor;
import com.willwinder.universalgcodesender.model.GUIBackend;
import com.willwinder.universalgcodesender.model.UGSEventDispatcher;
import javafx.application.Platform;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The backend of the JavaFX edition. Processing a program can take many seconds for a large one,
 * and the backend does it whenever a file is opened, the controller connects, the processors
 * change or a run-from line is set. Here all of that runs on one loader thread: a call from the
 * JavaFX thread returns at once and the outcome arrives through the backend's events, while a
 * call from any other thread waits for its turn and completes before returning, as the plain
 * backend would. The single thread also keeps the processing steps from overlapping.
 */
public class FxBackend extends GUIBackend {
    private static final Logger LOGGER = Logger.getLogger(FxBackend.class.getName());

    private final ExecutorService loader = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "gcode-loader");
        thread.setDaemon(true);
        return thread;
    });
    private volatile Thread loaderThread;

    public FxBackend(UGSEventDispatcher eventDispatcher) {
        super(eventDispatcher);
        loader.execute(() -> loaderThread = Thread.currentThread());
    }

    @Override
    public void setGcodeFile(File file) throws Exception {
        process("load " + file.getName(), () -> super.setGcodeFile(file));
    }

    @Override
    public void reloadGcodeFile() throws Exception {
        process("reload the program", super::reloadGcodeFile);
    }

    @Override
    public void reloadGcodeProcessors() throws Exception {
        process("reload the processors", super::reloadGcodeProcessors);
    }

    @Override
    public void applyCommandProcessor(CommandProcessor commandProcessor) throws Exception {
        process("apply a processor", () -> super.applyCommandProcessor(commandProcessor));
    }

    @Override
    public void removeCommandProcessor(CommandProcessor commandProcessor) throws Exception {
        process("remove a processor", () -> super.removeCommandProcessor(commandProcessor));
    }

    private void process(String description, Processing processing) throws Exception {
        if (Thread.currentThread() == loaderThread) {
            processing.run();
        } else if (Platform.isFxApplicationThread()) {
            loader.execute(() -> {
                try {
                    processing.run();
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Could not " + description, e);
                }
            });
        } else {
            Future<?> result = loader.submit(() -> {
                processing.run();
                return null;
            });
            try {
                result.get();
            } catch (InterruptedException e) {
                // The caller gave up waiting, so the processing is abandoned along with it.
                result.cancel(true);
                throw e;
            } catch (ExecutionException e) {
                throw e.getCause() instanceof Exception cause ? cause : e;
            }
        }
    }

    @FunctionalInterface
    private interface Processing {
        void run() throws Exception;
    }
}

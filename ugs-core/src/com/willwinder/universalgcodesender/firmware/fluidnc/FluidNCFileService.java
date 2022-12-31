/*
    Copyright 2022 Will Winder

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
package com.willwinder.universalgcodesender.firmware.fluidnc;

import com.willwinder.universalgcodesender.model.File;
import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.IFileService;
import com.willwinder.universalgcodesender.StatusPollTimer;
import com.willwinder.universalgcodesender.firmware.fluidnc.commands.DeleteFileCommand;
import com.willwinder.universalgcodesender.firmware.fluidnc.commands.DownloadFileCommand;
import com.willwinder.universalgcodesender.firmware.fluidnc.commands.ListFilesCommand;
import com.willwinder.universalgcodesender.firmware.fluidnc.commands.UploadFileCommand;

import java.io.IOException;
import java.util.List;

import static com.willwinder.universalgcodesender.utils.ControllerUtils.sendAndWaitForCompletion;
import static com.willwinder.universalgcodesender.utils.ControllerUtils.waitOnActiveCommands;

public class FluidNCFileService implements IFileService {
    private final IController controller;
    private final StatusPollTimer statusPollTimer;

    public FluidNCFileService(IController controller, StatusPollTimer statusPollTimer) {
        this.controller = controller;
        this.statusPollTimer = statusPollTimer;
    }

    @Override
    public byte[] downloadFile(File file) throws IOException {
        try {
            statusPollTimer.stop();
            DownloadFileCommand command = sendAndWaitForCompletion(controller, new DownloadFileCommand(file));
            if (command.isError()) {
                throw new IOException("Could not download the file: " + file.getAbsolutePath());
            }
            return controller.getCommunicator().xmodemReceive();
        } catch (Exception e) {
            throw new IOException("Couldn't download file " + file.getAbsolutePath(), e);
        } finally {
            statusPollTimer.start();
        }
    }

    @Override
    public void uploadFile(String filename, byte[] data) throws IOException {
        try {
            statusPollTimer.stop();

            // If the file name has not been specified with a filesystem (SD or localfs) prepend a default one.
            if (!filename.startsWith("/")) {
                filename = "/localfs/" + filename;
            }

            controller.sendCommandImmediately(new UploadFileCommand(filename));
            controller.getCommunicator().xmodemSend(data);
            waitOnActiveCommands(controller);
        } catch (Exception e) {
            throw new IOException("Couldn't upload file " + filename, e);
        } finally {
            statusPollTimer.start();
        }
    }

    @Override
    public void deleteFile(File file) throws IOException {
        try {
            statusPollTimer.stop();
            sendAndWaitForCompletion(controller, new DeleteFileCommand(file.getName()));
        } catch (Exception e) {
            throw new IOException("Couldn't delete file " + file.getAbsolutePath(), e);
        } finally {
            statusPollTimer.start();
        }
    }

    @Override
    public List<File> getFiles() throws IOException {
        try {
            ListFilesCommand command = sendAndWaitForCompletion(controller, new ListFilesCommand(), 4000);
            return command.getFiles();
        } catch (Exception e) {
            throw new IOException("Couldn't get file list", e);
        }
    }
}

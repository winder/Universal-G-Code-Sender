/*
    Copyright 2023 Will Winder

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
package com.willwinder.universalgcodesender.pendantui.v1.resources;

import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.services.RunFromService;
import com.willwinder.universalgcodesender.services.SendProgressService;
import com.willwinder.universalgcodesender.pendantui.v1.model.FileStatus;
import com.willwinder.universalgcodesender.pendantui.v1.model.WorkspaceFileList;
import com.willwinder.universalgcodesender.services.BackendFileLoader;
import com.willwinder.universalgcodesender.services.FileLoader;
import com.willwinder.universalgcodesender.services.LookupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

@Tag(name = "Files", description = "Endpoints for loading files and handling files")
@Path("/files")
public class FilesResource {

    @Inject
    private BackendAPI backendAPI;

    private final SendProgressService sendProgress = LookupService.lookup(SendProgressService.class);

    @POST
    @Path("uploadAndOpen")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Operation(summary = "Upload a file and open it")
    public void open(
            @FormDataParam("file") FormDataContentDisposition disposition, @FormDataParam("file") File file) throws Exception {
        String originalFileName = disposition.getFileName();
        File renamedFile = new File(file.getParentFile(), originalFileName);
        if (!file.renameTo(renamedFile)) {
            Files.copy(file.toPath(), renamedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            file.delete();
        }

        FileLoader fileLoader = LookupService.lookupOptional(FileLoader.class)
                .orElseGet(() -> new BackendFileLoader(backendAPI));
        fileLoader.openFile(renamedFile);
    }

    @POST
    @Path("send")
    @Produces(MediaType.APPLICATION_JSON)
    public void send() throws Exception {
        if (backendAPI.isPaused()) {
            backendAPI.pauseResume();
        } else {
            backendAPI.send();
        }
    }

    @POST
    @Path("runFromLine")
    @Operation(summary = "Prepare the currently loaded file to start streaming from a given line, skipping " +
            "everything before it while replaying position/spindle/coolant/work offset state - " +
            "does not itself start anything, a separate call to send() does that")
    public void runFromLine(@QueryParam("line") int line) {
        LookupService.lookup(RunFromService.class).runFromLine(line);
    }

    @GET
    @Path("pause")
    @Produces(MediaType.APPLICATION_JSON)
    public void pause() throws Exception {
        if (!backendAPI.isPaused()) {
            backendAPI.pauseResume();
        }
    }

    @GET
    @Path("cancel")
    @Produces(MediaType.APPLICATION_JSON)
    public void cancel() throws Exception {
        backendAPI.cancel();
    }

    @GET
    @Path("getWorkspaceFileList")
    @Produces(MediaType.APPLICATION_JSON)
    public WorkspaceFileList getWorkspaceFileList() {
        List<String> workspaceFileList = backendAPI.getWorkspaceFileList();
        WorkspaceFileList result = new WorkspaceFileList();
        result.setFileList(workspaceFileList);
        return result;
    }

    @POST
    @Path("openWorkspaceFile")
    public void openWorkspaceFile(@QueryParam("file") String file) throws Exception {
        backendAPI.openWorkspaceFile(file);
    }

    @GET
    @Path("getFileStatus")
    @Produces(MediaType.APPLICATION_JSON)
    public FileStatus getFileStatus() {
        return new FileStatus(Optional.ofNullable(backendAPI.getGcodeFile()).map(File::getAbsolutePath).orElse(""),
                sendProgress.getNumRows(),
                sendProgress.getNumCompletedRows(),
                sendProgress.getNumRemainingRows(),
                sendProgress.getDuration(),
                sendProgress.getRemainingDuration(),
                sendProgress.getLastCompletedCommandNumber());
    }

    @GET
    @Path("getFileContent")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(summary = "Get the raw gcode text of the currently loaded file")
    public String getFileContent() throws IOException {
        return Files.readString(currentGcodeFile().toPath());
    }

    @POST
    @Path("saveFileContent")
    @Consumes(MediaType.TEXT_PLAIN)
    @Operation(summary = "Save the raw gcode text of the currently loaded file and reload it")
    public void saveFileContent(String content) throws Exception {
        File gcodeFile = currentGcodeFile();
        Files.writeString(gcodeFile.toPath(), content);

        FileLoader fileLoader = LookupService.lookupOptional(FileLoader.class)
                .orElseGet(() -> new BackendFileLoader(backendAPI));
        fileLoader.openFile(gcodeFile);
    }

    @POST
    @Path("closeFile")
    @Operation(summary = "Close the currently loaded file")
    public void closeFile() throws Exception {
        backendAPI.unsetGcodeFile();
    }

    @POST
    @Path("saveFileContentAs")
    @Consumes(MediaType.TEXT_PLAIN)
    @Operation(summary = "Save the raw gcode text as a new file in the workspace directory, and open it")
    public void saveFileContentAs(@QueryParam("filename") String filename, String content) throws Exception {
        if (filename == null || filename.isBlank() || !filename.equals(new File(filename).getName())) {
            throw new BadRequestException("Invalid filename");
        }

        File currentFile = currentGcodeFile();
        File targetDirectory = workspaceDirectory().orElseGet(currentFile::getParentFile);
        File targetFile = new File(targetDirectory, ensureExtension(filename, currentFile));
        Files.writeString(targetFile.toPath(), content);

        FileLoader fileLoader = LookupService.lookupOptional(FileLoader.class)
                .orElseGet(() -> new BackendFileLoader(backendAPI));
        fileLoader.openFile(targetFile);
    }

    /**
     * "Save as" needs a folder that's actually meaningful to the user - "next to whatever
     * file happens to be currently open" fails silently for anything opened by uploading it
     * through the browser's file picker (see {@link #open}), since that lands in a JVM temp
     * directory the browser never reveals the real original path for. The configured
     * workspace directory (the same folder {@link #getWorkspaceFileList} already lists) is
     * always a real, known location the user picked, and the result shows up in that list
     * immediately - so prefer it whenever one is configured.
     */
    private Optional<File> workspaceDirectory() {
        String workspaceDirectory = backendAPI.getSettings().getWorkspaceDirectory();
        if (workspaceDirectory == null || workspaceDirectory.isBlank()) {
            return Optional.empty();
        }
        File folder = new File(workspaceDirectory);
        return folder.isDirectory() ? Optional.of(folder) : Optional.empty();
    }

    private static String ensureExtension(String filename, File referenceFile) {
        if (filename.contains(".")) {
            return filename;
        }
        String refName = referenceFile.getName();
        int dot = refName.lastIndexOf('.');
        return filename + (dot >= 0 ? refName.substring(dot) : ".gcode");
    }

    /**
     * These endpoints always operate on whichever file is already loaded (rather than taking a
     * client-supplied filename) - both because editing only makes sense for the currently open job,
     * and because it sidesteps needing to validate an arbitrary path: a file opened via upload (as
     * opposed to {@link #openWorkspaceFile}) doesn't live in the workspace directory, so restricting
     * these to workspace-only files (as an earlier version of this did) would have made them
     * unusable for anything but files picked from the workspace list.
     */
    private File currentGcodeFile() {
        File gcodeFile = backendAPI.getGcodeFile();
        if (gcodeFile == null) {
            throw new NotFoundException("No file is currently loaded");
        }
        return gcodeFile;
    }
}

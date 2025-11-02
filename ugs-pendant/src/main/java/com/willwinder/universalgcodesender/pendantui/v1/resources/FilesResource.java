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
import com.willwinder.universalgcodesender.pendantui.v1.model.FileStatus;
import com.willwinder.universalgcodesender.pendantui.v1.model.WorkspaceFileList;
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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

@Tag(name = "Files", description = "Endpoints for loading files and handling files")
@Path("/files")
public class FilesResource {

    @Inject
    private BackendAPI backendAPI;

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
        backendAPI.setGcodeFile(renamedFile);
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
                backendAPI.getNumRows(),
                backendAPI.getNumCompletedRows(),
                backendAPI.getNumRemainingRows(),
                backendAPI.getSendDuration(),
                backendAPI.getSendRemainingDuration());
    }
}

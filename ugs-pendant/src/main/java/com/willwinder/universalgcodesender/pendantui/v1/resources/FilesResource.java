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
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

@Path("/files")
public class FilesResource {

    @Inject
    private BackendAPI backendAPI;

    @POST
    @Path("uploadAndOpen")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public void open(@FormDataParam("file") InputStream fileInputStream, @FormDataParam("file") FormDataBodyPart bodyPart) throws Exception {
        String tempDir = System.getProperty("java.io.tmpdir");
        String fileName = bodyPart.getContentDisposition().getFileName();
        File file = new File(tempDir + File.separator + fileName);
        Files.copy(fileInputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        IOUtils.closeQuietly(fileInputStream);
        backendAPI.setGcodeFile(file);
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

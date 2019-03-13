package com.willwinder.universalgcodesender.pendantui.v1.controllers;

import com.willwinder.universalgcodesender.model.BackendAPI;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Path("/v1/files")
public class FilesController {

    @Inject
    private BackendAPI backendAPI;

    @POST
    @Path("uploadAndOpen")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response open(@FormDataParam("file") InputStream fileInputStream, @FormDataParam("file") FormDataBodyPart bodyPart) throws Exception {
        String tempDir = System.getProperty("java.io.tmpdir");
        String fileName = bodyPart.getContentDisposition().getFileName();
        File file = new File(tempDir + File.separator + fileName);
        Files.copy(fileInputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        IOUtils.closeQuietly(fileInputStream);
        backendAPI.setGcodeFile(file);
        return Response.ok().build();
    }

    @POST
    @Path("send")
    @Produces(MediaType.APPLICATION_JSON)
    public Response send() {
        try {
            if (backendAPI.isPaused()) {
                backendAPI.pauseResume();
            } else {
                backendAPI.send();
            }

            return Response.ok().build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    @GET
    @Path("pause")
    @Produces(MediaType.APPLICATION_JSON)
    public Response pause() {
        try {
            if (!backendAPI.isPaused()) {
                backendAPI.pauseResume();
            }

            return Response.ok().build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    @GET
    @Path("cancel")
    @Produces(MediaType.APPLICATION_JSON)
    public Response cancel() {
        try {
            backendAPI.cancel();
            return Response.ok().build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }
}

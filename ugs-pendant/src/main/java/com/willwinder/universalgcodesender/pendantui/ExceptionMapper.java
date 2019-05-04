package com.willwinder.universalgcodesender.pendantui;

import com.willwinder.universalgcodesender.pendantui.v1.model.PendantError;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class ExceptionMapper implements javax.ws.rs.ext.ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception e) {
        if(e instanceof WebApplicationException) {
            return ((WebApplicationException) e).getResponse();
        }

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new PendantError(e))
                .build();
    }
}
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
package com.willwinder.universalgcodesender.pendantui.html;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;

import java.io.InputStream;
import java.util.Objects;

@Path("/")
public class StaticResource {

    public static final String RESOURCES_PATH = "/resources/ugs-pendant/%s";

    private static String getMimeType(String path) {
        String mimeType = "application/octet-stream";
        if (path.endsWith(".js")) {
            mimeType = "text/javascript";
        } else if (path.endsWith(".html")) {
            mimeType = "text/html";
        } else if (path.endsWith(".css")) {
            mimeType = "text/css";
        } else if (path.endsWith(".ttf")) {
            mimeType = "font/ttf";
        }
        return mimeType;
    }

    @GET
    public Response getIndex() throws Exception {
        return getStaticResource("");
    }

    @GET
    @Path("{path:(jog|macros|run)$}")
    public Response getSubPage() throws Exception {
        return getStaticResource("");
    }

    @GET
    @Path("{path:.*\\.(jpg|gif|html|js|css|ico|ttf)$}")
    public Response getStaticResource(@PathParam("path") String path) {
        if (path.equalsIgnoreCase("")) {
            path = "index.html";
        }

        InputStream resource = StaticResource.class.getResourceAsStream(String.format(RESOURCES_PATH, path));
        String mimeType = getMimeType(path);
        return Objects.isNull(resource) ? Response.status(NOT_FOUND).build() :
                Response.ok().entity(resource).header(HttpHeaders.CONTENT_TYPE, mimeType).build();
    }
}

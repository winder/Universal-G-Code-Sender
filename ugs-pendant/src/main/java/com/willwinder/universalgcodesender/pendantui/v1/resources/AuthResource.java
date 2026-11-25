/*
    Copyright 2024 Will Winder

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
import com.willwinder.universalgcodesender.pendantui.auth.AuthService;
import com.willwinder.universalgcodesender.pendantui.auth.PendantAuthFilter;
import com.willwinder.universalgcodesender.utils.Settings;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

@Tag(name = "Auth", description = "Authentication endpoints for the web pendant")
@Path("/auth")
public class AuthResource {

    @Inject
    private BackendAPI backendAPI;

    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class AuthStatusResponse {
        private boolean authEnabled;
        private boolean authenticated;
        private String username;
        private String token;

        public boolean isAuthEnabled() {
            return authEnabled;
        }

        public void setAuthEnabled(boolean authEnabled) {
            this.authEnabled = authEnabled;
        }

        public boolean isAuthenticated() {
            return authenticated;
        }

        public void setAuthenticated(boolean authenticated) {
            this.authenticated = authenticated;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }

    @GET
    @Path("status")
    @Produces(MediaType.APPLICATION_JSON)
    public AuthStatusResponse getStatus(@Context HttpServletRequest request) {
        Settings settings = backendAPI.getSettings();
        AuthStatusResponse response = new AuthStatusResponse();
        boolean authEnabled = settings.isPendantAuthEnabled();
        response.setAuthEnabled(authEnabled);

        if (!authEnabled) {
            response.setAuthenticated(true);
            response.setUsername(settings.getPendantUsername());
            return response;
        }

        String token = PendantAuthFilter.extractToken(request);
        boolean valid = AuthService.isValidToken(token);
        response.setAuthenticated(valid);
        if (valid) {
            response.setUsername(AuthService.getUsernameForToken(token));
        }
        return response;
    }

    @POST
    @Path("login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(LoginRequest loginRequest) {
        Settings settings = backendAPI.getSettings();
        if (!settings.isPendantAuthEnabled()) {
            AuthStatusResponse ok = new AuthStatusResponse();
            ok.setAuthEnabled(false);
            ok.setAuthenticated(true);
            ok.setUsername(settings.getPendantUsername());
            return Response.ok(ok).build();
        }

        if (loginRequest == null || !AuthService.verifyCredentials(
                settings, loginRequest.getUsername(), loginRequest.getPassword())) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"Invalid username or password\"}")
                    .build();
        }

        String token = AuthService.createSession(settings, loginRequest.getUsername());
        int maxAgeSeconds = settings.getPendantSessionTimeoutMinutes() * 60;
        NewCookie cookie = new NewCookie.Builder(AuthService.SESSION_COOKIE_NAME)
                .value(token)
                .path("/")
                .maxAge(maxAgeSeconds)
                .httpOnly(true)
                .secure(settings.isPendantUseHttps())
                .build();

        AuthStatusResponse response = new AuthStatusResponse();
        response.setAuthEnabled(true);
        response.setAuthenticated(true);
        response.setUsername(loginRequest.getUsername());
        response.setToken(token);
        return Response.ok(response).cookie(cookie).build();
    }

    @POST
    @Path("logout")
    @Produces(MediaType.APPLICATION_JSON)
    public Response logout(@Context HttpServletRequest request) {
        String token = PendantAuthFilter.extractToken(request);
        AuthService.invalidateToken(token);
        NewCookie clearCookie = new NewCookie.Builder(AuthService.SESSION_COOKIE_NAME)
                .value("")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .build();
        return Response.ok("{\"authenticated\":false}").cookie(clearCookie).build();
    }
}

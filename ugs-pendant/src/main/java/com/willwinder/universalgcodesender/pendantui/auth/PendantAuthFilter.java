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
package com.willwinder.universalgcodesender.pendantui.auth;

import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.pendantui.BackendProvider;
import com.willwinder.universalgcodesender.utils.Settings;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Servlet filter enforcing session authentication on API and WebSocket endpoints
 * when pendant authentication is enabled.
 */
public class PendantAuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest httpReq) || !(response instanceof HttpServletResponse httpResp)) {
            chain.doFilter(request, response);
            return;
        }

        BackendAPI backendAPI = BackendProvider.getBackendAPI();
        if (backendAPI == null || backendAPI.getSettings() == null) {
            chain.doFilter(request, response);
            return;
        }

        Settings settings = backendAPI.getSettings();
        if (!settings.isPendantAuthEnabled()) {
            chain.doFilter(request, response);
            return;
        }

        String requestUri = httpReq.getRequestURI();
        if (isPublicPath(requestUri)) {
            chain.doFilter(request, response);
            return;
        }

        String token = extractToken(httpReq);
        if (AuthService.isValidToken(token)) {
            chain.doFilter(request, response);
            return;
        }

        httpResp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        httpResp.setContentType("application/json;charset=UTF-8");
        httpResp.getWriter().write("{\"error\":\"Unauthorized\"}");
    }

    private boolean isPublicPath(String uri) {
        if (uri == null) {
            return true;
        }
        return uri.startsWith("/api/v1/auth/login")
                || uri.startsWith("/api/v1/auth/logout")
                || uri.startsWith("/api/v1/auth/status");
    }

    public static String extractToken(HttpServletRequest req) {
        String authHeader = req.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }
        String queryToken = req.getParameter("token");
        if (queryToken != null && !queryToken.isEmpty()) {
            return queryToken;
        }
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (AuthService.SESSION_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}

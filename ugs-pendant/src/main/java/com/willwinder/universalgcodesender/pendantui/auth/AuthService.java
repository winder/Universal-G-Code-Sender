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

import com.willwinder.universalgcodesender.utils.Settings;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages simple username/password authentication and session tokens for the web pendant.
 */
public class AuthService {
    public static final String SESSION_COOKIE_NAME = "UGS_PENDANT_SESSION";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Map<String, SessionInfo> SESSIONS = new ConcurrentHashMap<>();

    public static class SessionInfo {
        private final String username;
        private final Instant expiresAt;

        public SessionInfo(String username, Instant expiresAt) {
            this.username = username;
            this.expiresAt = expiresAt;
        }

        public String getUsername() {
            return username;
        }

        public Instant getExpiresAt() {
            return expiresAt;
        }
    }

    private AuthService() {
    }

    public static boolean verifyCredentials(Settings settings, String username, String password) {
        if (username == null || password == null) {
            return false;
        }
        String expectedUser = settings.getPendantUsername();
        String expectedPass = settings.getPendantPassword();
        boolean userMatch = MessageDigest.isEqual(
                username.getBytes(StandardCharsets.UTF_8),
                expectedUser.getBytes(StandardCharsets.UTF_8));
        boolean passMatch = MessageDigest.isEqual(
                password.getBytes(StandardCharsets.UTF_8),
                expectedPass.getBytes(StandardCharsets.UTF_8));
        return userMatch && passMatch;
    }

    public static String createSession(Settings settings, String username) {
        cleanupExpired();
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        int timeoutMinutes = settings.getPendantSessionTimeoutMinutes();
        SESSIONS.put(token, new SessionInfo(username, Instant.now().plus(timeoutMinutes, ChronoUnit.MINUTES)));
        return token;
    }

    public static boolean isValidToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        SessionInfo info = SESSIONS.get(token);
        if (info == null) {
            return false;
        }
        if (Instant.now().isAfter(info.getExpiresAt())) {
            SESSIONS.remove(token);
            return false;
        }
        return true;
    }

    public static String getUsernameForToken(String token) {
        if (!isValidToken(token)) {
            return null;
        }
        SessionInfo info = SESSIONS.get(token);
        return info != null ? info.getUsername() : null;
    }

    public static void invalidateToken(String token) {
        if (token != null) {
            SESSIONS.remove(token);
        }
    }

    public static void invalidateAll() {
        SESSIONS.clear();
    }

    private static void cleanupExpired() {
        Instant now = Instant.now();
        SESSIONS.entrySet().removeIf(entry -> now.isAfter(entry.getValue().getExpiresAt()));
    }
}

/*
    Copyright 2026 Will Winder

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
package com.willwinder.universalgcodesender.pendantui.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.willwinder.universalgcodesender.model.Position;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

/**
 * {@link Position#getCartesian()} is a plain JavaBean getter that returns a freshly
 * derived {@link Position} - which itself has a "cartesian" getter, and so on. Jackson
 * serializes bean getters by default, so any endpoint returning a Position (directly or
 * nested, e.g. {@code Status.machineCoord}) recurses into "cartesian" forever, either
 * ballooning the response until Jackson's depth limit kicks in or blowing the stack
 * outright. It's a derived convenience for callers using the model directly, not real
 * state to expose over the API, so it's excluded from serialization here.
 */
@Provider
public class ObjectMapperProvider implements ContextResolver<ObjectMapper> {

    @JsonIgnoreProperties("cartesian")
    private abstract static class PositionMixin {
    }

    private final ObjectMapper objectMapper;

    public ObjectMapperProvider() {
        objectMapper = new ObjectMapper();
        objectMapper.addMixIn(Position.class, PositionMixin.class);
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return objectMapper;
    }
}

/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.universalgcodesender.pendantui;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.willwinder.universalgcodesender.model.Position;

import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

/**
 * Configures how the core model is mapped to JSON. {@link Position#getCartesian()} returns another
 * position, which Jackson would otherwise follow forever, so it is left out of the mapping.
 */
@Provider
public class ObjectMapperProvider implements ContextResolver<ObjectMapper> {
    private final ObjectMapper objectMapper = createObjectMapper();

    public static ObjectMapper createObjectMapper() {
        return new ObjectMapper().addMixIn(Position.class, PositionMixIn.class);
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return objectMapper;
    }

    @JsonIgnoreProperties({"cartesian"})
    private abstract static class PositionMixIn {
    }
}

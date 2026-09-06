package com.willwinder.universalgcodesender.pendantui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ObjectMapperProviderTest {

    @Test
    public void writeValueAsString_shouldSerializePositionWithoutTheCartesianCopy() throws Exception {
        ObjectMapper objectMapper = ObjectMapperProvider.createObjectMapper();
        Position position = new Position(1, 2, 3, 45, 0, 0, UnitUtils.Units.MM);

        String json = objectMapper.writeValueAsString(position);

        assertFalse(json.contains("cartesian"));
        assertTrue(json.contains("\"x\":1.0"));
        assertTrue(json.contains("\"a\":45.0"));
    }
}

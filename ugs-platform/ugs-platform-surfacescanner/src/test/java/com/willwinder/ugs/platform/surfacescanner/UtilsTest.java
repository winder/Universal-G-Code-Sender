package com.willwinder.ugs.platform.surfacescanner;

import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import static org.junit.Assert.*;
import org.junit.Test;

import java.util.List;

public class UtilsTest {
    @Test
    public void getMaxPosition_shouldReturnTheMaxPositionEvenIfNegative() {
        Position maxPosition = Utils.getMaxPosition(
                List.of(
                        new Position(-1, -2, -3, UnitUtils.Units.MM),
                        new Position(-3, -2, -1, UnitUtils.Units.MM)));

        assertEquals(new Position(-1, -2, -1, UnitUtils.Units.MM), maxPosition);
    }

    @Test
    public void getMinPosition_shouldReturnTheMinPositionEvenIfNegative() {
        Position minPosition = Utils.getMinPosition(
                List.of(
                        new Position(-1, -2, -3, UnitUtils.Units.MM),
                        new Position(-3, -2, -1, UnitUtils.Units.MM)));

        assertEquals(new Position(-3, -2, -3, UnitUtils.Units.MM), minPosition);
    }
}
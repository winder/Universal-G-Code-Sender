package com.willwinder.universalgcodesender.firmware.fluidnc;

import static org.junit.Assert.*;
import org.junit.Test;

public class SpeedMapTest {

    @Test
    public void getMaxShouldReturnTheHundredPercentMaxSpindleSpeed() {
        SpeedMap speedMap = new SpeedMap("0=0.00% 0=0.00% 5000=100.00%");
        assertEquals(5000, speedMap.getMax());
    }

    @Test
    public void getMaxShouldReturnTheMaxSpindleSpeedRegardlessOfOrder() {
        SpeedMap speedMap = new SpeedMap("5000=100.00% 0=0.00% ");
        assertEquals(5000, speedMap.getMax());
    }

    @Test
    public void getMaxShouldReturnValueOfTheHighestPercent() {
        SpeedMap speedMap = new SpeedMap("5000=99.00% 0=0.00% 4000=100%");
        assertEquals(4000, speedMap.getMax());
    }

    @Test
    public void speedMapShouldHandleExtraSpacing() {
        SpeedMap speedMap = new SpeedMap("0=0.00%  5000=50.00%  10000=100.00%");
        assertEquals(10000, speedMap.getMax());
    }
}
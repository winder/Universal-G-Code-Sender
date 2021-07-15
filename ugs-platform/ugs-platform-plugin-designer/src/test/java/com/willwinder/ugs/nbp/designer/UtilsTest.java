package com.willwinder.ugs.nbp.designer;

import org.junit.Test;

import static org.junit.Assert.*;

public class UtilsTest {

    @Test
    public void normalizeRotation() {
        assertEquals(1, Utils.normalizeRotation(361), 0.1);
        assertEquals(1, Utils.normalizeRotation(721), 0.1);
        assertEquals(359, Utils.normalizeRotation(-361), 0.1);
        assertEquals(359, Utils.normalizeRotation(-721), 0.1);
    }
}
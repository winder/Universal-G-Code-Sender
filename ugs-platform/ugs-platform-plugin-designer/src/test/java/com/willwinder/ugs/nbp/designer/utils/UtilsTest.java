package com.willwinder.ugs.nbp.designer.utils;

import com.willwinder.ugs.nbp.designer.Utils;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class UtilsTest {

    @Test
    public void normalizeRotation() {
        assertEquals(1, Utils.normalizeRotation(361), 0.1);
        assertEquals(1, Utils.normalizeRotation(721), 0.1);
        assertEquals(359, Utils.normalizeRotation(-361), 0.1);
        assertEquals(359, Utils.normalizeRotation(-721), 0.1);
    }
}
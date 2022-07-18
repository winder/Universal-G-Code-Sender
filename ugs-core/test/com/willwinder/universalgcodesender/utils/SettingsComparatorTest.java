package com.willwinder.universalgcodesender.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Joacim Breiler
 */
public class SettingsComparatorTest {

    @Test
    public void compareTwoStringShouldDoStringCompare() {
        SettingsComparator stringNumberComparator = new SettingsComparator();
        assertEquals(-1, stringNumberComparator.compare("a", "b"));
        assertEquals(1, stringNumberComparator.compare("b", "a"));
        assertEquals(0, stringNumberComparator.compare("a", "a"));
    }

    @Test
    public void compareTwoStringWithOneNumberShouldDoStringCompare() {
        SettingsComparator stringNumberComparator = new SettingsComparator();
        assertEquals(-1, stringNumberComparator.compare("a1", "b"));
        assertEquals(1, stringNumberComparator.compare("b", "a1"));
    }

    @Test
    public void compareTwoStringWithNumbersShouldDoNumberCompare() {
        SettingsComparator stringNumberComparator = new SettingsComparator();
        assertEquals(-1, stringNumberComparator.compare("a1", "b2"));
        assertEquals(1, stringNumberComparator.compare("b2", "a1"));
        assertEquals(-1, stringNumberComparator.compare("a1", "b10"));
        assertEquals(1, stringNumberComparator.compare("b10", "a1"));
    }

    @Test
    public void compareTwoStringsWithGrblSetting() {
        SettingsComparator stringNumberComparator = new SettingsComparator();
        assertEquals(-1, stringNumberComparator.compare("$1", "$2"));
        assertEquals(-13, stringNumberComparator.compare("$2", "1"));
        assertEquals(-9, stringNumberComparator.compare("$01", "$10"));
    }

    @Test
    public void compareTwoStringsWithFluidNcSetting() {
        SettingsComparator stringNumberComparator = new SettingsComparator();
        assertEquals(1, stringNumberComparator.compare("/test/test1", "/test/test"));
        assertEquals(-1, stringNumberComparator.compare("/test/test", "/test/test1"));
        assertEquals(-1, stringNumberComparator.compare("/test/test1", "/test/test2"));
        assertEquals(1, stringNumberComparator.compare("/test/test2", "/test/test1"));
        assertEquals(2, stringNumberComparator.compare("/test1/test", "/test/test1"));
    }
}

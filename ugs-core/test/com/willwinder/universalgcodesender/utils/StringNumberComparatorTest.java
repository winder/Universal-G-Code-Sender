package com.willwinder.universalgcodesender.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Joacim Breiler
 */
public class StringNumberComparatorTest {

    @Test
    public void compareTwoStringShouldDoStringCompare() {
        StringNumberComparator stringNumberComparator = new StringNumberComparator();
        assertEquals(-1, stringNumberComparator.compare("a", "b"));
        assertEquals(1, stringNumberComparator.compare("b", "a"));
        assertEquals(0, stringNumberComparator.compare("a", "a"));
    }

    @Test
    public void compareTwoStringWithOneNumberShouldDoStringCompare() {
        StringNumberComparator stringNumberComparator = new StringNumberComparator();
        assertEquals(-1, stringNumberComparator.compare("a1", "b"));
        assertEquals(1, stringNumberComparator.compare("b", "a1"));
    }

    @Test
    public void compareTwoStringWithNumbersShouldDoNumberCompare() {
        StringNumberComparator stringNumberComparator = new StringNumberComparator();
        assertEquals(-1, stringNumberComparator.compare("a1", "b2"));
        assertEquals(1, stringNumberComparator.compare("b2", "a1"));
        assertEquals(-9, stringNumberComparator.compare("a1", "b10"));
        assertEquals(9, stringNumberComparator.compare("b10", "a1"));
    }
}

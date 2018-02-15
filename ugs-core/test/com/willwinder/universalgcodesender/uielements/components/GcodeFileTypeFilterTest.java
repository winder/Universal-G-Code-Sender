/*
    Copyright 2018 Will Winder

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
package com.willwinder.universalgcodesender.uielements.components;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for gcode file type filter
 *
 * @author Joacim Breiler
 */
public class GcodeFileTypeFilterTest {
    private GcodeFileTypeFilter target;

    @Before
    public void setUp() {
        target = new GcodeFileTypeFilter();
    }

    @Test
    public void fileWithNoExtensionShouldNotBeAccepted() {
        assertFalse(target.accept(new File("a_unique_test_file")));
    }

    @Test
    public void fileWithGCodeExtensionShouldBeAccepted() {
        assertTrue(target.accept(new File("test.gcode")));
    }

    @Test
    public void fileWithNcExtensionShouldBeAccepted() {
        assertTrue(target.accept(new File("test.nc")));
    }

    @Test
    public void fileWithNgcExtensionShouldBeAccepted() {
        assertTrue(target.accept(new File("test.ngc")));
    }

    @Test
    public void fileWithTapExtensionShouldBeAccepted() {
        assertTrue(target.accept(new File("test.tap")));
    }

    @Test
    public void fileWithDocExtensionShouldNotBeAccepted() {
        assertFalse(target.accept(new File("test.doc")));
    }

    @Test
    public void fileWithUpperCaseGCodeExtensionShouldBeAccepted() {
        assertTrue(target.accept(new File("test.GCODE")));
    }
}

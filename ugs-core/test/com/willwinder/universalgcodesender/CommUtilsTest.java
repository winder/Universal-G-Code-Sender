/*
    Copyright 2013 Will Winder

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
package com.willwinder.universalgcodesender;

import com.willwinder.universalgcodesender.utils.CommUtils;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author wwinder
 */
public class CommUtilsTest {
    /**
     * Test of getSerialPortList method, of class CommUtils.
     */
    @Test
    public void testGetSerialPortList() {
        // Can't test this, varies depending on system.
        /*
        System.out.println("getSerialPortList");
        List expResult = null;
        List result = CommUtils.getSerialPortList();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
        */
    }

    /**
     * Test of checkRoomInBuffer method, of class CommUtils.
     */
    @Test
    public void testCheckRoomInBuffer() {
        System.out.println("checkRoomInBuffer");

        GcodeCommand nextCommand;
        Boolean expResult;
        Boolean result;

        List<GcodeCommand> list = new LinkedList<>();
        list.add(new GcodeCommand("twenty characters...", 0));
        list.add(new GcodeCommand("twenty characters...", 1));
        list.add(new GcodeCommand("twenty characters...", 2));
        list.add(new GcodeCommand("twenty characters...", 3));
        list.add(new GcodeCommand("twenty characters...", 4));
        
        // 100 characters + 5 for newlines, 18 characters remaining in buffer.
        
        StringBuilder biggestString = new StringBuilder();
        for (int i=0; i < (GrblUtils.GRBL_RX_BUFFER_SIZE - 105 - 1); i++) {
            biggestString.append('.');
        }
        
        // This command should just barely fit.
        nextCommand = new GcodeCommand(biggestString.toString(), 5);
        result = CommUtils.checkRoomInBuffer(list, nextCommand, GrblUtils.GRBL_RX_BUFFER_SIZE);
        expResult = true;
        assertEquals(expResult, result);
        
        biggestString.append('.');
        nextCommand = new GcodeCommand(biggestString.toString(), 5);

        expResult = false;
        result = CommUtils.checkRoomInBuffer(list, nextCommand, GrblUtils.GRBL_RX_BUFFER_SIZE);
        assertEquals(expResult, result);
    }

    /**
     * Test of getSizeOfBuffer method, of class CommUtils.
     */
    @Test
    public void testGetSizeOfBuffer() {
        System.out.println("getSizeOfBuffer");
        List<GcodeCommand> list = new LinkedList<>();
        list.add(new GcodeCommand("twenty characters...", 0));
        list.add(new GcodeCommand("twenty characters...", 1));
        list.add(new GcodeCommand("twenty characters...", 2));
        list.add(new GcodeCommand("twenty characters...", 3));
        list.add(new GcodeCommand("twenty characters...", 4));

        // 100characters + 5 newlines.
        int expResult = 105;
        int result = CommUtils.getSizeOfBuffer(list);
        assertEquals(expResult, result);
    }
}

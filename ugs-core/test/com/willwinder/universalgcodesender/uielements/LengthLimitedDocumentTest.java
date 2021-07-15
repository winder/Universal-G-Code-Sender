/*
    Copyright 2015 Will Winder

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
package com.willwinder.universalgcodesender.uielements;

import com.willwinder.universalgcodesender.uielements.components.LengthLimitedDocument;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author wwinder
 */
public class LengthLimitedDocumentTest {
    
    public LengthLimitedDocumentTest() {
    }
    
    @Before
    public void setUp() {
    }

    /**
     * Test of insertString method, of class LengthLimitedDocument.
     */
    @Test
    public void testInsertString() throws Exception {
        System.out.println("insertString");
        LengthLimitedDocument instance = new LengthLimitedDocument(1000);

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 99; i++) {
            sb.append(" ");
        }
        String hundred = sb.append("\n").toString();
        sb = new StringBuffer();
        for (int i = 0; i < 149; i++) {
            sb.append(" ");
        }
        String hundredfifty = sb.append("\n").toString();

        for (int i = 0; i < 10; i++) {
            instance.insertString(instance.getLength(), hundred, null);
        }

        assertEquals(1000, instance.getLength());

        // Overflow. need to remove the first 100 character chunk.
        instance.insertString(instance.getLength(), hundred, null);
        assertEquals(1000, instance.getLength());


        // Overflow. This time 200 characters are removed to break at a newline.
        instance.insertString(instance.getLength(), hundredfifty, null);
        assertEquals(950, instance.getLength());
    }
}

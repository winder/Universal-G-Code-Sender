/*
 * Mocks what GRBL would do when connected to a GrblCommunicator.
 */

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
package com.willwinder.universalgcodesender.mockobjects;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author wwinder
 */
public class MockGrbl {
    public PipedInputStream in;
    public ByteArrayOutputStream out;
    
    public PipedOutputStream local_out;
        
    public MockGrbl() {
        // This isn't joined to a stream becaues I can manipulate it.
        this.out = new ByteArrayOutputStream();

        // These two guys are joined so that the Serial object doesn't notice.
        this.local_out = new PipedOutputStream();
        try {
            this.in = new PipedInputStream(this.local_out);
        } catch (IOException ex) {
            Logger.getLogger(MockGrbl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String readStringFromGrblBuffer() {
        String str;
        
        try {
            str = this.out.toString(Charset.defaultCharset().name());
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(MockGrbl.class.getName()).log(Level.SEVERE, null, ex);
            str = "Exception in mock class";
        }
        
        this.out.reset();
        return str;
    }
    
    public byte readByteFromGrblBuffer() {
        byte b = 0x0;
        byte arr[];
        
        // Null stream.
        if (this.out.size() == 0){
            return b;
        }
        
        // Grab the first byte
        arr = this.out.toByteArray();
        b = arr[0];
        
        this.out.reset();
        
        // If there were multiple bytes, copy the rest back into the ostream.
        //if (arr.length > 1) {
            this.out.write(arr, 1, arr.length - 1);
        //}
        
        return b;
    }
    
    public void addOkFromGrbl() throws IOException {
        this.local_out.write("ok\r\n".getBytes(Charset.defaultCharset().name()));
    }
}

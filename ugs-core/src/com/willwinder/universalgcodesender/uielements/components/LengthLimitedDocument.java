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
package com.willwinder.universalgcodesender.uielements.components;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 *
 * @author wwinder
 */
public class LengthLimitedDocument extends PlainDocument {
    final int limit;
    final int hardLimit;

    public LengthLimitedDocument(int limit) {
        this.limit = limit;
        this.hardLimit = (int) (limit * 1.2);
    }

    private void truncateDataFor(int insertLength) throws BadLocationException {
        // Truncate when we get past 80% full.
        if ((getLength() + insertLength) > limit) {
            int idx = -1;

            // Look for the smallest amount to remove...
            int offset = -100;

            // TODO: If this is too expensive, remove larger batches.
            // Remove 25% of that 80% full
            //int offset = len-(int)Math.ceil(limit*.6)-100;

            // Find newline to make new text fit
            while ((offset + idx + 1) < insertLength) {
                offset += 100;
                String line = this.getText(offset, offset+100);
                idx = line.indexOf('\n');
            }

            // If no newline is found, remove the first 25%
            if (idx > 0) {
                this.remove(0, offset + idx + 1);
            } else {
                this.remove(0, (int)(limit*0.25));
            }
        }
    }
    
    /**
     * Appends data to the document, removing data from the front if necessary.
     * Data removed is split at the line level.
     * WARNING: I'm ignoring the 'offs' value.
     */
    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
        if (str == null) return;

        // Make sure the string will fit, then append it.
        truncateDataFor(str.length());
        super.insertString(getLength(), str, a);
    }
}

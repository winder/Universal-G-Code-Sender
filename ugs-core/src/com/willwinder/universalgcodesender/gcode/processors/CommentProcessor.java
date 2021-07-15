/**
 * Removes comments from the command. Comments are defined:
 * 
 * Any text surrounded by parenthesis "G0 (go to x1) X1" -> "G0 X1"
 * Any text following a semi-colon: "G0 X1 ; go to x1" -> "G0 X1"
 * Any percent symbols at the end of a line: "G0 X1%" -> "G0 X1"
 */
/*
    Copyright 2016 Will Winder

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
package com.willwinder.universalgcodesender.gcode.processors;

import com.willwinder.universalgcodesender.gcode.GcodePreprocessorUtils;

/**
 *
 * @author wwinder
 */
public class CommentProcessor extends PatternRemover {
    public CommentProcessor() {
        super(GcodePreprocessorUtils.COMMENT.pattern());
    }
}
/*
 * GcodeStreamReader.java
 *
 * Reads a 'GcodeStream' file containing command processing information, actual
 * command to send and other metadata like total number of commands.
 *
 * Created on Jan 7, 2016
 */
/*
    Copyright 2016-2017 Will Winder

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
package com.willwinder.universalgcodesender.utils;

import java.util.regex.Pattern;

/**
 *
 * @author wwinder
 */
public abstract class GcodeStream {
    protected final int NUM_COLUMNS           = 4;

    protected final int COL_ORIGINAL_COMMAND  = 0;
    protected final int COL_PROCESSED_COMMAND = 1;
    protected final int COL_COMMAND_NUMBER    = 2;
    protected final int COL_COMMENT           = 3;

    protected final String separator = "++";
    protected final Pattern splitPattern = Pattern.compile(Pattern.quote(separator));
    protected final String metaPrefix = "gsw_meta:";
}

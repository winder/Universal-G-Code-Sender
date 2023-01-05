/*
    Copyright 2016-2023 Will Winder

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
 * Reads a 'GcodeStream' file containing command processing information, actual
 * command to send and other metadata like total number of commands.
 *
 * @author wwinder
 */
public class GcodeStream {
    private GcodeStream() {}
    protected static final int NUM_COLUMNS           = 4;

    protected static final int COL_ORIGINAL_COMMAND  = 0;
    protected static final int COL_PROCESSED_COMMAND = 1;
    protected static final int COL_COMMAND_NUMBER    = 2;
    protected static final int COL_COMMENT           = 3;

    protected static final String FIELD_SEPARATOR = "¶¶";
    protected static final Pattern SPLIT_PATTERN = Pattern.compile(Pattern.quote(FIELD_SEPARATOR));
    protected static final String META_PREFIX = "gsw_meta:";
    protected static final String METADATA_RESERVED_SIZE = "                                                  ";
}

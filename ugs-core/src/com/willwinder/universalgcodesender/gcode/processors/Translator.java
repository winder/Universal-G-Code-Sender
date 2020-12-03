/*
    Copyright 2017-2020 Will Winder

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
import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;

import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

import static com.willwinder.universalgcodesender.Utils.formatter;

/**
 *
 * @author wwinder
 */
public class Translator implements CommandProcessor {
  private final Position offset;

  public Translator(Position offset) {
    this.offset = offset;
  }

  private String shift(String part, double amount) {
    try {
      return "" + part.charAt(0) + formatter.format(formatter.parse(part.substring(1)).doubleValue() + amount);
    } catch (ParseException e) {
      throw new IllegalArgumentException("Could not parse '" + part + "' as a double");
    }
  }

  @Override
  public List<String> processCommand(String command, GcodeState state) {
    // If the file is in absolute mode, no translation is needed.
    if (!state.inAbsoluteMode) {
      return Collections.singletonList(command);
    }

    String comment = GcodePreprocessorUtils.parseComment(command);
    String rawCommand = GcodePreprocessorUtils.removeComment(command);
    List<String> parts = GcodePreprocessorUtils.splitCommand(rawCommand);
    StringBuilder sb = new StringBuilder();

    UnitUtils.Units currentUnits = state.getUnits();
    double x = offset.getPositionIn(currentUnits).x;
    double y = offset.getPositionIn(currentUnits).y;
    double z = offset.getPositionIn(currentUnits).z;

    for (String part : parts) {
      switch (Character.toUpperCase(part.charAt(0))) {
        case 'X':
          sb.append(shift(part, x));
          break;
        case 'Y':
          sb.append(shift(part, y));
          break;
        case 'Z':
          sb.append(shift(part, z));
          break;

        // Grbl doesn't support absolute arcs, but what the hell.
        case 'I':
          if (state.inAbsoluteIJKMode) {
            sb.append(shift(part, x));
            break;
          }
        // fall through if not in absolute mode...
        case 'J':
          if (state.inAbsoluteIJKMode) {
            sb.append(shift(part, y));
            break;
          }
        // fall through if not in absolute mode...
        case 'K':
          if (state.inAbsoluteIJKMode) {
            sb.append(shift(part, z));
            break;
          }
        // fall through if not in absolute mode...
        default:
          sb.append(part);
      }
    }

    if (StringUtils.isNotBlank(comment)) {
      sb.append("(").append(comment).append(")");
    }

    return Collections.singletonList(sb.toString());
  }

  @Override
  public String getHelp() {
    return "Translates gcode location.";
  }
}

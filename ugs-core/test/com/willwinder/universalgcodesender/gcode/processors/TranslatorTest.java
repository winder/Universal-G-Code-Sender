/*
    Copyright 2017 Will Winder

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

import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.gcode.util.Code;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import com.willwinder.universalgcodesender.gcode.util.Plane;
import static com.willwinder.universalgcodesender.gcode.util.Plane.*;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.vecmath.Point3d;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import org.assertj.core.data.Offset;
import org.junit.Assert;
import org.junit.Before;

/**
 *
 * @author wwinder
 */
public class TranslatorTest {
  Translator instance;
  GcodeState state;

  // In case it's needed...
  DecimalProcessor truncator = new DecimalProcessor(4);

  @Before
  public void setup() {
      instance = new Translator(new Position(10.1, 20.2, 30.3, Units.MM));

      state = new GcodeState();
      state.units = Code.G21;
      state.inAbsoluteMode = true;
      state.inAbsoluteIJKMode = false;
      state.currentPoint = null;
  }

  @Test
  public void basicTranslate() throws Exception {
      List<String> result = instance.processCommand("G0 X0 Y0 Z0", state);

      Assertions.assertThat(result).hasSize(1).contains("G0X10.1Y20.2Z30.3");
  }

  @Test
  public void basicTranslateNegative() throws Exception {
      instance = new Translator(new Position(-10.1, -20.2, -30.3, Units.MM));

      List<String> result = instance.processCommand("G0 X10 Y20 Z30", state);

      Assertions.assertThat(result).hasSize(1);
      result = truncator.processCommand(result.get(0), state);
      Assertions.assertThat(result).hasSize(1).contains("G0X-0.1Y-0.2Z-0.3");
  }

  @Test
  public void arcTranslate() throws Exception {
      List<String> result = instance.processCommand("G3 X0 Y0 Z0 I0.5 J0.5", state);

      Assertions.assertThat(result).hasSize(1).contains("G3X10.1Y20.2Z30.3I0.5J0.5");
  }

  @Test
  public void arcTranslateAbsoluteIJK() throws Exception {
      state.inAbsoluteIJKMode = true;

      List<String> result = instance.processCommand("G3 X0 Y0 Z0 I0.5 J0.5", state);

      Assertions.assertThat(result).hasSize(1).contains("G3X10.1Y20.2Z30.3I10.6J20.7");
  }

  @Test
  public void convertUnits() throws Exception {
    // Offset is in MM, convert it to INCH
    state.units = Code.G20;

    List<String> result = instance.processCommand("G0 X0 Y0 Z0", state);
    Assertions.assertThat(result).hasSize(1);

    result = truncator.processCommand(result.get(0), state);

    Assertions.assertThat(result).hasSize(1).contains("G0X0.3976Y0.7953Z1.1929");
  }

  @Test
  public void handlesComments1() throws Exception {
      List<String> result = instance.processCommand("G0 X0 Y0 Z0 ; some comment", state);

      Assertions.assertThat(result).hasSize(1).contains("G0X10.1Y20.2Z30.3( some comment)");
  }

  @Test
  public void handlesComments2() throws Exception {
      List<String> result = instance.processCommand("(another comment)G0 X0 Y0 Z0", state);

      Assertions.assertThat(result).hasSize(1).contains("G0X10.1Y20.2Z30.3(another comment)");
  }
}

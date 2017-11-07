/*
    Copywrite 2017 Will Winder

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
package com.willwinder.ugs.platform.dowel

import com.google.common.collect.Iterables
import com.willwinder.universalgcodesender.gcode.util.GcodeUtils
import com.willwinder.universalgcodesender.model.UnitUtils
import java.io.BufferedWriter
import javax.vecmath.Point3d

/*

  @author wwinder
  Created on Oct 29, 2017
*/

public class DowelGenerator(var settings: DowelSettings) {

  public fun unitMultiplier() = UnitUtils.scaleUnits(settings.units, UnitUtils.Units.MM)

  public fun getLocations(): List<Point3d> {
    val mult= UnitUtils.scaleUnits(settings.units, UnitUtils.Units.MM)
    val offset = mult * (settings.dowelDiameter + settings.bitDiameter * 1.5)
    val corner = Point3d(
        mult * settings.dowelDiameter / 2.0,
        mult * settings.dowelDiameter / 2.0,
        0.0)

    val ret: MutableList<Point3d> = mutableListOf()
    for (x in 0 until settings.numDowelsX) {
      for (y in 0 until settings.numDowelsY) {
        ret.add(Point3d(corner.x + x * offset, corner.y + y * offset, 0.0))
      }
    }

    return ret
  }

  public fun generate(output: BufferedWriter) {
    // Set units and absolute movement/IJK mode.
    output.write("${GcodeUtils.unitCommand(settings.units)} G90 G91.1\n")
    output.write("M3 S10000\n")
    output.write("G17 F${settings.feed}\n")

    for (point in getLocations()) {
      generateOne(point, output)
    }
  }

  public fun generateOne(at: Point3d, output: BufferedWriter) {
    val radius = settings.bitDiameter / 2.0 + settings.dowelDiameter / 2.0
    val quarterDepth = settings.cutDepth / 4.0

    val arcSequence: Iterator<Point3d> = Iterables.cycle(listOf(
        Point3d(at.x - radius, at.y, 0.0),
        Point3d(at.x, at.y + radius, 0.0),
        Point3d(at.x + radius, at.y, 0.0),
        Point3d(at.x, at.y - radius, 0.0)
    )).iterator()

    var last = arcSequence.next();

    // Start
    output.write("G0 X${last.x} Y${last.y}\n")
    output.write("G0 Z0\n")

    // Create the helix
    var currentDepth = 0.0
    while (currentDepth > -settings.dowelLength) {
      val n = arcSequence.next()
      output.write("G02 X${n.x} Y${n.y} Z${currentDepth} I${at.x - last.x} J${at.y - last.y}\n")
      last = n
      currentDepth -= quarterDepth
    }

    // final loop at final depth
    for (i in 0..4) {
      val n = arcSequence.next()
      output.write("G02 X${n.x} Y${n.y} Z${-settings.dowelLength} I${at.x - last.x} J${at.y - last.y}\n")
      last = n
    }

    // Lift tool out of pocket.
    output.write("G0 Z${settings.safetyHeight}\n")
  }
}

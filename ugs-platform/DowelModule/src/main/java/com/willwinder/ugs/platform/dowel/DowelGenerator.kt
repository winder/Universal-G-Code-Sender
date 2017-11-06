/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.ugs.platform.dowel

import com.willwinder.universalgcodesender.model.UnitUtils
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
}

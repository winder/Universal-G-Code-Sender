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
package com.willwinder.ugs.platform.dowel

import com.jogamp.opengl.GL2
import com.jogamp.opengl.GLAutoDrawable
import com.jogamp.opengl.util.gl2.GLUT
import com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions
import com.willwinder.ugs.nbm.visualizer.shared.Renderable
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils

/*

  @author wwinder
  Created on Nov 5, 2017
*/

class DowelPreview(val description: String, val generator: DowelGenerator) : Renderable(6, description) {

    companion object {
      val glut = GLUT()
      val slices = 20
      val stacks = 20

      private fun drawDowel(gl: GL2, at: Position, diameter: Double, length: Double) {
        gl.glPushMatrix()
          gl.glTranslated(at.x, at.y, 0.0)
          gl.glRotated(180.0, 1.0, 0.0, 0.0)
          glut.glutSolidCylinder(diameter/2.0, length, slices, stacks)
        gl.glPopMatrix()
      }
    }

    override fun rotate() = true
    override fun center() = true

    override fun init(drawable: GLAutoDrawable?) {
    }

    override fun reloadPreferences(vo: VisualizerOptions?) {
    }

    override fun draw(drawable: GLAutoDrawable?, idle: Boolean, machineCoord: Position?, workCoord: Position?, objectMin: Position?, objectMax: Position?, scaleFactor: Double, mouseWorldCoordinates: Position?, rotation: Position?) {
      if (drawable?.gl?.gL2 == null) return
      val mult: Double = generator.unitMultiplier()
      drawable.gl.gL2.let {
        for (point in generator.getLocations(UnitUtils.Units.MM)) {
          drawDowel(
              it,
              point,
              mult * generator.settings.dowelDiameter,
              mult * generator.settings.dowelLength)
        }
      }
    }
}

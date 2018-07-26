/*
    Copyright 2018 Will Winder

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
package com.willwinder.ugs.nbm.visualizer.shared;

import com.willwinder.universalgcodesender.model.Position;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author wwinder
 */
@ServiceProvider(service=RotationService.class)
public class RotationService {
  public RotationService() {
    // configure algorithim...?
  }
  
  public Position squashRotation(Position p) {
    Position result = new Position(p);

    if (p.a != 0 || p.b != 0 || p.c != 0) {
      System.out.println("hype");
    }
    // X-Axis rotation
    // x1 = x0
    // y1 = y0cos(u) − z0sin(u)
    // z1 = y0sin(u) + z0cos(u)	
    if (p.a != 0) {
      double sinA = Math.sin(Math.toRadians(p.a));
      double cosA = Math.cos(Math.toRadians(p.a));

      result.y = result.y * cosA - result.z * sinA;
      result.z = result.y * sinA + result.z * cosA;
    }

    // Y-Axis rotation
    // x2 = x1cos(v) + z1sin(v)
    // y2 = y1
    // z2 = − x1sin(v) + z1cos(v)	
    if (p.b != 0) {
      double sinB = Math.sin(Math.toRadians(p.b));
      double cosB = Math.cos(Math.toRadians(p.b));

      result.x = result.x * cosB + result.z * sinB;
      result.z = -1 * result.x * sinB + result.z * cosB;
    }
    
    // Z-Axis rotation
    // x3 = x2cos(w) − y2sin(w)
    // y3 = x2sin(w) + y2cos(w)
    // z3 = z2	
    if (p.c != 0) {
      double sinC = Math.sin(Math.toRadians(p.c));
      double cosC = Math.cos(Math.toRadians(p.c));

      result.x = result.x * cosC - result.y * sinC;
      result.y = result.x * sinC + result.y * cosC;
    }

    p.a = 0;
    p.b = 0;
    p.c = 0;

    return result;
  }

}
/*
 * This file is part of JGCGen.
 *
 * JGCGen is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JGCGen is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JGCGen.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.ugs.nbp.designer.gcode.path;

/**
 * Supported axes.
 */
public enum Axis {
	// Note. XYZ must be the first axes, because some code relies on their ordinals.
	X,
	Y, 
	Z,
	A, /** Rotation around X */
	B, /** Rotation around Y */
	C, /** Rotation around Z */
	I, /** X offset in a plane */
	J, /** Y offset in a plane */
	K, /** Z offset in a plane */
	;
	/**
	 * Get an axis
	 * @return axis or null if not found
	 */
	static public Axis get(char a) {
		String ax = Character.toString(Character.toUpperCase(a));
		try {
			return valueOf(ax);
		} catch(IllegalArgumentException e) {
			return null;
		}
	}
}

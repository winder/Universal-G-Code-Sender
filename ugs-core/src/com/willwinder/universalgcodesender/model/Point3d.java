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
package com.willwinder.universalgcodesender.model;

/**
 * Stripped down version of Point3d to avoid a dependency on vecmath.
 * @author wwinder
 */
public class Point3d {
  /**
   * The x coordinate.
   */
  public	double	x;

  /**
   * The y coordinate.
   */
  public	double	y;

  /**
   * The z coordinate.
   */
  public	double	z;
  
  /**
   * Constructs and initializes a Point3d from the specified xyz coordinates.
   * @param x the x coordinate
   * @param y the y coordinate
   * @param z the z coordinate
   */
  public Point3d(double x, double y, double z)
  {
    set(x, y, z);
  }

  /**
   * Constructs and initializes a Point3d from the array of length 3.
   * @param t the array of length 3 containing xyz in order
   */
  public Point3d(double[] t)
  {
    set(t);
  }

  /**
   * Constructs and initializes a Point3d from the specified Point3d.
   * @param t1 the Point3d containing the initialization x y z data
   */
  public Point3d(Point3d t1)
  {
    set(t1);
  }

  /**
   * Constructs and initializes a Point3d to (0,0,0).
   */
  public Point3d()
  {
    this.x = (double) 0.0;
    this.y = (double) 0.0;
    this.z = (double) 0.0;
  }

  /**
   * Sets the value of this tuple to the specified xyzabc coordinates.
   * @param x the x coordinate
   * @param y the y coordinate
   * @param z the z coordinate
   */
  public final void set(double x, double y, double z)
  {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  /**
   * Sets the value of this tuple to the value of the xyz coordinates
   * located in the array of length 3.
   * @param t the array of length 3 containing xyz in order
   */
  public final void set(double[] t)
  {
    this.x = t[0];
    this.y = t[1];
    this.z = t[2];
  }

  /**
   * Sets the value of this tuple to the value of tuple t1.
   * @param t1 the tuple to be copied
   */
  public final void set(Point3d t1)
  {
    this.x = t1.x;
    this.y = t1.y;
    this.z = t1.z;
  }

  /**
   * Copies the x,y,z coordinates of this tuple into the array t
   * of length 3.
   * @param t  the target array 
   */
  public final void get(double[] t)
  {
    t[0] = this.x;
    t[1] = this.y;
    t[2] = this.z;
  }

  /**
   * Copies the x,y,z coordinates of this tuple into the tuple t.
   * @param t  the Point3d object into which the values of this object are copied
   */
  public final void get(Point3d t)
  {
    t.x = this.x;
    t.y = this.y;
    t.z = this.z;
  }
}
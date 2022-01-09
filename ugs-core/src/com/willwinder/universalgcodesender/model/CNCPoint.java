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
 * Modified version of Tuple3d which contains points relating to a CNC machine.
 */
public class CNCPoint {
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
   * The a coordinate.
   */
  public	double	a;

  /**
   * The b coordinate.
   */
  public	double	b;

  /**
   * The c coordinate.
   */
  public	double	c;

  /**
   * Constructs and initializes a CNCPoint from the specified xyz coordinates.
   * @param x the x coordinate
   * @param y the y coordinate
   * @param z the z coordinate
   * @param a the a coordinate
   * @param b the b coordinate
   * @param c the c coordinate
   */
  public CNCPoint(double x, double y, double z, double a, double b, double c)
  {
    set(x, y, z, a, b, c);
  }

  /**
   * Constructs and initializes a CNCPoint from the array of length 3.
   * @param t the array of length 3 containing xyz in order
   */
  public CNCPoint(double[] t)
  {
    set(t);
  }

  /**
   * Constructs and initializes a CNCPoint from the specified CNCPoint.
   * @param t1 the CNCPoint containing the initialization x y z data
   */
  public CNCPoint(CNCPoint t1)
  {
    set(t1);
  }

  /**
   * Constructs and initializes a CNCPoint to (0,0,0).
   */
  public CNCPoint()
  {
    this.x = (double) 0.0;
    this.y = (double) 0.0;
    this.z = (double) 0.0;
    this.a = (double) 0.0;
    this.b = (double) 0.0;
    this.c = (double) 0.0;
  }

  /**
   * Sets the value of this tuple to the specified xyzabc coordinates.
   * @param x the x coordinate
   * @param y the y coordinate
   * @param z the z coordinate
   * @param a the a coordinate
   * @param b the b coordinate
   * @param c the c coordinate
   */
  public final void set(double x, double y, double z, double a, double b, double c)
  {
    this.x = x;
    this.y = y;
    this.z = z;
    this.a = a;
    this.b = b;
    this.c = c;
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
    this.a = t[3];
    this.b = t[4];
    this.c = t[5];
  }

  /**
   * Sets the value of this tuple to the value of tuple t1.
   * @param t1 the tuple to be copied
   */
  public final void set(CNCPoint t1)
  {
    this.x = t1.x;
    this.y = t1.y;
    this.z = t1.z;
    this.a = t1.a;
    this.b = t1.b;
    this.c = t1.c;
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
    t[3] = this.a;
    t[4] = this.b;
    t[5] = this.c;
  }

  /**
   * Copies the x,y,z coordinates of this tuple into the tuple t.
   * @param t  the CNCPoint object into which the values of this object are copied
   */
  public final void get(CNCPoint t)
  {
    t.x = this.x;
    t.y = this.y;
    t.z = this.z;
    t.a = this.a;
    t.b = this.b;
    t.c = this.c;
  }


  /**
   * Sets the value of this tuple to the sum of tuples t1 and t2.
   * @param t1 the first tuple
   * @param t2 the second tuple
   */
  public final CNCPoint add(CNCPoint t1, CNCPoint t2)
  {
    this.x = t1.x + t2.x;
    this.y = t1.y + t2.y;
    this.z = t1.z + t2.z;
    this.a = t1.a + t2.a;
    this.b = t1.b + t2.b;
    this.c = t1.c + t2.c;
    return this;
  }


  /**  
   * Sets the value of this tuple to the sum of itself and t1.
   * @param t1 the other tuple
   */  
  public final CNCPoint add(CNCPoint t1)
  { 
    return add(this, t1);
  }

  /**
   * Sets the value of this tuple to the difference of tuples
   * t1 and t2 (this = t1 - t2).
   * @param t1 the first tuple
   * @param t2 the second tuple
   */
  public final CNCPoint sub(CNCPoint t1, CNCPoint t2)
  {
    this.x = t1.x - t2.x;
    this.y = t1.y - t2.y;
    this.z = t1.z - t2.z;
    this.a = t1.a - t2.a;
    this.b = t1.b - t2.b;
    this.c = t1.c - t2.c;
    return this;
  }

  /**  
   * Sets the value of this tuple to the difference
   * of itself and t1 (this = this - t1).
   * @param t1 the other tuple
   */  
  public final CNCPoint sub(CNCPoint t1)
  { 
    return sub(this, t1);
  }


  /**
   * Sets the value of this tuple to the negation of tuple t1.
   * @param t1 the source tuple
   */
  public final CNCPoint negate(CNCPoint t1)
  {
    this.x = -t1.x;
    this.y = -t1.y;
    this.z = -t1.z;
    this.a = -t1.a;
    this.b = -t1.b;
    this.c = -t1.c;
    return this;
  }


  /**
   * Negates the value of this tuple in place.
   */
  public final CNCPoint negate()
  {
    return negate(this);
  }


  /**
   * Sets the value of this tuple to the scalar multiplication
   * of tuple t1.
   * @param s the scalar value
   * @param t1 the source tuple
   */
  public final CNCPoint scale(double s, CNCPoint t1)
  {
    this.x = s*t1.x;
    this.y = s*t1.y;
    this.z = s*t1.z;
    this.a = s*t1.a;
    this.b = s*t1.b;
    this.c = s*t1.c;
    return this;
  }


  /**
   * Sets the value of this tuple to the scalar multiplication
   * of itself.
   * @param s the scalar value
   */
  public final CNCPoint scale(double s)
  {
    return scale(s, this);
  }


  /**
   * Sets the value of this tuple to the scalar multiplication
   * of tuple t1 and then adds tuple t2 (this = s*t1 + t2).
   * @param s the scalar value
   * @param t1 the tuple to be multipled
   * @param t2 the tuple to be added
   */
  public final CNCPoint scaleAdd(double s, CNCPoint t1, CNCPoint t2)
  {
    this.x = s*t1.x + t2.x;
    this.y = s*t1.y + t2.y;
    this.z = s*t1.z + t2.z;
    this.a = s*t1.a + t2.a;
    this.b = s*t1.b + t2.b;
    this.c = s*t1.c + t2.c;
    return this;
  }

  /**
   * Sets the value of this tuple to the scalar multiplication
   * of itself and then adds tuple t1 (this = s*this + t1).
   * @param s the scalar value
   * @param t1 the tuple to be added
   */  
  public final CNCPoint scaleAdd(double s, CNCPoint t1) {
    return scaleAdd(s, this, t1);
  }

  /**
   * Returns a string that contains the values of this CNCPoint.
   * The form is (x,y,z:a,b,c).
   * @return the String representation
   */  
  @Override
  public String toString() {
    return "(" + this.x + ", " + this.y + ", " + this.z + ": " + this.a + "," + this.b + "," + this.c + ")";
  }

  /**
   * Copied from private helper VecMathUtil.
   * 
   * Returns the representation of the specified floating-point
   * value according to the IEEE 754 floating-point "double format"
   * bit layout, after first mapping -0.0 to 0.0. This method is
   * identical to Double.doubleToLongBits(double) except that an
   * integer value of 0L is returned for a floating-point value of
   * -0.0. This is done for the purpose of computing a hash code
   * that satisfies the contract of hashCode() and equals(). The
   * equals() method in each vecmath class does a pair-wise "=="
   * test on each floating-point field in the class (e.g., x, y, and
   * z for a CNCPoint). Since 0.0&nbsp;==&nbsp;-0.0 returns true, we
   * must also return the same hash code for two objects, one of
   * which has a field with a value of -0.0 and the other of which
   * has a cooresponding field with a value of 0.0.
   *
   * @param d an input double precision floating-point number
   * @return the integer bits representing that floating-point
   * number, after first mapping -0.0f to 0.0f
   */
  private static long doubleToLongBits(double d) {
    // Check for +0 or -0
    if (d == 0.0) {
      return 0L;
    }
    else {
      return Double.doubleToLongBits(d);
    }
  }

  /**
   * Returns a hash code value based on the data values in this
   * object.  Two different CNCPoint objects with identical data values
   * (i.e., CNCPoint.equals returns true) will return the same hash
   * code value.  Two objects with different data members may return the
   * same hash value, although this is not likely.
   * @return the integer hash code value
   */  
  @Override
  public int hashCode() {
    long bits = 1L;
    bits = 31L * bits + doubleToLongBits(x);
    bits = 31L * bits + doubleToLongBits(y);
    bits = 31L * bits + doubleToLongBits(z);
    bits = 31L * bits + doubleToLongBits(a);
    bits = 31L * bits + doubleToLongBits(b);
    bits = 31L * bits + doubleToLongBits(c);
    return (int) (bits ^ (bits >> 32));
  }


  /**
   * Returns true if all of the data members of CNCPoint t1 are
   * equal to the corresponding data members in this CNCPoint.
   * @param t1  the tuple with which the comparison is made
   * @return  true or false
   */  
  public boolean equals(CNCPoint t1)
  {
    try {
      return(this.x == t1.x && this.y == t1.y && this.z == t1.z && this.a == t1.a && this.b == t1.b && this.c == t1.c);
    }
    catch (NullPointerException e2) {return false;}
  }

  /**
   * Returns true if the Object t1 is of type CNCPoint and all of the
   * data members of t1 are equal to the corresponding data members in
   * this CNCPoint.
   * @param t1  the Object with which the comparison is made
   * @return  true or false
   */  
  public boolean equals(Object t1)
  {
    try {
      CNCPoint t2 = (CNCPoint) t1;
      return equals(t2);
    }
    catch (ClassCastException   e1) {return false;}
    catch (NullPointerException e2) {return false;}
  }

  /**
   * Returns true if the L-infinite distance between this tuple
   * and tuple t1 is less than or equal to the epsilon parameter, 
   * otherwise returns false.  The L-infinite
   * distance is equal to MAX[abs(x1-x2), abs(y1-y2), abs(z1-z2)].
   * @param t1  the tuple to be compared to this tuple
   * @param epsilon  the threshold value  
   * @return  true or false
   */
  public boolean epsilonEquals(CNCPoint t1, double epsilon)
  {
    double diff;

    diff = x - t1.x;
    if(Double.isNaN(diff)) return false;
    if((diff<0?-diff:diff) > epsilon) return false;

    diff = y - t1.y;
    if(Double.isNaN(diff)) return false;
    if((diff<0?-diff:diff) > epsilon) return false;

    diff = z - t1.z;
    if(Double.isNaN(diff)) return false;
    if((diff<0?-diff:diff) > epsilon) return false;

    diff = a - t1.a;
    if(Double.isNaN(diff)) return false;
    if((diff<0?-diff:diff) > epsilon) return false;

    diff = b - t1.b;
    if(Double.isNaN(diff)) return false;
    if((diff<0?-diff:diff) > epsilon) return false;

    diff = c - t1.c;
    if(Double.isNaN(diff)) return false;
    if((diff<0?-diff:diff) > epsilon) return false;

    return true;
  }

  /**
   *  Clamps the tuple parameter to the range [low, high] and 
   *  places the values into this tuple.  
   *  @param min   the lowest value in the tuple after clamping
   *  @param max  the highest value in the tuple after clamping 
   *  @param t   the source tuple, which will not be modified
   */
  public final void clamp(double min, double max, CNCPoint t) {
    if( t.x > max ) {
      x = max;
    } else if( t.x < min ){
      x = min;
    } else {
      x = t.x;
    }

    if( t.y > max ) {
      y = max;
    } else if( t.y < min ){
      y = min;
    } else {
      y = t.y;
    }

    if( t.z > max ) {
      z = max;
    } else if( t.z < min ){
      z = min;
    } else {
      z = t.z;
    }

    if( t.a > max ) {
      a = max;
    } else if( t.a < min ){
      a = min;
    } else {
      a = t.a;
    }

    if( t.b > max ) {
      b = max;
    } else if( t.b < min ){
      b = min;
    } else {
      b = t.b;
    }

    if( t.c > max ) {
      c = max;
    } else if( t.c < min ){
      c = min;
    } else {
      c = t.c;
    }
  }

  /** 
   *  Clamps the minimum value of the tuple parameter to the min 
   *  parameter and places the values into this tuple.
   *  @param min   the lowest value in the tuple after clamping 
   *  @param t   the source tuple, which will not be modified
   */   
  public final void clampMin(double min, CNCPoint t) { 
    if( t.x < min ) {
      x = min;
    } else {
      x = t.x;
    }

    if( t.y < min ) {
      y = min;
    } else {
      y = t.y;
    }

    if( t.z < min ) {
      z = min;
    } else {
      z = t.z;
    }

    if( t.a < min ) {
      a = min;
    } else {
      a = t.a;
    }

    if( t.b < min ) {
      b = min;
    } else {
      b = t.b;
    }

    if( t.c < min ) {
      c = min;
    } else {
      c = t.c;
    }
  } 

  /**  
   *  Clamps the maximum value of the tuple parameter to the max 
   *  parameter and places the values into this tuple.
   *  @param max the highest value in the tuple after clamping  
   *  @param t   the source tuple, which will not be modified
   */    
  public final void clampMax(double max, CNCPoint t) {  
    if( t.x > max ) {
      x = max;
    } else {
      x = t.x;
    }

    if( t.y > max ) {
      y = max;
    } else {
      y = t.y;
    }

    if( t.z > max ) {
      z = max;
    } else {
      z = t.z;
    }

    if( t.a > max ) {
      a = max;
    } else {
      a = t.a;
    }

    if( t.b > max ) {
      b = max;
    } else {
      b = t.b;
    }

    if( t.c > max ) {
      c = max;
    } else {
      c = t.c;
    }
  } 

  /**  
   *  Sets each component of the tuple parameter to its absolute 
   *  value and places the modified values into this tuple.
   *  @param t   the source tuple, which will not be modified
   */    
  public final void absolute(CNCPoint t)
  {
    x = Math.abs(t.x);
    y = Math.abs(t.y);
    z = Math.abs(t.z);
    a = Math.abs(t.a);
    b = Math.abs(t.b);
    c = Math.abs(t.c);
  } 

  /**
   *  Sets each component of this tuple to its absolute value.
   */
  public final void absolute()
  {
    absolute(this);
  }

  /**
   *  Clamps this tuple to the range [low, high].
   *  @param min  the lowest value in this tuple after clamping
   *  @param max  the highest value in this tuple after clamping
   */
  public final void clamp(double min, double max) {
    clamp(min, max, this);
  }

  /**
   *  Clamps the minimum value of this tuple to the min parameter.
   *  @param min   the lowest value in this tuple after clamping
   */
  public final void clampMin(double min) { 
    if( x < min ) x=min;
    if( y < min ) y=min;
    if( z < min ) z=min;
    if( a < min ) a=min;
    if( b < min ) b=min;
    if( c < min ) c=min;
  } 

  /**
   *  Clamps the maximum value of this tuple to the max parameter.
   *  @param max   the highest value in the tuple after clamping
   */
  public final void clampMax(double max) { 
    if( x > max ) x=max;
    if( y > max ) y=max;
    if( z > max ) z=max;
    if( a > max ) a=max;
    if( b > max ) b=max;
    if( c > max ) c=max;
  }

  /**
   *  Linearly interpolates between tuples t1 and t2 and places the 
   *  result into this tuple:  this = (1-alpha)*t1 + alpha*t2.
   *  @param t1  the first tuple
   *  @param t2  the second tuple  
   *  @param alpha  the alpha interpolation parameter  
   */   
  public final void interpolate(CNCPoint t1, CNCPoint t2, double alpha) {
    this.x = (1-alpha)*t1.x + alpha*t2.x;
    this.y = (1-alpha)*t1.y + alpha*t2.y;
    this.z = (1-alpha)*t1.z + alpha*t2.z;
    this.a = (1-alpha)*t1.a + alpha*t2.a;
    this.b = (1-alpha)*t1.b + alpha*t2.b;
    this.c = (1-alpha)*t1.c + alpha*t2.c;
  }

  /**   
   *  Linearly interpolates between this tuple and tuple t1 and 
   *  places the result into this tuple:  this = (1-alpha)*this + alpha*t1. 
   *  @param t1  the first tuple 
   *  @param alpha  the alpha interpolation parameter   
   */    
  public final void interpolate(CNCPoint t1, double alpha) {
    interpolate(this, t1, alpha);
  }  

  /**
	 * Get the <i>x</i> coordinate.
	 * 
	 * @return  the <i>x</i> coordinate.
	 */
	public final double getX() {
		return x;
	}

	/**
	 * Set the <i>x</i> coordinate.
	 * 
	 * @param x  value to <i>x</i> coordinate.
	 */
	public final void setX(double x) {
		this.x = x;
	}

	/**
	 * Get the <i>y</i> coordinate.
	 * 
	 * @return the <i>y</i> coordinate.
	 */
	public final double getY() {
		return y;
	}

	/**
	 * Set the <i>y</i> coordinate.
	 * 
	 * @param y value to <i>y</i> coordinate.
	 */
	public final void setY(double y) {
		this.y = y;
	}

	/**
	 * Get the <i>z</i> coordinate.
	 * 
	 * @return the <i>z</i> coordinate.
	 */
	public final double getZ() {
		return z;
	}

	/**
	 * Set the <i>z</i> coordinate.
	 */
	public final void setZ(double z) {
		this.z = z;
	}

  /**
	 * Get the <i>a</i> coordinate.
	 * 
	 * @return  the <i>a</i> coordinate.
	 */
	public final double getA() {
		return a;
	}

	/**
	 * Set the <i>a</i> coordinate.
	 * 
	 * @param a  value to <i>a</i> coordinate.
	 */
	public final void setA(double a) {
		this.a = a;
	}

	/**
	 * Get the <i>b</i> coordinate.
	 * 
	 * @return the <i>b</i> coordinate.
	 */
	public final double getB() {
		return b;
	}

	/**
	 * Set the <i>b</i> coordinate.
	 * 
	 * @param b value to <i>b</i> coordinate.
	 */
	public final void setB(double b) {
		this.b = b;
	}

	/**
	 * Get the <i>c</i> coordinate.
	 * 
	 * @return the <i>c</i> coordinate.
	 */
	public final double getC() {
		return c;
	}

	/**
	 * Set the <i>c</i> coordinate.
	 */
	public final void setC(double c) {
		this.c = c;
	}

  /**
   * Returns the distance between this point and point p1.
   * @param p1 the other point
   * @return the distance 
   */
  public final double distanceXYZ(CNCPoint p1)
    {
      double dx, dy, dz;

      dx = this.x-p1.x;
      dy = this.y-p1.y;
      dz = this.z-p1.z;
      return Math.sqrt(dx*dx+dy*dy+dz*dz);
    }

  /**
   * Sets the value of this vector to the normalization of vector v1.
   * @param v1 the un-normalized vector
   */
  public final void normalizeXYZ(CNCPoint v1)
  {
    double norm;

    norm = 1.0/Math.sqrt(v1.x*v1.x + v1.y*v1.y + v1.z*v1.z);
    this.x = v1.x*norm;
    this.y = v1.y*norm;
    this.z = v1.z*norm;
  }

  /**
   * Normalizes this vector in place.
   */
  public final void normalizeXYZ()
  {
    normalizeXYZ(this);
  }
}
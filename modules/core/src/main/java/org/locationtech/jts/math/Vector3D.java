/*
 * Copyright (c) 2016 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jts.math;

import org.locationtech.jts.geom.Coordinate;

/**
 * Represents a vector in 3-dimensional Cartesian space.
 * 
 * @author mdavis
 *
 */
public class Vector3D {
	
	/**
	 * Computes the dot product of the 3D vectors AB and CD.
	 * 
	 * @param A the start point of the first vector
	 * @param B the end point of the first vector
	 * @param C the start point of the second vector
	 * @param D the end point of the second vector
	 * @return the dot product
	 */
	public static double dot(Coordinate A, Coordinate B, Coordinate C, Coordinate D)
	{
		double ABx = B.x - A.x;
		double ABy = B.y - A.y;
		double ABz = B.getZ() - A.getZ();
		double CDx = D.x - C.x;
		double CDy = D.y - C.y;
		double CDz = D.getZ() - C.getZ();
		return ABx*CDx + ABy*CDy + ABz*CDz;
	}

  /**
   * Creates a new vector with given X, Y and Z components.
   * 
   * @param x the X component
   * @param y the Y component
   * @param z the Z component
   * @return a new vector
   */
  public static Vector3D create(double x, double y, double z) {
    return new Vector3D(x, y, z);
  }

  /**
   * Creates a vector from a 3D {@link Coordinate}. 
   * The coordinate should have the
   * X,Y and Z ordinates specified.
   * 
   * @param coord the Coordinate to copy
   * @return a new vector
   */
  public static Vector3D create(Coordinate coord) {
    return new Vector3D(coord);
  }

	/**
	 * Computes the 3D dot-product of two {@link Coordinate}s.
	 * 
   * @param v1 the first vector
   * @param v2 the second vector
	 * @return the dot product of the vectors
	 */
	public static double dot(Coordinate v1, Coordinate v2) {
		return v1.x * v2.x + v1.y * v2.y + v1.getZ() * v2.getZ();
	}

	private double x;
	private double y;
	private double z;

  /**
   * Creates a new 3D vector from a {@link Coordinate}. The coordinate should have
   * the X,Y and Z ordinates specified.
   * 
   * @param v the Coordinate to copy
   */
  public Vector3D(Coordinate v) {
    x = v.x;
    y = v.y;
    z = v.getZ();
  }

  /**
   * Creates a new vector with the direction and magnitude
   * of the difference between the 
   * <tt>to</tt> and <tt>from</tt> {@link Coordinate}s.
   * 
   * @param from the origin Coordinate
   * @param to the destination Coordinate
   */
	public Vector3D(Coordinate from, Coordinate to) {
		x = to.x - from.x;
		y = to.y - from.y;
		z = to.getZ() - from.getZ();
	}

	/**
	 * Creates a vector with the givne components.
	 * 
	 * @param x the X component
	 * @param y the Y component
	 * @param z the Z component
	 */
	public Vector3D(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Gets the X component of this vector.
	 * 
	 * @return the value of the X component
	 */
	public double getX() {
		return x;
	}

  /**
   * Gets the Y component of this vector.
   * 
   * @return the value of the Y component
   */
	public double getY() {
		return y;
	}

  /**
   * Gets the Z component of this vector.
   * 
   * @return the value of the Z component
   */
	public double getZ() {
		return z;
	}

	/**
	 * Computes a vector which is the sum
	 * of this vector and the given vector.
	 * 
	 * @param v the vector to add
	 * @return the sum of this and <code>v</code>
	 */
	public Vector3D add(Vector3D v) {
		return create(x + v.x, y + v.y, z + v.z);
	}

	/**
   * Computes a vector which is the difference
   * of this vector and the given vector.
   * 
   * @param v the vector to subtract
   * @return the difference of this and <code>v</code>
   */
	public Vector3D subtract(Vector3D v) {
		return create(x - v.x, y - v.y, z - v.z);
	}

  /**
   * Creates a new vector which has the same direction
   * and with length equals to the length of this vector
   * divided by the scalar value <code>d</code>.
   * 
   * @param d the scalar divisor
   * @return a new vector with divided length
   */
  public Vector3D divide(double d) {
    return create(x / d, y / d, z / d);
  }
  
  /**
   * Computes the dot-product of two vectors
   * 
   * @param v a vector
   * @return the dot product of the vectors
   */
  public double dot(Vector3D v) {
    return x * v.x + y * v.y + z * v.z;
  }

	/**
   * Computes the length of this vector.
   * 
   * @return the length of the vector
   */
	public double length() {
		return Math.sqrt(x * x + y * y + z * z);
	}

	/**
	 * Computes the length of a vector.
	 * 
	 * @param v a coordinate representing a 3D vector
	 * @return the length of the vector
	 */
	public static double length(Coordinate v) {
		return Math.sqrt(v.x * v.x + v.y * v.y + v.getZ() * v.getZ());
	}

  /**
   * Computes a vector having identical direction
   * but normalized to have length 1.
   * 
   * @return a new normalized vector
   */
	public Vector3D normalize() {
		double length = length();
		if (length > 0.0)
			return divide(length());
		return create(0.0, 0.0, 0.0);
	}

  /**
   * Computes a vector having identical direction
   * but normalized to have length 1.
   * 
   * @param v a coordinate representing a 3D vector
   * @return a coordinate representing the normalized vector
   */
  public static Coordinate normalize(Coordinate v) {
    double len = length(v);
    return new Coordinate(v.x / len, v.y / len, v.getZ() / len);
  }

  /**
   * Gets a string representation of this vector
   * 
   * @return a string representing this vector
   */
  public String toString() {
    return "[" + x + ", " + y + ", " + z + "]";
  }
		
  /**
   * Tests if a vector <tt>o</tt> has the same values for the components.
   * 
   * @param o a <tt>Vector3D</tt> with which to do the comparison.
   * @return true if <tt>other</tt> is a <tt>Vector3D</tt> with the same values
   *         for the x and y components.
   */
  public boolean equals(Object o) {
    if ( !(o instanceof Vector3D) ) {
      return false;
    }
    Vector3D v = (Vector3D) o;
    return x == v.x && y == v.y && z == v.z;
  }

  /**
   * Gets a hashcode for this vector.
   * 
   * @return a hashcode for this vector
   */
  public int hashCode() {
    // Algorithm from Effective Java by Joshua Bloch
    int result = 17;
    result = 37 * result + Coordinate.hashCode(x);
    result = 37 * result + Coordinate.hashCode(y);
    result = 37 * result + Coordinate.hashCode(z);
    return result;
  }

}

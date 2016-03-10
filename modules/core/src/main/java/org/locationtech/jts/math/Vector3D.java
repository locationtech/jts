/*
 * Copyright (c) 2016 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
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
	 * @param A
	 * @param B
	 * @param C
	 * @param D
	 * @return the dot product
	 */
	public static double dot(Coordinate A, Coordinate B, Coordinate C, Coordinate D)
	{
		double ABx = B.x - A.x;
		double ABy = B.y - A.y;
		double ABz = B.z - A.z;
		double CDx = D.x - C.x;
		double CDy = D.y - C.y;
		double CDz = D.z - C.z;
		return ABx*CDx + ABy*CDy + ABz*CDz;
	}

	/**
	 * Creates a new vector with given X and Y components.
	 * 
	 * @param x
	 *            the x component
	 * @param y
	 *            the y component
	 * @param z
	 *            the z component
	 * @return a new vector
	 */
	public static Vector3D create(double x, double y, double z) {
		return new Vector3D(x, y, z);
	}

	/**
	 * Creates a vector from a {@link Coordinate}.
	 * 
	 * @param coord
	 *            the Coordinate to copy
	 * @return a new vector
	 */
	public static Vector3D create(Coordinate coord) {
		return new Vector3D(coord);
	}

	public Vector3D(Coordinate v) {
		x = v.x;
		y = v.y;
		z = v.z;
	}

	/**
	 * Computes the 3D dot-product of two {@link Coordinate}s.
	 * 
   * @param v1 the first vector
   * @param v2 the second vector
	 * @return the dot product of the vectors
	 */
	public static double dot(Coordinate v1, Coordinate v2) {
		return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
	}

	private double x;
	private double y;
	private double z;

	public Vector3D(Coordinate from, Coordinate to) {
		x = to.x - from.x;
		y = to.y - from.y;
		z = to.z - from.z;
	}

	public Vector3D(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ() {
		return z;
	}


	/**
	 * Computes the dot-product of two vectors
	 * 
	 * @param v
	 *            a vector
	 * @return the dot product of the vectors
	 */
	public double dot(Vector3D v) {
		return x * v.x + y * v.y + z * v.z;
	}

	public double length() {
		return Math.sqrt(x * x + y * y + z * z);
	}

	public static double length(Coordinate v) {
		return Math.sqrt(v.x * v.x + v.y * v.y + v.z * v.z);
	}

	public Vector3D normalize() {
		double length = length();
		if (length > 0.0)
			return divide(length());
		return create(0.0, 0.0, 0.0);
	}

	private Vector3D divide(double d) {
		return create(x / d, y / d, z / d);
	}

	public static Coordinate normalize(Coordinate v) {
		double len = length(v);
		return new Coordinate(v.x / len, v.y / len, v.z / len);
	}
	  /**
	   * Gets a string representation of this vector
	   * 
	   * @return a string representing this vector
	   */
		public String toString() {
			return "[" + x + ", " + y + ", " + z + "]";
		}
		

}

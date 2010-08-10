/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */

package com.vividsolutions.jts.algorithm;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.util.Assert;

/**
 * Functions for performing mathematics on vectors in the 2D plane. 
 * Vectors are represented as single coordinates; 
 * the implied vector is assumed to start at the origin.
 * <p>
 * Vector arithmetic is useful in computing various geometric constructions.
 * 
 * @author Martin Davis
 * @version 1.0
 */

public class VectorMath 
{
	/**
	 * Normalizes a vector to have magnitude 1
	 * @param v the vector to normalize
	 * @return the normalized vector
	 */
    public static Coordinate normalize(Coordinate v) {
      double absVal = Math.sqrt(v.x * v.x + v.y * v.y);
      return new Coordinate(v.x / absVal, v.y / absVal);
    }

  	/**
  	 * Normalizes a vector to have magnitude 1 in-place
  	 * @param v the vector to normalize
  	 */
    public static void normalizeSelf(Coordinate v) {
      double absVal = Math.sqrt(v.x * v.x + v.y * v.y);
      v.x /= absVal;
      v.y /= absVal;
    }

    /**
     * Computes the dot-product of two vectors
     * @param v1 a vector
     * @param v2 a vector
     * @return the dot product of the vectors
     */
    public static double dotProduct(Coordinate v1, Coordinate v2) {
        return v1.x * v2.x + v1.y * v2.y;
    }

    /**
     * Computes the average of two vectors. 
     * This is equivalent to computing the midpoint of the line segment
     * joining the vector endpoints.
     * 
     * @param v0 a vector 
     * @param v1 a vector
     * @return the average of the vectors
     */
    public static Coordinate average(Coordinate v0, Coordinate v1)
  	{
      return new Coordinate( 
      		(v0.x + v1.x) / 2,
          (v0.y + v1.y) / 2);
  	}
    
    /**
     * Computes the product of a scalar and a vector.
     * 
     * @param s a scalar value
     * @param v a vector
     * @return the product of the scalar and the vector
     */
    public static Coordinate multiply(double s, Coordinate v)
  	{
      return new Coordinate(s * v.x, s * v.y);
  	}
  	
    /**
     * Computes a point a given fraction of length along
     * the line joining the endpoints of two vectors.
     * This is equivalent to computing the weighted sum
     * of the two vectors.
     * 
     * @param v0 a vector
     * @param v1 a vector
     * @param lengthFraction the fraction of the length
     * @return the vector to the computed point
     */
  	public static Coordinate pointAlong(Coordinate v0, Coordinate v1, double lengthFraction)
    {
      Coordinate coord = new Coordinate();
      coord.x = v0.x + lengthFraction * (v1.x - v0.x);
      coord.y = v0.y + lengthFraction * (v1.y - v0.y);
      return coord;
    }
    
  	/**
  	 * Computes the sum of two vectors.
  	 * 
     * @param v0 a vector
     * @param v1 a vector
  	 * @return the sum of the vectors
  	 */
  	public static Coordinate sum(Coordinate v0, Coordinate v1)
    {
    	return new Coordinate(v0.x + v1.x, v0.y + v1.y);
    }

  	/**
  	 * Computes the difference of two vectors [v0 - v1].
  	 * 
     * @param v0 a vector
     * @param v1 a vector
  	 * @return the sum of the vectors
  	 */
  	public static Coordinate difference(Coordinate v0, Coordinate v1)
    {
    	return new Coordinate(v0.x - v1.x, v0.y - v1.y);
    }

  	/**
  	 * Rotates a vector by a given number of quarter-circles
  	 * (i.e. multiples of 90 degrees or Pi/2 radians).
  	 * A positive number rotates counter-clockwise, 
  	 * a negative number rotates clockwise.
  	 * Under this operation the magnitude of the vector 
  	 * and the absolute values 
  	 * of the ordinates do not change, only their sign
  	 * and ordinate index.
  	 * 
  	 * @param v the vector to rotate.
  	 * @param numQuarters the number of quarter-circles to rotate by
  	 * @return the rotated vector.
  	 */
  	public static Coordinate rotateByQuarterCircle(Coordinate v, int numQuarters)
    {
  		int nQuad = numQuarters % 4;
  		if (numQuarters < 0 && nQuad != 0) {
  			nQuad =  nQuad + 4;
  		}
  		switch (nQuad) {
  		case 0: return new Coordinate(v.x, v.y);
  		case 1: return new Coordinate(-v.y, v.x);
  		case 2: return new Coordinate(-v.x, -v.y);
  		case 3: return new Coordinate(v.y, -v.x);
  		}
    	Assert.shouldNeverReachHere();
    	return null;
    }


}